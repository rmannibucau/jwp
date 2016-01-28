package com.github.rmannibucau.jwp.service;

import org.apache.deltaspike.core.api.config.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.Instant;
import java.util.Date;

@ApplicationScoped
public class DateParser {
    @Inject
    @ConfigProperty(name = "jwp.date.parser", defaultValue = "default")
    private String type;

    public Date parse(final String in) {
        if ("default".equals(type)) {
            return Date.from(Instant.parse(in));
        }
        throw new IllegalArgumentException(type + " not supported");
    }
}
