package com.medilabosolutions.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medilabosolutions.model.Note;
import com.medilabosolutions.repository.NoteRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ImportJsonService {

    private final NoteRepository noteRepository;
    private final ObjectMapper objectMapper;

    private List<Note> generateNotes(File jsonFile) {
        List<Note> notes = new ArrayList<>();

        try {
            notes = objectMapper.readValue(jsonFile, new TypeReference<List<Note>>() {});
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return notes;
    }

    private int insertInto(List<Note> notes) {
        int inserts = 0;
        for (Note note : notes) {
            noteRepository.save(note).subscribe();
            inserts++;
        }
        return inserts;
    }

    public String importTo(File jsonFile) {
        List<Note> notes = generateNotes(jsonFile);
        int inserts = insertInto(notes);
        return inserts + "/" + notes.size();
    }
}
