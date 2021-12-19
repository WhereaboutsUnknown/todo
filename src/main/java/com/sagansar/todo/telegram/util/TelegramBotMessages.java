package com.sagansar.todo.telegram.util;

import lombok.Getter;

@Getter
public enum TelegramBotMessages {
    ON_START(1L),
    ON_STOP(2L),
    UNKNOWN_COMMAND(3L),
    CANNOT_RECOGNIZE(4L),
    HELP(5L),
    DEFAULT(9223372036854775806L);

    TelegramBotMessages(Long id) {
        this.id = id;
    }

    private final Long id;

    public static TelegramBotMessages fromId(Long id) {
        for (TelegramBotMessages messageType : values()) {
            if (messageType.id.equals(id)) {
                return messageType;
            }
        }
        return DEFAULT;
    }
}
