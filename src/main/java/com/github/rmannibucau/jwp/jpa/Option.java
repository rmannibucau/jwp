package com.github.rmannibucau.jwp.jpa;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "wp_options")
public class Option {
    @Id
    @Column(name = "option_id")
    private long optionId;

    @Column(nullable = false, length = 20)
    private String autoload;

    @Column(name = "option_name", nullable = false, length = 191)
    private String optionName;

    @Column(name = "option_value", nullable = false)
    private String optionValue;
}