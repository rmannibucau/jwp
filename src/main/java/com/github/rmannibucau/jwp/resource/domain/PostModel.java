package com.github.rmannibucau.jwp.resource.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostModel {
    private long id;
    private UserModel author;
    private Date date;
    private Date modified;
    private String title;
    private String content;
    private String excerpt;
    private String status;
    private String type;
    private long parentId;
    private Collection<TermModel> categories;
    private Collection<TermModel> tags;
}
