package com.sagansar.todo.model.work.taskmeta;

import com.sagansar.todo.model.work.TodoStatus;
import lombok.Getter;

import java.util.Set;

@Getter
public enum TaskAccess {
    REMOVE_WORKERS(TodoStatus.Status.DRAFT, TodoStatus.Status.TODO, TodoStatus.Status.DISCUSSION, TodoStatus.Status.GO),
    INVITE_WORKERS(TodoStatus.Status.TODO, TodoStatus.Status.DISCUSSION),
    CANCEL_TASK(TodoStatus.Status.TODO, TodoStatus.Status.DISCUSSION, TodoStatus.Status.GO, TodoStatus.Status.DONE),
    ARCHIVE_TASK(TodoStatus.Status.GO, TodoStatus.Status.DONE, TodoStatus.Status.APPROVED),
    DELETE_TASK(TodoStatus.Status.DRAFT, TodoStatus.Status.CANCELED, TodoStatus.Status.ARCHIVE);

    private final Set<TodoStatus.Status> allowedStatuses;

    TaskAccess(TodoStatus.Status... statuses) {
        this.allowedStatuses = Set.of(statuses);
    }

    public boolean hasAccess(TodoStatus.Status status) {
        return this.allowedStatuses.contains(status);
    }
}
