package dev.nilswitt.webmap.base.ui.views;

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
import dev.nilswitt.webmap.base.ui.views.components.MissionGroupEditDialog;
import dev.nilswitt.webmap.entities.MissionGroup;
import dev.nilswitt.webmap.entities.SecurityGroup;
import dev.nilswitt.webmap.entities.Unit;
import dev.nilswitt.webmap.entities.User;
import dev.nilswitt.webmap.entities.repositories.MapGroupRepository;
import dev.nilswitt.webmap.entities.repositories.MissionGroupRepository;
import dev.nilswitt.webmap.entities.repositories.UnitRepository;
import dev.nilswitt.webmap.security.PermissionVerifier;
import jakarta.annotation.security.RolesAllowed;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Route(value = "ui/missiongroups", layout = MainLayout.class)
@Menu(order = 6, icon = "vaadin:flag", title = "Missions")
@RolesAllowed("MISSIONGROUP_VIEW")
public class MissionGroupView extends VerticalLayout {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneOffset.UTC);

    private final Grid<MissionGroup> grid = new Grid<>(MissionGroup.class, false);
    private final MissionGroupEditDialog editDialog;
    private final MissionGroupRepository missionRepository;
    private final AuthenticationContext authenticationContext;
    private final PermissionVerifier permissionVerifier;

    public MissionGroupView(MissionGroupRepository missionRepository,
                            UnitRepository unitRepository,
                            MapGroupRepository mapGroupRepository,
                            AuthenticationContext authenticationContext,
                            PermissionVerifier permissionVerifier) {
        this.missionRepository = missionRepository;
        this.authenticationContext = authenticationContext;
        this.permissionVerifier = permissionVerifier;

        this.editDialog = new MissionGroupEditDialog(mission -> {
            this.missionRepository.save(mission);
            //ToDo: This is a workaround as the relationship between mission and unit is not properly saved by JPA.
            for (Unit unit : mission.getUnits()) {
                unit.setMissionGroup(mission);
                unitRepository.save(unit);
            }
            this.grid.getDataProvider().refreshItem(mission);
        }, unitRepository, mapGroupRepository);

        configureGrid();

        Button createBtn = new Button("Create", e -> {
            User user = currentUser();
            if (!permissionVerifier.hasAccess(user, SecurityGroup.UserRoleScopeEnum.CREATE, SecurityGroup.UserRoleTypeEnum.MISSIONGROUP)) {
                Notification.show("You are not allowed to create missions");
                return;
            }
            editDialog.open(null);
        });
        createBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        setSizeFull();
        setPadding(false);
        setSpacing(false);
        getStyle().setOverflow(Style.Overflow.HIDDEN);

        add(new ViewToolbar("Missions", ViewToolbar.group(createBtn)));
        add(grid, editDialog);
    }

    private void configureGrid() {
        grid.addColumn(MissionGroup::getName).setHeader("Name").setKey("name").setSortable(true);
        grid.addColumn(m -> m.getStartTime() != null ? FORMATTER.format(m.getStartTime()) : "")
                .setHeader("Start").setKey("startTime").setSortable(true);
        grid.addColumn(m -> m.getEndTime() != null ? FORMATTER.format(m.getEndTime()) : "—")
                .setHeader("End").setKey("endTime");

        grid.setItems(missionRepository.findAll());
        grid.setSizeFull();
        grid.setEmptyStateText("No missions found");
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);

        new MissionContextMenu(grid);
    }

    private User currentUser() {
        return authenticationContext.getAuthenticatedUser(User.class).orElse(null);
    }

    private class MissionContextMenu extends GridContextMenu<MissionGroup> {
        MissionContextMenu(Grid<MissionGroup> target) {
            super(target);
            addItem("Edit", e -> e.getItem().ifPresent(mission -> {
                User user = currentUser();
                if (!permissionVerifier.hasAccess(user, SecurityGroup.UserRoleScopeEnum.EDIT, SecurityGroup.UserRoleTypeEnum.MISSIONGROUP)) {
                    Notification.show("You are not allowed to edit missions");
                    return;
                }
                editDialog.open(mission);
            }));
            addItem("Delete", e -> e.getItem().ifPresent(mission -> {
                User user = currentUser();
                if (!permissionVerifier.hasAccess(user, SecurityGroup.UserRoleScopeEnum.DELETE, SecurityGroup.UserRoleTypeEnum.MISSIONGROUP)) {
                    Notification.show("You are not allowed to delete missions");
                    return;
                }
                ConfirmDialog confirmDialog = new ConfirmDialog();
                confirmDialog.setHeader("Delete Mission");
                confirmDialog.setText("Are you sure you want to delete mission '" + mission.getName() + "'?");
                confirmDialog.setCancelable(true);
                confirmDialog.setConfirmText("Delete");
                confirmDialog.addConfirmListener(ev -> {
                    missionRepository.delete(mission);
                    grid.getDataProvider().refreshAll();
                    confirmDialog.close();
                });
                add(confirmDialog);
                confirmDialog.open();
            }));
        }
    }
}


