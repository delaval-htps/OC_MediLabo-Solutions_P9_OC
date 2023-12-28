package com.medilabosolutions.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import com.medilabosolutions.dto.AssessmentDto;
import com.medilabosolutions.dto.PatientDto;
import com.medilabosolutions.model.ExpectedRisk;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {DiabetesRiskService.class})
class DiabetesRiskServiceTest {

    @Autowired
    private DiabetesRiskService cut;


    @Test
    void expectedRiskAccordingToGenderAndAge_WhenNoTermTriggers() {
        int countTermTriggers = 0;
        ExpectedRisk result = cut.expectedRiskAccordingToGenderAndAge("M", 30, countTermTriggers);
        assertEquals(ExpectedRisk.NONE, result);
    }

    @Test
    void expectedRiskAccordingToGenderAndAge_WhenOneTermTriggersAndOlderThan30() {
        int countTermTriggers = 1;
        ExpectedRisk result = cut.expectedRiskAccordingToGenderAndAge("M", 31, countTermTriggers);
        assertEquals(ExpectedRisk.NONE, result);
    }

    @ParameterizedTest
    @ValueSource(ints = {2, 3, 4, 5})
    void expectedRiskAccordingToGenderAndAge_WhenOlderThan30AndCountTermTriggerBetweenTwoAndFive(int countTermTriggers) {
        ExpectedRisk result = cut.expectedRiskAccordingToGenderAndAge("M", 31, countTermTriggers);
        assertEquals(ExpectedRisk.BORDERLINE, result);
    }

    @ParameterizedTest
    @ValueSource(ints = {6, 7})
    void expectedRiskAccordingToGenderAndAge_WhenOlderThan30AndCountTermTriggerBetweenSixAndSeven(int countTermTriggers) {
        ExpectedRisk result = cut.expectedRiskAccordingToGenderAndAge("M", 31, countTermTriggers);
        assertEquals(ExpectedRisk.INDANGER, result);
    }

    @Test
    void expectedRiskAccordingToGenderAndAge_WhenUpToSevenTermTriggersAndOlderThan30() {
        int countTermTriggers = 8;
        ExpectedRisk result = cut.expectedRiskAccordingToGenderAndAge("M", 31, countTermTriggers);
        assertEquals(ExpectedRisk.EARLYONSET, result);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2})
    void expectedRiskAccordingToGenderAndAge_WhenMaleYoungerThan30AndCountTermTriggerBetweenOneAndTwo(int countTermTriggers) {
        ExpectedRisk result = cut.expectedRiskAccordingToGenderAndAge("M", 30, countTermTriggers);
        assertEquals(ExpectedRisk.NONE, result);
    }

    @ParameterizedTest
    @ValueSource(ints = {3, 4})
    void expectedRiskAccordingToGenderAndAge_WhenMaleYoungerThan30AndCountTermTriggerBetweenThreeAndFour(int countTermTriggers) {
        ExpectedRisk result = cut.expectedRiskAccordingToGenderAndAge("M", 30, countTermTriggers);
        assertEquals(ExpectedRisk.INDANGER, result);
    }


    @Test
    void expectedRiskAccordingToGenderAndAge_WhenMaleYoungerThan30AndCountTermTriggerUpToFour() {
        int countTermTriggers = 5;
        ExpectedRisk result = cut.expectedRiskAccordingToGenderAndAge("M", 30, countTermTriggers);
        assertEquals(ExpectedRisk.EARLYONSET, result);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3})
    void expectedRiskAccordingToGenderAndAge_WhenFemaleYoungerThan30AndCountTermTriggerBetweenOneAndThree(int countTermTriggers) {
        ExpectedRisk result = cut.expectedRiskAccordingToGenderAndAge("F", 30, countTermTriggers);
        assertEquals(ExpectedRisk.NONE, result);
    }

    @ParameterizedTest
    @ValueSource(ints = {4, 5, 6})
    void expectedRiskAccordingToGenderAndAge_WhenFemaleYoungerThan30AndCountTermTriggerBetweenFourAndSix(int countTermTriggers) {
        ExpectedRisk result = cut.expectedRiskAccordingToGenderAndAge("F", 30, countTermTriggers);
        assertEquals(ExpectedRisk.INDANGER, result);
    }


    @Test
    void expectedRiskAccordingToGenderAndAge_WhenFemaleYoungerThan30AndCountTermTriggerUpToSix() {
        int countTermTriggers = 7;
        ExpectedRisk result = cut.expectedRiskAccordingToGenderAndAge("F", 30, countTermTriggers);
        assertEquals(ExpectedRisk.EARLYONSET, result);
    }

    @Test
    void riskAssessment() {
        PatientDto patientDto = PatientDto.builder()
                .id(1L)
                .dateOfBirth(LocalDate.of(1976, 12, 27))
                .firstName("Dorian")
                .lastName("DELAVAL").genre("M").build();
        int countTermTriggers = 5;

        AssessmentDto result = cut.riskAssessment(patientDto, countTermTriggers);

        assertEquals(1L, result.getPatientId());
        assertEquals("Borderline", result.getExpectedRisk());
        assertEquals(5, result.getCountTriggers());
    }

}
