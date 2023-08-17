package com.medilabosolutions.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import com.medilabosolutions.model.Patient;
import com.medilabosolutions.repository.PatientRepository;
import com.medilabosolutions.service.PatientService;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class PatientControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private PatientService patientService;

    @Autowired
    private PatientRepository patientRepository;

    @Test
    void testGetAllPatients() {
        webTestClient.get().uri("/patients").exchange().expectStatus().isOk()
                .expectBodyList(Patient.class)
                .value(patients -> patients.size(), Matchers.equalTo(4))
                .value(patients -> patients.get(0).getLastName(), Matchers.equalTo("TestNone"))
                .value(patients -> patients.get(1).getLastName(),
                        Matchers.equalTo("TestBorderline"))
                .value(patients -> patients.get(2).getLastName(), Matchers.equalTo("TestInDanger"))
                .value(patients -> patients.get(3).getLastName(),
                        Matchers.equalTo("TestEarlyOnset"));
    }


    @Test
    void testGetPatientById() {
        webTestClient.get().uri("/patients/{id}", 1).exchange().expectStatus().isOk()
                .expectBodyList(Patient.class)
                .value(patients -> patients.size(), Matchers.equalTo(1))
                .value(patients -> patients.get(0).getLastName(), Matchers.equalTo("TestNone"))
                .value(patients -> patients.get(0).getFirstName(), Matchers.equalTo("Test"))
                .value(patients -> patients.get(0).getDateOfBirth(), Matchers.is(LocalDate.parse("1966-12-31",DateTimeFormatter.ISO_DATE)))
                .value(patients -> patients.get(0).getGenre(), Matchers.equalTo("F"))
                .value(patients -> patients.get(0).getAddress(), Matchers.equalTo("1 Brookside St"))
                .value(patients -> patients.get(0).getPhoneNumber(),
                        Matchers.equalTo("100-222-3333"));

    }

    @Test
    void testCreatePatient() {

    }

   



    @Test
    void testUpdatePatient() {

    }

     @Test
    void testDeletePatient() {

    }
}
