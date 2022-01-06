package com.sagansar.todo.model.work;

import java.time.LocalDateTime;
import java.time.ZoneId;

public interface TaskTemplate {

    LocalDateTime getDeadline();

    String getHeader();

    String getDescription();

    String getStack();
}
