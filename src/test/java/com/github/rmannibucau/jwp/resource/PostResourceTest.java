package com.github.rmannibucau.jwp.resource;

import com.github.rmannibucau.jwp.jpa.Post;
import com.github.rmannibucau.jwp.jpa.Term;
import com.github.rmannibucau.jwp.jpa.TermTaxonomy;
import com.github.rmannibucau.jwp.jpa.User;
import com.github.rmannibucau.jwp.resource.domain.PostModel;
import com.github.rmannibucau.jwp.resource.domain.PostPage;
import com.github.rmannibucau.jwp.resource.domain.TermModel;
import com.github.rmannibucau.jwp.runner.SingleContainerRunner;
import org.apache.openejb.testing.RandomPort;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.UserTransaction;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import java.net.URL;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toSet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(SingleContainerRunner.class)
public class PostResourceTest {
    @Inject
    private UserTransaction ut;

    @PersistenceContext
    private EntityManager em;

    @RandomPort("http")
    private URL base;

    private Post[] posts;
    private User[] users;
    private long startTime;

    @Before
    public void data() throws Exception {
        ut.begin();

        users = new User[2];
        IntStream.range(0, users.length)
            .forEach(i -> {
                final User user = new User();
                user.setDisplayName("Foo Bar #" + i);
                user.setUserEmail("foo" + i + "@bar.com");
                user.setUserPass("secret" + i);
                user.setUserRegistered(new Date());
                user.setUserLogin("foo" + i);
                user.setUserActivationKey("");
                user.setUserNiceName("Nice" + i);
                user.setUserUrl("");
                em.persist(user);
                users[i] = user;
            });


        final Term tag = new Term();
        tag.setName("A Tag");
        tag.setSlug("a-tag");
        em.persist(tag);

        final Term category = new Term();
        category.setName("A Cat");
        category.setSlug("a-cat");
        em.persist(category);

        posts = new Post[10];
        startTime = System.currentTimeMillis() - (TimeUnit.SECONDS.toMillis(20)); // fake a time scale for the test purpose
        IntStream.range(0, posts.length)
            .forEach(i -> {
                final Post post = new Post();
                post.setPostContent("bla bla #" + i);
                post.setPostStatus(i % 3 == 0 ? "publish" : "draft");
                post.setPostTitle("bla");
                post.setCommentStatus("open");
                post.setPingStatus("open");
                post.setPostName("hello");
                post.setGuid("http://local/?p=1");
                post.setMenuOrder(0);
                post.setPostType(i % 6 != 0 ? "post" : "page");
                post.setPostAuthor(users[i % users.length]);
                post.setPostDate(new Date(startTime + (i * 2 * TimeUnit.SECONDS.toMillis(1))));

                if (i % 5 == 0) {
                    final TermTaxonomy termTaxonomy = new TermTaxonomy();
                    termTaxonomy.setTerm(tag);
                    termTaxonomy.setTaxonomy("post_tag");
                    termTaxonomy.setDescription("A Tag Desc");
                    em.persist(termTaxonomy);

                    post.setTermTaxonomies(new HashSet<>());
                    post.getTermTaxonomies().add(termTaxonomy);
                }
                if (i % 4 == 0) {
                    final TermTaxonomy termTaxonomy = new TermTaxonomy();
                    termTaxonomy.setTerm(category);
                    termTaxonomy.setTaxonomy("category");
                    termTaxonomy.setDescription("A Cat Desc for #" + i);
                    em.persist(termTaxonomy);

                    post.setTermTaxonomies(new HashSet<>());
                    post.getTermTaxonomies().add(termTaxonomy);
                }

                em.persist(post);
                posts[i] = post;
            });

        ut.commit();
    }

    @After
    public void clean() throws Exception {
        ut.begin();
        em.createQuery("delete from TermTaxonomy").executeUpdate();
        em.createQuery("delete from Term").executeUpdate();
        em.createQuery("delete from Post").executeUpdate();
        em.createQuery("delete from User").executeUpdate();
        ut.commit();
    }

    @Test
    public void getPost() {
        final PostModel postModel = ClientBuilder.newBuilder().build()
            .target(appBase())
            .path("api/posts/{id}")
            .resolveTemplate("id", posts[3].getId())
            .request(MediaType.APPLICATION_JSON_TYPE)
            .get(PostModel.class);
        assertNotNull(postModel);
        assertNotNull(postModel.getModified());
        assertNotNull(postModel.getDate());
        assertEquals("bla", postModel.getTitle());
        assertEquals("bla bla #3", postModel.getContent());
        assertEquals("", postModel.getExcerpt());
        assertEquals("publish", postModel.getStatus());
        assertEquals(0, postModel.getParentId());
        assertTrue(postModel.getCategories().isEmpty());
        assertTrue(postModel.getTags().isEmpty());
        assertEquals(posts[3].getId(), postModel.getId());
        assertNotNull(postModel.getAuthor());
        assertEquals("foo1", postModel.getAuthor().getLogin()); // mapper has been tested in user tests so just check we have something there
    }

    @Test
    public void getAll() {
        final PostPage posts = ClientBuilder.newBuilder().build()
            .target(appBase())
            .path("api/posts")
            .queryParam("type", "all")
            .queryParam("order_by", "none")
            .request(MediaType.APPLICATION_JSON_TYPE)
            .get(PostPage.class);
        assertEquals(10, posts.getFound());
        IntStream.range(0, this.posts.length).forEach(i -> assertPost(this.posts[i], posts.getItems().get(i)));
    }

    @Test
    public void getAllPagination() {
        final int pageSize = 5;
        final WebTarget target = ClientBuilder.newBuilder().build()
            .target(appBase())
            .path("api/posts")
            .queryParam("type", "all")
            .queryParam("order_by", "none")
            .queryParam("number", pageSize);
        IntStream.range(0, 2).forEach(pageIdx -> {
            final int offset = pageSize * pageIdx;
            final PostPage page = target
                .queryParam("offset", offset)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(PostPage.class);
            assertEquals(10, page.getFound());

            final Iterator<PostModel> posts = page.getItems().iterator();
            IntStream.range(offset, offset + pageSize).forEach(i -> {
                assertTrue(posts.hasNext());

                final PostModel post = posts.next();
                assertPost(this.posts[i], post);
            });
            assertFalse(posts.hasNext());
        });
    }

    @Test
    public void getAllOrderBy() {
        {
            final PostPage posts = ClientBuilder.newBuilder().build()
                .target(appBase())
                .path("api/posts")
                .queryParam("type", "all")
                .queryParam("order_by", "postType") // desc by default
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(PostPage.class);
            assertEquals(10, posts.getFound());
            IntStream.range(0, 8).forEach(i -> assertEquals("post", posts.getItems().get(i).getType()));
            IntStream.range(8, 10).forEach(i -> assertEquals("page", posts.getItems().get(i).getType()));
        }
        {
            final PostPage posts = ClientBuilder.newBuilder().build()
                .target(appBase())
                .path("api/posts")
                .queryParam("type", "all")
                .queryParam("order_by", "postType") // desc by default
                .queryParam("order", "asc")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(PostPage.class);
            assertEquals(10, posts.getFound());
            IntStream.range(0, 2).forEach(i -> assertEquals("page", posts.getItems().get(i).getType()));
            IntStream.range(2, 10).forEach(i -> assertEquals("post", posts.getItems().get(i).getType()));
        }
    }

    @Test
    public void getAllByType() {
        final PostPage posts = ClientBuilder.newBuilder().build()
            .target(appBase())
            .path("api/posts")
            .queryParam("type", "page")
            .request(MediaType.APPLICATION_JSON_TYPE)
            .get(PostPage.class);
        assertEquals(2, posts.getFound());
        IntStream.range(0, 2).forEach(i -> assertEquals("page", posts.getItems().get(i).getType()));
    }

    @Test
    public void getAllByStatus() {
        final PostPage posts = ClientBuilder.newBuilder().build()
            .target(appBase())
            .path("api/posts")
            .queryParam("status", "draft")
            .request(MediaType.APPLICATION_JSON_TYPE)
            .get(PostPage.class);
        assertEquals(6, posts.getFound());
        IntStream.range(0, 6).forEach(i -> assertEquals("draft", posts.getItems().get(i).getStatus()));
    }

    @Test
    public void getAllByAuthor() {
        final PostPage posts = ClientBuilder.newBuilder().build()
            .target(appBase())
            .path("api/posts")
            .queryParam("author", this.users[0].getId())
            .request(MediaType.APPLICATION_JSON_TYPE)
            .get(PostPage.class);
        assertEquals(3, posts.getFound());
        IntStream.range(0, 3).forEach(i -> assertEquals(this.users[0].getUserLogin(), posts.getItems().get(i).getAuthor().getLogin()));
    }

    @Test
    public void getAllByTag() {
        final PostPage posts = ClientBuilder.newBuilder().build()
            .target(appBase())
            .path("api/posts")
            .queryParam("tag", "A Tag")
            .request(MediaType.APPLICATION_JSON_TYPE)
            .get(PostPage.class);
        assertEquals(1, posts.getFound());
        assertTrue(posts.getItems().iterator().next().getTags().stream().map(TermModel::getName).collect(Collectors.toList()).contains("A Tag"));
    }

    @Test
    public void getAllByCategory() {
        final PostPage posts = ClientBuilder.newBuilder().build()
            .target(appBase())
            .path("api/posts")
            .queryParam("type", "all")
            .queryParam("category", "A Cat")
            .request(MediaType.APPLICATION_JSON_TYPE)
            .get(PostPage.class);
        assertEquals(3, posts.getFound());
        IntStream.range(0, 3)
            .forEach(i -> assertTrue(posts.getItems().get(i).getCategories().stream().map(TermModel::getName).collect(Collectors.toList()).contains("A Cat")));;
    }

    @Test
    public void getAllByAfter() {
        final long timePoint = startTime + TimeUnit.SECONDS.toMillis(2 * 3);
        final PostPage posts = ClientBuilder.newBuilder().build()
            .target(appBase())
            .path("api/posts")
            .queryParam("after", new Date(timePoint).toInstant().toString())
            .request(MediaType.APPLICATION_JSON_TYPE)
            .get(PostPage.class);
        assertEquals(6, posts.getFound());
        IntStream.range(0, 6)
            .forEach(i -> assertTrue(TimeUnit.MILLISECONDS.toSeconds(posts.getItems().get(i).getDate().getTime()) >= TimeUnit.MILLISECONDS.toSeconds(timePoint)));
    }

    @Test
    public void getAllByBefore() {
        final long timePoint = startTime + TimeUnit.SECONDS.toMillis(2 * 3);
        final PostPage posts = ClientBuilder.newBuilder().build()
            .target(appBase())
            .path("api/posts")
            .queryParam("before", new Date(timePoint).toInstant().toString())
            .request(MediaType.APPLICATION_JSON_TYPE)
            .get(PostPage.class);
        assertEquals(3, posts.getFound());
        IntStream.range(0, 3)
            .forEach(i -> assertTrue(TimeUnit.MILLISECONDS.toSeconds(posts.getItems().get(i).getDate().getTime()) <= TimeUnit.MILLISECONDS.toSeconds(timePoint)));
    }

    private String appBase() {
        return base.toExternalForm() + "jwp";
    }

    private static void assertPost(final Post reference, final PostModel retrieved) {
        assertEquals(reference.getPostContent(), retrieved.getContent());
        assertEquals(reference.getPostStatus(), retrieved.getStatus());
        assertEquals(reference.getPostTitle(), retrieved.getTitle());
        assertEquals(reference.getPostAuthor().getId(), retrieved.getAuthor().getId());
        assertEquals(reference.getPostDate().getTime(), retrieved.getDate().getTime(), TimeUnit.SECONDS.toMillis(2));
        assertEquals(0, retrieved.getParentId());
        assertEquals(reference.getPostExcerpt(), retrieved.getExcerpt());
        assertEquals(reference.getPostModified().getTime(), retrieved.getModified().getTime(), TimeUnit.SECONDS.toMillis(2));
        assertEquals(
            ofNullable(reference.getTermTaxonomies()).orElse(emptyList()).stream()
                .filter(t -> "category".equals(t.getTaxonomy()))
                .map(c -> c.getTerm().getName())
                .collect(toSet()),
            retrieved.getCategories().stream().map(TermModel::getName).collect(toSet()));
        assertEquals(
            ofNullable(reference.getTermTaxonomies()).orElse(emptyList()).stream()
                .filter(t -> "post_tag".equals(t.getTaxonomy()))
                .map(c -> c.getTerm().getName())
                .collect(toSet()),
            retrieved.getTags().stream().map(TermModel::getName).collect(toSet()));
    }
}
