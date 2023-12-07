package com.medilabosolutions.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class NoteDto {

    private String id;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private String date;

    private String content;

    private PatientDataDto patient;
}
