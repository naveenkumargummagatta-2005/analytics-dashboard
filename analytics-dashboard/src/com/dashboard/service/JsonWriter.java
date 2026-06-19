package com.dashboard.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Minimal JSON writer. For a dataset this size, pulling in a full library
 * like Jackson or Gson is overkill, and writing this by hand keeps the
 * project dependency-free (only the JDBC driver jar is needed).
 *
 * It only supports the shapes this project actually produces: maps of
 * String -> Number, and a flat list of String keys, which is all the
 * AggregationService output requires.
 */
public class JsonWriter {

    public static String mapToJson(Map<String, ? extends Number> map) {
        StringBuilder sb = new StringBuilder("{");
        int i = 0;
        for (Map.Entry<String, ? extends Number> entry : map.entrySet()) {
            if (i++ > 0) sb.append(",");
            sb.append("\"").append(escape(entry.getKey())).append("\":");
            sb.append(formatNumber(entry.getValue()));
        }
        sb.append("}");
        return sb.toString();
    }

    public static String keysToJsonArray(List<String> keys) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < keys.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append("\"").append(escape(keys.get(i))).append("\"");
        }
        sb.append("]");
        return sb.toString();
    }

    public static String valuesToJsonArray(List<? extends Number> values) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(formatNumber(values.get(i)));
        }
        sb.append("]");
        return sb.toString();
    }

    private static String formatNumber(Number n) {
        if (n instanceof BigDecimal) {
            return ((BigDecimal) n).setScale(2, java.math.RoundingMode.HALF_UP).toPlainString();
        }
        return String.valueOf(n);
    }

    private static String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
