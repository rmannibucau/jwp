package com.github.rmannibucau.jwp.service;

import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PHPServiceTest {
    @Test
    public void admin() {
        final String from = "a:1:{s:13:\"administrator\";b:1;}"; // wp_capabilities in user_meta
        final Map<?, ?> parsed = new PHPService().deserialize(from, Map.class);
        assertEquals(1, parsed.size());
        assertTrue((Boolean) parsed.get("administrator"));
    }

    @Test
    public void sessionToken() { // session_tokens in user_meta
        final String from =
                "a:1:{" +
                        "s:64:\"ba1f53e995acf7951dbbcc15146a7ad3fdccb9f074abedd6b7baf067125097f3\";" +
                        "a:4:{" +
                            "s:10:\"expiration\";i:1453918590;" +
                            "s:2:\"ip\";s:9:\"127.0.0.1\";" +
                            "s:2:\"ua\";s:104:\"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/48.0.2564.82 Safari/537.36\";" +
                            "s:5:\"login\";i:1453745790;}}";
        final Map<?, ?> parsed = new PHPService().deserialize(from, Map.class);
        assertEquals(1, parsed.size());
        final Map<?, ?> token = Map.class.cast(parsed.get("ba1f53e995acf7951dbbcc15146a7ad3fdccb9f074abedd6b7baf067125097f3"));
        assertEquals(4, token.size());
        assertEquals("127.0.0.1", token.get("ip"));
        assertEquals(1453918590, token.get("expiration"));
        assertEquals("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/48.0.2564.82 Safari/537.36", token.get("ua"));
        assertEquals(1453745790, token.get("login"));
    }
}
