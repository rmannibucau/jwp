package com.github.rmannibucau.jwp.resource.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostPage {
    private long found;
    private List<PostModel> items;
}
