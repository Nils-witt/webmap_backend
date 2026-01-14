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
import dev.nilswitt.webmap.base.ui.MainLayout;
import dev.nilswitt.webmap.base.ui.ViewToolbar;
import dev.nilswitt.webmap.entities.SecurityGroup;
import dev.nilswitt.webmap.entities.repositories.SecurityGroupRepository;
import dev.nilswitt.webmap.views.components.SecurityGroupEditDialog;
import jakarta.annotation.security.PermitAll;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Optional;

import static com.vaadin.flow.spring.data.VaadinSpringDataHelpers.toSpringPageRequest;

@Route(value = "ui/groups", layout = MainLayout.class)
@Menu(order = 4, icon = "vaadin:key", title = "Roles")
@PermitAll
public class SecurityGroupView extends VerticalLayout {
    final Grid<SecurityGroup> securityGroupGrid;
    final Button createBtn;
    final SecurityGroupEditDialog editDialog;

    public SecurityGroupView(SecurityGroupRepository repository) {
        this.securityGroupGrid = new Grid<>();

        this.editDialog = new SecurityGroupEditDialog((securityGroup) -> {
            repository.save(securityGroup);
            securityGroupGrid.getDataProvider().refreshAll();
        });


        createBtn = new Button("Create", event -> {
            editDialog.open(null);
        });
        createBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        var dateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).withLocale(getLocale())
                .withZone(ZoneId.systemDefault());
        var dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(getLocale());

        securityGroupGrid.setItems(query -> repository.findAll(toSpringPageRequest(query)).stream());
        securityGroupGrid.addColumn(SecurityGroup::getName).setHeader("Name");

        securityGroupGrid.setEmptyStateText("There are no roles");
        securityGroupGrid.setSizeFull();
        securityGroupGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        GridContextMenu<SecurityGroup> menu = securityGroupGrid.addContextMenu();
        menu.addItem("Edit", event -> {
            Optional<SecurityGroup> item = event.getItem();
            item.ifPresent(editDialog::open);
        });
        menu.addItem("Delete", event -> {
            Optional<SecurityGroup> item = event.getItem();
            item.ifPresent(userRole -> {
                ConfirmDialog confirmDialog = new ConfirmDialog();
                confirmDialog.setHeader("Delete Group");
                confirmDialog.setText("Are you sure you want to delete role '" + userRole.getName() + "'?");
                confirmDialog.setCancelable(true);
                confirmDialog.setConfirmText("Delete");
                confirmDialog.addConfirmListener(e -> {
                    repository.delete(userRole);
                    securityGroupGrid.getDataProvider().refreshAll();
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

        add(new ViewToolbar("Security Groups", ViewToolbar.group(createBtn)));
        add(securityGroupGrid);
        add(editDialog);
    }
}
