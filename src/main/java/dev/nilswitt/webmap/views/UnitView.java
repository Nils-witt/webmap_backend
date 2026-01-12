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
import dev.nilswitt.webmap.entities.Unit;
import dev.nilswitt.webmap.entities.repositories.UnitRepository;
import dev.nilswitt.webmap.views.components.UnitEditDialog;

import static com.vaadin.flow.spring.data.VaadinSpringDataHelpers.toSpringPageRequest;

@Route("units")
@Menu(order = 2, icon = "vaadin:road", title = "Units")
public class UnitView extends VerticalLayout {
    private final Grid<Unit> unitGrid = new Grid<>();
    private final Button createBtn = new Button("Create");
    private final UnitEditDialog editDialog;

    public UnitView(UnitRepository unitRepository) {
        editDialog = new UnitEditDialog(unit -> {
            unitRepository.save(unit);
            unitGrid.getDataProvider().refreshAll();
        });

        configureCreateButton();
        configureGrid(unitRepository);

        setSizeFull();
        setPadding(false);
        setSpacing(false);
        getStyle().setOverflow(Style.Overflow.HIDDEN);

        add(new ViewToolbar("Unit List", ViewToolbar.group(createBtn)));
        add(unitGrid, editDialog);
    }

    private void configureCreateButton() {
        createBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        createBtn.addClickListener(event -> editDialog.open(null));
    }

    private void configureGrid(UnitRepository unitRepository) {
        unitGrid.setItems(query -> unitRepository.findAll(toSpringPageRequest(query)).stream());
        unitGrid.addColumn(Unit::getName).setHeader("Name");
        unitGrid.addColumn(unit -> unit.getPosition().getLatitude()).setHeader("Latitude");
        unitGrid.addColumn(unit -> unit.getPosition().getLongitude()).setHeader("Longitude");
        unitGrid.addColumn(unit -> unit.getPosition().getAltitude()).setHeader("Altitude");
        unitGrid.addColumn(Unit::getStatus).setHeader("Status");
        unitGrid.addColumn(unit -> unit.isSpeakRequest() ? "Yes" : "No").setHeader("Speak Request");

        unitGrid.setEmptyStateText("There are no units");
        unitGrid.setSizeFull();
        unitGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER);

        GridContextMenu<Unit> menu = unitGrid.addContextMenu();
        menu.addItem("Edit", event -> event.getItem().ifPresent(editDialog::open));
        menu.addItem("Delete", event -> event.getItem().ifPresent(unit -> openDeleteDialog(unitRepository, unit)));
    }

    private void openDeleteDialog(UnitRepository unitRepository, Unit unit) {
        ConfirmDialog confirmDialog = new ConfirmDialog();
        confirmDialog.setHeader("Delete Unit");
        confirmDialog.setText("Are you sure you want to delete unit '" + unit.getName() + "'?");
        confirmDialog.setCancelable(true);
        confirmDialog.setConfirmText("Delete");
        confirmDialog.addConfirmListener(e -> {
            unitRepository.delete(unit);
            unitGrid.getDataProvider().refreshAll();
            confirmDialog.close();
            remove(confirmDialog);
        });
        add(confirmDialog);
        confirmDialog.open();
    }
}
