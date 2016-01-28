package com.github.rmannibucau.jwp.resource;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

import static java.util.Arrays.asList;

@ApplicationPath("api")
public class JwpApplication extends Application {
    private final Set<Class<?>> classes = new HashSet<>();

    @Override
    public synchronized Set<Class<?>> getClasses() {
        if (classes.isEmpty()) {
            classes.addAll(asList(UserResource.class, PostResource.class));
        }
        return classes;
    }
}
