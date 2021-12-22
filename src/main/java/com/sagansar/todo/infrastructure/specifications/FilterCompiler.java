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
                if (Operator.AND.equals(operatorType)) {
                    specification = specification.and(new SearchSpecification<>(expression));
                }
                if (Operator.OR.equals(operatorType)) {
                    specification = specification.or(new SearchSpecification<>(expression));
                }
            }
            first = false;
        }
        return specification;
    }

    public SearchCriteria compile(String data) {
        Pattern pattern = Pattern.compile("(\\w+?)([:<>!~#])(\\w+?),");
        Matcher matcher = pattern.matcher(data + ",");
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.setKey(matcher.group(1));
        searchCriteria.setOperation(matcher.group(2));
        searchCriteria.setValue(matcher.group(3));
        return searchCriteria;
    }

    private enum Operator {
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
