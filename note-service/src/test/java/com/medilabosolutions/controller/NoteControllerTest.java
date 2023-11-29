package com.medilabosolutions.controller;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import com.medilabosolutions.configuration.ConfigNoteService;
import com.medilabosolutions.dto.NoteDto;
import com.medilabosolutions.dto.PatientDataDto;
import com.medilabosolutions.repository.NoteRepository;
import lombok.extern.slf4j.Slf4j;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(value = ConfigNoteService.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Slf4j
class NoteControllerTest {

        @Autowired
        private WebTestClient webTestClient;



        @Autowired
        private NoteRepository noteRepository;

        
        private static final String MOCK_NOTE_ID = "testId";

        @AfterAll
        public void closeDb() {
        noteRepository.deleteAll().subscribe();
        }

        @Test
        @Order(1)
        void testGetAllNotes() {

                webTestClient.get().uri("/notes").exchange().expectStatus().isOk()
                                .expectBodyList(NoteDto.class)
                                .value(notes -> notes.get(0).getId(), Matchers.notNullValue())
                                .value(notes -> notes.get(0).getDate(), Matchers.notNullValue())
                                .value(notes -> notes.get(0).getContent(), Matchers.equalToIgnoringCase("Le patient déclare qu'il 'se sent très bien'"))
                                .value(notes -> notes.get(0).getPatient(), Matchers.notNullValue())
                                .value(notes -> notes.get(0).getPatient().getId(), Matchers.equalTo(1L))
                                .value(notes -> notes.get(0).getPatient().getName(), Matchers.equalTo("TestName"));
        }


        @Test
        @Order(2)
        void testGetNoteById() {

                log.info("mockNoteId = {}", MOCK_NOTE_ID);
                webTestClient.get().uri("/notes/{id}", MOCK_NOTE_ID).exchange().expectStatus().isOk()
                                .expectBodyList(NoteDto.class)
                                .value(notes -> notes.get(0).getDate(), Matchers.notNullValue())
                                .value(notes -> notes.get(0).getContent(), Matchers.equalToIgnoringCase("Le patient déclare qu'il 'se sent très bien'"))
                                .value(notes -> notes.get(0).getPatient(), Matchers.notNullValue())
                                .value(notes -> notes.get(0).getPatient().getId(), Matchers.equalTo(1L))
                                .value(notes -> notes.get(0).getPatient().getName(), Matchers.equalTo("TestName"));

        }

        @Test
        @Order(3)
        void testGetNoteById_whenIdNotExisted_thenReturnNotFoundException() {

                webTestClient.get().uri("/notes/{id}", "IdNotExisted").exchange()
                                .expectStatus().isNotFound().expectBody()
                                .jsonPath("$.length()").isEqualTo(6)
                                .jsonPath("$.type").isEqualTo("http://medilabosolutions/")
                                .jsonPath("$.title").isEqualTo("404-Note not found")
                                .jsonPath("$.status").isEqualTo("404")
                                .jsonPath("$.detail").isEqualTo("Note with id: IdNotExisted was not found")
                                .jsonPath("$.instance").isEqualTo("/notes/IdNotExisted")
                                .jsonPath("$.properties.timeStamp").isNotEmpty()
                                .jsonPath("$.properties.requestId").isNotEmpty();
        }


        @Test
        @Order(4)
        void testCreateNote() {
                NoteDto noteToSave = NoteDto.builder()
                                .date("2023-11-27 12:00:00")
                                .content("test content")
                                .patient(PatientDataDto.builder().id(1L).name("testName").build())
                                .build();

                webTestClient.post().uri("/notes").bodyValue(noteToSave).exchange()
                                .expectStatus().isCreated()
                                .expectBody(NoteDto.class)
                                .value(n -> n.getDate(), Matchers.equalTo("2023-11-27T12:00"))
                                .value(n -> n.getContent(), Matchers.equalTo(noteToSave.getContent()))
                                .value(n -> n.getPatient().getName(), Matchers.equalTo(noteToSave.getPatient().getName()))
                                .value(n -> n.getPatient().getId(), Matchers.equalTo(1L));
        }

        @Test
        @Order(5)
        void testCreateNoteById_whenInvalidInputData_thenReturnInvalidFields() {

                NoteDto notebody = NoteDto.builder()
                                .date("2023-11-27 12:00")// date doesn't match with CustomDateValidator
                                .content("coucou")
                                .patient(PatientDataDto.builder().id(1L).name("testName").build())
                                .build();

                webTestClient.post().uri("/notes").contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(notebody).exchange()
                                .expectStatus().isBadRequest().expectBody()
                                .jsonPath("$.length()").isEqualTo(6)
                                .jsonPath("$.type").isEqualTo("http://medilabosolutions/")
                                .jsonPath("$.title").isEqualTo("400-Invalid fields: ")
                                .jsonPath("$.status").isEqualTo("400")
                                .jsonPath("$.instance").isEqualTo("/notes")
                                .jsonPath("$.properties.timeStamp").isNotEmpty()
                                .jsonPath("$.properties.requestId").isNotEmpty();
        }



        @Test
        @Order(6)
        void testUpdateNote_whenExistingId_thenUpdateAndReturnUpdatedNote() {

                NoteDto updatedNote = NoteDto.builder()
                                .date("2023-11-27 12:00:00")
                                .content("updated content")
                                .patient(PatientDataDto.builder().id(1L).name("updatedName").build())
                                .build();

                webTestClient.put().uri("/notes/{id}", MOCK_NOTE_ID).bodyValue(updatedNote).exchange()
                                .expectStatus().isOk()
                                .expectBody(NoteDto.class)
                                .value(n -> n.getDate(), Matchers.equalTo("2023-11-27T12:00"))
                                .value(n -> n.getContent(), Matchers.equalTo(updatedNote.getContent()))
                                .value(n -> n.getPatient().getName(), Matchers.equalTo(updatedNote.getPatient().getName()))
                                .value(n -> n.getPatient().getId(), Matchers.equalTo(1L));
        }


        @Test
        @Order(7)
        void testUpdateNote_whenNonExistingId_thenReturnNotFoundException() {
                NoteDto updatedNote = NoteDto.builder()
                                .date("2023-11-27 12:00:00")
                                .content("updated content")
                                .patient(PatientDataDto.builder().id(1L).name("updatedName").build())
                                .build();
                webTestClient.put().uri("/notes/{id}", "IdNotExisted").bodyValue(updatedNote).exchange()
                                .expectStatus().isNotFound()
                                .expectBody()
                                .jsonPath("$.length()").isEqualTo(6)
                                .jsonPath("$.type").isEqualTo("http://medilabosolutions/")
                                .jsonPath("$.title").isEqualTo("404-Note not found")
                                .jsonPath("$.status").isEqualTo("404")
                                .jsonPath("$.detail").isEqualTo("Note with id: IdNotExisted was not found")
                                .jsonPath("$.instance").isEqualTo("/notes/IdNotExisted")
                                .jsonPath("$.properties.timeStamp").isNotEmpty()
                                .jsonPath("$.properties.requestId").isNotEmpty();
        }



        @Test
        @Order(6)
        void testDeleteNote_whenExistingId_thenDeleteAndReturnDeletedNote() {
                webTestClient.delete().uri("/notes/{id}", MOCK_NOTE_ID).exchange()
                                .expectStatus().isOk()
                                .expectBody(NoteDto.class)
                                .value(n -> n.getId(), Matchers.equalTo(MOCK_NOTE_ID))
                                .value(n -> n.getDate(), Matchers.equalTo("2023-11-27T12:00"))
                                .value(n -> n.getContent(), Matchers.equalTo("updated content"))
                                .value(n -> n.getPatient().getName(), Matchers.equalTo("updatedName"))
                                .value(n -> n.getPatient().getId(), Matchers.equalTo(1L));
        }

        @Test
        @Order(7)
        void testDeleteNote_whenNonExistingId_thenReturnNotFound() {
                webTestClient.delete().uri("/notes/{id}", "IdNotExisted").exchange()
                                .expectStatus().isNotFound()
                                .expectBody()
                                .jsonPath("$.length()").isEqualTo(6)
                                .jsonPath("$.type").isEqualTo("http://medilabosolutions/")
                                .jsonPath("$.title").isEqualTo("404-Note not found")
                                .jsonPath("$.status").isEqualTo("404")
                                .jsonPath("$.detail").isEqualTo("Note with id: IdNotExisted was not found")
                                .jsonPath("$.instance").isEqualTo("/notes/IdNotExisted")
                                .jsonPath("$.properties.timeStamp").isNotEmpty()
                                .jsonPath("$.properties.requestId").isNotEmpty();
        }
}
