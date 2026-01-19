package dev.nilswitt.webmap.views.components;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import dev.nilswitt.webmap.entities.*;
import dev.nilswitt.webmap.entities.repositories.SecurityGroupPermissionsRepository;
import dev.nilswitt.webmap.entities.repositories.SecurityGroupRepository;
import dev.nilswitt.webmap.entities.repositories.UserPermissionsRepository;
import dev.nilswitt.webmap.entities.repositories.UserRepository;
import lombok.Getter;

import java.util.function.Consumer;

public abstract class AbstractPermissionsDialog extends Dialog {

    @Getter
    private final Grid<UserPermission> userPermissionGrid;
    @Getter
    private final Grid<SecurityGroupPermission> groupPermissionGrid;

    private final UserRepository userRepository;
    private final SecurityGroupRepository securityGroupRepository;

    public AbstractPermissionsDialog(UserPermissionsRepository repository, UserRepository userRepository, SecurityGroupRepository securityGroupRepository, SecurityGroupPermissionsRepository securityGroupPermissionsRepository) {
        this.userRepository = userRepository;
        this.securityGroupRepository = securityGroupRepository;
        this.setHeaderTitle("Permissions");
        this.setWidth("80%");
        HorizontalLayout layout = new HorizontalLayout();
        add(layout);
        userPermissionGrid = new Grid<>();
        userPermissionGrid.addColumn(up -> up.getUser().getUsername()).setHeader("User");
        userPermissionGrid.addColumn(up -> up.getScope().name()).setHeader("Role Scope");
        userPermissionGrid.addComponentColumn(up -> {
            Button deleteButton = new Button("Delete");
            deleteButton.addClickListener(e -> {
                repository.delete(up);
                refresh();
            });
            return deleteButton;
        });

        Button userAddButton = new Button("Add User Permission", e -> {
            AddUserPermissionDialog addUserPermissionDialog = new AddUserPermissionDialog(permission -> {
                repository.save(permission);
                refresh();
            });
            addUserPermissionDialog.open();
        });
        layout.add(new VerticalLayout(userPermissionGrid, userAddButton));
        groupPermissionGrid = new Grid<>();
        groupPermissionGrid.addColumn(up -> up.getSecurityGroup().getName()).setHeader("Group");
        groupPermissionGrid.addColumn(up -> up.getScope().name()).setHeader("Role Scope");
        groupPermissionGrid.addComponentColumn(up -> {
            Button deleteButton = new Button("Delete");
            deleteButton.addClickListener(e -> {
                securityGroupPermissionsRepository.delete(up);
                refresh();
            });
            return deleteButton;
        });

        Button groupAddButton = new Button("Add Group Permission", e -> {
            AddGroupPermissionDialog addGroupPermissionDialog = new AddGroupPermissionDialog(permission -> {
                securityGroupPermissionsRepository.save(permission);
                refresh();
            });
            addGroupPermissionDialog.open();
        });
        layout.add(new VerticalLayout(groupPermissionGrid, groupAddButton));
    }

    abstract void refresh();

    abstract void injectEntity(UserPermission userPermission);

    abstract void injectEntity(SecurityGroupPermission securityGroupPermission);

    private class AddUserPermissionDialog extends Dialog {

        private ComboBox<User> userComboBox;
        private ComboBox<SecurityGroup.UserRoleScopeEnum> scopeComboBox;

        public AddUserPermissionDialog(Consumer<UserPermission> callback) {
            this.setHeaderTitle("Add User Permission");
            this.setWidth("40%");
            userComboBox = new ComboBox<>("User");
            userComboBox.setItems(userRepository.findAll());
            userComboBox.setItemLabelGenerator(User::getUsername);
            scopeComboBox = new ComboBox<>("Role Scope");
            scopeComboBox.setItems(SecurityGroup.UserRoleScopeEnum.values());
            scopeComboBox.setValue(SecurityGroup.UserRoleScopeEnum.VIEW);
            FormLayout form = new FormLayout();

            form.addFormRow(userComboBox, scopeComboBox);

            form.add(new Button("Add", e -> {
                if (userComboBox.isEmpty() || scopeComboBox.isEmpty() || scopeComboBox.getValue() == null) {
                    return;
                }
                UserPermission permission = new UserPermission();
                permission.setUser(userComboBox.getValue());
                permission.setScope(scopeComboBox.getValue());
                injectEntity(permission);
                callback.accept(permission);
                this.close();

            }));
            this.add(new VerticalLayout(form));
        }
    }

    private class AddGroupPermissionDialog extends Dialog {

        private ComboBox<SecurityGroup> securityGroupComboBox;
        private ComboBox<SecurityGroup.UserRoleScopeEnum> scopeComboBox;

        public AddGroupPermissionDialog(Consumer<SecurityGroupPermission> callback) {
            this.setHeaderTitle("Add Group Permission");
            this.setWidth("40%");
            securityGroupComboBox = new ComboBox<>("Group");
            securityGroupComboBox.setItems(securityGroupRepository.findAll());
            securityGroupComboBox.setItemLabelGenerator(SecurityGroup::getName);
            scopeComboBox = new ComboBox<>("Role Scope");
            scopeComboBox.setItems(SecurityGroup.UserRoleScopeEnum.values());
            scopeComboBox.setValue(SecurityGroup.UserRoleScopeEnum.VIEW);
            FormLayout form = new FormLayout();

            form.addFormRow(securityGroupComboBox, scopeComboBox);

            form.add(new Button("Add", e -> {
                if (securityGroupComboBox.isEmpty() || scopeComboBox.isEmpty() || scopeComboBox.getValue() == null) {
                    return;
                }
                SecurityGroupPermission permission = new SecurityGroupPermission();
                permission.setSecurityGroup(securityGroupComboBox.getValue());
                permission.setScope(scopeComboBox.getValue());
                injectEntity(permission);
                callback.accept(permission);
                this.close();
            }));
            this.add(new VerticalLayout(form));
        }
    }
}
