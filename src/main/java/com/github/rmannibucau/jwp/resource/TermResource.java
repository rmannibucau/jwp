package com.github.rmannibucau.jwp.resource;

import com.github.rmannibucau.jwp.jpa.TermTaxonomy;
import com.github.rmannibucau.jwp.resource.domain.TermModel;
import com.github.rmannibucau.jwp.resource.domain.TermPage;
import com.github.rmannibucau.jwp.resource.mapper.TermMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.Collection;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

@Path("terms")
@Api("terms")
@Produces(MediaType.APPLICATION_JSON)
@Transactional
@ApplicationScoped
public class TermResource {
    private static final Collection<String> POSSIBLE_ORDER_BY = asList("id", "name");

    @PersistenceContext
    private EntityManager em;

    @Inject
    private TermMapper mapper;

    @GET
    @ApiOperation(value = "Find matching terms.")
    public TermPage get(@QueryParam("taxonomy")
                        @DefaultValue("all")
                        @ApiParam(value = "taxonomy (type of term)", defaultValue = "all")
                        final String taxonomy,

                        @QueryParam("number")
                        @ApiParam(value = "page size", defaultValue = "20")
                        @DefaultValue("20")
                        final int number,

                        @QueryParam("offset")
                        @ApiParam(value = "item offset", defaultValue = "0")
                        @DefaultValue("0")
                        final int offset,

                        @QueryParam("order_by")
                        @ApiParam(value = "ordering field", defaultValue = "name")
                        @DefaultValue("name")
                        final String orderBy,

                        @QueryParam("order")
                        @ApiParam(value = "order (asc/desc)", defaultValue = "asc")
                        @DefaultValue("asc")
                        final String order) {
        if (!POSSIBLE_ORDER_BY.contains(orderBy)) {
            throw new IllegalArgumentException("Invalid order_by: " + orderBy);
        }
        if (!QueryParameters.POSSIBLE_ORDER.contains(order)) {
            throw new IllegalArgumentException("Invalid order: " + order);
        }

        final CriteriaBuilder builder = em.getCriteriaBuilder();
        final CriteriaQuery<TermTaxonomy> query = builder.createQuery(TermTaxonomy.class);
        final CriteriaQuery<Long> countQuery = builder.createQuery(Long.class);
        final Root<TermTaxonomy> root = query.from(TermTaxonomy.class);
        final Root<TermTaxonomy> countRoot = countQuery.from(TermTaxonomy.class);

        final CriteriaQuery<TermTaxonomy> select = query.select(root);
        final CriteriaQuery<Long> countSelect = countQuery.select(builder.count(countRoot));
        if (!"all".equals(taxonomy)) {
            select.where(builder.equal(root.get("taxonomy"), taxonomy));
            countSelect.where(builder.equal(countRoot.get("taxonomy"), taxonomy));
        }
        final javax.persistence.criteria.Path<Object> orderPath = root.get("term").get(orderBy);
        select.orderBy("desc".equals(order) ? builder.desc(orderPath) : builder.asc(orderPath));

        final List<TermModel> items = em.createQuery(select)
            .setMaxResults(number).setFirstResult(offset).getResultList().stream()
            .map(t -> mapper.toModel(t, t.getTerm()))
            .collect(toList());

        long total;
        try {
            total = ofNullable(em.createQuery(countQuery).getSingleResult()).orElse(0L);
        } catch (final NoResultException nre) {
            total = 0;
        }

        return new TermPage(total, items);
    }
}
