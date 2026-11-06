package com.example.notary.web;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class Json {

    private Json() {
    }

    static String stringify(Object value) {
        StringBuilder result = new StringBuilder();
        write(value, result);
        return result.toString();
    }

    static Map<String, Object> parseObject(String json) {
        Object value = new Parser(json).parse();
        if (!(value instanceof Map<?, ?> map)) {
            throw new IllegalArgumentException("Ожидался JSON-объект");
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) map;
        return result;
    }

    private static void write(Object value, StringBuilder result) {
        if (value == null) {
            result.append("null");
        } else if (value instanceof String || value instanceof Character) {
            quote(value.toString(), result);
        } else if (value instanceof Number || value instanceof Boolean) {
            result.append(value);
        } else if (value instanceof Map<?, ?> map) {
            result.append('{');
            boolean first = true;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (!first) {
                    result.append(',');
                }
                first = false;
                quote(entry.getKey().toString(), result);
                result.append(':');
                write(entry.getValue(), result);
            }
            result.append('}');
        } else if (value instanceof Iterable<?> iterable) {
            result.append('[');
            boolean first = true;
            for (Object item : iterable) {
                if (!first) {
                    result.append(',');
                }
                first = false;
                write(item, result);
            }
            result.append(']');
        } else {
            quote(value.toString(), result);
        }
    }

    private static void quote(String value, StringBuilder result) {
        result.append('"');
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"' -> result.append("\\\"");
                case '\\' -> result.append("\\\\");
                case '\b' -> result.append("\\b");
                case '\f' -> result.append("\\f");
                case '\n' -> result.append("\\n");
                case '\r' -> result.append("\\r");
                case '\t' -> result.append("\\t");
                default -> {
                    if (c < 32) {
                        result.append("\\u%04x".formatted((int) c));
                    } else {
                        result.append(c);
                    }
                }
            }
        }
        result.append('"');
    }

    private static final class Parser {
        private final String text;
        private int position;

        private Parser(String text) {
            this.text = text == null ? "" : text;
        }

        private Object parse() {
            Object value = value();
            whitespace();
            if (position != text.length()) {
                throw error("Лишние данные после JSON");
            }
            return value;
        }

        private Object value() {
            whitespace();
            if (position >= text.length()) {
                throw error("Неожиданный конец JSON");
            }
            return switch (text.charAt(position)) {
                case '{' -> object();
                case '[' -> array();
                case '"' -> string();
                case 't' -> literal("true", Boolean.TRUE);
                case 'f' -> literal("false", Boolean.FALSE);
                case 'n' -> literal("null", null);
                default -> number();
            };
        }

        private Map<String, Object> object() {
            Map<String, Object> result = new LinkedHashMap<>();
            position++;
            whitespace();
            if (take('}')) {
                return result;
            }
            while (true) {
                whitespace();
                if (position >= text.length() || text.charAt(position) != '"') {
                    throw error("Ожидалось имя поля");
                }
                String key = string();
                whitespace();
                if (!take(':')) {
                    throw error("Ожидалось двоеточие");
                }
                result.put(key, value());
                whitespace();
                if (take('}')) {
                    return result;
                }
                if (!take(',')) {
                    throw error("Ожидалась запятая");
                }
            }
        }

        private List<Object> array() {
            List<Object> result = new ArrayList<>();
            position++;
            whitespace();
            if (take(']')) {
                return result;
            }
            while (true) {
                result.add(value());
                whitespace();
                if (take(']')) {
                    return result;
                }
                if (!take(',')) {
                    throw error("Ожидалась запятая");
                }
            }
        }

        private String string() {
            position++;
            StringBuilder result = new StringBuilder();
            while (position < text.length()) {
                char c = text.charAt(position++);
                if (c == '"') {
                    return result.toString();
                }
                if (c != '\\') {
                    result.append(c);
                    continue;
                }
                if (position >= text.length()) {
                    throw error("Некорректная escape-последовательность");
                }
                char escaped = text.charAt(position++);
                switch (escaped) {
                    case '"', '\\', '/' -> result.append(escaped);
                    case 'b' -> result.append('\b');
                    case 'f' -> result.append('\f');
                    case 'n' -> result.append('\n');
                    case 'r' -> result.append('\r');
                    case 't' -> result.append('\t');
                    case 'u' -> {
                        if (position + 4 > text.length()) {
                            throw error("Некорректный Unicode-символ");
                        }
                        try {
                            result.append((char) Integer.parseInt(text.substring(position, position + 4), 16));
                        } catch (NumberFormatException e) {
                            throw error("Некорректный Unicode-символ");
                        }
                        position += 4;
                    }
                    default -> throw error("Неизвестная escape-последовательность");
                }
            }
            throw error("Незакрытая строка");
        }

        private BigDecimal number() {
            int start = position;
            while (position < text.length() && "-+0123456789.eE".indexOf(text.charAt(position)) >= 0) {
                position++;
            }
            try {
                return new BigDecimal(text.substring(start, position));
            } catch (NumberFormatException e) {
                throw error("Некорректное число");
            }
        }

        private Object literal(String literal, Object value) {
            if (!text.startsWith(literal, position)) {
                throw error("Некорректное значение");
            }
            position += literal.length();
            return value;
        }

        private boolean take(char expected) {
            if (position < text.length() && text.charAt(position) == expected) {
                position++;
                return true;
            }
            return false;
        }

        private void whitespace() {
            while (position < text.length() && Character.isWhitespace(text.charAt(position))) {
                position++;
            }
        }

        private IllegalArgumentException error(String message) {
            return new IllegalArgumentException(message + " (позиция " + position + ")");
        }
    }
}
