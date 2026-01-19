package dev.nilswitt.webmap.security;

import dev.nilswitt.webmap.entities.*;
import dev.nilswitt.webmap.entities.repositories.SecurityGroupPermissionsRepository;
import dev.nilswitt.webmap.entities.repositories.UserPermissionsRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public final class PermissionUtil {

    private final UserPermissionsRepository userPermissionsRepository;
    private final SecurityGroupPermissionsRepository securityGroupPermissionsRepository;

    private PermissionUtil(UserPermissionsRepository userPermissionsRepository, SecurityGroupPermissionsRepository securityGroupPermissionsRepository) {
        this.userPermissionsRepository = userPermissionsRepository;
        this.securityGroupPermissionsRepository = securityGroupPermissionsRepository;
    }

    public boolean hasAccess(User user, SecurityGroup.UserRoleScopeEnum requiredScope, SecurityGroup.UserRoleTypeEnum type) {
        String requiredRole = buildRole(type, requiredScope);
        String typeAdminRole = buildRole(type, SecurityGroup.UserRoleScopeEnum.ADMIN);
        String globalScopeRole = buildRole(SecurityGroup.UserRoleTypeEnum.GLOBAL, requiredScope);
        String globalAdminRole = buildRole(SecurityGroup.UserRoleTypeEnum.GLOBAL, SecurityGroup.UserRoleScopeEnum.ADMIN);

        return user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals(requiredRole)
                        || role.equals(typeAdminRole)
                        || role.equals(globalScopeRole)
                        || role.equals(globalAdminRole));
    }

    public boolean hasAccess(User user, SecurityGroup.UserRoleScopeEnum requiredScope, MapOverlay mapOverlay) {
        if (hasAnyScope(user, SecurityGroup.UserRoleTypeEnum.MAPOVERLAY,
                SecurityGroup.UserRoleScopeEnum.VIEW,
                SecurityGroup.UserRoleScopeEnum.EDIT,
                SecurityGroup.UserRoleScopeEnum.ADMIN)) {
            return true;
        }

        Optional<UserPermission> userPermission = userPermissionsRepository.findByUserAndMapOverlay(user, mapOverlay);
        if (userPermission.isPresent()) {
            if (testScope(requiredScope, userPermission.get().getScope())) {
                return true;
            }
        } else {
            for (SecurityGroup sg : user.getSecurityGroups()) {
                Optional<SecurityGroupPermission> sgPermission =
                        securityGroupPermissionsRepository.findBySecurityGroupAndMapOverlay(sg, mapOverlay);
                if (sgPermission.isPresent()) {
                    if (testScope(requiredScope, sgPermission.get().getScope())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean hasAccess(User user, SecurityGroup.UserRoleScopeEnum requiredScope, Unit unit) {
        if (hasAnyScope(user, SecurityGroup.UserRoleTypeEnum.UNIT,
                SecurityGroup.UserRoleScopeEnum.VIEW,
                SecurityGroup.UserRoleScopeEnum.EDIT,
                SecurityGroup.UserRoleScopeEnum.ADMIN)) {
            return true;
        }

        Optional<UserPermission> userPermission = userPermissionsRepository.findByUserAndUnit(user, unit);
        if (userPermission.isPresent()) {
            if (testScope(requiredScope, userPermission.get().getScope())) {
                return true;
            }
        } else {
            for (SecurityGroup sg : user.getSecurityGroups()) {
                Optional<SecurityGroupPermission> sgPermission =
                        securityGroupPermissionsRepository.findBySecurityGroupAndUnit(sg, unit);
                if (sgPermission.isPresent()) {
                    if (testScope(requiredScope, sgPermission.get().getScope())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean hasAccess(User user, SecurityGroup.UserRoleScopeEnum requiredScope, MapItem mapItem) {
        if (hasAnyScope(user, SecurityGroup.UserRoleTypeEnum.MAPITEM,
                SecurityGroup.UserRoleScopeEnum.VIEW,
                SecurityGroup.UserRoleScopeEnum.EDIT,
                SecurityGroup.UserRoleScopeEnum.ADMIN)) {
            return true;
        }

        Optional<UserPermission> userPermission = userPermissionsRepository.findByUserAndMapItem(user, mapItem);
        if (userPermission.isPresent()) {
            if (testScope(requiredScope, userPermission.get().getScope())) {
                return true;
            }
        } else {
            for (SecurityGroup sg : user.getSecurityGroups()) {
                Optional<SecurityGroupPermission> sgPermission =
                        securityGroupPermissionsRepository.findBySecurityGroupAndMapItem(sg, mapItem);
                if (sgPermission.isPresent()) {
                    if (testScope(requiredScope, sgPermission.get().getScope())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean hasAccess(User user, SecurityGroup.UserRoleScopeEnum requiredScope, MapBaseLayer mapBaseLayer) {
        if (hasAnyScope(user, SecurityGroup.UserRoleTypeEnum.MAPBASELAYER,
                SecurityGroup.UserRoleScopeEnum.VIEW,
                SecurityGroup.UserRoleScopeEnum.EDIT,
                SecurityGroup.UserRoleScopeEnum.ADMIN)) {
            return true;
        }

        Optional<UserPermission> userPermission = userPermissionsRepository.findByUserAndBaseLayer(user, mapBaseLayer);
        if (userPermission.isPresent()) {
            if (testScope(requiredScope, userPermission.get().getScope())) {
                return true;
            }
        } else {
            for (SecurityGroup sg : user.getSecurityGroups()) {
                Optional<SecurityGroupPermission> sgPermission =
                        securityGroupPermissionsRepository.findBySecurityGroupAndBaseLayer(sg, mapBaseLayer);
                if (sgPermission.isPresent()) {
                    if (testScope(requiredScope, sgPermission.get().getScope())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean hasAccess(User user, SecurityGroup.UserRoleScopeEnum requiredScope, User checkUser) {
        if (hasAnyScope(user, SecurityGroup.UserRoleTypeEnum.USER,
                SecurityGroup.UserRoleScopeEnum.VIEW,
                SecurityGroup.UserRoleScopeEnum.EDIT,
                SecurityGroup.UserRoleScopeEnum.ADMIN)) {
            return true;
        }

        Optional<UserPermission> userPermission = userPermissionsRepository.findByUserAndEntityUser(user, checkUser);
        if (userPermission.isPresent()) {
            if (testScope(requiredScope, userPermission.get().getScope())) {
                return true;
            }
        } else {
            for (SecurityGroup sg : user.getSecurityGroups()) {
                Optional<SecurityGroupPermission> sgPermission =
                        securityGroupPermissionsRepository.findBySecurityGroupAndEntityUser(sg, checkUser);
                if (sgPermission.isPresent()) {
                    if (testScope(requiredScope, sgPermission.get().getScope())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean testScope(SecurityGroup.UserRoleScopeEnum requiredScope, SecurityGroup.UserRoleScopeEnum providedScope) {
        return switch (requiredScope) {
            case VIEW -> isView(providedScope);
            case EDIT -> isEdit(providedScope);
            case CREATE -> isCreate(providedScope);
            case DELETE -> isDelete(providedScope);
            case ADMIN -> providedScope == SecurityGroup.UserRoleScopeEnum.ADMIN;
        };
    }

    public static boolean isView(SecurityGroup.UserRoleScopeEnum toTest) {
        return toTest == SecurityGroup.UserRoleScopeEnum.VIEW || toTest == SecurityGroup.UserRoleScopeEnum.EDIT || toTest == SecurityGroup.UserRoleScopeEnum.ADMIN;
    }

    public static boolean isEdit(SecurityGroup.UserRoleScopeEnum toTest) {
        return toTest == SecurityGroup.UserRoleScopeEnum.VIEW || toTest == SecurityGroup.UserRoleScopeEnum.EDIT || toTest == SecurityGroup.UserRoleScopeEnum.ADMIN;
    }

    public static boolean isCreate(SecurityGroup.UserRoleScopeEnum toTest) {
        return toTest == SecurityGroup.UserRoleScopeEnum.CREATE || toTest == SecurityGroup.UserRoleScopeEnum.ADMIN;
    }

    public static boolean isDelete(SecurityGroup.UserRoleScopeEnum toTest) {
        return toTest == SecurityGroup.UserRoleScopeEnum.DELETE || toTest == SecurityGroup.UserRoleScopeEnum.ADMIN;
    }


    public static boolean hasAnyScope(User user, SecurityGroup.UserRoleTypeEnum type,
                                      SecurityGroup.UserRoleScopeEnum... scopes) {
        if (scopes == null || scopes.length == 0) {
            return false;
        }
        return Arrays.stream(scopes).anyMatch(scope -> hasScope(user, type, scope));
    }

    private static boolean hasScope(User user, SecurityGroup.UserRoleTypeEnum type,
                                    SecurityGroup.UserRoleScopeEnum scope) {
        if (user == null || type == null || scope == null) {
            return false;
        }
        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();
        if (authorities == null) {
            return false;
        }
        String requiredRole = buildRole(type, scope);
        String typeAdminRole = buildRole(type, SecurityGroup.UserRoleScopeEnum.ADMIN);
        String globalScopeRole = buildRole(SecurityGroup.UserRoleTypeEnum.GLOBAL, scope);
        String globalAdminRole = buildRole(SecurityGroup.UserRoleTypeEnum.GLOBAL, SecurityGroup.UserRoleScopeEnum.ADMIN);

        return authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals(requiredRole)
                        || role.equals(typeAdminRole)
                        || role.equals(globalScopeRole)
                        || role.equals(globalAdminRole));
    }

    private static String buildRole(SecurityGroup.UserRoleTypeEnum type, SecurityGroup.UserRoleScopeEnum scope) {
        return "ROLE_" + type.name() + "_" + scope.name();
    }

    public List<MapOverlay> getMapOverlaysForUser(User userDetails) {
        ArrayList<MapOverlay> permittedOverlays = new ArrayList<>(this.userPermissionsRepository.findByUserAndMapOverlayNotNull(userDetails).stream().map(UserPermission::getMapOverlay).toList());
        for (SecurityGroup sg : userDetails.getSecurityGroups()) {
            permittedOverlays.addAll(this.securityGroupPermissionsRepository.findBySecurityGroupAndMapOverlayNotNull(sg).stream().map(SecurityGroupPermission::getMapOverlay).toList());
        }
        return permittedOverlays.stream().distinct().toList();
    }

    public List<Unit> getUnitsForUser(User userDetails) {
        ArrayList<Unit> permittedOverlays = new ArrayList<>(this.userPermissionsRepository.findByUserAndUnitNotNull(userDetails).stream().map(UserPermission::getUnit).toList());
        for (SecurityGroup sg : userDetails.getSecurityGroups()) {
            permittedOverlays.addAll(this.securityGroupPermissionsRepository.findBySecurityGroupAndUnitNotNull(sg).stream().map(SecurityGroupPermission::getUnit).toList());
        }
        return permittedOverlays.stream().distinct().toList();
    }

    public List<MapItem> getMapItemsForUser(User userDetails) {
        ArrayList<MapItem> permittedOverlays = new ArrayList<>(this.userPermissionsRepository.findByUserAndMapItemNotNull(userDetails).stream().map(UserPermission::getMapItem).toList());
        for (SecurityGroup sg : userDetails.getSecurityGroups()) {
            permittedOverlays.addAll(this.securityGroupPermissionsRepository.findBySecurityGroupAndMapItemNotNull(sg).stream().map(SecurityGroupPermission::getMapItem).toList());
        }
        return permittedOverlays.stream().distinct().toList();
    }

    public List<MapBaseLayer> getMapBaseLayersForUser(User userDetails) {
        ArrayList<MapBaseLayer> permittedOverlays = new ArrayList<>(this.userPermissionsRepository.findByUserAndBaseLayerNotNull(userDetails).stream().map(UserPermission::getBaseLayer).toList());
        for (SecurityGroup sg : userDetails.getSecurityGroups()) {
            permittedOverlays.addAll(this.securityGroupPermissionsRepository.findBySecurityGroupAndBaseLayerNotNull(sg).stream().map(SecurityGroupPermission::getBaseLayer).toList());
        }
        return permittedOverlays.stream().distinct().toList();
    }
    public List<User> getUsersForUser(User userDetails) {
        ArrayList<User> permittedOverlays = new ArrayList<>(this.userPermissionsRepository.findByUserAndEntityUserNotNull(userDetails).stream().map(UserPermission::getEntityUser).toList());
        for (SecurityGroup sg : userDetails.getSecurityGroups()) {
            permittedOverlays.addAll(this.securityGroupPermissionsRepository.findBySecurityGroupAndEntityUserNotNull(sg).stream().map(SecurityGroupPermission::getEntityUser).toList());
        }
        return permittedOverlays.stream().distinct().toList();
    }
}
