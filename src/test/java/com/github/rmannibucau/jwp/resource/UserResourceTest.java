package com.github.rmannibucau.jwp.resource;

import com.github.rmannibucau.jwp.jpa.Post;
import com.github.rmannibucau.jwp.jpa.User;
import com.github.rmannibucau.jwp.jpa.UserMeta;
import com.github.rmannibucau.jwp.resource.domain.UserModel;
import com.github.rmannibucau.jwp.resource.domain.UserPage;
import org.apache.openejb.api.configuration.PersistenceUnitDefinition;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.ContainerProperties;
import org.apache.openejb.testing.ContainerProperties.Property;
import org.apache.openejb.testing.Default;
import org.apache.openejb.testing.EnableServices;
import org.apache.openejb.testing.Jars;
import org.apache.openejb.testing.RandomPort;
import org.apache.openejb.testing.SimpleLog;
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
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@Default
@SimpleLog
@Jars("deltaspike-")
@PersistenceUnitDefinition
@EnableServices(jaxrs = true)
@RunWith(ApplicationComposer.class)
@Classes(cdi = true, context = "jwp")
@ContainerProperties({
    @Property(name = "jwp", value = "new://Resource?type=DataSource"),
    @Property(name = "jwp.JdbcDriver", value = "org.h2.Driver"),
    @Property(name = "jwp.JdbcUrl", value = "jdbc:h2:mem:jwp_user")
})
public class UserResourceTest {
    @Inject
    private UserTransaction ut;

    @PersistenceContext
    private EntityManager em;

    @RandomPort("http")
    private URL base;

    @Before
    public void data() throws Exception {
        ut.begin();

        IntStream.range(0, 10)
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

                {
                    final UserMeta meta = new UserMeta();
                    meta.setUser(user);
                    meta.setMetaKey("first_name");
                    meta.setMetaValue("Foo" + i);
                    em.persist(meta);
                }
                {
                    final UserMeta meta = new UserMeta();
                    meta.setUser(user);
                    meta.setMetaKey("last_name");
                    meta.setMetaValue("Bar" + i);
                    em.persist(meta);
                }
                {
                    final UserMeta meta = new UserMeta();
                    meta.setUser(user);
                    meta.setMetaKey("wp_capabilities");
                    meta.setMetaValue("a:1:{s:13:\"administrator\";b:" + (i % 2 == 0 ? '1' : '0') + ";}");
                    em.persist(meta);
                }

                if (i % 3 == 0) {
                    final Post post = new Post();
                    post.setPostAuthor(user);
                    post.setPostContent("bla bla");
                    post.setPostStatus("publish");
                    post.setPostTitle("bla");
                    post.setCommentStatus("open");
                    post.setPingStatus("open");
                    post.setPostName("hello");
                    post.setGuid("http://local/?p=1");
                    post.setMenuOrder(0);
                    post.setPostType(i % 6 != 0 ? "post" : "page");
                    post.setPostContentFiltered("");
                    post.setPostMimeType("");
                    post.setPostExcerpt("");
                    post.setPinged("");
                    post.setToPing("");
                    post.setPostPassword("");
                    em.persist(post);
                }
            });

        ut.commit();
    }

    @Test
    public void getUsers() {
        final UserPage page = ClientBuilder.newBuilder().build()
            .target(base.toExternalForm() + "jwp")
            .path("api/users")
            .request(MediaType.APPLICATION_JSON_TYPE)
            .get(UserPage.class);
        assertEquals(10, page.getFound());

        final Iterator<UserModel> users = page.getItems().iterator();
        IntStream.range(0, 10).forEach(i -> {
            assertTrue(users.hasNext());

            final UserModel model = users.next();
            assertUser(i, model);
        });
        assertFalse(users.hasNext());
    }

    @Test
    public void getUsersPagination() {
        final int pageSize = 5;
        final WebTarget target = ClientBuilder.newBuilder().build()
            .target(base.toExternalForm() + "jwp")
            .path("api/users")
            .queryParam("number", pageSize);

        IntStream.range(0, 2).forEach(pageIdx -> {
            final int offset = pageSize * pageIdx;
            final UserPage page = target
                .queryParam("offset", offset)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(UserPage.class);
            assertEquals(10, page.getFound());

            final Iterator<UserModel> users = page.getItems().iterator();
            IntStream.range(offset, offset + pageSize).forEach(i -> {
                assertTrue(users.hasNext());

                final UserModel model = users.next();
                assertUser(i, model);
            });
            assertFalse(users.hasNext());
        });
    }

    @Test
    public void getPostAuthors() {
        final UserPage users = ClientBuilder.newBuilder().build()
            .target(base.toExternalForm() + "jwp")
            .path("api/users")
            .queryParam("authors_only", true)
            .request(MediaType.APPLICATION_JSON_TYPE)
            .get(UserPage.class);
        assertEquals(2, users.getFound());
        assertEquals(2, users.getItems().size());
        assertUser(3, users.getItems().get(0));
        assertUser(9, users.getItems().get(1));
    }

    @Test
    public void getPageAuthors() {
        final List<UserModel> users = ClientBuilder.newBuilder().build()
            .target(base.toExternalForm() + "jwp")
            .path("api/users")
            .queryParam("authors_only", true)
            .queryParam("type", "page")
            .request(MediaType.APPLICATION_JSON_TYPE)
            .get(UserPage.class).getItems();
        assertEquals(2, users.size());
        assertUser(0, users.get(0));
        assertUser(6, users.get(1));
    }

    @Test
    public void getMissingTypeAuthors() {
        final Collection<UserModel> users = ClientBuilder.newBuilder().build()
            .target(base.toExternalForm() + "jwp")
            .path("api/users")
            .queryParam("authors_only", true)
            .queryParam("type", "missing")
            .request(MediaType.APPLICATION_JSON_TYPE)
            .get(UserPage.class).getItems();
        assertEquals(0, users.size());
    }

    @Test
    public void get() {
        // get a user and in particular its id to avoid to assume it is 1 for instance
        final UserModel ref = ClientBuilder.newBuilder().build()
            .target(base.toExternalForm() + "jwp")
            .path("api/users")
            .request(MediaType.APPLICATION_JSON_TYPE)
            .get(UserPage.class)
            .getItems().iterator().next();

        // the test actually start there for this method
        final UserModel user = ClientBuilder.newBuilder().build()
            .target(base.toExternalForm() + "jwp")
            .path("api/users/{id}")
            .resolveTemplate("id", ref.getId())
            .request(MediaType.APPLICATION_JSON_TYPE)
            .get(UserModel.class);
        assertUser(Integer.parseInt(ref.getLogin().replace("foo", "")), user);
    }

    private static void assertUser(final int i, final UserModel model) {
        assertEquals("foo" + i, model.getLogin());
        assertEquals("foo" + i + "@bar.com", model.getEmail());
        assertEquals("Foo Bar #" + i, model.getName());
        assertEquals("Foo" + i, model.getFirstName());
        assertEquals("Bar" + i, model.getLastName());
        assertEquals("Nice" + i, model.getNiceName());
        assertNotNull(model.getAvatarUrl());
        assertEquals(i % 2 == 0, model.isSuperAdmin());
    }
}
