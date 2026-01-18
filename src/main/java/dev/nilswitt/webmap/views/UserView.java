package dev.nilswitt.webmap.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.dom.Style;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.Route;
import dev.nilswitt.webmap.base.ui.ViewToolbar;
import dev.nilswitt.webmap.entities.SecurityGroup;
import dev.nilswitt.webmap.entities.User;
import dev.nilswitt.webmap.entities.repositories.SecurityGroupRepository;
import dev.nilswitt.webmap.entities.repositories.UserRepository;
import dev.nilswitt.webmap.views.components.PasswordChangeDialog;
import dev.nilswitt.webmap.views.components.UserEditDialog;
import dev.nilswitt.webmap.views.filters.UserFilter;
import jakarta.annotation.security.PermitAll;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Route("ui/users")
@Menu(order = 5, icon = "vaadin:user", title = "Users")
@PermitAll
public class UserView extends VerticalLayout {
    final Grid<User> userGrid;
    final Button createBtn;
    final UserEditDialog editDialog;
    final PasswordChangeDialog passwordChangeDialog;
    final UserRepository userRepository;
    final PasswordEncoder passwordEncoder;
    UserFilter userFilter;


    public UserView(UserRepository userRepository, SecurityGroupRepository securityGroupRepository, PasswordEncoder passwordEncoder) {
        this.userGrid = new Grid<>();
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.passwordChangeDialog = new PasswordChangeDialog(userRepository, passwordEncoder);

        this.editDialog = new UserEditDialog((user) -> {
            userRepository.save(user);
            userGrid.getDataProvider().refreshAll();
        }, securityGroupRepository);


        createBtn = new Button("Create", event -> {
            editDialog.open(null);
        });
        createBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        userGrid.addColumn(User::getUsername).setKey(String.valueOf(UserFilter.Columns.USERNAME)).setHeader("Username").setSortable(true).setComparator(User::getUsername);
        userGrid.addColumn(User::getFirstName).setKey(String.valueOf(UserFilter.Columns.FIRST_NAME)).setHeader("First Name").setSortable(true);
        userGrid.addColumn(User::getLastName).setKey(String.valueOf(UserFilter.Columns.LAST_NAME)).setHeader("Last Name").setSortable(true);
        userGrid.addColumn(User::getEmail).setKey(String.valueOf(UserFilter.Columns.EMAIL)).setHeader("Email").setSortable(true);
        userGrid.addColumn(User::isEnabled).setKey(String.valueOf(UserFilter.Columns.ENABLED)).setHeader("Enabled").setSortable(true);
        userGrid.addColumn(u -> !u.isAccountNonLocked()).setKey(String.valueOf(UserFilter.Columns.IS_LOCKED)).setHeader("Is Locked").setSortable(true);
        userGrid.addColumn(user -> String.join(", ", user.getSecurityGroups().stream().map(SecurityGroup::getName).toList()))
                .setHeader("Groups").setKey(String.valueOf(UserFilter.Columns.GROUPS));
        userGrid.setItemsPageable(this::list);

        userGrid.setEmptyStateText("There are no users");
        userGrid.setSizeFull();
        userGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER);

        setSizeFull();
        setPadding(false);
        setSpacing(false);
        getStyle().setOverflow(Style.Overflow.HIDDEN);
        userFilter = new UserFilter((userExample -> {
            userGrid.getDataProvider().refreshAll();
        }));
        userFilter.setUp(userGrid);
        add(new ViewToolbar("User List", ViewToolbar.group(createBtn)));

        add(userGrid);
        add(editDialog);

        new PersonContextMenu(userGrid);
    }

    public List<User> list(Pageable pageable) {
        return userRepository.findAll(userFilter.getExample(), pageable).stream().toList();
    }


    private class PersonContextMenu extends GridContextMenu<User> {
        public PersonContextMenu(Grid<User> target) {

            super(target);
            this.addItem("Edit", event -> {
                Optional<User> item = event.getItem();
                item.ifPresent(editDialog::open);
            });
            this.addItem("Delete", event -> {
                Optional<User> item = event.getItem();
                item.ifPresent(user -> {
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

            this.addItem("Change Password", event -> {
                Optional<User> item = event.getItem();
                if (item.isEmpty()) return;
                item.ifPresent(passwordChangeDialog::open);
            });
            setDynamicContentHandler(Objects::nonNull);
        }
    }
}
