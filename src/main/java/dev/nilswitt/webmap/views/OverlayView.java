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
import dev.nilswitt.webmap.entities.MapOverlay;
import dev.nilswitt.webmap.entities.SecurityGroup;
import dev.nilswitt.webmap.entities.User;
import dev.nilswitt.webmap.entities.repositories.MapOverlayRepository;
import dev.nilswitt.webmap.entities.repositories.SecurityGroupRepository;
import dev.nilswitt.webmap.records.OverlayConfig;
import dev.nilswitt.webmap.views.components.MapOverlayEditDialog;
import dev.nilswitt.webmap.views.components.UploadOverlayDialog;
import dev.nilswitt.webmap.views.filters.OverlayFilter;
import jakarta.annotation.security.RolesAllowed;
import org.apache.commons.io.FileUtils;
import org.springframework.data.domain.Pageable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import dev.nilswitt.webmap.security.PermissionUtil;

@Route("ui/map/overlays")
@Menu(order = 2, icon = "vaadin:clipboard-check", title = "Overlays")
@RolesAllowed("ROLE_MAP_OVERLAYS_VIEW")
public class OverlayView extends VerticalLayout {

    private  final Grid<MapOverlay> mapOverlayGrid;
    private final Button createBtn;
    private  final MapOverlayEditDialog editDialog;

    private final OverlayConfig overlayConfig;
    private final OverlayFilter overlayFilter;

    private final MapOverlayRepository mapOverlayRepository;
    private final AuthenticationContext authenticationContext;

    public OverlayView(MapOverlayRepository mapOverlayRepository, OverlayConfig overlayConfig, SecurityGroupRepository securityGroupRepository,
                       AuthenticationContext authenticationContext) {
        this.mapOverlayRepository = mapOverlayRepository;
        this.overlayConfig = overlayConfig;
        this.authenticationContext = authenticationContext;

        this.mapOverlayGrid = new Grid<>();

        this.editDialog = new MapOverlayEditDialog((mapOverlay) -> {
            this.mapOverlayRepository.save(mapOverlay);
            this.mapOverlayGrid.getDataProvider().refreshAll();
        }, securityGroupRepository);


        this.createBtn = new Button("Create", event -> {
            User user = currentUser();
            if (!PermissionUtil.hasAnyScope(user, SecurityGroup.UserRoleTypeEnum.MAP_OVERLAYS,
                    SecurityGroup.UserRoleScopeEnum.CREATE)) {
                Notification.show("You cannot create overlays");
                return;
            }
            this.editDialog.open(null);
        });
        this.createBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        this.mapOverlayGrid.setItemsPageable(this::list);
        this.mapOverlayGrid.addColumn(MapOverlay::getName).setKey(String.valueOf(OverlayFilter.Columns.NAME)).setHeader("Name");
        this.mapOverlayGrid.addColumn(MapOverlay::getFullTileUrl).setHeader("Url");
        this.mapOverlayGrid.addColumn(MapOverlay::getLayerVersion).setHeader("Layer Version");

        this.mapOverlayGrid.setEmptyStateText("There are no overlays");
        this.mapOverlayGrid.setSizeFull();
        this.mapOverlayGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER);


        new SecurityGroupContextMenu(this.mapOverlayGrid);
        this.overlayFilter = new OverlayFilter((mapOverlayExample -> {
            this.mapOverlayGrid.getDataProvider().refreshAll();
        }));
        this.overlayFilter.setUp(this.mapOverlayGrid);

        this.setSizeFull();
        this.setPadding(false);
        this.setSpacing(false);
        this.getStyle().setOverflow(Style.Overflow.HIDDEN);

        this.add(new ViewToolbar("Overlay List", ViewToolbar.group(createBtn)));
        this.add(mapOverlayGrid);
        this.add(editDialog);
    }

    private User currentUser() {
        return this.authenticationContext.getAuthenticatedUser(User.class).orElse(null);
    }

    private List<MapOverlay> list(Pageable pageable) {
        return this.mapOverlayRepository.findAll(this.overlayFilter.getExample(), pageable).stream().toList();
    }

    private class SecurityGroupContextMenu extends GridContextMenu<MapOverlay> {
        public SecurityGroupContextMenu(Grid<MapOverlay> target) {
            super(target);
            this.addItem("upload", event -> {
                event.getItem().ifPresent(mapOverlay -> {
                    User user = currentUser();
                    if (!PermissionUtil.hasAnyScope(user, SecurityGroup.UserRoleTypeEnum.MAP_OVERLAYS,
                            SecurityGroup.UserRoleScopeEnum.EDIT, SecurityGroup.UserRoleScopeEnum.CREATE)) {
                        Notification.show("You cannot upload for overlays");
                        return;
                    }
                    UploadOverlayDialog uploadOverlayDialog = new UploadOverlayDialog(mapOverlayRepository, mapOverlay, overlayConfig);
                    add(uploadOverlayDialog);
                    uploadOverlayDialog.open();
                });
            });
            this.addItem("Edit", event -> {
                event.getItem().ifPresent(mapOverlay -> {
                    User user = currentUser();
                    if (!PermissionUtil.hasAnyScope(user, SecurityGroup.UserRoleTypeEnum.MAP_OVERLAYS,
                            SecurityGroup.UserRoleScopeEnum.EDIT, SecurityGroup.UserRoleScopeEnum.CREATE)) {
                        Notification.show("You cannot edit overlays");
                        return;
                    }
                    editDialog.open(mapOverlay);
                });
            });
            this.addItem("Delete", event -> {
                event.getItem().ifPresent(mapOverlay -> {
                    User user = currentUser();
                    if (!PermissionUtil.hasScope(user, SecurityGroup.UserRoleTypeEnum.MAP_OVERLAYS,
                            SecurityGroup.UserRoleScopeEnum.DELETE)) {
                        Notification.show("You cannot delete overlays");
                        return;
                    }
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
                    add(confirmDialog);
                    confirmDialog.open();
                });
            });
            this.addItem("Delete old Versions", event -> {
                event.getItem().ifPresent(mapOverlay -> {
                    User user = currentUser();
                    if (!PermissionUtil.hasScope(user, SecurityGroup.UserRoleTypeEnum.MAP_OVERLAYS,
                            SecurityGroup.UserRoleScopeEnum.DELETE)) {
                        Notification.show("You cannot delete overlay versions");
                        return;
                    }
                    // Call method to delete old file versions
                    File baseOverlayDir = Path.of(overlayConfig.basePath(), mapOverlay.getId().toString()).toFile();
                    if (baseOverlayDir.exists() && baseOverlayDir.isDirectory()) {
                        File[] versionDirs = baseOverlayDir.listFiles(File::isDirectory);
                        if (versionDirs != null) {
                            for (File versionDir : versionDirs) {
                                try {
                                    int version = Integer.parseInt(versionDir.getName());
                                    if (version < mapOverlay.getLayerVersion()) {
                                        FileUtils.deleteDirectory(versionDir);
                                    }
                                } catch (NumberFormatException e) {
                                    // Ignore directories that are not version numbers
                                } catch (IOException e) {
                                    // Handle deletion error
                                }
                            }
                        }
                    }
                });
            });

            this.setDynamicContentHandler(Objects::nonNull);
        }
    }
}
