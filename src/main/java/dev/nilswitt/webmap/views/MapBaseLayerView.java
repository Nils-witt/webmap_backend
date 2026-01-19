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
import dev.nilswitt.webmap.entities.MapBaseLayer;
import dev.nilswitt.webmap.entities.SecurityGroup;
import dev.nilswitt.webmap.entities.User;
import dev.nilswitt.webmap.entities.repositories.*;
import dev.nilswitt.webmap.security.PermissionUtil;
import dev.nilswitt.webmap.views.components.MapBaseLayerEditDialog;
import dev.nilswitt.webmap.views.components.MapBaseLayerPermissionsDialog;
import dev.nilswitt.webmap.views.components.OverlayPermissionsDialog;
import jakarta.annotation.security.RolesAllowed;

import static com.vaadin.flow.spring.data.VaadinSpringDataHelpers.toSpringPageRequest;

@Route("ui/map/baselayer")
@Menu(order = 1, icon = "vaadin:map-marker", title = "Map Base Layer")
@RolesAllowed("MAPBASELAYER_VIEW")
public class MapBaseLayerView extends VerticalLayout {
    private final Grid<MapBaseLayer> mapBaseLayerGrid = new Grid<>();
    private final Button createBtn = new Button("Create");
    private final MapBaseLayerEditDialog editDialog;
    private final AuthenticationContext authenticationContext;
    private final MapBaseLayerPermissionsDialog permissionsDialog;
    private final PermissionUtil permissionUtil;

    public MapBaseLayerView(MapBaseLayerRepository mapBaseLayerRepository, AuthenticationContext authenticationContext, SecurityGroupRepository securityGroupRepository,
                            SecurityGroupPermissionsRepository securityGroupPermissionsRepository,
                            UserPermissionsRepository userPermissionsRepository,
                            UserRepository userRepository, PermissionUtil permissionUtil) {
        this.authenticationContext = authenticationContext;
        this.permissionUtil = permissionUtil;
        editDialog = new MapBaseLayerEditDialog(mapItem -> {
            mapBaseLayerRepository.save(mapItem);
            mapBaseLayerGrid.getDataProvider().refreshAll();
        });
        this.permissionsDialog = new MapBaseLayerPermissionsDialog(userPermissionsRepository, userRepository, securityGroupRepository, securityGroupPermissionsRepository);
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
        createBtn.addClickListener(event -> {
            User user = currentUser();
            if (!PermissionUtil.hasAnyScope(user, SecurityGroup.UserRoleTypeEnum.MAPBASELAYER,
                    SecurityGroup.UserRoleScopeEnum.CREATE)) {
                Notification.show("You cannot create base layers");
                return;
            }
            editDialog.open(null);
        });
    }

    private void configureGrid(MapBaseLayerRepository mapBaseLayerRepository) {
        mapBaseLayerGrid.setItems(query -> mapBaseLayerRepository.findAll(toSpringPageRequest(query)).stream());
        mapBaseLayerGrid.addColumn(MapBaseLayer::getName).setHeader("Name");
        mapBaseLayerGrid.addColumn(MapBaseLayer::getUrl).setHeader("Url");

        mapBaseLayerGrid.setEmptyStateText("There are no map items");
        mapBaseLayerGrid.setSizeFull();
        mapBaseLayerGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER);

        GridContextMenu<MapBaseLayer> menu = mapBaseLayerGrid.addContextMenu();
        menu.addItem("Permissions", event -> {
            event.getItem().ifPresent(mapOverlay -> {
                User user = currentUser();
                if (!permissionUtil.hasAccess(user, SecurityGroup.UserRoleScopeEnum.ADMIN, SecurityGroup.UserRoleTypeEnum.MAPBASELAYER)) {
                    Notification.show("You cannot edit overlay permissions");
                    return;
                }
                permissionsDialog.open(mapOverlay);
            });
        });
        menu.addItem("Edit", event -> event.getItem().ifPresent(mapBaseLayer -> {
            User user = currentUser();
            if (!permissionUtil.hasAccess(user, SecurityGroup.UserRoleScopeEnum.EDIT, SecurityGroup.UserRoleTypeEnum.MAPBASELAYER)) {
                Notification.show("You cannot edit base layers");
                return;
            }
            editDialog.open(mapBaseLayer);
        }));
        menu.addItem("Delete", event -> event.getItem().ifPresent(entity -> {
            User user = currentUser();
            if (!permissionUtil.hasAccess(user, SecurityGroup.UserRoleScopeEnum.DELETE, SecurityGroup.UserRoleTypeEnum.MAPBASELAYER)) {
                Notification.show("You cannot delete base layers");
                return;
            }
            openDeleteDialog(mapBaseLayerRepository, entity);
        }));
    }

    private User currentUser() {
        return this.authenticationContext.getAuthenticatedUser(User.class).orElse(null);
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
