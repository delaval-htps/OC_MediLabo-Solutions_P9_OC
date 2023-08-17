package com.medilabosolutions;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import com.medilabosolutions.configuration.ConfigTestR2dbc;

@DataR2dbcTest
@ActiveProfiles("test")
@Import(ConfigTestR2dbc.class)
public class PatientServiceApplicationTest {

    @Test
    public void contextLoads() throws InterruptedException {
     
    }
}
