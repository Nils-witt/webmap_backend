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
import com.vaadin.flow.server.auth.AnonymousAllowed;
import dev.nilswitt.webmap.base.ui.ViewToolbar;
import dev.nilswitt.webmap.entities.MapBaseLayer;
import dev.nilswitt.webmap.entities.repositories.MapBaseLayerRepository;
import dev.nilswitt.webmap.views.components.MapBaseLayerEditDialog;

import static com.vaadin.flow.spring.data.VaadinSpringDataHelpers.toSpringPageRequest;

@Route("ui/map/baselayer")
@Menu(order = 1, icon = "vaadin:map-marker", title = "Map Base Layer")
@AnonymousAllowed
public class MapBaseLayerView extends VerticalLayout {
    private final Grid<MapBaseLayer> mapBaseLayerGrid = new Grid<>();
    private final Button createBtn = new Button("Create");
    private final MapBaseLayerEditDialog editDialog;

    public MapBaseLayerView(MapBaseLayerRepository mapBaseLayerRepository) {
        editDialog = new MapBaseLayerEditDialog(mapItem -> {
            mapBaseLayerRepository.save(mapItem);
            mapBaseLayerGrid.getDataProvider().refreshAll();
        });

        configureCreateButton();
        configureGrid(mapBaseLayerRepository);

        setSizeFull();
        setPadding(false);
        setSpacing(false);
        getStyle().setOverflow(Style.Overflow.HIDDEN);

        add(new ViewToolbar("Map BaseLayer List", ViewToolbar.group(createBtn)));
        add(mapBaseLayerGrid, editDialog);
    }

    private void configureCreateButton() {
        createBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        createBtn.addClickListener(event -> editDialog.open(null));
    }

    private void configureGrid(MapBaseLayerRepository mapBaseLayerRepository) {
        mapBaseLayerGrid.setItems(query -> mapBaseLayerRepository.findAll(toSpringPageRequest(query)).stream());
        mapBaseLayerGrid.addColumn(MapBaseLayer::getName).setHeader("Name");
        mapBaseLayerGrid.addColumn(MapBaseLayer::getUrl).setHeader("Url");

        mapBaseLayerGrid.setEmptyStateText("There are no map items");
        mapBaseLayerGrid.setSizeFull();
        mapBaseLayerGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER);

        GridContextMenu<MapBaseLayer> menu = mapBaseLayerGrid.addContextMenu();
        menu.addItem("Edit", event -> event.getItem().ifPresent(editDialog::open));
        menu.addItem("Delete", event -> event.getItem().ifPresent(entity -> openDeleteDialog(mapBaseLayerRepository, entity)));
    }

    private void openDeleteDialog(MapBaseLayerRepository mapBaseLayerRepository, MapBaseLayer entity) {
        ConfirmDialog confirmDialog = new ConfirmDialog();
        confirmDialog.setHeader("Delete Map Item");
        confirmDialog.setText("Are you sure you want to delete map item '" + entity.getName() + "'?");
        confirmDialog.setCancelable(true);
        confirmDialog.setConfirmText("Delete");
        confirmDialog.addConfirmListener(e -> {
            mapBaseLayerRepository.delete(entity);
            mapBaseLayerGrid.getDataProvider().refreshAll();
            confirmDialog.close();
            remove(confirmDialog);
        });
        add(confirmDialog);
        confirmDialog.open();
    }
}
