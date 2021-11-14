package com.sagansar.todo.model.general;

public enum RoleEnum {
    ADMIN,
    MANAGER,
    FREELANCER,
    USER;

    public static RoleEnum fromId(int id) {
        switch (id) {
            case 1:
                return ADMIN;
            case 2:
                return MANAGER;
            case 3:
                return FREELANCER;
            default:
                return USER;
        }
    }
}
