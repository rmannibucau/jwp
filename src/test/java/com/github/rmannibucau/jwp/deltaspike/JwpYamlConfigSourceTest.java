package com.github.rmannibucau.jwp.deltaspike;

import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class JwpYamlConfigSourceTest {
    @Test
    public void checkProperties() {
        final String original = System.getProperty("jwp.base");
        System.setProperty("jwp.base", "src/test/data");
        try {
            final Map<String, String> props = new JwpYamlConfigSource().getProperties();
            assertNotNull(props);
            assertEquals(2, props.size());
            assertEquals("default", props.get("jwp.date.parser"));
            assertEquals("http://www.gravatar.com/avatar/{hash}?size=200", props.get("jwp.gravatar.image.pattern"));
        } finally {
            if (original == null) {
                System.clearProperty("jwp.base");
            } else {
                System.setProperty("jwp.base", original);
            }
        }
    }
}
