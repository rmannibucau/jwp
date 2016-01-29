package com.github.rmannibucau.jwp.runner;

import org.apache.openejb.api.configuration.PersistenceUnitDefinition;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.ContainerProperties;
import org.apache.openejb.testing.Default;
import org.apache.openejb.testing.EnableServices;
import org.apache.openejb.testing.Jars;
import org.apache.openejb.testing.RandomPort;
import org.apache.openejb.testing.SimpleLog;
import org.junit.runner.RunWith;

import java.net.URL;

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
@Application
public class AppDescriptor {
    @RandomPort("http")
    private URL base;
}
