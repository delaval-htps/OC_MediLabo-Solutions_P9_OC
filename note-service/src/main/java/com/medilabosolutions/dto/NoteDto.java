package com.medilabosolutions.dto;

import java.time.LocalDateTime;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class NoteDto {
   

    @NotNull
    private LocalDateTime date;
    private String content;
    
    private PatientDataDto patient;
}
