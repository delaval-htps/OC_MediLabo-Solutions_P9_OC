package com.medilabosolutions.controller;

import static org.mockito.Mockito.when;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import reactor.core.publisher.Mono;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(value = "test")
class RiskControllerTest {


    @MockBean // to mock loadbalanced webclient builder in RiskController
    private WebClient.Builder mockWebClientBuilder;
  

    // to mock webclient for request to patient and to note in RiskController
    private WebClient mockWebClientPatient;
    private WebClient mockWebClientNote;

    @Mock // to change the return of exchangeToMono() in each webclient of RiskController
    private ExchangeFunction exchangeFunction;

    // to call directly endpoint of RiskController
    @Autowired
    private WebTestClient webTestClient;

    @Test
    void getPatientAssessment_WhenPatientNotExists() {

        // build mockWebClient for patient service to return a clientResponse with status NOT_FOUND
        mockWebClientPatient = WebClient.builder()
                .exchangeFunction(clientRequest -> Mono.just(ClientResponse.create(HttpStatus.NOT_FOUND).build())).build();

        // mock loadbalanced webclient to mockWebClient
        when(mockWebClientBuilder.baseUrl(Mockito.anyString())).thenReturn(mockWebClientBuilder);
        when(mockWebClientBuilder.build()).thenReturn(mockWebClientPatient);

        webTestClient.get().uri("/risks/diabetes_assessment/patient_id/{patient_id}", 1000)
                .exchange().expectStatus().isNotFound();
    }

    @Test
    void getPatientAssessment_WhenPatientExists() {

        // build mockWebClient for patient service to return a clientResponse with status NOT_FOUND
        mockWebClientPatient = WebClient.builder()
                .exchangeFunction(clientRequest -> Mono.just(ClientResponse.create(HttpStatus.OK).build())).build();

        // mock loadbalanced webclient to mockWebClient
        when(mockWebClientBuilder.baseUrl(Mockito.anyString())).thenReturn(mockWebClientBuilder);
        when(mockWebClientBuilder.build()).thenReturn(mockWebClientPatient);

        webTestClient.get().uri("/risks/diabetes_assessment/patient_id/{patient_id}", 3)
                .exchange().expectStatus().isOk();
    }

    @Test
    void getPatientAssessment_WhenErrorRiskService() {

        // build mockWebClient for patient service to return a clientResponse with status NOT_FOUND
        mockWebClientPatient = WebClient.builder()
                .exchangeFunction(clientRequest -> Mono.just(ClientResponse.create(HttpStatus.BAD_REQUEST).build())).build();

        // mock loadbalanced webclient to mockWebClient
        when(mockWebClientBuilder.baseUrl(Mockito.anyString())).thenReturn(mockWebClientBuilder);
        when(mockWebClientBuilder.build()).thenReturn(mockWebClientPatient);

        webTestClient.get().uri("/risks/diabetes_assessment/patient_id/{patient_id}", 3)
                .exchange().expectStatus().is4xxClientError();
    }


    @Test
    void getPatientAssessment_WhenNoteServiceReturnError() throws JsonProcessingException {

        // build mockWebClient for patient service to return a clientResponse with status NOT_FOUND
        Mono<ClientResponse> noteResponse =
                Mono.just(ClientResponse.create(HttpStatus.BAD_REQUEST)
                // .header("content", "application/json")
                //         .body(objectMapper.writeValueAsString(AssessmentDto.builder()
                //                 .patientId(1L)
                //                 .expectedRisk(ExpectedRisk.BORDERLINE.getAbreviation())
                //                 .countTriggers(4).build()))
                        .build());

        mockWebClientNote = WebClient.builder()
                .exchangeFunction(clientRequest -> noteResponse).build();
        mockWebClientPatient = WebClient.builder()
                .exchangeFunction(clientRequest -> mockWebClientNote.get().uri("lb://PATIENT-SERVICE").exchangeToMono(Mockito.any())).build();

        // mock loadbalanced webclient to mockWebClient
        when(mockWebClientBuilder.baseUrl("lb://PATIENT-SERVICE")).thenReturn(mockWebClientBuilder);
        when(mockWebClientBuilder.build()).thenReturn(mockWebClientPatient);
        when(mockWebClientBuilder.baseUrl("lb://NOTE-SERVICE")).thenReturn(mockWebClientBuilder);
        when(mockWebClientBuilder.build()).thenReturn(mockWebClientNote);

        webTestClient.get().uri("/risks/diabetes_assessment/patient_id/{patient_id}", 1L)
                .exchange().expectStatus().is4xxClientError();
    }

    @Test
    void getPatientAssessment_WhenNoteServiceReturnAssessmentDto() throws JsonProcessingException {

        // build mockWebClient for patient service to return a clientResponse with status NOT_FOUND
        Mono<ClientResponse> noteResponse =
                Mono.just(ClientResponse.create(HttpStatus.OK)
                // .header("content", "application/json")
                //         .body(objectMapper.writeValueAsString(AssessmentDto.builder()
                //                 .patientId(1L)
                //                 .expectedRisk(ExpectedRisk.BORDERLINE.getAbreviation())
                //                 .countTriggers(4).build()))
                        .build());

        mockWebClientNote = WebClient.builder()
                .exchangeFunction(clientRequest -> noteResponse).build();
        // mockWebClientPatient = WebClient.builder()
        //         .exchangeFunction(clientRequest -> mockWebClientNote.get().uri(Mockito.anyString()).exchangeToMono(Mockito.any())).build();

        // mock loadbalanced webclient to mockWebClient
        when(mockWebClientBuilder.baseUrl("lb://PATIENT-SERVICE")).thenReturn(mockWebClientBuilder);
        when(mockWebClientBuilder.build()).thenReturn(mockWebClientPatient);
        when(mockWebClientBuilder.baseUrl("lb://NOTE-SERVICE")).thenReturn(mockWebClientBuilder);
        when(mockWebClientBuilder.build()).thenReturn(mockWebClientNote);

        webTestClient.get().uri("/risks/diabetes_assessment/patient_id/{patient_id}", 1L)
                .exchange().expectStatus().isOk();
    }
}
