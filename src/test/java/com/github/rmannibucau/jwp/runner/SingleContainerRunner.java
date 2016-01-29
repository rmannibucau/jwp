package com.github.rmannibucau.jwp.runner;

import org.apache.openejb.api.configuration.PersistenceUnitDefinition;
import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.testing.ApplicationComposers;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.ContainerProperties;
import org.apache.openejb.testing.Default;
import org.apache.openejb.testing.EnableServices;
import org.apache.openejb.testing.Jars;
import org.apache.openejb.testing.RandomPort;
import org.apache.openejb.testing.SimpleLog;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.inject.OWBInjector;
import org.junit.rules.MethodRule;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import java.lang.reflect.Field;
import java.net.URL;
import java.util.List;

// goal is to share the same container for all embedded tests and hold the config there
// only works if all tests use the same config
public class SingleContainerRunner extends BlockJUnit4ClassRunner {
    private static volatile boolean started = false;
    private static final App APP = new App();

    public SingleContainerRunner(final Class<?> klass) throws InitializationError {
        super(klass);
    }

    @Override
    protected List<MethodRule> rules(final Object test) {
        final List<MethodRule> rules = super.rules(test);
        rules.add((base, method, target) -> new Statement() {
            @Override
            public void evaluate() throws Throwable {
                if (!started) {
                    started = true;
                    new ApplicationComposers(App.class) {
                        @Override
                        public void deployApp(final Object inputTestInstance) throws Exception {
                            super.deployApp(inputTestInstance);
                            final ThreadContext previous = ThreadContext.getThreadContext(); // dont here for logging
                            final ApplicationComposers comp = this;
                            Runtime.getRuntime().addShutdownHook(new Thread() {
                                @Override
                                public void run() {
                                    try {
                                        comp.after();
                                    } catch (final Exception e) {
                                        ThreadContext.exit(previous);
                                        throw new IllegalStateException(e);
                                    }
                                }
                            });
                        }
                    }.before(APP);
                }
                OWBInjector.inject(WebBeansContext.currentInstance().getBeanManagerImpl(), target, null);
                customInject(target);
                base.evaluate();
            }
        });
        return rules;
    }

    private void customInject(final Object target) throws IllegalAccessException {
        final Class<?> aClass = target.getClass();
        for (final Field f : aClass.getDeclaredFields()) {
            if (f.isAnnotationPresent(RandomPort.class)) {
                f.setAccessible(true);
                f.set(target, APP.base);
            }
        }
        final Class<?> superclass = aClass.getSuperclass();
        if (superclass != Object.class) {
            customInject(superclass);
        }
    }

    @Default
    @SimpleLog
    @Jars("deltaspike-")
    @PersistenceUnitDefinition
    @EnableServices(jaxrs = true)
    @RunWith(SingleContainerRunner.class)
    @Classes(cdi = true, context = "jwp")
    @ContainerProperties({
        @ContainerProperties.Property(name = "jwp", value = "new://Resource?type=DataSource"),
        @ContainerProperties.Property(name = "jwp.JdbcDriver", value = "org.h2.Driver"),
        @ContainerProperties.Property(name = "jwp.JdbcUrl", value = "jdbc:h2:mem:jwp")
        //,@Property(name = "jwp.LogSql", value = "true") // to dump sal queries
    })
    public static class App {
        @RandomPort("http")
        private URL base;
    }
}
