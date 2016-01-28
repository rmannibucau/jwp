package com.github.rmannibucau.jwp.resource.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserModel {
    private long id;
    private String login;
    private String email;
    private String name;
    private String firstName;
    private String lastName;
    private String niceName;
    private String url;
    private String avatarUrl;
    private boolean isSuperAdmin;
}
