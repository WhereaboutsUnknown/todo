package com.sagansar.todo.model.general;

import lombok.Getter;

@Getter
public enum FilePurpose {
    USER_AVATAR(1L, Extension.Type.IMAGE, "user.png"),
    TASK_VIDEO(2L, Extension.Type.VIDEO, ""),
    UNKNOWN(0, Extension.Type.FILE, "");

    private final Long id;
    private final Extension.Type type;
    private final String defaultFile;

    FilePurpose(long id, Extension.Type type, String defaultFile) {
        this.id = id;
        this.type = type;
        this.defaultFile = defaultFile;
    }

    public static FilePurpose fromId(long id) {
        for (FilePurpose purpose : values()) {
            if (purpose.id == id) {
                return purpose;
            }
        }
        return UNKNOWN;
    }
}
