package com.medilabosolutions.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.r2dbc.connection.init.CompositeDatabasePopulator;
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer;
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator;
import io.r2dbc.spi.ConnectionFactory;

/**
 * class configuration to initialize database with script sql at start up of application
 */
@Configuration
@Profile("dev")
public class ConfigDevR2dbc {


    @Bean
   
    public ConnectionFactoryInitializer initializerDatabase(ConnectionFactory connectionFactory) {
        ConnectionFactoryInitializer initializer = new ConnectionFactoryInitializer();
        initializer.setConnectionFactory(connectionFactory);

        CompositeDatabasePopulator populator = new CompositeDatabasePopulator();
        populator.addPopulators(new ResourceDatabasePopulator(new ClassPathResource("schema-dev.sql"),
                new ClassPathResource("data-dev.sql")));
    initializer.setDatabasePopulator(populator);
        return initializer;
    }
   
}
