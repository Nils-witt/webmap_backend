package dev.nilswitt.webmap.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.dom.Style;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.security.AuthenticationContext;
import dev.nilswitt.webmap.base.ui.ViewToolbar;
import dev.nilswitt.webmap.entities.MapItem;
import dev.nilswitt.webmap.entities.SecurityGroup;
import dev.nilswitt.webmap.entities.User;
import dev.nilswitt.webmap.entities.repositories.*;
import dev.nilswitt.webmap.security.PermissionUtil;
import dev.nilswitt.webmap.views.components.MapItemEditDialog;
import dev.nilswitt.webmap.views.components.MapItemPermissionsDialog;
import dev.nilswitt.webmap.views.filters.MapItemFilter;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Objects;

@Route("ui/map/items")
@Menu(order = 1, icon = "vaadin:map-marker", title = "Map Items")
@RolesAllowed("MAPITEM_VIEW")
public class MapItemView extends VerticalLayout {
    private final Grid<MapItem> mapItemGrid = new Grid<>();
    private final Button createBtn = new Button("Create");
    private final MapItemEditDialog editDialog;
    private final MapItemPermissionsDialog permissionsDialog;

    private final MapItemRepository mapItemRepository;
    private final MapItemFilter mapItemFilter;
    private final AuthenticationContext authenticationContext;
    private final PermissionUtil permissionUtil;

    public MapItemView(MapItemRepository mapItemRepository, AuthenticationContext authenticationContext, SecurityGroupRepository securityGroupRepository,
                       SecurityGroupPermissionsRepository securityGroupPermissionsRepository,
                       UserPermissionsRepository userPermissionsRepository,
                       UserRepository userRepository, PermissionUtil permissionUtil, MapGroupRepository mapGroupRepository) {
        this.permissionUtil = permissionUtil;
        this.mapItemRepository = mapItemRepository;
        this.authenticationContext = authenticationContext;
        this.editDialog = new MapItemEditDialog(mapItem -> {
            this.mapItemRepository.save(mapItem);
            this.mapItemGrid.getDataProvider().refreshAll();
        }, mapGroupRepository);
        this.permissionsDialog = new MapItemPermissionsDialog(userPermissionsRepository, userRepository, securityGroupRepository, securityGroupPermissionsRepository);
        this.configureCreateButton();
        this.configureGrid();
        this.mapItemFilter = new MapItemFilter(securityGroupExample -> {
            this.mapItemGrid.getDataProvider().refreshAll();
        }, mapGroupRepository);
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
        this.createBtn.addClickListener(event -> {
            User user = currentUser();
            if (!PermissionUtil.hasAnyScope(user, SecurityGroup.UserRoleTypeEnum.MAPITEM,
                    SecurityGroup.UserRoleScopeEnum.CREATE)) {
                Notification.show("You cannot create map items");
                return;
            }
            this.editDialog.open(null);
        });
    }

    private void configureGrid() {
        this.mapItemGrid.setItemsPageable(this::list);
        this.mapItemGrid.addColumn(MapItem::getName).setKey(String.valueOf(MapItemFilter.Columns.NAME)).setHeader("Name");
        this.mapItemGrid.addColumn(mapItem -> mapItem.getMapGroup() != null ? mapItem.getMapGroup().getName() : "None").setKey("mapGroup").setHeader("Map Group");
        this.mapItemGrid.addColumn(mapItem -> mapItem.getPosition().getLatitude()).setHeader("Latitude");
        this.mapItemGrid.addColumn(mapItem -> mapItem.getPosition().getLongitude()).setHeader("Longitude");
        this.mapItemGrid.addColumn(mapItem -> mapItem.getPosition().getAltitude()).setHeader("Altitude");

        this.mapItemGrid.setEmptyStateText("There are no map items");
        this.mapItemGrid.setSizeFull();
        this.mapItemGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        new MapItemContextMenu(this.mapItemGrid);

    }

    private User currentUser() {
        return this.authenticationContext.getAuthenticatedUser(User.class).orElse(null);
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
            this.addItem("Permissions", event -> {
                event.getItem().ifPresent(mapOverlay -> {
                    User user = currentUser();
                    if (!permissionUtil.hasAccess(user, SecurityGroup.UserRoleScopeEnum.ADMIN, SecurityGroup.UserRoleTypeEnum.MAPITEM)) {
                        Notification.show("You cannot edit overlay permissions");
                        return;
                    }
                    permissionsDialog.open(mapOverlay);
                });
            });
            this.addItem("Edit", event -> event.getItem().ifPresent(mapItem -> {
                User user = currentUser();
                if (!PermissionUtil.hasAnyScope(user, SecurityGroup.UserRoleTypeEnum.MAPITEM, SecurityGroup.UserRoleScopeEnum.EDIT, SecurityGroup.UserRoleScopeEnum.CREATE)) {
                    Notification.show("You cannot edit map items");
                    return;
                }
                editDialog.open(mapItem);
            }));
            this.addItem("Delete", event -> event.getItem().ifPresent(mapItem -> {
                User user = currentUser();
                if (!permissionUtil.hasAccess(user, SecurityGroup.UserRoleScopeEnum.DELETE, SecurityGroup.UserRoleTypeEnum.MAPITEM)) {
                    Notification.show("You cannot delete map items");
                    return;
                }
                MapItemView.this.openDeleteDialog(mapItem);
            }));
            setDynamicContentHandler(Objects::nonNull);
        }
    }
}
