package com.medilabosolutions.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medilabosolutions.dto.NoteDto;
import com.medilabosolutions.model.Note;
import com.medilabosolutions.repository.NoteRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ImportJsonService {

    private final NoteService noteService;
    private final ModelMapper modelMapper;
    private final ObjectMapper objectMapper;

    private List<NoteDto> generateNotes(File jsonFile) {
        List<NoteDto> notes = new ArrayList<>();

        try {
            notes = objectMapper.readValue(jsonFile, new TypeReference<List<NoteDto>>() {});
        } catch (IOException e) {
            e.printStackTrace();
        }
        return notes;
    
    }

    private int insertInto(List<NoteDto> notes) {
        int inserts = 0;
        for (NoteDto note : notes) {
            noteService.createNote(modelMapper.map(note, Note.class)).subscribe();
            inserts++;
        }
        return inserts;
    }

    public String importTo(File jsonFile) {
        List<NoteDto> notes = generateNotes(jsonFile);
        int inserts = insertInto(notes);
        return inserts + "/" + notes.size();
    }
}
