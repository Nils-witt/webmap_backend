package dev.nilswitt.webmap.api.helpers;

import dev.nilswitt.webmap.entities.User;

public class AuthRestHelper {

    public static boolean validateUser(User user) {
        if (user != null) {
            return true;
        } else {
            return false;
        }
    }
}
