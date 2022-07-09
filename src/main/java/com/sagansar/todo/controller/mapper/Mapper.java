package com.sagansar.todo.controller.mapper;

import com.sagansar.todo.controller.dto.PersonNameDto;
import com.sagansar.todo.model.general.User;
import com.sagansar.todo.model.worker.Worker;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Set;
import java.util.function.Function;

public class Mapper {

    private static final Logger logger = LoggerFactory.getLogger(Mapper.class);

    public static Object map(Object source, Object result) {
        return map(source, result, Collections.emptySet());
    }

    public static Object map(Object source, Object result, Set<String> ignore) {
        var sourceClass = source.getClass();
        var resultFields = result.getClass().getDeclaredFields();
        for (Field resultField : resultFields) {
            String fieldName = resultField.getName();
            if (ignore.contains(fieldName)) {
                continue;
            }
            try {
                var sourceField = sourceClass.getDeclaredField(fieldName);
                resultField.setAccessible(true);
                sourceField.setAccessible(true);
                var sourceValue = sourceField.get(source);
                if (sourceValue == null) {
                    continue;
                }
                if (resultField.getType().getName().contains(".todo")) {
                    var constructor = resultField.getType().getDeclaredConstructor();
                    constructor.setAccessible(true);
                    var newObj = constructor.newInstance();
                    sourceValue = mapSystemObject(sourceValue, newObj);
                    constructor.setAccessible(false);
                }
                resultField.set(result, sourceValue);
                resultField.setAccessible(false);
                sourceField.setAccessible(false);
            } catch (NoSuchFieldException | IllegalAccessException | NoSuchMethodException | InvocationTargetException | InstantiationException e) {
                logger.error(e.getMessage());
            }
        }
        return result;
    }

    public static Object mapSystemObject(Object source, Object result) {
        Conversion conversion = Conversion.fromClasses(source.getClass(), result.getClass());
        if (conversion == null) {
            map(source, result);
            return result;
        }
        return conversion.mapper.apply(source);
    }

    @Getter
    @AllArgsConstructor
    private enum Conversion {
        WORKER_TO_PERSON(Worker.class, PersonNameDto.class, (Object worker) -> PersonMapper.workerToName((Worker) worker)),
        USER_TO_PERSON(User.class, PersonNameDto.class, (Object user) -> {
            PersonNameDto dto = new PersonNameDto();
            User source = (User) user;
            dto.setId(source.getId());
            dto.setAvatar(source.getAvatar());
            dto.setName(source.getFirstName() + " " + source.getPatronym() + " " + source.getSurname());
            return dto;
        });

        private final Class<?> sourceClass;
        private final Class<?> resultClass;
        private final Function<Object, Object> mapper;

        public static Conversion fromClasses(Class<?> source, Class<?> result) {
            for (Conversion conversion : values()) {
                if (conversion.resultClass.equals(result) && conversion.sourceClass.equals(source)) {
                    return conversion;
                }
            }
            return null;
        }
    }
}
