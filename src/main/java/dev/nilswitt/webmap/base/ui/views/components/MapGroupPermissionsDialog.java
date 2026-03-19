package dev.nilswitt.webmap.base.ui.views.components;

import dev.nilswitt.webmap.entities.MapGroup;
import dev.nilswitt.webmap.entities.SecurityGroupPermission;
import dev.nilswitt.webmap.entities.UserPermission;
import dev.nilswitt.webmap.entities.repositories.SecurityGroupPermissionsRepository;
import dev.nilswitt.webmap.entities.repositories.SecurityGroupRepository;
import dev.nilswitt.webmap.entities.repositories.UserPermissionsRepository;
import dev.nilswitt.webmap.entities.repositories.UserRepository;

public class MapGroupPermissionsDialog extends AbstractPermissionsDialog {

    private MapGroup entity;
    private final UserPermissionsRepository userPermissionsRepository;
    private final SecurityGroupPermissionsRepository securityGroupPermissionsRepository;

    public MapGroupPermissionsDialog(UserPermissionsRepository repository, UserRepository userRepository, SecurityGroupRepository securityGroupRepository, SecurityGroupPermissionsRepository securityGroupPermissionsRepository) {
        super(repository, userRepository, securityGroupRepository, securityGroupPermissionsRepository);
        this.userPermissionsRepository = repository;
        this.securityGroupPermissionsRepository = securityGroupPermissionsRepository;
        refresh();
    }

    void refresh() {
        super.getUserPermissionGrid().setItems(userPermissionsRepository.findByMapGroup(entity));
        super.getGroupPermissionGrid().setItems(securityGroupPermissionsRepository.findByMapGroup(entity));
    }

    @Override
    void injectEntity(UserPermission userPermission) {
        userPermission.setMapGroup(entity);
    }

    @Override
    void injectEntity(SecurityGroupPermission securityGroupPermission) {
        securityGroupPermission.setMapGroup(entity);
    }


    public void open(MapGroup entity) {
        this.entity = entity;
        super.open();
        refresh();
    }
}
