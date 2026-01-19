package dev.nilswitt.webmap.views.components;

import dev.nilswitt.webmap.entities.MapBaseLayer;
import dev.nilswitt.webmap.entities.SecurityGroupPermission;
import dev.nilswitt.webmap.entities.UserPermission;
import dev.nilswitt.webmap.entities.repositories.SecurityGroupPermissionsRepository;
import dev.nilswitt.webmap.entities.repositories.SecurityGroupRepository;
import dev.nilswitt.webmap.entities.repositories.UserPermissionsRepository;
import dev.nilswitt.webmap.entities.repositories.UserRepository;

public class MapBaseLayerPermissionsDialog extends AbstractPermissionsDialog {

    private MapBaseLayer entity;
    private final UserPermissionsRepository userPermissionsRepository;
    private final SecurityGroupPermissionsRepository securityGroupPermissionsRepository;

    public MapBaseLayerPermissionsDialog(UserPermissionsRepository repository, UserRepository userRepository, SecurityGroupRepository securityGroupRepository, SecurityGroupPermissionsRepository securityGroupPermissionsRepository) {
        super(repository, userRepository, securityGroupRepository, securityGroupPermissionsRepository);
        this.userPermissionsRepository = repository;
        this.securityGroupPermissionsRepository = securityGroupPermissionsRepository;
        refresh();
    }

    void refresh() {
        super.getUserPermissionGrid().setItems(userPermissionsRepository.findByBaseLayer(entity));
        super.getGroupPermissionGrid().setItems(securityGroupPermissionsRepository.findByBaseLayer(entity));
    }

    @Override
    void injectEntity(UserPermission userPermission) {
        userPermission.setBaseLayer(entity);
    }

    @Override
    void injectEntity(SecurityGroupPermission securityGroupPermission) {
        securityGroupPermission.setBaseLayer(entity);
    }


    public void open(MapBaseLayer entity) {
        this.entity = entity;
        super.open();
        refresh();
    }
}
