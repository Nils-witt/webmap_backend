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
import dev.nilswitt.webmap.base.ui.MainLayout;
import dev.nilswitt.webmap.base.ui.ViewToolbar;
import dev.nilswitt.webmap.entities.MapGroup;
import dev.nilswitt.webmap.entities.SecurityGroup;
import dev.nilswitt.webmap.entities.User;
import dev.nilswitt.webmap.entities.repositories.MapGroupRepository;
import dev.nilswitt.webmap.security.PermissionUtil;
import dev.nilswitt.webmap.views.components.MapGroupEditDialog;
import dev.nilswitt.webmap.views.filters.MapGroupFilter;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Objects;

@Route(value = "ui/map/groups", layout = MainLayout.class)
@Menu(order = 4, icon = "vaadin:key", title = "Map Groups")
@RolesAllowed("MAPGROUP_VIEW")
public class MapGroupView extends VerticalLayout {
    private final Grid<MapGroup> mapGroupGrid;
    private final Button createBtn;
    private final MapGroupEditDialog editDialog;


    private final MapGroupRepository mapGroupRepository;
    private final MapGroupFilter mapGroupFilter;
    private final AuthenticationContext authenticationContext;
    private final PermissionUtil permissionUtil ;

    public MapGroupView(MapGroupRepository repository, AuthenticationContext authenticationContext, PermissionUtil permissionUtil) {
        this.permissionUtil = permissionUtil;
        this.mapGroupRepository = repository;
        this.authenticationContext = authenticationContext;

        this.mapGroupGrid = new Grid<>(MapGroup.class, false);

        this.editDialog = new MapGroupEditDialog((securityGroup) -> {
            this.mapGroupRepository.save(securityGroup);
            this.mapGroupGrid.getDataProvider().refreshAll();
        });


        this.createBtn = new Button("Create", event -> {
            User user = currentUser();
            if (!PermissionUtil.hasAnyScope(user, SecurityGroup.UserRoleTypeEnum.MAPGROUP,
                    SecurityGroup.UserRoleScopeEnum.CREATE)) {
                Notification.show("You cannot create map groups");
                return;
            }
            this.editDialog.open(null);
        });
        this.createBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        this.mapGroupGrid.setItemsPageable(this::list);
        this.mapGroupGrid.addColumn(MapGroup::getName).setKey(String.valueOf(MapGroupFilter.Columns.NAME)).setHeader("Name");

        this.mapGroupGrid.setEmptyStateText("There are no map groups");
        this.mapGroupGrid.setSizeFull();
        this.mapGroupGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER);

        new MapGroupContextMenu(this.mapGroupGrid);
        this.mapGroupFilter = new MapGroupFilter((securityGroupExample -> {
            this.mapGroupGrid.getDataProvider().refreshAll();
        }));
        this.mapGroupFilter.setUp(this.mapGroupGrid);

        this.setSizeFull();
        this.setPadding(false);
        this.setSpacing(false);
        this.getStyle().setOverflow(Style.Overflow.HIDDEN);

        this.add(new ViewToolbar("Map Groups", ViewToolbar.group(this.createBtn)));
        this.add(this.mapGroupGrid);
        this.add(this.editDialog);
    }

    private User currentUser() {
        return this.authenticationContext.getAuthenticatedUser(User.class).orElse(null);
    }

    private List<MapGroup> list(Pageable pageable) {
        return this.mapGroupRepository.findAll(this.mapGroupFilter.getExample(), pageable).stream().toList();
    }

    private class MapGroupContextMenu extends GridContextMenu<MapGroup> {
        public MapGroupContextMenu(Grid<MapGroup> target) {
            super(target);
            this.addItem("Edit", event -> {
                event.getItem().ifPresent(mapGroup -> {
                    User user = currentUser();
                    if (!permissionUtil.hasAccess(user, SecurityGroup.UserRoleScopeEnum.EDIT, SecurityGroup.UserRoleTypeEnum.SECURITYGROUP)) {
                        Notification.show("You cannot edit map groups");
                        return;
                    }
                    editDialog.open(mapGroup);
                });
            });
            this.addItem("Delete", event -> {
                event.getItem().ifPresent(mapGroup -> {
                    User user = currentUser();
                    if (!permissionUtil.hasAccess(user, SecurityGroup.UserRoleScopeEnum.DELETE, SecurityGroup.UserRoleTypeEnum.SECURITYGROUP)) {
                        Notification.show("You cannot delete map groups");
                        return;
                    }
                    ConfirmDialog confirmDialog = new ConfirmDialog();
                    confirmDialog.setHeader("Delete Map Group");
                    confirmDialog.setText("Are you sure you want to delete map groups '" + mapGroup.getName() + "'?");
                    confirmDialog.setCancelable(true);
                    confirmDialog.setConfirmText("Delete");
                    confirmDialog.addConfirmListener(e -> {
                        mapGroupRepository.delete(mapGroup);
                        mapGroupGrid.getDataProvider().refreshAll();
                        confirmDialog.close();
                        this.remove(confirmDialog);
                    });
                    add(confirmDialog);
                    confirmDialog.open();
                });
            });
            this.setDynamicContentHandler(Objects::nonNull);
        }
    }
}
