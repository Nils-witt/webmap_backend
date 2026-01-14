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
import dev.nilswitt.webmap.entities.MapItem;
import dev.nilswitt.webmap.entities.repositories.MapItemRepository;
import dev.nilswitt.webmap.views.components.MapItemEditDialog;
import jakarta.annotation.security.PermitAll;

import static com.vaadin.flow.spring.data.VaadinSpringDataHelpers.toSpringPageRequest;

@Route("ui/map-items")
@Menu(order = 1, icon = "vaadin:map-marker", title = "Map Items")
@PermitAll
public class MapItemView extends VerticalLayout {
    private final Grid<MapItem> mapItemGrid = new Grid<>();
    private final Button createBtn = new Button("Create");
    private final MapItemEditDialog editDialog;

    public MapItemView(MapItemRepository mapItemRepository) {
        editDialog = new MapItemEditDialog(mapItem -> {
            mapItemRepository.save(mapItem);
            mapItemGrid.getDataProvider().refreshAll();
        });

        configureCreateButton();
        configureGrid(mapItemRepository);

        setSizeFull();
        setPadding(false);
        setSpacing(false);
        getStyle().setOverflow(Style.Overflow.HIDDEN);

        add(new ViewToolbar("Map Item List", ViewToolbar.group(createBtn)));
        add(mapItemGrid, editDialog);
    }

    private void configureCreateButton() {
        createBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        createBtn.addClickListener(event -> editDialog.open(null));
    }

    private void configureGrid(MapItemRepository mapItemRepository) {
        mapItemGrid.setItems(query -> mapItemRepository.findAll(toSpringPageRequest(query)).stream());
        mapItemGrid.addColumn(MapItem::getName).setHeader("Name");
        mapItemGrid.addColumn(mapItem -> mapItem.getPosition().getLatitude()).setHeader("Latitude");
        mapItemGrid.addColumn(mapItem -> mapItem.getPosition().getLongitude()).setHeader("Longitude");
        mapItemGrid.addColumn(mapItem -> mapItem.getPosition().getAltitude()).setHeader("Altitude");

        mapItemGrid.setEmptyStateText("There are no map items");
        mapItemGrid.setSizeFull();
        mapItemGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER);

        GridContextMenu<MapItem> menu = mapItemGrid.addContextMenu();
        menu.addItem("Edit", event -> event.getItem().ifPresent(editDialog::open));
        menu.addItem("Delete", event -> event.getItem().ifPresent(mapItem -> openDeleteDialog(mapItemRepository, mapItem)));
    }

    private void openDeleteDialog(MapItemRepository mapItemRepository, MapItem mapItem) {
        ConfirmDialog confirmDialog = new ConfirmDialog();
        confirmDialog.setHeader("Delete Map Item");
        confirmDialog.setText("Are you sure you want to delete map item '" + mapItem.getName() + "'?");
        confirmDialog.setCancelable(true);
        confirmDialog.setConfirmText("Delete");
        confirmDialog.addConfirmListener(e -> {
            mapItemRepository.delete(mapItem);
            mapItemGrid.getDataProvider().refreshAll();
            confirmDialog.close();
            remove(confirmDialog);
        });
        add(confirmDialog);
        confirmDialog.open();
    }
}
