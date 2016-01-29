package com.github.rmannibucau.jwp.resource.mapper;

import com.github.rmannibucau.jwp.jpa.Post;
import com.github.rmannibucau.jwp.jpa.TermTaxonomy;
import com.github.rmannibucau.jwp.resource.domain.PostModel;
import com.github.rmannibucau.jwp.resource.domain.TermModel;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

@ApplicationScoped
public class PostMapper {
    @Inject
    private UserMapper userMapper;

    @Inject
    private TermMapper termMapper;

    public PostModel toModel(final Post post) {
        final PostModel postModel = new PostModel();
        postModel.setId(post.getId());
        postModel.setContent(post.getPostContent());
        postModel.setExcerpt(post.getPostExcerpt());
        postModel.setTitle(post.getPostTitle());
        postModel.setStatus(post.getPostStatus());
        postModel.setType(post.getPostType());
        ofNullable(post.getParent()).ifPresent(parent -> postModel.setParentId(parent.getId()));
        ofNullable(post.getPostDate()).ifPresent(d -> postModel.setDate(new Date(d.getTime())));
        ofNullable(post.getPostModified()).ifPresent(d -> postModel.setModified(new Date(d.getTime())));
        postModel.setAuthor(ofNullable(post.getPostAuthor()).map(a -> userMapper.toModel(a)).orElse(null));
        postModel.setCategories(mapTerms(findTerms("category", post)));
        postModel.setTags(mapTerms(findTerms("post_tag", post)));
        return postModel;
    }

    private static Collection<TermTaxonomy> findTerms(final String taxonomy, final Post post) {
        return ofNullable(post.getTermTaxonomies()).orElse(emptyList()).stream()
            .filter(t -> taxonomy.equals(t.getTaxonomy()))
            .collect(toList());
    }

    private List<TermModel> mapTerms(final Collection<TermTaxonomy> terms) {
        return terms.stream().map(TermTaxonomy::getTerm).map(termMapper::toModel).collect(toList());
    }
}
