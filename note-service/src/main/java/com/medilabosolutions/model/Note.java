package com.medilabosolutions.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "notes")
public class Note {

    @Id
    private String id;

    private String date;
    private String content;
    
    private PatientData patient;

}
