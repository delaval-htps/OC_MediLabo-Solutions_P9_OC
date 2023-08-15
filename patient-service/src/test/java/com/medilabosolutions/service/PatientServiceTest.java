package com.medilabosolutions.service;

import java.time.LocalDate;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import com.medilabosolutions.model.Patient;
import com.medilabosolutions.repository.PatientRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(SpringExtension.class)
public class PatientServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @InjectMocks
    private PatientService cut;

    private static Patient mockPatient1;
    private static Patient mockPatientToUpdate;
    private static Patient mockPatient2;
    private static Patient mockPatientToDelete;

    @BeforeAll
    public static void setUp() {

        mockPatient1 = Patient.builder()
                .id((long) 1)
                .firstName("patient1")
                .lastName("TestPatient1")
                .dateOfBirth(LocalDate.now())
                .genre("M")
                .build();

        mockPatientToUpdate = mockPatient1;

        mockPatient2 = Patient.builder()
                .id((long) 2)
                .firstName("patient2")
                .lastName("TestPatient2")
                .dateOfBirth(LocalDate.now())
                .genre("F")
                .build();

        mockPatientToDelete = mockPatient2;
    }

    @Test
    public void findAll() {
        // given
        Mockito.when(patientRepository.findAll())
                .thenReturn(Flux.just(mockPatient1, mockPatient2));
        // when and then
        StepVerifier.create(cut.findAll())
                .expectNext(mockPatient1, mockPatient2)
                .expectComplete()
                .verify();
    }

    @Test
    public void findById() {

        Mockito.when(patientRepository.findById(Mockito.anyLong()))
                .thenReturn(Mono.just(mockPatient1));

        StepVerifier.create(cut.findById((long) 1))
                .expectNext(mockPatient1)
                .expectComplete()
                .verify();
    }

    @Test
    public void createPatient() {

        Mockito.when(patientRepository.save(Mockito.any(Patient.class)))
                .thenReturn(Mono.just(mockPatient1));

        StepVerifier.create(cut.createPatient(mockPatient1))
                .expectNext(mockPatient1)
                .expectComplete()
                .verify();
    }

    @Test
    public void updatePatient() {

        Mockito.when(patientRepository.findById(Mockito.anyLong()))
                .thenReturn(Mono.just(mockPatientToUpdate));

        Mockito.when(patientRepository.save(Mockito.any(Patient.class)))
                .thenReturn(Mono.just(mockPatient2));

        StepVerifier.create(cut.updatePatient((long) 1, mockPatient2))
                .expectNext(mockPatient2)
                .expectComplete()
                .verify();
    }


    @Test
    public void deleteById() {
        Mockito.when(patientRepository.findById(Mockito.anyLong()))
                .thenReturn(Mono.just(mockPatientToDelete));

        Mockito.when(patientRepository.delete(Mockito.any(Patient.class)))
        .thenReturn(Mono.empty());

        StepVerifier.create(cut.deleteById((long) 1)).expectNext(mockPatient2)
                .expectComplete().verify();
    
    }
}
