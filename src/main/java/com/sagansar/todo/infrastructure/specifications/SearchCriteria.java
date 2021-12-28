package com.sagansar.todo.infrastructure.specifications;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SearchCriteria {
    private String key;
    private String operation;
    private Object value;

    public SearchCriteria(String key, String operation, Object value) {
        this.key = key;
        this.operation = operation;
        if ("true".equals(value)) {
            this.value = Boolean.TRUE;
        } else if ("false".equals(value)) {
            this.value = Boolean.FALSE;
        } else {
            this.value = value;
        }
    }

    public void setValue(Object value) {
        if ("true".equals(value)) {
            this.value = Boolean.TRUE;
        } else if ("false".equals(value)) {
            this.value = Boolean.FALSE;
        } else {
            this.value = value;
        }
    }
}