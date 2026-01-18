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
import dev.nilswitt.webmap.entities.SecurityGroup;
import dev.nilswitt.webmap.entities.User;
import dev.nilswitt.webmap.entities.repositories.SecurityGroupRepository;
import dev.nilswitt.webmap.views.components.SecurityGroupEditDialog;
import dev.nilswitt.webmap.views.filters.SecurityGroupFilter;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Objects;
import dev.nilswitt.webmap.security.PermissionUtil;

@Route(value = "ui/groups", layout = MainLayout.class)
@Menu(order = 4, icon = "vaadin:key", title = "Roles")
@RolesAllowed("SECURITYGROUP_VIEW")
public class SecurityGroupView extends VerticalLayout {
    private final Grid<SecurityGroup> securityGroupGrid;
    private final Button createBtn;
    private final SecurityGroupEditDialog editDialog;


    private final SecurityGroupRepository securityGroupRepository;
    private final SecurityGroupFilter securityGroupFilter;
    private final AuthenticationContext authenticationContext;

    public SecurityGroupView(SecurityGroupRepository repository, AuthenticationContext authenticationContext) {
        this.securityGroupRepository = repository;
        this.authenticationContext = authenticationContext;

        this.securityGroupGrid = new Grid<>(SecurityGroup.class, false);

        this.editDialog = new SecurityGroupEditDialog((securityGroup) -> {
            this.securityGroupRepository.save(securityGroup);
            this.securityGroupGrid.getDataProvider().refreshAll();
        });


        this.createBtn = new Button("Create", event -> {
            User user = currentUser();
            if (!PermissionUtil.hasAnyScope(user, SecurityGroup.UserRoleTypeEnum.SECURITYGROUP,
                    SecurityGroup.UserRoleScopeEnum.CREATE)) {
                Notification.show("You cannot create roles");
                return;
            }
            this.editDialog.open(null);
        });
        this.createBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        this.securityGroupGrid.setItemsPageable(this::list);
        this.securityGroupGrid.addColumn(SecurityGroup::getName).setKey(String.valueOf(SecurityGroupFilter.Columns.NAME)).setHeader("Name");

        this.securityGroupGrid.setEmptyStateText("There are no roles");
        this.securityGroupGrid.setSizeFull();
        this.securityGroupGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER);

        new SecurityGroupContextMenu(this.securityGroupGrid);
        this.securityGroupFilter = new SecurityGroupFilter((securityGroupExample -> {
            this.securityGroupGrid.getDataProvider().refreshAll();
        }));
        this.securityGroupFilter.setUp(this.securityGroupGrid);

        this.setSizeFull();
        this.setPadding(false);
        this.setSpacing(false);
        this.getStyle().setOverflow(Style.Overflow.HIDDEN);

        this.add(new ViewToolbar("Security Groups", ViewToolbar.group(this.createBtn)));
        this.add(this.securityGroupGrid);
        this.add(this.editDialog);
    }

    private User currentUser() {
        return this.authenticationContext.getAuthenticatedUser(User.class).orElse(null);
    }

    private List<SecurityGroup> list(Pageable pageable) {
        return this.securityGroupRepository.findAll(this.securityGroupFilter.getExample(), pageable).stream().toList();
    }

    private class SecurityGroupContextMenu extends GridContextMenu<SecurityGroup> {
        public SecurityGroupContextMenu(Grid<SecurityGroup> target) {
            super(target);
            this.addItem("Edit", event -> {
                event.getItem().ifPresent(securityGroup -> {
                    User user = currentUser();
                    if (!PermissionUtil.hasAnyScope(user, SecurityGroup.UserRoleTypeEnum.SECURITYGROUP,
                            SecurityGroup.UserRoleScopeEnum.EDIT)) {
                        Notification.show("You cannot edit roles");
                        return;
                    }
                    editDialog.open(securityGroup);
                });
            });
            this.addItem("Delete", event -> {
                event.getItem().ifPresent(userRole -> {
                    User user = currentUser();
                    if (!PermissionUtil.hasScope(user, SecurityGroup.UserRoleTypeEnum.SECURITYGROUP,
                            SecurityGroup.UserRoleScopeEnum.DELETE)) {
                        Notification.show("You cannot delete roles");
                        return;
                    }
                    ConfirmDialog confirmDialog = new ConfirmDialog();
                    confirmDialog.setHeader("Delete Group");
                    confirmDialog.setText("Are you sure you want to delete role '" + userRole.getName() + "'?");
                    confirmDialog.setCancelable(true);
                    confirmDialog.setConfirmText("Delete");
                    confirmDialog.addConfirmListener(e -> {
                        securityGroupRepository.delete(userRole);
                        securityGroupGrid.getDataProvider().refreshAll();
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
