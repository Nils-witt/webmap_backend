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
import dev.nilswitt.webmap.entities.UserRole;
import dev.nilswitt.webmap.entities.repositories.UserRoleRepository;
import dev.nilswitt.webmap.views.components.RoleEditDialog;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Optional;

import static com.vaadin.flow.spring.data.VaadinSpringDataHelpers.toSpringPageRequest;

@Route("roles")
@Menu(order = 1, icon = "vaadin:key", title = "Roles")
public class RolesView extends VerticalLayout {
    final Grid<UserRole> userRoleGrid;
    final Button createBtn;
    final RoleEditDialog editDialog;

    public RolesView(UserRoleRepository userRoleRepository) {
        this.userRoleGrid = new Grid<>();

        this.editDialog = new RoleEditDialog((userRole) -> {
            userRoleRepository.save(userRole);
            userRoleGrid.getDataProvider().refreshAll();
        });


        createBtn = new Button("Create", event -> {
            editDialog.open(null);
        });
        createBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        var dateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).withLocale(getLocale())
                .withZone(ZoneId.systemDefault());
        var dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(getLocale());

        userRoleGrid.setItems(query -> userRoleRepository.findAll(toSpringPageRequest(query)).stream());
        userRoleGrid.addColumn(UserRole::getName).setHeader("Name");

        userRoleGrid.setEmptyStateText("There are no roles");
        userRoleGrid.setSizeFull();
        userRoleGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        GridContextMenu<UserRole> menu = userRoleGrid.addContextMenu();
        menu.addItem("Edit", event -> {
            Optional<UserRole> item = event.getItem();
            item.ifPresent(editDialog::open);
        });
        menu.addItem("Delete", event -> {
            Optional<UserRole> item = event.getItem();
            item.ifPresent(userRole -> {
                ConfirmDialog confirmDialog = new ConfirmDialog();
                confirmDialog.setHeader("Delete Role");
                confirmDialog.setText("Are you sure you want to delete role '" + userRole.getName() + "'?");
                confirmDialog.setCancelable(true);
                confirmDialog.setConfirmText("Delete");
                confirmDialog.addConfirmListener(e -> {
                    userRoleRepository.delete(userRole);
                    userRoleGrid.getDataProvider().refreshAll();
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

        add(new ViewToolbar("Role List", ViewToolbar.group(createBtn)));
        add(userRoleGrid);
        add(editDialog);
    }
}
