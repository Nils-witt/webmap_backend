package dev.nilswitt.webmap.security;

import dev.nilswitt.webmap.entities.SecurityGroup;
import dev.nilswitt.webmap.entities.User;
import org.springframework.security.core.GrantedAuthority;

import java.util.Arrays;
import java.util.Collection;

public final class PermissionUtil {

    private PermissionUtil() {
    }

    public static boolean hasAnyScope(User user, SecurityGroup.UserRoleTypeEnum type,
                                      SecurityGroup.UserRoleScopeEnum... scopes) {
        if (scopes == null || scopes.length == 0) {
            return false;
        }
        return Arrays.stream(scopes).anyMatch(scope -> hasScope(user, type, scope));
    }

    public static boolean hasScope(User user, SecurityGroup.UserRoleTypeEnum type,
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
}
