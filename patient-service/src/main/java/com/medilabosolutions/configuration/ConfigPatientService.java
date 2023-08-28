package com.medilabosolutions.configuration;

import org.modelmapper.ModelMapper;
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
public class ConfigPatientService {

    /**
     * Bean creation use to create and populate datasource with sql scripts
     * 
     * @param connectionFactory factory to create a connexion with mysql driver
     * @return Bean of ConnectionFactoryInitializer to initialize database at bootstrap of
     *         application
     */
    @Bean
    public ConnectionFactoryInitializer initializerDatabase(ConnectionFactory connectionFactory) {

        ConnectionFactoryInitializer initializer = new ConnectionFactoryInitializer();
        initializer.setConnectionFactory(connectionFactory);

        CompositeDatabasePopulator populator = new CompositeDatabasePopulator();

        populator.addPopulators(
                new ResourceDatabasePopulator(new ClassPathResource("schema-dev.sql"),
                        new ClassPathResource("data-dev.sql")));
        initializer.setDatabasePopulator(populator);

        return initializer;
    }

    /**
     * Bean to use singleton of modelMapper by injection in application 
     * @return modelMapper to map entity to Dto and vice versa
     */
    @Bean
    public ModelMapper getModelMapper() {
        return new ModelMapper();
    }

}
