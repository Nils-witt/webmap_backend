package dev.nilswitt.webmap.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
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
import jakarta.annotation.security.PermitAll;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static com.vaadin.flow.spring.data.VaadinSpringDataHelpers.toSpringPageRequest;

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
        userGrid.setItems(query -> userRepository.findAll(toSpringPageRequest(query)).stream());
        userGrid.addColumn(User::getUsername).setHeader("Username");
        userGrid.addColumn(User::getFirstName).setHeader("First Name");
        userGrid.addColumn(User::getLastName).setHeader("Last Name");
        userGrid.addColumn(User::getEmail).setHeader("Email");
        userGrid.addColumn(User::isEnabled).setHeader("Enabled");
        userGrid.addColumn(u -> !u.isAccountNonLocked()).setHeader("Is Locked");
        userGrid.addColumn(user -> String.join(", ", user.getSecurityGroups().stream().map(SecurityGroup::getName).toList()))
                .setHeader("Groups");

        userGrid.setEmptyStateText("There are no users");
        userGrid.setSizeFull();
        userGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER);

        setSizeFull();
        setPadding(false);
        setSpacing(false);
        getStyle().setOverflow(Style.Overflow.HIDDEN);

        add(new ViewToolbar("User List", ViewToolbar.group(createBtn)));
        add(userGrid);
        add(editDialog);

        PersonContextMenu contextMenu = new PersonContextMenu(userGrid);
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
            setDynamicContentHandler(person -> {
                // Do not show context menu when header is clicked
                if (person == null) {
                    return false;
                }

                return true;
            });
        }
    }
}
