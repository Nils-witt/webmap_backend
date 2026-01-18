package dev.nilswitt.webmap.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.dom.Style;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.Route;
import dev.nilswitt.webmap.base.ui.ViewToolbar;
import dev.nilswitt.webmap.entities.Unit;
import dev.nilswitt.webmap.entities.repositories.UnitRepository;
import dev.nilswitt.webmap.views.components.UnitEditDialog;
import dev.nilswitt.webmap.views.filters.UnitFilter;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Route("ui/units")
@Menu(order = 3, icon = "vaadin:road", title = "Units")
@RolesAllowed("UNITS_VIEW")
public class UnitView extends VerticalLayout {
    private final Grid<Unit> unitGrid = new Grid<>();
    private final Button createBtn = new Button("Create");
    private final UnitEditDialog editDialog;
    private final TextField nameField = new TextField();
    private UnitRepository unitRepository;
    private UnitFilter unitFilter;

    public UnitView(UnitRepository unitRepository) {
        this.unitRepository = unitRepository;
        editDialog = new UnitEditDialog(unit -> {
            unitRepository.save(unit);
            refresh();
        });


        configureCreateButton();
        configureGrid(unitRepository);
        setSizeFull();
        setPadding(false);
        setSpacing(false);
        getStyle().setOverflow(Style.Overflow.HIDDEN);

        nameField.setValueChangeMode(ValueChangeMode.EAGER);
        nameField.setClearButtonVisible(true);
        nameField.addValueChangeListener(e -> {
            refresh();
        });


        add(new ViewToolbar("Unit List", ViewToolbar.group(nameField, createBtn)));
        add(unitGrid, editDialog);
    }

    private void configureCreateButton() {
        createBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        createBtn.addClickListener(event -> editDialog.open(null));
    }

    public List<Unit> list(Pageable pageable) {

        return unitRepository.findAll(unitFilter.getExample(), pageable).stream().toList();
    }

    private void refresh() {
        unitGrid.getDataProvider().refreshAll();
    }

    private void configureGrid(UnitRepository unitRepository) {
        unitGrid.setItemsPageable(this::list);
        unitGrid.addColumn(Unit::getName).setKey(String.valueOf(UnitFilter.Columns.NAME)).setHeader("Name").setSortable(true);
        unitGrid.addColumn(unit -> unit.getPosition().getLatitude()).setHeader("Latitude");
        unitGrid.addColumn(unit -> unit.getPosition().getLongitude()).setHeader("Longitude");
        unitGrid.addColumn(unit -> unit.getPosition().getAltitude()).setHeader("Altitude");
        unitGrid.addColumn(Unit::getStatus).setKey(String.valueOf(UnitFilter.Columns.STATUS)).setHeader("Status").setSortable(true);
        unitGrid.addColumn(unit -> unit.isSpeakRequest() ? "Yes" : "No").setHeader("Speak Request");

        unitGrid.setEmptyStateText("There are no units");
        unitGrid.setSizeFull();
        unitGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        unitFilter = new UnitFilter(example -> {
            unitGrid.getDataProvider().refreshAll();
            refresh();
        });
        unitFilter.setUp(unitGrid);


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
            refresh();
            confirmDialog.close();
            remove(confirmDialog);
        });
        add(confirmDialog);
        confirmDialog.open();
    }
}
