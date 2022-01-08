package com.sagansar.todo.controller.util;

import lombok.Getter;

@Getter
public enum AccountError implements ErrorView {

    PROFILE_DELETED("Профиль удален", "deleted", "Профиль удален"),
    USER_BLOCKED("Аккаунт заблокирован", "blocked", "Аккаунт заблокирован");

    private final String title;

    private final String selector;

    private final String header;

    AccountError(String title, String selector, String header) {
        this.title = title;
        this.selector = selector;
        this.header = header;
    }
}
