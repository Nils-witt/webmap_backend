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
import dev.nilswitt.webmap.entities.SecurityGroup;
import dev.nilswitt.webmap.entities.Unit;
import dev.nilswitt.webmap.entities.User;
import dev.nilswitt.webmap.entities.repositories.*;
import dev.nilswitt.webmap.security.PermissionUtil;
import dev.nilswitt.webmap.views.components.UnitEditDialog;
import dev.nilswitt.webmap.views.components.UnitIconEditDialog;
import dev.nilswitt.webmap.views.components.UnitPermissionsDialog;
import dev.nilswitt.webmap.views.filters.UnitFilter;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Objects;

@Route("ui/units")
@Menu(order = 3, icon = "vaadin:road", title = "Units")
@RolesAllowed("UNIT_VIEW")
public class UnitView extends VerticalLayout {
    private final Grid<Unit> unitGrid = new Grid<>();
    private final Button createBtn = new Button("Create");
    private final UnitEditDialog editDialog;
    private final UnitIconEditDialog iconEditDialog;
    private final UnitPermissionsDialog permissionsDialog;

    private final PermissionUtil permissionUtil;

    private final UnitRepository unitRepository;
    private final UnitFilter unitFilter;
    private final AuthenticationContext authenticationContext;

    public UnitView(UnitRepository unitRepository, AuthenticationContext authenticationContext, SecurityGroupRepository securityGroupRepository,
                    SecurityGroupPermissionsRepository securityGroupPermissionsRepository,
                    UserPermissionsRepository userPermissionsRepository,
                    UserRepository userRepository, PermissionUtil permissionUtil) {
        this.permissionUtil = permissionUtil;
        this.unitRepository = unitRepository;
        this.authenticationContext = authenticationContext;
        this.editDialog = new UnitEditDialog(unit -> {
            this.unitRepository.save(unit);
            this.refresh();
        });
        this.iconEditDialog = new UnitIconEditDialog(unit -> {
            this.unitRepository.save(unit);
            this.refresh();
        });
        this.permissionsDialog = new UnitPermissionsDialog(userPermissionsRepository, userRepository, securityGroupRepository, securityGroupPermissionsRepository);
        this.configureCreateButton();
        this.configureGrid();
        this.setSizeFull();
        this.setPadding(false);
        this.setSpacing(false);
        this.getStyle().setOverflow(Style.Overflow.HIDDEN);

        this.unitFilter = new UnitFilter(example -> {
            this.unitGrid.getDataProvider().refreshAll();
            this.refresh();
        });
        this.unitFilter.setUp(this.unitGrid);


        this.add(new ViewToolbar("Unit List", ViewToolbar.group(this.createBtn)));
        this.add(unitGrid, editDialog);
    }

    private void configureCreateButton() {
        this.createBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        this.createBtn.addClickListener(event -> {
            User user = currentUser();
            if (!PermissionUtil.hasAnyScope(user, SecurityGroup.UserRoleTypeEnum.UNIT,
                    SecurityGroup.UserRoleScopeEnum.CREATE)) {
                Notification.show("You cannot create units");
                return;
            }
            editDialog.open(null);
        });
    }

    public List<Unit> list(Pageable pageable) {

        return this.unitRepository.findAll(this.unitFilter.getExample(), pageable).stream().toList();
    }

    private void refresh() {
        this.unitGrid.getDataProvider().refreshAll();
    }

    private void configureGrid() {
        this.unitGrid.setItemsPageable(this::list);
        this.unitGrid.addColumn(Unit::getName).setKey(String.valueOf(UnitFilter.Columns.NAME)).setHeader("Name").setSortable(true);
        this.unitGrid.addColumn(unit -> unit.getPosition().getLatitude()).setHeader("Latitude");
        this.unitGrid.addColumn(unit -> unit.getPosition().getLongitude()).setHeader("Longitude");
        this.unitGrid.addColumn(unit -> unit.getPosition().getAltitude()).setHeader("Altitude");
        this.unitGrid.addColumn(Unit::getStatus).setKey(String.valueOf(UnitFilter.Columns.STATUS)).setHeader("Status").setSortable(true);
        this.unitGrid.addColumn(unit -> unit.isSpeakRequest() ? "Yes" : "No").setHeader("Speak Request");

        this.unitGrid.setEmptyStateText("There are no units");
        this.unitGrid.setSizeFull();
        this.unitGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER);

        new UnitContextMenu(this.unitGrid);

    }

    private User currentUser() {
        return this.authenticationContext.getAuthenticatedUser(User.class).orElse(null);
    }

    private void openDeleteDialog(Unit unit) {
        ConfirmDialog confirmDialog = new ConfirmDialog();
        confirmDialog.setHeader("Delete Unit");
        confirmDialog.setText("Are you sure you want to delete unit '" + unit.getName() + "'?");
        confirmDialog.setCancelable(true);
        confirmDialog.setConfirmText("Delete");
        confirmDialog.addConfirmListener(e -> {
            this.unitRepository.delete(unit);
            this.refresh();
            confirmDialog.close();
            this.remove(confirmDialog);
        });
        add(confirmDialog);
        confirmDialog.open();
    }

    private class UnitContextMenu extends GridContextMenu<Unit> {
        public UnitContextMenu(Grid<Unit> target) {
            super(target);
            this.addItem("Permissions", event -> {
                event.getItem().ifPresent(mapOverlay -> {
                    User user = currentUser();
                    if (!permissionUtil.hasAccess(user, SecurityGroup.UserRoleScopeEnum.ADMIN, SecurityGroup.UserRoleTypeEnum.UNIT)) {
                        Notification.show("You cannot edit overlay permissions");
                        return;
                    }
                    permissionsDialog.open(mapOverlay);
                });
            });
            this.addItem("Edit", event -> event.getItem().ifPresent(unit -> {
                User user = currentUser();
                if (!permissionUtil.hasAccess(user, SecurityGroup.UserRoleScopeEnum.EDIT, SecurityGroup.UserRoleTypeEnum.UNIT)) {
                    Notification.show("You cannot edit units");
                    return;
                }
                editDialog.open(unit);
            }));
            this.addItem("Edit Icon", event -> event.getItem().ifPresent(unit -> {
                User user = currentUser();
                if (!permissionUtil.hasAccess(user, SecurityGroup.UserRoleScopeEnum.EDIT, SecurityGroup.UserRoleTypeEnum.UNIT)) {
                    Notification.show("You cannot edit units");
                    return;
                }
                iconEditDialog.open(unit);
            }));
            this.addItem("Delete", event -> event.getItem().ifPresent(unit -> {
                User user = currentUser();
                if (!permissionUtil.hasAccess(user, SecurityGroup.UserRoleScopeEnum.DELETE, SecurityGroup.UserRoleTypeEnum.UNIT)) {
                    Notification.show("You cannot delete units");
                    return;
                }
                UnitView.this.openDeleteDialog(unit);
            }));
            this.setDynamicContentHandler(Objects::nonNull);
        }
    }
}
