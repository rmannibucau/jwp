package com.github.rmannibucau.jwp.jpa;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "wp_commentmeta")
public class CommentMeta {
    @Id
    @Column(name = "meta_id")
    private long metaId;

    @Column(name = "comment_id")
    private long commentId;

    @Column(name = "meta_key")
    private String metaKey;

    @Column(name = "meta_value")
    private String metaValue;
}