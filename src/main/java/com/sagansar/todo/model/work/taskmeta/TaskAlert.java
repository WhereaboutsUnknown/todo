package com.sagansar.todo.model.work.taskmeta;

public interface TaskAlert {

    boolean isError();

    boolean isWarning();

    boolean isInfo();

    String alert();
}
