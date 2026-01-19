package dev.nilswitt.webmap.views.components;

import dev.nilswitt.webmap.entities.MapOverlay;
import dev.nilswitt.webmap.entities.SecurityGroupPermission;
import dev.nilswitt.webmap.entities.UserPermission;
import dev.nilswitt.webmap.entities.repositories.SecurityGroupPermissionsRepository;
import dev.nilswitt.webmap.entities.repositories.SecurityGroupRepository;
import dev.nilswitt.webmap.entities.repositories.UserPermissionsRepository;
import dev.nilswitt.webmap.entities.repositories.UserRepository;

public class OverlayPermissionsDialog extends AbstractPermissionsDialog {

    private MapOverlay overlay;
    private final UserPermissionsRepository userPermissionsRepository;
    private final SecurityGroupPermissionsRepository securityGroupPermissionsRepository;

    public OverlayPermissionsDialog(UserPermissionsRepository repository, UserRepository userRepository, SecurityGroupRepository securityGroupRepository, SecurityGroupPermissionsRepository securityGroupPermissionsRepository) {
        super(repository, userRepository, securityGroupRepository, securityGroupPermissionsRepository);
        this.userPermissionsRepository = repository;
        this.securityGroupPermissionsRepository = securityGroupPermissionsRepository;
        refresh();
    }

    void refresh() {
        super.getUserPermissionGrid().setItems(userPermissionsRepository.findByMapOverlay(overlay));
        super.getGroupPermissionGrid().setItems(securityGroupPermissionsRepository.findByMapOverlay(overlay));
    }

    @Override
    void injectEntity(UserPermission userPermission) {
        userPermission.setMapOverlay(overlay);
    }

    @Override
    void injectEntity(SecurityGroupPermission securityGroupPermission) {
        securityGroupPermission.setMapOverlay(overlay);
    }


    public void open(MapOverlay mapOverlay) {
        this.overlay = mapOverlay;
        super.open();
        refresh();
    }
}
