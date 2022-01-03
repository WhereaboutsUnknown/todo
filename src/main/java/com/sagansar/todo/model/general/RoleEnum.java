package com.sagansar.todo.model.general;

public enum RoleEnum {
    ADMIN,
    MANAGER,
    FREELANCER,
    SUPERVISOR,
    USER;

    public static RoleEnum fromId(int id) {
        switch (id) {
            case 1:
                return ADMIN;
            case 2:
                return MANAGER;
            case 3:
                return FREELANCER;
            case 4:
                return SUPERVISOR;
            default:
                return USER;
        }
    }
}
