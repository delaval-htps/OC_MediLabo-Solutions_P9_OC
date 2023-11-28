package com.medilabosolutions.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.hamcrest.Matchers;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import com.medilabosolutions.model.Patient;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PatientControllerTest {

        @Autowired
        private WebTestClient webTestClient;

        @SpyBean
        private ModelMapper modelMapper;

        @Test
        @Order(1)
        void testGetAllPatients() {
                webTestClient.get().uri("/patients").exchange().expectStatus().isOk()
                                .expectBodyList(Patient.class)
                                .value(patients -> patients.size(), Matchers.equalTo(4))
                                .value(patients -> patients.get(0).getLastName(),
                                                Matchers.equalTo("TestNone"))
                                .value(patients -> patients.get(1).getLastName(),
                                                Matchers.equalTo("TestBorderline"))
                                .value(patients -> patients.get(2).getLastName(),
                                                Matchers.equalTo("TestInDanger"))
                                .value(patients -> patients.get(3).getLastName(),
                                                Matchers.equalTo("TestEarlyOnset"));
        }


        @Test
        @Order(2)
        void testGetPatientById() {
                webTestClient.get().uri("/patients/{id}", 1).exchange().expectStatus().isOk()
                                .expectBodyList(Patient.class)
                                .value(patients -> patients.size(), Matchers.equalTo(1))
                                .value(patients -> patients.get(0).getLastName(),
                                                Matchers.equalTo("TestNone"))
                                .value(patients -> patients.get(0).getFirstName(),
                                                Matchers.equalTo("Test"))
                                .value(patients -> patients.get(0).getDateOfBirth(),
                                                Matchers.is(LocalDate.parse("1966-12-31",
                                                                DateTimeFormatter.ISO_DATE)))
                                .value(patients -> patients.get(0).getGenre(),
                                                Matchers.equalTo("F"))
                                .value(patients -> patients.get(0).getAddress(),
                                                Matchers.equalTo("1 Brookside St"))
                                .value(patients -> patients.get(0).getPhoneNumber(),
                                                Matchers.equalTo("100-222-3333"));

        }

        @Test
        @Order(3)
        void testGetPatientById_whenIdNotExisted_thenReturnNotFoundException() {

                webTestClient.get().uri("/patients/{id}", 10).exchange()
                                .expectStatus().isNotFound().expectBody()
                                .jsonPath("$.length()").isEqualTo(7)
                                .jsonPath("$.type").isEqualTo("http://medilabosolutions/")
                                .jsonPath("$.title").isEqualTo("404-Patient not found")
                                .jsonPath("$.status").isEqualTo("404")
                                .jsonPath("$.detail")
                                .isEqualTo("Patient with id: 10 was not found")
                                .jsonPath("$.instance").isEqualTo("/patients/10")
                                .jsonPath("$.timeStamp").isNotEmpty()
                                .jsonPath("$.requestId").isNotEmpty();
        }


        @Test
        @Order(4)
        void testCreatePatient() {
                Patient patientToSave = Patient.builder()
                                .lastName("Patient")
                                .firstName("TestPatientToSave")
                                .dateOfBirth(LocalDate.of(1976, 12, 27))
                                .genre("F")
                                .build();

                webTestClient.post().uri("/patients").bodyValue(patientToSave).exchange()
                                .expectStatus().isCreated()
                                .expectBody(Patient.class)
                                .value(p -> p.getLastName(),
                                                Matchers.equalTo(patientToSave.getLastName()))
                                .value(p -> p.getFirstName(),
                                                Matchers.equalTo(patientToSave.getFirstName()))
                                .value(p -> p.getDateOfBirth(),
                                                Matchers.equalTo(patientToSave.getDateOfBirth()))
                                .value(p -> p.getGenre(),
                                                Matchers.equalTo(patientToSave.getGenre()));
        }

        @Test
        @Order(5)
        void testCreatePatientById_whenInvalidInputData_thenReturnInvalidFields()
                        throws JSONException {

                String jsonInputString =
                                "{\"lastName\":\"test\",\"firstName\":\"test\",\"genre\":\"\",\"dateOfBirth\":\"2022-03-28\"}";

                JSONObject jsonInput = new JSONObject(jsonInputString);
                webTestClient.post().uri("/patients").contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(BodyInserters.fromValue(jsonInput)).exchange()
                                .expectStatus().isBadRequest().expectBody()
                                .jsonPath("$.length()").isEqualTo(8)
                                .jsonPath("$.type").isEqualTo("http://medilabosolutions/")
                                .jsonPath("$.title").isEqualTo("400-Invalid fields: ")
                                .jsonPath("$.status").isEqualTo("400")
                                .jsonPath("$.detail").value(Matchers.containsString("genre"))
                                .jsonPath("$.instance").isEqualTo("/patients")
                                .jsonPath("$.bindingResult").isNotEmpty()
                                .jsonPath("$.timeStamp").isNotEmpty()
                                .jsonPath("$.requestId").isNotEmpty();
        }



        @Test
        @Order(6)
        void testUpdatePatient_whenExistingId_thenUpdateAndReturnUpdatedPatient() {
                Patient updatedPatient = Patient.builder()
                                .lastName("Patient")
                                .firstName("TestUpdatedPatient")
                                .dateOfBirth(LocalDate.now().minusDays(1))
                                .genre("F")
                                .build();
                webTestClient.put().uri("/patients/{id}", 2).bodyValue(updatedPatient).exchange()
                                .expectStatus().isOk()
                                .expectBody(Patient.class)
                                .value(p -> p.getLastName(),
                                                Matchers.equalTo(updatedPatient.getLastName()))
                                .value(p -> p.getFirstName(),
                                                Matchers.equalTo(updatedPatient.getFirstName()))
                                .value(p -> p.getDateOfBirth(),
                                                Matchers.equalTo(updatedPatient.getDateOfBirth()))
                                .value(p -> p.getGenre(),
                                                Matchers.equalTo(updatedPatient.getGenre()));
        }


        @Test
        @Order(7)
        void testUpdatePatient_whenNonExistingId_thenReturnNotFoundException() {
                Patient updatedPatient = Patient.builder()
                                .lastName("Patient")
                                .firstName("TestUpdatedPatient")
                                .dateOfBirth(LocalDate.now().minusDays(1))
                                .genre("F")
                                .build();
                webTestClient.put().uri("/patients/{id}", 10).bodyValue(updatedPatient).exchange()
                                .expectStatus().isNotFound()
                                .expectBody()
                                .jsonPath("$.length()").isEqualTo(7)
                                .jsonPath("$.type").isEqualTo("http://medilabosolutions/")
                                .jsonPath("$.title").isEqualTo("404-Patient not found")
                                .jsonPath("$.status").isEqualTo("404")
                                .jsonPath("$.detail")
                                .isEqualTo("Patient with id: 10 was not found")
                                .jsonPath("$.instance").isEqualTo("/patients/10")
                                .jsonPath("$.timeStamp").isNotEmpty()
                                .jsonPath("$.requestId").isNotEmpty();
        }



        @Test
        @Order(6)
        void testDeletePatient_whenExistingId_thenDeleteAndReturnDeletedPatient() {
                webTestClient.delete().uri("/patients/{id}", 4).exchange()
                                .expectStatus().isOk()
                                .expectBody(Patient.class)
                                .value(deletedPatient -> deletedPatient.getLastName(),
                                                Matchers.equalTo("TestEarlyOnset"))
                                .value(deletedPatient -> deletedPatient.getFirstName(),
                                                Matchers.equalTo("Test"))
                                .value(deletedPatient -> deletedPatient.getDateOfBirth(),
                                                Matchers.is(LocalDate.parse("2002-06-28",
                                                                DateTimeFormatter.ISO_DATE)))
                                .value(deletedPatient -> deletedPatient.getGenre(),
                                                Matchers.equalTo("F"))
                                .value(deletedPatient -> deletedPatient.getAddress(),
                                                Matchers.equalTo("4 Valley Dr"))
                                .value(deletedPatient -> deletedPatient.getPhoneNumber(),
                                                Matchers.equalTo("400-555-6666"));
        }

        @Test
        @Order(7)
        void testDeletePatient_whenNonExistingId_thenReturnNotFound() {
                webTestClient.delete().uri("/patients/{id}", 10).exchange()
                                .expectStatus().isNotFound()
                                .expectBody()
                                .jsonPath("$.length()").isEqualTo(7)
                                .jsonPath("$.type").isEqualTo("http://medilabosolutions/")
                                .jsonPath("$.title").isEqualTo("404-Patient not found")
                                .jsonPath("$.status").isEqualTo("404")
                                .jsonPath("$.detail")
                                .isEqualTo("Patient with id: 10 was not found")
                                .jsonPath("$.instance").isEqualTo("/patients/10")
                                .jsonPath("$.timeStamp").isNotEmpty()
                                .jsonPath("$.requestId").isNotEmpty();
        }
}
