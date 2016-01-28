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
@Table(name = "wp_comments")
public class Comment {
    @Id
    @Column(name = "comment_ID")
    private long commentID;

    @Column(name = "comment_agent", nullable = false)
    private String commentAgent;

    @Column(name = "comment_approved", nullable = false, length = 20)
    private String commentApproved;

    @Column(name = "comment_author", nullable = false)
    private String commentAuthor;

    @Column(name = "comment_author_email", nullable = false, length = 100)
    private String commentAuthorEmail;

    @Column(name = "comment_author_IP", nullable = false, length = 100)
    private String commentAuthorIP;

    @Column(name = "comment_author_url", nullable = false, length = 200)
    private String commentAuthorUrl;

    @Column(name = "comment_content", nullable = false)
    private String commentContent;

    @Temporal(TIMESTAMP)
    @Column(name = "comment_date", nullable = false)
    private Date commentDate;

    @Temporal(TIMESTAMP)
    @Column(name = "comment_date_gmt", nullable = false)
    private Date commentDateGmt;

    @Column(name = "comment_karma")
    private int commentKarma;

    @Column(name = "comment_parent")
    private long commentParent;

    @Column(name = "comment_post_ID")
    private long commentPostID;

    @Column(name = "comment_type", nullable = false, length = 20)
    private String commentType;

    @Column(name = "user_id")
    private long userId;
}