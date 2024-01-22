package com.medilabosolutions.service;

import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import com.medilabosolutions.model.Note;
import com.medilabosolutions.model.PatientData;
import com.medilabosolutions.repository.NoteRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(SpringExtension.class)
public class NoteServiceTest {
    @Mock
    private NoteRepository noteRepository;
    @InjectMocks
    private NoteService cut;

    private static Note mockNote1;
    private static Note mockNoteToUpdate;
    private static Note mockNote2;
    private static Note mockNoteToDelete;

    @BeforeAll
    public static void setUp() {

        mockNote1 = Note.builder()
                .id("mocknote1")
                .content("note1")
                .date(LocalDateTime.now())
                .patient(PatientData.builder()
                        .name("patient_mocknote1")
                        .id((long) 1).build())
                .build();

        mockNoteToUpdate = mockNote1;

        mockNote2 = Note.builder()
                .id("mocknote2")
                .content("note2")
                .date(LocalDateTime.now())
                .patient(PatientData.builder()
                        .name("patient_mocknote2")
                        .id((long) 2).build())
                .build();

        mockNoteToDelete = mockNote2;
    }

    @Test
    void findAll() {
        // given
        Mockito.when(noteRepository.findAll()).thenReturn(Flux.just(mockNote1, mockNote2));
        // when and then
        StepVerifier.create(cut.getAllNotes())
                .expectNext(mockNote1, mockNote2)
                .expectComplete()
                .verify();
    }

     @Test
        void findById() {

                Mockito.when(noteRepository.findById(Mockito.anyString()))
                                .thenReturn(Mono.just(mockNote1));

                StepVerifier.create(cut.findById("mocknote1"))
                                .expectNext(mockNote1)
                                .expectComplete()
                                .verify();
        }

        @Test
        void createPatient() {

                Mockito.when(noteRepository.save(Mockito.any(Note.class)))
                                .thenReturn(Mono.just(mockNote1));

                StepVerifier.create(cut.createNote(mockNote1))
                                .expectNext(mockNote1)
                                .expectComplete()
                                .verify();
        }

        @Test
        void updatePatient() {

                Mockito.when(noteRepository.findById(Mockito.anyString()))
                                .thenReturn(Mono.just(mockNoteToUpdate));

                Mockito.when(noteRepository.save(Mockito.any(Note.class)))
                                .thenReturn(Mono.just(mockNote2));

                StepVerifier.create(cut.updateNote("mocknote1", mockNote2))
                                .expectNext(mockNote2)
                                .expectComplete()
                                .verify();
        }


        @Test
        void deleteById() {
                Mockito.when(noteRepository.findById(Mockito.anyString()))
                                .thenReturn(Mono.just(mockNoteToDelete));

                Mockito.when(noteRepository.delete(Mockito.any(Note.class)))
                                .thenReturn(Mono.empty());

                StepVerifier.create(cut.deleteNote("mocknote2")).expectNext(mockNote2)
                                .expectComplete().verify();

        }

}
