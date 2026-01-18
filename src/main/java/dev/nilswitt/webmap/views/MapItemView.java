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
import dev.nilswitt.webmap.views.filters.MapItemFilter;
import jakarta.annotation.security.PermitAll;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Objects;

@Route("ui/map/items")
@Menu(order = 1, icon = "vaadin:map-marker", title = "Map Items")
@PermitAll
public class MapItemView extends VerticalLayout {
    private final Grid<MapItem> mapItemGrid = new Grid<>();
    private final Button createBtn = new Button("Create");
    private final MapItemEditDialog editDialog;

    private final MapItemRepository mapItemRepository;
    private final MapItemFilter mapItemFilter;

    public MapItemView(MapItemRepository mapItemRepository) {
        this.mapItemRepository = mapItemRepository;
        this.editDialog = new MapItemEditDialog(mapItem -> {
            this.mapItemRepository.save(mapItem);
            this.mapItemGrid.getDataProvider().refreshAll();
        });

        this.configureCreateButton();
        this.configureGrid();
        this.mapItemFilter = new MapItemFilter(securityGroupExample -> {
            this.mapItemGrid.getDataProvider().refreshAll();
        });
        this.mapItemFilter.setUp(this.mapItemGrid);

        this.setSizeFull();
        this.setPadding(false);
        this.setSpacing(false);
        this.getStyle().setOverflow(Style.Overflow.HIDDEN);

        this.add(new ViewToolbar("Map Item List", ViewToolbar.group(this.createBtn)));
        this.add(this.mapItemGrid, this.editDialog);
    }

    private void configureCreateButton() {
        this.createBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        this.createBtn.addClickListener(event -> editDialog.open(null));
    }

    private void configureGrid() {
        this.mapItemGrid.setItemsPageable(this::list);
        this.mapItemGrid.addColumn(MapItem::getName).setKey(String.valueOf(MapItemFilter.Columns.NAME)).setHeader("Name");
        this.mapItemGrid.addColumn(mapItem -> mapItem.getPosition().getLatitude()).setHeader("Latitude");
        this.mapItemGrid.addColumn(mapItem -> mapItem.getPosition().getLongitude()).setHeader("Longitude");
        this.mapItemGrid.addColumn(mapItem -> mapItem.getPosition().getAltitude()).setHeader("Altitude");

        this.mapItemGrid.setEmptyStateText("There are no map items");
        this.mapItemGrid.setSizeFull();
        this.mapItemGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        new MapItemContextMenu(this.mapItemGrid);

    }

    private void openDeleteDialog(MapItem mapItem) {
        ConfirmDialog confirmDialog = new ConfirmDialog();
        confirmDialog.setHeader("Delete Map Item");
        confirmDialog.setText("Are you sure you want to delete map item '" + mapItem.getName() + "'?");
        confirmDialog.setCancelable(true);
        confirmDialog.setConfirmText("Delete");
        confirmDialog.addConfirmListener(e -> {
            this.mapItemRepository.delete(mapItem);
            this.mapItemGrid.getDataProvider().refreshAll();
            confirmDialog.close();
            this.remove(confirmDialog);
        });
        this.add(confirmDialog);
        confirmDialog.open();
    }

    public List<MapItem> list(Pageable pageable) {
        return this.mapItemRepository.findAll(this.mapItemFilter.getExample(), pageable).stream().toList();
    }

    private class MapItemContextMenu extends GridContextMenu<MapItem> {
        public MapItemContextMenu(Grid<MapItem> target) {
            super(target);
            this.addItem("Edit", event -> event.getItem().ifPresent(editDialog::open));
            this.addItem("Delete", event -> event.getItem().ifPresent(MapItemView.this::openDeleteDialog));
            setDynamicContentHandler(Objects::nonNull);
        }
    }
}
