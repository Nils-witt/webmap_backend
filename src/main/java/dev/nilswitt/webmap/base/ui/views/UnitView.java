package dev.nilswitt.webmap.base.ui.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.popover.Popover;
import com.vaadin.flow.component.popover.PopoverPosition;
import com.vaadin.flow.dom.Style;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.streams.DownloadHandler;
import com.vaadin.flow.server.streams.DownloadResponse;
import com.vaadin.flow.spring.security.AuthenticationContext;
import dev.nilswitt.webmap.base.ui.ViewToolbar;
import dev.nilswitt.webmap.base.ui.views.components.UnitAssignmentDialog;
import dev.nilswitt.webmap.base.ui.views.components.UnitEditDialog;
import dev.nilswitt.webmap.base.ui.views.components.UnitIconEditDialog;
import dev.nilswitt.webmap.base.ui.views.components.UnitPermissionsDialog;
import dev.nilswitt.webmap.base.ui.views.filters.UnitFilter;
import dev.nilswitt.webmap.entities.SecurityGroup;
import dev.nilswitt.webmap.entities.Unit;
import dev.nilswitt.webmap.entities.User;
import dev.nilswitt.webmap.entities.repositories.*;
import dev.nilswitt.webmap.security.PermissionVerifier;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.data.domain.Pageable;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
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
    private final UnitAssignmentDialog unitAssignmentDialog;

    private final PermissionVerifier permissionVerifier;

    private final UnitRepository unitRepository;
    private final UnitFilter unitFilter;
    private final AuthenticationContext authenticationContext;

    public UnitView(UnitRepository unitRepository, AuthenticationContext authenticationContext, SecurityGroupRepository securityGroupRepository,
                    SecurityGroupPermissionsRepository securityGroupPermissionsRepository,
                    UserPermissionsRepository userPermissionsRepository,
                    UserRepository userRepository, PermissionVerifier permissionVerifier, UserUnitAssignmentRepository userUnitAssignmentRepository) {
        this.permissionVerifier = permissionVerifier;
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
        this.unitAssignmentDialog = new UnitAssignmentDialog(userUnitAssignmentRepository, userRepository   );
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


        this.add(new ViewToolbar("Unit List", ViewToolbar.group(setUpMultiSelect(),this.createBtn)));
        this.add(unitGrid, editDialog);
    }

    private void configureCreateButton() {
        this.createBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        this.createBtn.addClickListener(event -> {
            User user = currentUser();
            if (!PermissionVerifier.hasAnyScope(user, SecurityGroup.UserRoleTypeEnum.UNIT,
                    SecurityGroup.UserRoleScopeEnum.CREATE)) {
                Notification.show("You cannot create units");
                return;
            }
            editDialog.open(null);
        });
    }

    private static final DateTimeFormatter CSV_TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

    private Button setUpMultiSelect() {
        this.unitGrid.setSelectionMode(Grid.SelectionMode.MULTI);

        Popover popover = new Popover();
        popover.setWidth("200px");
        popover.setHeight("auto");
        popover.setPosition(PopoverPosition.BOTTOM);

        Button openPopupButton = new Button("Open Popup");
        popover.setTarget(openPopupButton);
        openPopupButton.addClickListener(e -> {
            if (unitGrid.getSelectedItems().isEmpty()) {
                Notification.show("Please select at least one unit.");
                return;
            }
            popover.open();
        });

        DownloadHandler downloadHandler = DownloadHandler.fromInputStream(event -> {
            StringBuilder sb = new StringBuilder();
            sb.append("ID,Name,Status,Latitude,Longitude,Altitude,Accuracy,Timestamp\n");
            for (Unit unit : unitGrid.getSelectedItems()) {
                sb.append(unit.getId()).append(",");
                sb.append(escapeCsvField(unit.getName())).append(",");
                sb.append(unit.getPosition().getLatitude()).append(",");
                sb.append(unit.getPosition().getLongitude()).append(",");
                sb.append(unit.getPosition().getAltitude()).append(",");
                sb.append(unit.getPosition().getAccuracy()).append(",");
                sb.append(unit.getPosition().getTimestamp() != null
                        ? CSV_TIMESTAMP_FORMAT.format(unit.getPosition().getTimestamp())
                        : "").append("\n");
            }
            byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
            return new DownloadResponse(new ByteArrayInputStream(bytes), "units.csv", "text/csv", bytes.length);
        });

        Anchor downloadLink = new Anchor(downloadHandler, "");
        downloadLink.getElement().setAttribute("download", true);
        Button downloadButton = new Button("Download CSV");
        downloadLink.add(downloadButton);
        downloadButton.addClickListener(e -> popover.close());

        popover.add(downloadLink);

        return openPopupButton;
    }

    private static String escapeCsvField(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    public List<Unit> list(Pageable pageable) {
        // this.unitFilter.getExample(),
        return this.unitRepository.findAll(this.unitFilter.getExample(), pageable).stream().toList();
    }

    private void refresh() {
        this.unitGrid.getDataProvider().refreshAll();
    }

    private void configureGrid() {
        this.unitGrid.setItemsPageable(this::list);
        this.unitGrid.addColumn(Unit::getName).setKey("name").setHeader("Name").setSortable(true);
        this.unitGrid.addColumn(unit -> unit.getPosition().getTimestamp()).setHeader("PosTime");
        this.unitGrid.addColumn(unit -> unit.getPosition().getLatitude()).setHeader("Latitude");
        this.unitGrid.addColumn(unit -> unit.getPosition().getLongitude()).setHeader("Longitude");
        this.unitGrid.addColumn(unit -> unit.getPosition().getAltitude()).setHeader("Altitude");

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
            this.addItem("Permissions", event -> event.getItem().ifPresent(mapOverlay -> {
                User user = currentUser();
                if (!permissionVerifier.hasAccess(user, SecurityGroup.UserRoleScopeEnum.ADMIN, SecurityGroup.UserRoleTypeEnum.UNIT)) {
                    Notification.show("You cannot edit overlay permissions");
                    return;
                }
                permissionsDialog.open(mapOverlay);
            }));
            this.addItem("Edit", event -> event.getItem().ifPresent(unit -> {
                User user = currentUser();
                if (!permissionVerifier.hasAccess(user, SecurityGroup.UserRoleScopeEnum.EDIT, SecurityGroup.UserRoleTypeEnum.UNIT)) {
                    Notification.show("You cannot edit units");
                    return;
                }
                editDialog.open(unit);
            }));
            this.addItem("Edit Icon", event -> event.getItem().ifPresent(unit -> {
                User user = currentUser();
                if (!permissionVerifier.hasAccess(user, SecurityGroup.UserRoleScopeEnum.EDIT, SecurityGroup.UserRoleTypeEnum.UNIT)) {
                    Notification.show("You cannot edit units");
                    return;
                }
                iconEditDialog.open(unit);
            }));
            this.addItem("Delete", event -> event.getItem().ifPresent(unit -> {
                User user = currentUser();
                if (!permissionVerifier.hasAccess(user, SecurityGroup.UserRoleScopeEnum.DELETE, SecurityGroup.UserRoleTypeEnum.UNIT)) {
                    Notification.show("You cannot delete units");
                    return;
                }
                UnitView.this.openDeleteDialog(unit);
            }));
            this.addItem("Assign users", event -> event.getItem().ifPresent(unit -> {
                User user = currentUser();
                if (!permissionVerifier.hasAccess(user, SecurityGroup.UserRoleScopeEnum.EDIT, SecurityGroup.UserRoleTypeEnum.UNIT)) {
                    Notification.show("You cannot edit units");
                    return;
                }
                unitAssignmentDialog.open(unit);
            }));

            this.setDynamicContentHandler(Objects::nonNull);
        }
    }
}
