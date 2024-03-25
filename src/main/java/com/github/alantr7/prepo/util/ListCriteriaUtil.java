package com.github.alantr7.prepo.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListCriteriaUtil {
    
    public static Map<String, String> getValues(String input) {
        String[] split;
        if (input.contains(";"))
            split = input.split(";");
        else split = new String[] { input };

        Map<String, String> values = new HashMap<>();
        for (var item : split) {
            if (!item.contains("="))
                return null;
            
            String regex = "[a-zA-Z0-9_]+";
            String[] pair = item.split("=");

            if (pair.length != 2)
                return null;

            if (!pair[0].matches(regex) || !pair[1].matches(regex))
                return null;

            values.put(pair[0], pair[1]);
        }

        return values;
    }

    public static boolean isValid(String input) {
        return getValues(input) != null;
    }

}
