package dev.nilswitt.webmap.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.dom.Style;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.security.AuthenticationContext;
import dev.nilswitt.webmap.base.ui.ViewToolbar;
import dev.nilswitt.webmap.entities.SecurityGroup;
import dev.nilswitt.webmap.entities.User;
import dev.nilswitt.webmap.entities.repositories.SecurityGroupRepository;
import dev.nilswitt.webmap.entities.repositories.UserRepository;
import dev.nilswitt.webmap.security.PermissionUtil;
import dev.nilswitt.webmap.views.components.PasswordChangeDialog;
import dev.nilswitt.webmap.views.components.UserEditDialog;
import dev.nilswitt.webmap.views.filters.UserFilter;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Objects;

@Route("ui/users")
@Menu(order = 5, icon = "vaadin:user", title = "Users")
@RolesAllowed("ROLE_USERS_VIEW")
public class UserView extends VerticalLayout {
    private final Grid<User> userGrid;
    private final Button createBtn;
    private final UserEditDialog editDialog;
    private final PasswordChangeDialog passwordChangeDialog;
    private final UserRepository userRepository;
    private final UserFilter userFilter;

    private AuthenticationContext authenticationContext;


    public UserView(UserRepository userRepository, SecurityGroupRepository securityGroupRepository, PasswordEncoder passwordEncoder, AuthenticationContext authenticationContext) {
        this.userGrid = new Grid<>();
        this.userRepository = userRepository;
        this.authenticationContext = authenticationContext;
        this.passwordChangeDialog = new PasswordChangeDialog(userRepository, passwordEncoder);

        this.editDialog = new UserEditDialog((user) -> {
            this.userRepository.save(user);
            this.userGrid.getDataProvider().refreshAll();
        }, securityGroupRepository);


        this.createBtn = new Button("Create", event -> {
            User actingUser = currentUser();
            if (!PermissionUtil.hasAnyScope(actingUser, SecurityGroup.UserRoleTypeEnum.USERS,
                    SecurityGroup.UserRoleScopeEnum.CREATE)) {
                Notification.show("You cannot create users");
                return;
            }
            this.editDialog.open(null);
        });
        this.createBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        this.setUpGrid();

        this.userFilter = new UserFilter((userExample -> {
            this.userGrid.getDataProvider().refreshAll();
        }));
        this.userFilter.setUp(userGrid);

        this.setSizeFull();
        this.setPadding(false);
        this.setSpacing(false);
        this.getStyle().setOverflow(Style.Overflow.HIDDEN);

        this.add(new ViewToolbar("User List", ViewToolbar.group(createBtn)));

        this.add(userGrid);
        this.add(editDialog);

        new PersonContextMenu(userGrid);
    }

    private void setUpGrid() {
        this.userGrid.addColumn(User::getUsername).setKey(String.valueOf(UserFilter.Columns.USERNAME)).setHeader("Username").setSortable(true).setComparator(User::getUsername);
        this.userGrid.addColumn(User::getFirstName).setKey(String.valueOf(UserFilter.Columns.FIRST_NAME)).setHeader("First Name").setSortable(true);
        this.userGrid.addColumn(User::getLastName).setKey(String.valueOf(UserFilter.Columns.LAST_NAME)).setHeader("Last Name").setSortable(true);
        this.userGrid.addColumn(User::getEmail).setKey(String.valueOf(UserFilter.Columns.EMAIL)).setHeader("Email").setSortable(true);
        this.userGrid.addColumn(User::isEnabled).setKey(String.valueOf(UserFilter.Columns.ENABLED)).setHeader("Enabled").setSortable(true);
        this.userGrid.addColumn(u -> !u.isAccountNonLocked()).setKey(String.valueOf(UserFilter.Columns.IS_LOCKED)).setHeader("Is Locked").setSortable(true);
        this.userGrid.addColumn(user -> String.join(", ", user.getSecurityGroups().stream().map(SecurityGroup::getName).toList()))
                .setHeader("Groups").setKey(String.valueOf(UserFilter.Columns.GROUPS));
        this.userGrid.setItemsPageable(this::list);

        this.userGrid.setEmptyStateText("There are no users");
        this.userGrid.setSizeFull();
        this.userGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
    }

    private List<User> list(Pageable pageable) {
        return this.userRepository.findAll(this.userFilter.getExample(), pageable).stream().toList();
    }

    private User currentUser() {
        return this.authenticationContext.getAuthenticatedUser(User.class).orElse(null);
    }


    private class PersonContextMenu extends GridContextMenu<User> {
        public PersonContextMenu(Grid<User> target) {

            super(target);
            this.addItem("Edit", event -> {
                event.getItem().ifPresent(user -> {
                    User actingUser = currentUser();
                    if (!PermissionUtil.hasAnyScope(actingUser, SecurityGroup.UserRoleTypeEnum.USERS,
                            SecurityGroup.UserRoleScopeEnum.EDIT)) {
                        Notification.show("You cannot edit users");
                        return;
                    }
                    editDialog.open(user);
                });
            });
            this.addItem("Delete", event -> {
                event.getItem().ifPresent(user -> {
                    User actingUser = currentUser();
                    if (!PermissionUtil.hasAnyScope(actingUser, SecurityGroup.UserRoleTypeEnum.USERS,
                            SecurityGroup.UserRoleScopeEnum.DELETE)) {
                        Notification.show("You cannot delete users");
                        return;
                    }
                    ConfirmDialog confirmDialog = new ConfirmDialog();
                    confirmDialog.setHeader("Delete User");
                    confirmDialog.setText("Are you sure you want to delete user '" + user.getUsername() + "'?");
                    confirmDialog.setCancelable(true);
                    confirmDialog.setConfirmText("Delete");
                    confirmDialog.addConfirmListener(e -> {
                        userRepository.delete(user);
                        userGrid.getDataProvider().refreshAll();
                        confirmDialog.close();
                        this.remove(confirmDialog);
                    });
                    add(confirmDialog);
                    confirmDialog.open();
                });
            });

            this.addItem("Change Password", event -> event.getItem().ifPresent(user -> {
                User actingUser = currentUser();
                if (!PermissionUtil.hasAnyScope(actingUser, SecurityGroup.UserRoleTypeEnum.USERS,
                        SecurityGroup.UserRoleScopeEnum.ADMIN)) {
                    Notification.show("You cannot change passwords");
                    return;
                }
                passwordChangeDialog.open(user);
            }));
            setDynamicContentHandler(Objects::nonNull);
        }
    }
}
