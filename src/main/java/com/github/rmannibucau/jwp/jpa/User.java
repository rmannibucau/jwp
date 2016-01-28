package com.github.rmannibucau.jwp.jpa;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static javax.persistence.FetchType.EAGER;
import static javax.persistence.GenerationType.IDENTITY;
import static javax.persistence.TemporalType.TIMESTAMP;

@Data
@Entity
@Table(name = "wp_users")
public class User {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private long id;

    @Column(name = "display_name", nullable = false, length = 250)
    private String displayName;

    @Column(name = "user_activation_key", nullable = false)
    private String userActivationKey;

    @Column(name = "user_email", nullable = false, length = 100)
    private String userEmail;

    @Column(name = "user_login", nullable = false, length = 60)
    private String userLogin;

    @Column(name = "user_nicename", nullable = false, length = 50)
    private String userNiceName;

    @Column(name = "user_pass", nullable = false)
    private String userPass;

    @Temporal(TIMESTAMP)
    @Column(name = "user_registered", nullable = false)
    private Date userRegistered;

    @Column(name = "user_status")
    private int userStatus;

    @Column(name = "user_url", nullable = false, length = 100)
    private String userUrl;

    @OneToMany(fetch = EAGER, mappedBy = "user")
    private Collection<UserMeta> metas;

    public Map<String, String> mapMetas() {
        return ofNullable(getMetas()).orElse(emptyList()).stream()
                .collect(Collectors.toMap(UserMeta::getMetaKey, UserMeta::getMetaValue));
    }
}