package com.medilabosolutions.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.medilabosolutions.validation.CustomDateFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class NoteDto {


    @NotNull(message = "date must be not null")
    @CustomDateFormat
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private String date;

    @NotBlank(message = "content must be not null or blank")
    private String content;

    @Valid
    private PatientDataDto patient;
}
