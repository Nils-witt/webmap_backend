package dev.nilswitt.webmap.entities.repositories;

import dev.nilswitt.webmap.entities.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SecurityGroupPermissionsRepository extends JpaRepository<SecurityGroupPermission, UUID> {
    List<SecurityGroupPermission> findByMapOverlay(MapOverlay mapOverlay);

    List<SecurityGroupPermission> findByMapItem(MapItem mapItem);

    List<SecurityGroupPermission> findByBaseLayer(MapBaseLayer mapBaseLayer);

    List<SecurityGroupPermission> findByUnit(Unit unit);

    List<SecurityGroupPermission> findByEntityUser(User entityUser);

    List<SecurityGroupPermission> findBySecurityGroupAndMapOverlayNotNull(SecurityGroup securityGroup);

    List<SecurityGroupPermission> findBySecurityGroupAndMapItemNotNull(SecurityGroup securityGroup);

    List<SecurityGroupPermission> findBySecurityGroupAndBaseLayerNotNull(SecurityGroup securityGroup);

    List<SecurityGroupPermission> findBySecurityGroupAndUnitNotNull(SecurityGroup securityGroup);

    List<SecurityGroupPermission> findBySecurityGroupAndEntityUserNotNull(SecurityGroup securityGroup);

    Optional<SecurityGroupPermission> findBySecurityGroupAndMapItem(SecurityGroup securityGroup, MapItem mapItem);

    Optional<SecurityGroupPermission> findBySecurityGroupAndMapOverlay(SecurityGroup securityGroup, MapOverlay mapOverlay);

    Optional<SecurityGroupPermission> findBySecurityGroupAndBaseLayer(SecurityGroup securityGroup, MapBaseLayer mapBaseLayer);

    Optional<SecurityGroupPermission> findBySecurityGroupAndUnit(SecurityGroup securityGroup, Unit unit);

    Optional<SecurityGroupPermission> findBySecurityGroupAndEntityUser(SecurityGroup securityGroup, User entityUser);

}