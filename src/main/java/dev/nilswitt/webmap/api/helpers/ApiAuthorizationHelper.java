package dev.nilswitt.webmap.api.helpers;

import dev.nilswitt.webmap.api.exceptions.ForbiddenException;
import dev.nilswitt.webmap.entities.SecurityGroup;
import dev.nilswitt.webmap.entities.User;
import dev.nilswitt.webmap.security.PermissionUtil;

public final class ApiAuthorizationHelper {

    private ApiAuthorizationHelper() {
    }

    public static void requireScope(User user, SecurityGroup.UserRoleTypeEnum type, SecurityGroup.UserRoleScopeEnum scope, String message) {
        if (!AuthRestHelper.validateUser(user) || !PermissionUtil.hasScope(user, type, scope)) {
            throw new ForbiddenException(message);
        }
    }

    public static void requireAnyScope(User user, SecurityGroup.UserRoleTypeEnum type, String message, SecurityGroup.UserRoleScopeEnum... scopes) {
        if (!AuthRestHelper.validateUser(user) || !PermissionUtil.hasAnyScope(user, type, scopes)) {
            throw new ForbiddenException(message);
        }
    }
}
