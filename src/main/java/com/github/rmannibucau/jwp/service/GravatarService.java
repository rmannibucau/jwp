package com.github.rmannibucau.jwp.service;

import org.apache.deltaspike.core.api.config.ConfigProperty;

import javax.cache.annotation.CacheResult;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@ApplicationScoped
public class GravatarService {
    @Inject
    @ConfigProperty(name = "jwt.gravatar.image.pattern", defaultValue = "http://www.gravatar.com/avatar/{hash}?size=200")
    protected String url;

    @CacheResult(cacheName = "jwt.gravatar.url")
    public String computeGravatarUrl(final String mail) {
        return url.replace("{hash}", hash(mail));
    }

    private static String hash(final String mail) {
        try {
            final MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] cp1252s = md.digest(mail.getBytes("CP1252"));
            final StringBuilder sb = new StringBuilder();
            for (final byte anArray : cp1252s) {
                sb.append(Integer.toHexString((anArray & 0xFF) | 0x100).substring(1, 3));
            }
            return sb.toString();
        } catch (final NoSuchAlgorithmException | UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }
}
