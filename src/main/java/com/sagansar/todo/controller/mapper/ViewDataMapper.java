package com.sagansar.todo.controller.mapper;

public abstract class ViewDataMapper {
     public static String formatAge(Integer age) {
        if (age == null) {
            return "";
        }
        if (age <= 20) {
            return age + " лет";
        }
        if (age % 10 == 1) {
            return age + " год";
        }
        if (age % 10 < 5) {
            return age + " года";
        }
        return age + " лет";
    }
}
