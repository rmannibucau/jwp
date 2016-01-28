package com.github.rmannibucau.jwp.resource.mapper;

import com.github.rmannibucau.jwp.jpa.Term;
import com.github.rmannibucau.jwp.resource.domain.TermModel;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TermMapper {
    public TermModel toModel(final Term term) {
        return new TermModel(term.getTermId(), term.getName(), term.getSlug(), term.getTermGroup());
    }
}
