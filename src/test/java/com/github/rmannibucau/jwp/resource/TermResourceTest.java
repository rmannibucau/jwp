package com.github.rmannibucau.jwp.resource;

import com.github.rmannibucau.jwp.jpa.Term;
import com.github.rmannibucau.jwp.jpa.TermTaxonomy;
import com.github.rmannibucau.jwp.resource.domain.TermModel;
import com.github.rmannibucau.jwp.resource.domain.TermPage;
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
import javax.ws.rs.core.MediaType;
import java.net.URL;
import java.util.Iterator;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(SingleContainerRunner.class)
public class TermResourceTest {
    @Inject
    private UserTransaction ut;

    @PersistenceContext
    private EntityManager em;

    @RandomPort("http")
    private URL base;

    private Term[] terms;
    private TermTaxonomy[] termTaxonomies;

    @Before
    public void data() throws Exception {
        ut.begin();

        terms = new Term[10];
        IntStream.range(0, terms.length)
            .forEach(i -> {
                final Term term = new Term();
                term.setName("Term #" + i);
                term.setSlug("term-" + i);
                em.persist(term);
                terms[i] = term;
            });


        termTaxonomies = new TermTaxonomy[terms.length];
        IntStream.range(0, termTaxonomies.length)
            .forEach(i -> {
                final TermTaxonomy termTaxonomy = new TermTaxonomy();
                termTaxonomy.setTerm(terms[i]);
                termTaxonomy.setTaxonomy(i % 3 == 0 ? "post_tag" : "category");
                termTaxonomy.setDescription("A Tag Desc");
                em.persist(termTaxonomy);

                em.persist(termTaxonomy);
                termTaxonomies[i] = termTaxonomy;
            });

        ut.commit();
    }

    @After
    public void clean() throws Exception {
        ut.begin();
        em.createQuery("delete from TermTaxonomy").executeUpdate();
        em.createQuery("delete from Term").executeUpdate();
        ut.commit();
    }

    @Test
    public void findTerms() {
        final TermPage page = ClientBuilder.newBuilder().build()
            .target(base.toExternalForm() + "jwp")
            .path("api/terms")
            .request(MediaType.APPLICATION_JSON_TYPE)
            .get(TermPage.class);
        assertEquals(10, page.getFound());

        final Iterator<TermModel> terms = page.getItems().iterator();
        IntStream.range(0, 10).forEach(i -> {
            assertTrue(terms.hasNext());
            assertTerm(i, terms.next());
        });
        assertFalse(terms.hasNext());
    }

    @Test
    public void findTermsByType() {
        final TermPage page = ClientBuilder.newBuilder().build()
            .target(base.toExternalForm() + "jwp")
            .path("api/terms")
            .queryParam("taxonomy", "category")
            .request(MediaType.APPLICATION_JSON_TYPE)
            .get(TermPage.class);
        assertEquals(6, page.getFound());

        final Iterator<TermModel> terms = page.getItems().iterator();
        IntStream.range(0, 10).forEach(i -> {
            if (i % 3 == 0) {
                return; // a tag so not returned
            }
            assertTrue(terms.hasNext());
            final TermModel next = terms.next();
            assertTerm(i, next);
            assertEquals("category", next.getType());
        });
        assertFalse(terms.hasNext());
    }

    private void assertTerm(final int i, final TermModel model) {
        assertEquals(terms[i].getName(), model.getName());
        assertEquals(terms[i].getSlug(), model.getSlug());
    }
}
