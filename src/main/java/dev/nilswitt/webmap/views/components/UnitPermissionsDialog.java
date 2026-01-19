package dev.nilswitt.webmap.views.components;

import dev.nilswitt.webmap.entities.SecurityGroupPermission;
import dev.nilswitt.webmap.entities.Unit;
import dev.nilswitt.webmap.entities.UserPermission;
import dev.nilswitt.webmap.entities.repositories.SecurityGroupPermissionsRepository;
import dev.nilswitt.webmap.entities.repositories.SecurityGroupRepository;
import dev.nilswitt.webmap.entities.repositories.UserPermissionsRepository;
import dev.nilswitt.webmap.entities.repositories.UserRepository;

public class UnitPermissionsDialog extends AbstractPermissionsDialog {

    private Unit entity;
    private final UserPermissionsRepository userPermissionsRepository;
    private final SecurityGroupPermissionsRepository securityGroupPermissionsRepository;

    public UnitPermissionsDialog(UserPermissionsRepository repository, UserRepository userRepository, SecurityGroupRepository securityGroupRepository, SecurityGroupPermissionsRepository securityGroupPermissionsRepository) {
        super(repository, userRepository, securityGroupRepository, securityGroupPermissionsRepository);
        this.userPermissionsRepository = repository;
        this.securityGroupPermissionsRepository = securityGroupPermissionsRepository;
        refresh();
    }

    void refresh() {
        super.getUserPermissionGrid().setItems(userPermissionsRepository.findByUnit(entity));
        super.getGroupPermissionGrid().setItems(securityGroupPermissionsRepository.findByUnit(entity));
    }

    @Override
    void injectEntity(UserPermission userPermission) {
        userPermission.setUnit(entity);
    }

    @Override
    void injectEntity(SecurityGroupPermission securityGroupPermission) {
        securityGroupPermission.setUnit(entity);
    }


    public void open(Unit entity) {
        this.entity = entity;
        super.open();
        refresh();
    }
}
