package com.github.rmannibucau.jwp.deltaspike;

import org.apache.deltaspike.core.impl.config.MapConfigSource;
import org.yaml.snakeyaml.Yaml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class JwpYamlConfigSource extends MapConfigSource {
    public JwpYamlConfigSource() {
        super(
            Stream.of("jwp.base", "openejb.base")
                .map(System::getProperty)
                .filter(s -> s != null)
                .map(File::new)
                .filter(File::isDirectory)
                .flatMap(f -> Stream.of(new File(f, "conf/jwp.yml"), new File(f, "etc/jwp.yml")))
                .filter(File::isFile)
                .findFirst()
                .map(file -> {
                    final Yaml yaml = new Yaml();
                    final Map<String, String> map = new HashMap<>();
                    try (final Reader reader = new BufferedReader(new FileReader(file))) {
                        for (final Object object : yaml.loadAll(reader)) {
                            if (object == null || !Map.class.isInstance(object)) {
                                continue;
                            }

                            enrich(map, Map.class.cast(object), "");
                        }
                    } catch (final IOException e) {
                        throw new IllegalArgumentException(e);
                    }
                    return map;
                }).orElse(new HashMap<>()));
    }

    @Override
    public String getConfigName() {
        return "jwp";
    }

    private static void enrich(final Map<String, String> map, final Map<?, ?> object, final String s) {
        for (final Map.Entry<?, ?> entry : object.entrySet()) {
            final String key = (s.isEmpty() ? s : (s + '.')) + entry.getKey();
            final Object val = entry.getValue();
            if (String.class.isInstance(val)) {
                map.put(key, String.valueOf(val));
            } else if (Map.class.isInstance(val)) {
                enrich(map, Map.class.cast(val), key);
            } else if (Collection.class.isInstance(val)) {
                final StringBuilder builder = new StringBuilder();
                for (final Object o : Collection.class.cast(val)) {
                    builder.append(String.valueOf(o)).append(',');
                }
                if (builder.length() > 0) {
                    builder.setLength(builder.length() - 1);
                }
                map.put(key, builder.toString());
            } else {
                throw new IllegalArgumentException("type not supported: " + val);
            }
        }
    }
}
