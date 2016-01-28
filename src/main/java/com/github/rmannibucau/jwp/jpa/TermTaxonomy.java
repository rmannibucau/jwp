package com.github.rmannibucau.jwp.jpa;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import static javax.persistence.GenerationType.IDENTITY;

@Data
@Entity
@Table(name = "wp_term_taxonomy")
public class TermTaxonomy {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "term_taxonomy_id")
    private long termTaxonomyId;

    private long count;

    @Column(nullable = false)
    private String description;

    private long parent;

    @Column(nullable = false, length = 32)
    private String taxonomy;

    @ManyToOne
    @JoinColumn(name = "term_id")
    private Term term;
}