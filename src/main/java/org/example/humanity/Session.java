package org.example.humanity;

import org.example.humanity.models.UsersModel;

/**
 * Текущий залогиненный пользователь. Живёт всё время работы приложения.
 */
public final class Session {

    private static UsersModel currentUser;

    private Session() {
    }

    public static UsersModel getCurrentUser() {
        return currentUser;
    }

    public static void login(UsersModel user) {
        currentUser = user;
    }

    public static void logout() {
        currentUser = null;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    /** true только для роли admin. Всё остальное (удаление, правка) доступно только админу. */
    public static boolean isAdmin() {
        return currentUser != null && "admin".equals(currentUser.getRole());
    }
}
