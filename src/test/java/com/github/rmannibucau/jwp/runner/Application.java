package com.github.rmannibucau.jwp.runner;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ FIELD, TYPE })
@Retention(RUNTIME)
public @interface Application {
}
