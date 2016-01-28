package com.github.rmannibucau.jwp.swagger2;

import io.swagger.jaxrs.config.BeanConfig;
import io.swagger.jaxrs.listing.ApiListingResource;
import io.swagger.models.Swagger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.beans.Introspector;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;

import static java.util.Optional.ofNullable;

@PreMatching
public class Swagger2ContainerRequestFilter extends ApiListingResource implements ContainerRequestFilter {
    @Context
    private UriInfo ui;

    @Context
    private ServletContext servletContext;

    @Context
    private HttpHeaders httpHeaders;

    @Context
    private Application application;

    private final BeanConfig config = new BeanConfig() {
        @Override
        public Set<Class<?>> classes() {
            // don't use reflections to scan the classpath since we have an app
            // Note: TomEE is nice enough to replace our application by an Internal one having the explicit list of classes :).
            // TODO: we could either fill getClasses() for other container or falback on reflections scanning
            return application.getClasses();
        }
    };

    @Override
    protected synchronized Swagger scan(final Application app, final ServletConfig sc) {
        systemPropertyOverrides();
        config.setScan(false); // just to force the info init
        servletContext.setAttribute("reader", config);
        return super.scan(app, sc);
    }

    @Override
    public void filter(final ContainerRequestContext requestContext) throws IOException {
        final String path = ui.getPath();
        if (path.endsWith("swagger.json")) {
            requestContext.abortWith(Response.fromResponse(getListingJson(application, null, httpHeaders, ui)).type(MediaType.APPLICATION_JSON_TYPE).build());
        }
    }

    private void systemPropertyOverrides() {
        for (final Method m : BeanConfig.class.getMethods()) {
            if (m.getName().startsWith("set") && m.getParameterCount() == 1) {
                ofNullable(System.getProperty("swagger2." + Introspector.decapitalize(m.getName().substring("set".length()))))
                    .ifPresent(val -> {
                        try {
                            m.invoke(config, val);
                        } catch (final IllegalAccessException e) {
                            throw new IllegalArgumentException(e);
                        } catch (final InvocationTargetException e) {
                            throw new IllegalArgumentException(e.getCause());
                        }
                    });
            }
        }
    }

    public void setSchemes(final String schemes) {
        config.setSchemes(schemes.split(" *, *"));
    }

    public void setTitle(final String title) {
        config.setTitle(title);
    }

    public void setVersion(final String version) {
        config.setVersion(version);
    }

    public void setDescription(final String description) {
        config.setDescription(description);
    }

    public void setTermsOfServiceUrl(final String termsOfServiceUrl) {
        config.setTermsOfServiceUrl(termsOfServiceUrl);
    }

    public void setContact(final String contact) {
        config.setContact(contact);
    }

    public void setLicense(final String license) {
        config.setLicense(license);
    }

    public void setLicenseUrl(final String licenseUrl) {
        config.setLicenseUrl(licenseUrl);
    }

    public void setHost(final String host) {
        config.setHost(host);
    }

    public void setFilterClass(final String filterClass) {
        config.setFilterClass(filterClass);
    }

    public void setBasePath(final String basePath) {
        config.setBasePath(basePath);
    }

    public void setPrettyPrint(final String prettyPrint) { // actually ignored if not using swagger jackson provider
        config.setPrettyPrint(prettyPrint);
    }
}

