package com.github.rmannibucau.jwp.jpa;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import static javax.persistence.GenerationType.IDENTITY;

@Data
@EqualsAndHashCode(of = "umetaId")
@Entity
@Table(name = "wp_usermeta")
public class UserMeta {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "umeta_id")
    private long umetaId;

    @Column(name = "meta_key")
    private String metaKey;

    @Column(name = "meta_value")
    private String metaValue;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}