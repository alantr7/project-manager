package com.github.alantr7.prepo.util;

import java.util.LinkedHashMap;
import java.util.Map;

public class WhereQuery {

    private final Map<String, Object> conditions = new LinkedHashMap<>();

    private static final Object EMPTY = new Object();

    public WhereQuery put(String condition, Object value) {
        conditions.put(condition, value);
        return this;
    }

    public WhereQuery put(String condition) {
        return put(condition, EMPTY);
    }

    public String getQuery() {
        int ord = 1;
        String[] conditionsKeys = new String[conditions.size()];

        for (var entry : conditions.entrySet()) {
            conditionsKeys[ord - 1] = entry.getKey().replace("%n", "?" + ord);
            ord++;
        }

        return String.join(" and ", conditionsKeys);
    }

    public Object[] getParameters() {
        int size = 0;
        var parameters = new Object[conditions.size()];

        for (var entry : conditions.entrySet()) {
            if (entry.getValue() == EMPTY)
                continue;

            parameters[size++] = entry.getValue();
        }

        if (parameters.length == size)
            return parameters;

        var copy = new Object[size];
        System.arraycopy(parameters, 0, copy, 0, size);


        return copy;
    }

    public static WhereQuery with(String condition, Object value) {
        return new WhereQuery().put(condition, value);
    }

}
