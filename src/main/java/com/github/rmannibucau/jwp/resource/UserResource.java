package com.github.rmannibucau.jwp.resource;

import com.github.rmannibucau.jwp.jpa.Post;
import com.github.rmannibucau.jwp.jpa.User;
import com.github.rmannibucau.jwp.resource.domain.UserModel;
import com.github.rmannibucau.jwp.resource.domain.UserPage;
import com.github.rmannibucau.jwp.resource.mapper.UserMapper;
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
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import javax.transaction.Transactional;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

@Transactional
@ApplicationScoped
@Path("users")
@Produces(MediaType.APPLICATION_JSON)
@Api("user")
public class UserResource {
    private static final Collection<String> POSSIBLE_ORDER_BY = asList("id", "displayName", "userEmail", "userNiceName");

    @PersistenceContext
    private EntityManager em;

    @Inject
    private UserMapper mapper;

    @GET
    @ApiOperation(value = "Find users.")
    public UserPage getUsers(@QueryParam("number")
                             @ApiParam(value = "page size", defaultValue = "20")
                             @DefaultValue("20")
                             final int number,

                             @QueryParam("offset")
                             @ApiParam(value = "item offset", defaultValue = "0")
                             @DefaultValue("0")
                             final int offset,

                             @QueryParam("order_by")
                             @ApiParam(value = "ordering field", defaultValue = "displayName")
                             @DefaultValue("displayName")
                             final String orderBy,

                             @QueryParam("order")
                             @ApiParam(value = "order (asc/desc)", defaultValue = "asc")
                             @DefaultValue("asc")
                             final String order,

                             @QueryParam("authors_only")
                             @ApiParam(value = "keep only author users", defaultValue = "false")
                             @DefaultValue("false")
                             final boolean authorsOnly,

                             @QueryParam("type")
                             @ApiParam(value = "page type (page, post, ...)", defaultValue = "post")
                             @DefaultValue("post")
                             final String type) {

        if (!POSSIBLE_ORDER_BY.contains(orderBy)) {
            throw new IllegalArgumentException("Invalid order_by: " + orderBy);
        }
        if (!QueryParameters.POSSIBLE_ORDER.contains(order)) {
            throw new IllegalArgumentException("Invalid order: " + order);
        }

        final CriteriaBuilder builder = em.getCriteriaBuilder();
        final CriteriaQuery<User> query = builder.createQuery(User.class);
        final CriteriaQuery<Long> countQuery = builder.createQuery(Long.class);
        final Root<User> root = query.from(User.class);
        final Root<User> count = countQuery.from(User.class);

        final CriteriaQuery<User> select = query.select(root);
        final CriteriaQuery<Long> countSelect = countQuery.select(builder.count(count));
        if (authorsOnly) { // where (select count(p) from Post p where p.postAuthor = u) > 0
            Stream.of(select, countSelect).forEach(q -> {
                final Subquery<Long> subquery = q.subquery(Long.class);
                final Root<Post> fromPost = subquery.from(Post.class);
                final Predicate authorClause = builder.equal(fromPost.get("postAuthor"), root);
                if (!"all".equalsIgnoreCase(type)) {
                    subquery.where(builder.and(authorClause, builder.equal(fromPost.get("postType"), type)));
                } else {
                    subquery.where(authorClause);
                }
                q.where(builder.greaterThan(subquery.select(builder.count(fromPost)), 0L));
            });
        }
        select.orderBy("desc".equals(order) ? builder.desc(root.get(orderBy)) : builder.asc(root.get(orderBy)));

        final List<UserModel> items = em.createQuery(select)
            .setMaxResults(number).setFirstResult(offset).getResultList().stream()
            .map(mapper::toModel)
            .collect(toList());

        long total;
        try {
            total = ofNullable(em.createQuery(countQuery).getSingleResult()).orElse(0L);
        } catch (final NoResultException nre) {
            total = 0;
        }

        return new UserPage(total, items);
    }

    @GET
    @Path("{id}")
    @ApiOperation(value = "Find a user by id.")
    public UserModel get(@PathParam("id") @ApiParam(value = "user id", required = true) final long userId) {
        return ofNullable(em.find(User.class, userId)).map(mapper::toModel).orElse(null);
    }
}
