package com.github.rmannibucau.jwp.resource;

import lombok.NoArgsConstructor;

import java.util.Collection;

import static java.util.Arrays.asList;
import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public class QueryParameters {
    public static final Collection<String> POSSIBLE_ORDER = asList("asc", "desc");
}
