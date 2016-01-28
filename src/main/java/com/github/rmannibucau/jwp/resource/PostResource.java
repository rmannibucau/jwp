package com.github.rmannibucau.jwp.resource;

import com.github.rmannibucau.jwp.jpa.Post;
import com.github.rmannibucau.jwp.resource.domain.PostModel;
import com.github.rmannibucau.jwp.resource.domain.PostPage;
import com.github.rmannibucau.jwp.resource.mapper.PostMapper;
import com.github.rmannibucau.jwp.service.DateParser;
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
import javax.transaction.Transactional;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

@Transactional
@ApplicationScoped
@Path("posts")
@Api("post")
@Produces(MediaType.APPLICATION_JSON)
public class PostResource {
    private static final Collection<String> POSSIBLE_ORDER_BY = asList(
        "none",
        "id", "postTitle", "postType", "postDate",
        "postAuthor.id", "postAuthor.displayName", "postAuthor.userEmail", "postAuthor.userNiceName");

    @PersistenceContext
    private EntityManager em;

    @Inject
    private PostMapper mapper;

    @Inject
    private DateParser dateParser;

    @GET
    @Path("{id}")
    @ApiOperation(value = "Find a post by id.")
    public PostModel get(@PathParam("id") final long id) {
        return ofNullable(em.find(Post.class, id)).map(mapper::toModel).orElse(null);
    }

    @GET
    @ApiOperation(value = "Find posts.")
    public PostPage getPosts(@QueryParam("number")
                             @ApiParam(value = "page size", defaultValue = "20")
                             @DefaultValue("20")
                             final int number,

                             @QueryParam("offset")
                             @ApiParam(value = "items offset", defaultValue = "0")
                             @DefaultValue("0")
                             final int offset,

                             @QueryParam("order_by")
                             @ApiParam(value = "order by field", defaultValue = "postDate")
                             @DefaultValue("postDate")
                             final String orderBy,

                             @QueryParam("order")
                             @ApiParam(value = "order (asc/desc)", defaultValue = "desc")
                             @DefaultValue("desc")
                             final String order,

                             @QueryParam("type")
                             @ApiParam(value = "post type (page, post, ...)", defaultValue = "post")
                             @DefaultValue("post")
                             final String type,

                             @QueryParam("after")
                             @ApiParam(value = "starting date, post created before are ignored")
                             final String afterDate,

                             @QueryParam("before")
                             @ApiParam(value = "ending date, post created after are ignored")
                             final String beforeDate,

                             @QueryParam("status")
                             @ApiParam(value = "post status (publish, draft, ...)", allowMultiple = true)
                             final Collection<String> status,

                             @QueryParam("author")
                             @ApiParam(value = "post author id", allowMultiple = true)
                             final Collection<Long> authors,

                             @QueryParam("parent")
                             @ApiParam(value = "post parent page id", defaultValue = "0")
                             @DefaultValue("0")
                             final long parentId,

                             @QueryParam("tag")
                             @ApiParam(value = "post tag", allowMultiple = true)
                             final Collection<String> tags,

                             @QueryParam("category")
                             @ApiParam(value = "post category", allowMultiple = true)
                             final Collection<String> categories) {
        if (!POSSIBLE_ORDER_BY.contains(orderBy)) {
            throw new IllegalArgumentException("Invalid order_by: " + orderBy);
        }
        if (!QueryParameters.POSSIBLE_ORDER.contains(order)) {
            throw new IllegalArgumentException("Invalid order: " + order);
        }

        final CriteriaBuilder builder = em.getCriteriaBuilder();
        final CriteriaQuery<Post> query = builder.createQuery(Post.class);
        final CriteriaQuery<Long> countQuery = builder.createQuery(Long.class);
        final Root<Post> root = query.from(Post.class);
        final Root<Post> countRoot = countQuery.from(Post.class);

        final CriteriaQuery<Post> select = query.select(root);
        final CriteriaQuery<Long> countSelect = countQuery.select(builder.count(countRoot));
        final Collection<Predicate> predicates = new ArrayList<>();
        final Collection<Predicate> countPredicates = new ArrayList<>();
        if (!"all".equals(type)) {
            predicates.add(builder.equal(root.get("postType"), type));
            countPredicates.add(builder.equal(countRoot.get("postType"), type));
        }
        ofNullable(afterDate).ifPresent(after -> {
            final Date asDate = dateParser.parse(after);
            predicates.add(builder.greaterThanOrEqualTo(root.get("postDate"), asDate));
            countPredicates.add(builder.greaterThanOrEqualTo(countRoot.get("postDate"), asDate));
        });
        ofNullable(beforeDate).ifPresent(before -> {
            final Date asDate = dateParser.parse(before);
            predicates.add(builder.lessThanOrEqualTo(root.get("postDate"), asDate));
            countPredicates.add(builder.lessThanOrEqualTo(countRoot.get("postDate"), asDate));
        });
        ofNullable(status).filter(t -> t != null && !t.isEmpty()).ifPresent(t -> {
            predicates.add(root.get("postStatus").in(status));
            countPredicates.add(countRoot.get("postStatus").in(status));
        });
        if (parentId > 0) {
            predicates.add(builder.equal(root.get("parent").get("id"), parentId));
            countPredicates.add(builder.equal(countRoot.get("parent").get("id"), parentId));
        }
        ofNullable(authors).filter(a -> a != null && !a.isEmpty()).ifPresent(a -> {
            predicates.add(root.get("postAuthor").get("id").in(a));
            countPredicates.add(countRoot.get("postAuthor").get("id").in(a));
        });
        whereTermTaxonomyContains(tags, "post_tag", builder, root, countRoot, predicates, countPredicates);
        whereTermTaxonomyContains(categories, "category", builder, root, countRoot, predicates, countPredicates);

        if (!predicates.isEmpty()) {
            select.where(builder.and(predicates.stream().toArray(Predicate[]::new)));
            countSelect.where(builder.and(countPredicates.stream().toArray(Predicate[]::new)));
        }
        if (!"none".equals(orderBy)) {
            select.orderBy("desc".equals(order) ? builder.desc(root.get(orderBy)) : builder.asc(root.get(orderBy)));
        }

        final List<PostModel> items = em.createQuery(select)
            .setMaxResults(number).setFirstResult(offset).getResultList().stream()
            .map(mapper::toModel)
            .collect(toList());

        long total;
        try {
            total = ofNullable(em.createQuery(countQuery).getSingleResult()).orElse(0L);
        } catch (final NoResultException nre) {
            total = 0;
        }

        return new PostPage(total, items);
    }

    private void whereTermTaxonomyContains(final Collection<String> values, final String type,
                                           final CriteriaBuilder builder,
                                           final Root<Post> root, final Root<Post> countRoot,
                                           final Collection<Predicate> predicates, final Collection<Predicate> countPredicates) {
        ofNullable(values).filter(t -> t != null && !t.isEmpty()).ifPresent(t -> {
            predicates.add(builder.and(
                builder.equal(root.get("termTaxonomies").get("taxonomy"), type),
                root.get("termTaxonomies").get("term").get("name").in(t)
            ));
            countPredicates.add(builder.and(
                builder.equal(countRoot.get("termTaxonomies").get("taxonomy"), type),
                countRoot.get("termTaxonomies").get("term").get("name").in(t)
            ));
        });
    }
}
