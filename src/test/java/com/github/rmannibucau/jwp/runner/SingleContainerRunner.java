package com.github.rmannibucau.jwp.runner;

import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.testing.ApplicationComposers;
import org.apache.openejb.testing.RandomPort;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.inject.OWBInjector;
import org.apache.xbean.finder.AnnotationFinder;
import org.apache.xbean.finder.archive.FileArchive;
import org.junit.rules.MethodRule;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.apache.openejb.loader.JarLocation.jarLocation;

// goal is to share the same container for all embedded tests and hold the config there
// only works if all tests use the same config
public class SingleContainerRunner extends BlockJUnit4ClassRunner {
    private static volatile boolean started = false;
    private static final AtomicReference<Object> APP = new AtomicReference<>();
    private static final AtomicReference<Thread> HOOK = new AtomicReference<>();

    public static void setApp(final Object o) {
        APP.set(o);
    }

    public static void close() {
        final Thread hook = HOOK.get();
        if (hook != null) {
            hook.run();
            Runtime.getRuntime().removeShutdownHook(hook);
            HOOK.compareAndSet(hook, null);
            APP.set(null);
        }
    }

    public SingleContainerRunner(final Class<?> klass) throws InitializationError {
        super(klass);

        if (APP.get() == null) {
            final Class<?> type;
            final String typeStr = System.getProperty("tomee.application-composer.application");
            if (typeStr != null) {
                try {
                    type = Thread.currentThread().getContextClassLoader().loadClass(typeStr);
                } catch (final ClassNotFoundException e) {
                    throw new IllegalArgumentException(e);
                }
            } else {
                final Iterator<Class<?>> descriptors =
                    new AnnotationFinder(new FileArchive(Thread.currentThread().getContextClassLoader(), jarLocation(klass)), false)
                        .findAnnotatedClasses(Application.class).iterator();
                if (!descriptors.hasNext()) {
                    throw new IllegalArgumentException("No descriptor class using @Application");
                }
                type = descriptors.next();
                if (descriptors.hasNext()) {
                    throw new IllegalArgumentException("Ambiguous @Application: " + type + ", " + descriptors.next());
                }
            }
            try {
                APP.compareAndSet(null, type.newInstance());
            } catch (final InstantiationException | IllegalAccessException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    @Override
    protected List<MethodRule> rules(final Object test) {
        final List<MethodRule> rules = super.rules(test);
        rules.add((base, method, target) -> new Statement() {
            @Override
            public void evaluate() throws Throwable {
                start();
                OWBInjector.inject(WebBeansContext.currentInstance().getBeanManagerImpl(), target, null);
                composerInject(target);
                base.evaluate();
            }

            private void start() throws Exception {
                if (!started) {
                    final Object app = APP.get();
                    new ApplicationComposers(app.getClass()) {
                        @Override
                        public void deployApp(final Object inputTestInstance) throws Exception {
                            super.deployApp(inputTestInstance);
                            if (!started) {
                                final ThreadContext previous = ThreadContext.getThreadContext(); // dont here for logging
                                final ApplicationComposers comp = this;
                                final Thread hook = new Thread() {
                                    @Override
                                    public void run() {
                                        try {
                                            comp.after();
                                        } catch (final Exception e) {
                                            ThreadContext.exit(previous);
                                            throw new IllegalStateException(e);
                                        }
                                    }
                                };
                                HOOK.set(hook);
                                Runtime.getRuntime().addShutdownHook(hook);
                                started = true;
                            }
                        }
                    }.before(app);
                }
            }
        });
        return rules;
    }

    private void composerInject(final Object target) throws IllegalAccessException {
        final Object app = APP.get();
        final Class<?> aClass = target.getClass();
        for (final Field f : aClass.getDeclaredFields()) {
            if (f.isAnnotationPresent(RandomPort.class)) {
                for (final Field field : app.getClass().getDeclaredFields()) {
                    if (field.getType() ==  f.getType()) {
                        if (!field.isAccessible()) {
                            field.setAccessible(true);
                        }
                        if (!f.isAccessible()) {
                            f.setAccessible(true);
                        }

                        final Object value = field.get(app);
                        f.set(target, value);
                        break;
                    }
                }
            } else if (f.isAnnotationPresent(Application.class)) {
                if (!f.isAccessible()) {
                    f.setAccessible(true);
                }
                f.set(target, app);
            }
        }
        final Class<?> superclass = aClass.getSuperclass();
        if (superclass != Object.class) {
            composerInject(superclass);
        }
    }
}
