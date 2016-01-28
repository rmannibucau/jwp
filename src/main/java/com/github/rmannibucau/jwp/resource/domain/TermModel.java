package com.github.rmannibucau.jwp.resource.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TermModel {
    private long id;
    private String name;
    private String slug;
    private long group;
}
