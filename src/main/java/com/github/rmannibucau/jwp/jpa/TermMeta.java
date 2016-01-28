package com.github.rmannibucau.jwp.jpa;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "wp_termmeta")
public class TermMeta {
    @Id
    @Column(name = "meta_id")
    private long metaId;

    @Column(name = "meta_key")
    private String metaKey;

    @Column(name = "meta_value")
    private String metaValue;

    @Column(name = "term_id")
    private long termId;
}