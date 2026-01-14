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
import dev.nilswitt.webmap.entities.MapOverlay;
import dev.nilswitt.webmap.entities.repositories.MapOverlayRepository;
import dev.nilswitt.webmap.views.components.MapOverlayEditDialog;
import jakarta.annotation.security.RolesAllowed;

import java.util.Optional;

import static com.vaadin.flow.spring.data.VaadinSpringDataHelpers.toSpringPageRequest;

@Route("ui/overlays")
@Menu(order = 2, icon = "vaadin:clipboard-check", title = "Overlays")
@RolesAllowed("ROLE_MAP_OVERLAYS_VIEW")
public class OverlayView extends VerticalLayout {
    final Grid<MapOverlay> mapOverlayGrid;
    final Button createBtn;
    final MapOverlayEditDialog editDialog;

    public OverlayView(MapOverlayRepository mapOverlayRepository) {
        this.mapOverlayGrid = new Grid<>();

        this.editDialog = new MapOverlayEditDialog((mapOverlay) -> {
            mapOverlayRepository.save(mapOverlay);
            mapOverlayGrid.getDataProvider().refreshAll();
        });


        createBtn = new Button("Create", event -> {
            editDialog.open(null);
        });
        createBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        mapOverlayGrid.setItems(query -> mapOverlayRepository.findAll(toSpringPageRequest(query)).stream());
        mapOverlayGrid.addColumn(MapOverlay::getName).setHeader("Name");

        mapOverlayGrid.setEmptyStateText("There are no overlays");
        mapOverlayGrid.setSizeFull();
        mapOverlayGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        GridContextMenu<MapOverlay> menu = mapOverlayGrid.addContextMenu();
        menu.addItem("Edit", event -> {
            Optional<MapOverlay> item = event.getItem();
            item.ifPresent(editDialog::open);
        });
        menu.addItem("Delete", event -> {
            Optional<MapOverlay> item = event.getItem();
            item.ifPresent(mapOverlay -> {
                ConfirmDialog confirmDialog = new ConfirmDialog();
                confirmDialog.setHeader("Delete Overlay");
                confirmDialog.setText("Are you sure you want to delete overlay '" + mapOverlay.getName() + "'?");
                confirmDialog.setCancelable(true);
                confirmDialog.setConfirmText("Delete");
                confirmDialog.addConfirmListener(e -> {
                    mapOverlayRepository.delete(mapOverlay);
                    mapOverlayGrid.getDataProvider().refreshAll();
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

        add(new ViewToolbar("Overlay List", ViewToolbar.group(createBtn)));
        add(mapOverlayGrid);
        add(editDialog);
    }
}
