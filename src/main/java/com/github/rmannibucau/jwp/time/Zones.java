package com.github.rmannibucau.jwp.time;

import lombok.NoArgsConstructor;

import java.time.ZoneId;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public final class Zones {
    public static final ZoneId GMT = ZoneId.of("GMT");
}
