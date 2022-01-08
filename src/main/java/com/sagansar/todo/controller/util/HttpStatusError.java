package com.sagansar.todo.controller.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum HttpStatusError implements ErrorView {
    NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "404 Page Not Found",
            "404: СТРАНИЦА НЕ НАЙДЕНА",
            "Такой страницы не существует, но вы можете вернуться на домашнюю страницу"
    ),
    FORBIDDEN(
            HttpStatus.FORBIDDEN,
            "403 Forbidden",
            "403: ДОСТУП ЗАПРЕЩЕН",
            "У вас нет доступа к данной странице. Вы можете обратиться к администратору или вернуться на домашнюю страницу"
    ),
    GATEWAY_TIMEOUT(
            HttpStatus.GATEWAY_TIMEOUT,
            "504 Gateway Time Out",
            "504: ПРЕВЫШЕНО ВРЕМЯ ОЖИДАНИЯ",
            "Вы видите эту страницу, потому что сервер не отвечал слишком долго. Попробуйте обновить страницу нажатием F5. Если вы видите эту страницу повторно, обратитесь к администратору"
    ),
    INTERNAL(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Ошибка!",
            "Что-то пошло не так...",
            "Попробуйте обновить страницу нажатием F5. Если вы видите эту страницу повторно, обратитесь к администратору"
    );

    private final HttpStatus status;
    private final String title;
    private final String header;
    private final String message;

    public String getSelector() {
        return "status" + status.value();
    }

    public static HttpStatusError error(int status) {
        for (HttpStatusError error : values()) {
            if (error.getStatus().value() == status) {
                return error;
            }
        }
        return INTERNAL;
    }
}
