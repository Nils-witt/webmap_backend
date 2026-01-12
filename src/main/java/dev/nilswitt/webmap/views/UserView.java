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
import dev.nilswitt.webmap.entities.User;
import dev.nilswitt.webmap.entities.repositories.UserRepository;
import dev.nilswitt.webmap.entities.repositories.UserRoleRepository;
import dev.nilswitt.webmap.views.components.UserEditDialog;

import java.util.Optional;

import static com.vaadin.flow.spring.data.VaadinSpringDataHelpers.toSpringPageRequest;

@Route("users")
@Menu(order = 1, icon = "vaadin:user", title = "Users")
public class UserView extends VerticalLayout {
    final Grid<User> userGrid;
    final Button createBtn;
    final UserEditDialog editDialog;

    public UserView(UserRepository userRepository, UserRoleRepository userRoleRepository) {
        this.userGrid = new Grid<>();

        this.editDialog = new UserEditDialog((user) -> {
            userRepository.save(user);
            userGrid.getDataProvider().refreshAll();
        }, userRoleRepository);


        createBtn = new Button("Create", event -> {
            editDialog.open(null);
        });
        createBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        userGrid.setItems(query -> userRepository.findAll(toSpringPageRequest(query)).stream());
        userGrid.addColumn(User::getUsername).setHeader("Username");
        userGrid.addColumn(User::getFirstName).setHeader("First Name");
        userGrid.addColumn(User::getLastName).setHeader("Last Name");
        userGrid.addColumn(User::getEmail).setHeader("Email");
        userGrid.addColumn(user -> String.join(", ", user.getRoles().stream().map(role -> role.getName()).toList()))
                .setHeader("Roles");

        userGrid.setEmptyStateText("There are no users");
        userGrid.setSizeFull();
        userGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        GridContextMenu<User> menu = userGrid.addContextMenu();
        menu.addItem("Edit", event -> {
            Optional<User> item = event.getItem();
            item.ifPresent(editDialog::open);
        });
        menu.addItem("Delete", event -> {
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
                this.add(confirmDialog);
                confirmDialog.open();
            });
        });
        setSizeFull();
        setPadding(false);
        setSpacing(false);
        getStyle().setOverflow(Style.Overflow.HIDDEN);

        add(new ViewToolbar("User List", ViewToolbar.group(createBtn)));
        add(userGrid);
        add(editDialog);
    }
}
