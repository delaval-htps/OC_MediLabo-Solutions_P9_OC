package com.medilabosolutions.dto;

import lombok.Data;

@Data
public class NoteDto {
   

    private String date;
    private String content;
    
    private PatientDataDto patient;
}
