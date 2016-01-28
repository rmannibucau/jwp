package com.github.rmannibucau.jwp.jpa;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import java.util.Date;

import static javax.persistence.TemporalType.TIMESTAMP;

@Data
@Entity
@Table(name = "wp_links")
public class Link {
    @Id
    @Column(name = "link_id")
    private long linkId;

    @Column(name = "link_description", nullable = false)
    private String linkDescription;

    @Column(name = "link_image", nullable = false)
    private String linkImage;

    @Column(name = "link_name", nullable = false)
    private String linkName;

    @Column(name = "link_notes", nullable = false)
    private String linkNotes;

    @Column(name = "link_owner")
    private long linkOwner;

    @Column(name = "link_rating")
    private int linkRating;

    @Column(name = "link_rel", nullable = false)
    private String linkRel;

    @Column(name = "link_rss", nullable = false)
    private String linkRss;

    @Column(name = "link_target", nullable = false, length = 25)
    private String linkTarget;

    @Temporal(TIMESTAMP)
    @Column(name = "link_updated", nullable = false)
    private Date linkUpdated;

    @Column(name = "link_url", nullable = false)
    private String linkUrl;

    @Column(name = "link_visible", nullable = false, length = 20)
    private String linkVisible;
}