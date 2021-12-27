package com.sagansar.todo.infrastructure.specifications;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class FilterCompiler<T> {

    public Specification<T> compile(List<SearchCriteria> criteria, String operator) {
        Operator operatorType = Operator.resolve(operator);
        if (criteria == null || criteria.isEmpty() || !StringUtils.hasText(operator)) {
            return null;
        }
        Specification<T> specification = new SearchSpecification<>(criteria.get(0));
        boolean first = true;
        for (SearchCriteria expression : criteria) {
            if (!first) {
                specification = append(specification, expression, operatorType);
            }
            first = false;
        }
        return specification;
    }

    public Specification<T> append(Specification<T> original, SearchCriteria addition, Operator operator) {
        if (Operator.AND.equals(operator)) {
            original = original.and(new SearchSpecification<>(addition));
        } else if (Operator.OR.equals(operator)) {
            original = original.or(new SearchSpecification<>(addition));
        }
        return original;
    }

    public SearchCriteria compile(String data) {
        Pattern pattern = Pattern.compile("([.А-Яа-я\\w]+?)([:<>!~#])([.А-Яа-я\\w]+)");
        Matcher matcher = pattern.matcher(data);
        SearchCriteria searchCriteria = new SearchCriteria();
        if (matcher.find()) {
            searchCriteria.setKey(matcher.group(1));
            searchCriteria.setOperation(matcher.group(2));
            searchCriteria.setValue(matcher.group(3));
        }
        return searchCriteria;
    }

    public enum Operator {
        AND,
        OR;

        public static Operator resolve(String operator) {
            if ("and".equalsIgnoreCase(operator)) {
                return AND;
            }
            if ("or".equalsIgnoreCase(operator)) {
                return OR;
            }
            throw new IllegalArgumentException("Отсутствует оператор сравнения!");
        }
    }
}
