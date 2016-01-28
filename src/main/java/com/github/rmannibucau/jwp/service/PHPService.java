package com.github.rmannibucau.jwp.service;

import lombok.AllArgsConstructor;

import javax.enterprise.context.ApplicationScoped;
import java.util.HashMap;
import java.util.Map;

// an optim could be to use from/to indices instead of substring
@ApplicationScoped
public class PHPService {
    private static final It<Object> NULL_IT = new It<>(null, 2);

    public Object deserialize(final String o) {
        return deserialize(o, null);
    }

    public <T> T deserialize(final String o, final Class<T> type) {
        if (o == null || o.isEmpty()) {
            throw new IllegalArgumentException("empty serialized string");
        }
        return parse(o, type).value;
    }

    private <T> It<T> parse(final String o, final Class<T> type) {
        final char c = o.charAt(0);
        switch (c) {
            case 's':
                if (type != null && String.class != type) {
                    throw new IllegalArgumentException("Expected String but got " + type);
                }
                return (It<T>) parseString(o);
            case 'i':
                if (type != null && int.class != type) {
                    throw new IllegalArgumentException("Expected int but got " + type);
                }
                return (It<T>) parseInt(o);
            case 'd':
                if (type != null && double.class != type) {
                    throw new IllegalArgumentException("Expected double but got " + type);
                }
                return (It<T>) parseDouble(o);
            case 'b':
                if (type != null && boolean.class != type) {
                    throw new IllegalArgumentException("Expected boolean but got " + type);
                }
                return (It<T>) parseBool(o);
            case 'N':
                return (It<T>) parseNull(o);
            case 'a':
                if (type != null && Map.class != type) {
                    throw new IllegalArgumentException("Expected Map but got " + type);
                }
                return (It<T>) parseArray(o);
            case 'O':
                throw new IllegalStateException("Object support not yet implemented");
            default:
                throw new IllegalArgumentException("unexpected '" + c + "' in '" + o + "'");
        }
    }

    // a:size:{key definition;value definition;(repeated per element)}
    private It<Map> parseArray(final String o) {
        final int sizeStart = 1 + o.indexOf(':');
        final int sizeEnd = o.indexOf(':', 1 + sizeStart);
        final int length = Integer.parseInt(o.substring(sizeStart, sizeEnd));
        final Map<Object, Object> out = new HashMap<>();
        int parseStart = sizeEnd + 2;
        for (int i = 0; i < length; i++) {
            final It<?> key = parse(o.substring(parseStart), null);
            parseStart += key.end;
            final It<?> value = parse(o.substring(parseStart), null);
            out.put(key.value, value.value);
            parseStart += value.end;
        }
        return new It<>(out, 1 + parseStart);
    }

    // N;
    private It<Object> parseNull(final String o) {
        return NULL_IT;
    }

    // b:{1 or 0};
    private It<Boolean> parseBool(final String o) {
        final int endIndex = o.indexOf(';');
        return new It<>("1".equals(o.substring(1 + o.indexOf(':'), endIndex)), endIndex + 1);
    }

    // d:{value};
    private It<Double> parseDouble(final String o) {
        final int endIndex = o.indexOf(';');
        return new It<>(Double.parseDouble(o.substring(1 + o.indexOf(':'), endIndex)), endIndex + 1);
    }

    // b:{value};
    private It<Integer> parseInt(final String o) {
        final int endIndex = o.indexOf(';');
        return new It<>(Integer.parseInt(o.substring(1 + o.indexOf(':'), endIndex)), endIndex + 1);
    }

    // s:{length}:"{value}";
    private It<String> parseString(final String o) {
        final int sizeStart = 1 + o.indexOf(':');
        final int sizeEnd = o.indexOf(':', 1 + sizeStart);
        final int length = Integer.parseInt(o.substring(sizeStart, sizeEnd));
        final int endIndex = sizeEnd + 1 + length + 1;
        return new It<>(o.substring(2 + o.indexOf(':', 1 + o.indexOf(':')), endIndex), endIndex + 2);
    }

    @AllArgsConstructor
    private static class It<T> {
        private final T value;
        private final int end;
    }
}
