package com.github.rmannibucau.jwp.jpa;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Temporal;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Date;

import static com.github.rmannibucau.jwp.time.Zones.GMT;
import static javax.persistence.GenerationType.IDENTITY;
import static javax.persistence.TemporalType.TIMESTAMP;

@Data
@Entity
@Table(name = "wp_posts")
public class Post {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private long id;

    @Column(name = "comment_count")
    private long commentCount;

    @Column(name = "comment_status", nullable = false, length = 20)
    private String commentStatus;

    @Column(nullable = false)
    private String guid;

    @Column(name = "menu_order")
    private int menuOrder;

    @Column(name = "ping_status", nullable = false, length = 20)
    private String pingStatus;

    @Column(nullable = false)
    private String pinged;

    @ManyToOne
    @JoinColumn(name = "post_author")
    private User postAuthor;

    @Column(name = "post_content", nullable = false)
    private String postContent;

    @Column(name = "post_content_filtered", nullable = false)
    private String postContentFiltered;

    @Temporal(TIMESTAMP)
    @Column(name = "post_date", nullable = false)
    private Date postDate;

    @Temporal(TIMESTAMP)
    @Column(name = "post_date_gmt", nullable = false)
    private Date postDateGmt;

    @Column(name = "post_excerpt", nullable = false)
    private String postExcerpt;

    @Column(name = "post_mime_type", nullable = false, length = 100)
    private String postMimeType;

    @Temporal(TIMESTAMP)
    @Column(name = "post_modified", nullable = false)
    private Date postModified;

    @Temporal(TIMESTAMP)
    @Column(name = "post_modified_gmt", nullable = false)
    private Date postModifiedGmt;

    @Column(name = "post_name", nullable = false, length = 200)
    private String postName;

    @ManyToOne
    @JoinColumn(name = "post_parent")
    private Post parent;

    @Column(name = "post_password", nullable = false, length = 20)
    private String postPassword;

    @Column(name = "post_status", nullable = false, length = 20)
    private String postStatus;

    @Column(name = "post_title", nullable = false)
    private String postTitle;

    @Column(name = "post_type", nullable = false, length = 20)
    private String postType;

    @Column(name = "to_ping", nullable = false)
    private String toPing;

    @OneToMany(mappedBy = "post")
    private Collection<PostMeta> metas;

    @ManyToMany
    @JoinTable(
        name = "wp_term_relationships",
        joinColumns = @JoinColumn(name = "object_id", referencedColumnName = "id"),
        inverseJoinColumns = @JoinColumn(name = "term_taxonomy_id", referencedColumnName = "term_taxonomy_id"))
    private Collection<TermTaxonomy> termTaxonomies;

    @PrePersist
    private void persist() {
        update();
        postDate = postDate == null ? postModified : postDate;
        postDateGmt = postDateGmt == null ? postModifiedGmt : postDateGmt;

        // not null on DB side but empty is valid and used by wp
        if (postContentFiltered == null) {
            postContentFiltered = "";
        }
        if (postMimeType == null) {
            postMimeType = "";
        }
        if (postExcerpt == null) {
            postExcerpt = "";
        }
        if (pinged == null) {
            pinged = "";
        }
        if (toPing == null) {
            toPing = "";
        }
        if (postPassword == null) {
            postPassword = "";
        }
    }

    @PreUpdate
    private void update() {
        postModified = postModified == null ? new Date() : postModified;
        postModifiedGmt = postModifiedGmt == null ? Date.from(ZonedDateTime.now(GMT).toInstant()) : postModifiedGmt;
    }
}