package com.github.rmannibucau.jwp.resource.mapper;

import com.github.rmannibucau.jwp.jpa.User;
import com.github.rmannibucau.jwp.jpa.UserMeta;
import com.github.rmannibucau.jwp.resource.domain.UserModel;
import com.github.rmannibucau.jwp.service.GravatarService;
import com.github.rmannibucau.jwp.service.PHPService;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;

@ApplicationScoped
public class UserMapper {
    @Inject
    private GravatarService gravatarService;

    @Inject
    private PHPService phpService;

    public UserModel toModel(final User user) {
        final Map<String, String> metas = ofNullable(user.getMetas()).orElse(emptyList()).stream()
            .collect(Collectors.toMap(UserMeta::getMetaKey, UserMeta::getMetaValue));
        return new UserModel(
            user.getId(), user.getUserLogin(), user.getUserEmail(), user.getDisplayName(),
            metas.get("first_name"), metas.get("last_name"), user.getUserNiceName(), user.getUserUrl(),
            ofNullable(user.getUserEmail()).map(gravatarService::computeGravatarUrl).orElse(null),
            Boolean.class.cast(
                ofNullable(metas.get("wp_capabilities")).map(phpService::deserialize).map(Map.class::cast).orElse(Collections.emptyMap())
                    .getOrDefault("administrator", Boolean.FALSE)));
    }
}
