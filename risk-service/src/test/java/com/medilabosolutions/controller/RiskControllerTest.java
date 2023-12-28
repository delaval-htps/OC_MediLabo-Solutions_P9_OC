package com.medilabosolutions.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
 class RiskControllerTest {

    // @Autowired
    // private WebClient.Builder lbWebClientBuilder;

    // @Autowired
    // private DiabetesRiskService riskService;

    // @Autowired
    // private MessageSource messageSource;

    @Autowired
    private WebTestClient webTestClient;

    //  public static MockWebServer mockWebClientPatient;
    //  public static MockWebServer mockWebClientNote;

    // @BeforeAll
    // static void setUp() throws IOException {
    //     mockWebClientPatient = new MockWebServer();
    //     mockWebClientNote = new MockWebServer();
    //     mockWebClientPatient.start();
    //     mockWebClientNote.start();
    // }

    // @AfterAll
    // static void tearDown() throws IOException {
    //     mockWebClientPatient.shutdown();
    //     mockWebClientNote.shutdown();
    // }

 

    @Test
    void getPatientAssessment_WhenPatientNotExists() {
        webTestClient.get().uri("/risks/diabetes_assessment/patient_id/{patient_id}", 1000)
                .exchange().expectStatus().isNotFound();
    }
    @Test
    void getPatientAssessment_WhenPatientExists() {
        webTestClient.get().uri("/risks/diabetes_assessment/patient_id/{patient_id}", 3)
                .exchange().expectStatus().isOk();
    }
}
