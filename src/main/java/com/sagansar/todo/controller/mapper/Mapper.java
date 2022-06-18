package com.sagansar.todo.controller.mapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Set;

public class Mapper {

    private static final Logger logger = LoggerFactory.getLogger(Mapper.class);

    public static Object map(Object source, Object result) {
        return map(source, result, Collections.emptySet());
    }

    public static Object map(Object source, Object result, Set<String> ignore) {
        var sourceClass = source.getClass();
        var resultFields = result.getClass().getDeclaredFields();
        for (Field resultField : resultFields) {
            if (resultField.getType().getName().contains(".todo")) {
                continue;
            }
            String fieldName = resultField.getName();
            if (ignore.contains(fieldName)) {
                continue;
            }
            try {
                var sourceField = sourceClass.getDeclaredField(fieldName);
                resultField.setAccessible(true);
                sourceField.setAccessible(true);
                resultField.set(result, sourceField.get(source));
                resultField.setAccessible(false);
                sourceField.setAccessible(false);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                logger.error(e.getMessage());
            }
        }
        return result;
    }
}
