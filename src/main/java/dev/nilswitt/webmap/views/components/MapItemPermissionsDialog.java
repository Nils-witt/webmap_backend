package dev.nilswitt.webmap.views.components;

import dev.nilswitt.webmap.entities.MapItem;
import dev.nilswitt.webmap.entities.SecurityGroupPermission;
import dev.nilswitt.webmap.entities.UserPermission;
import dev.nilswitt.webmap.entities.repositories.SecurityGroupPermissionsRepository;
import dev.nilswitt.webmap.entities.repositories.SecurityGroupRepository;
import dev.nilswitt.webmap.entities.repositories.UserPermissionsRepository;
import dev.nilswitt.webmap.entities.repositories.UserRepository;

public class MapItemPermissionsDialog extends AbstractPermissionsDialog {

    private MapItem entity;
    private final UserPermissionsRepository userPermissionsRepository;
    private final SecurityGroupPermissionsRepository securityGroupPermissionsRepository;

    public MapItemPermissionsDialog(UserPermissionsRepository repository, UserRepository userRepository, SecurityGroupRepository securityGroupRepository, SecurityGroupPermissionsRepository securityGroupPermissionsRepository) {
        super(repository, userRepository, securityGroupRepository, securityGroupPermissionsRepository);
        this.userPermissionsRepository = repository;
        this.securityGroupPermissionsRepository = securityGroupPermissionsRepository;
        refresh();
    }

    void refresh() {
        super.getUserPermissionGrid().setItems(userPermissionsRepository.findByMapItem(entity));
        super.getGroupPermissionGrid().setItems(securityGroupPermissionsRepository.findByMapItem(entity));
    }

    @Override
    void injectEntity(UserPermission userPermission) {
        userPermission.setMapItem(entity);
    }

    @Override
    void injectEntity(SecurityGroupPermission securityGroupPermission) {
        securityGroupPermission.setMapItem(entity);
    }


    public void open(MapItem entity) {
        this.entity = entity;
        super.open();
        refresh();
    }
}
