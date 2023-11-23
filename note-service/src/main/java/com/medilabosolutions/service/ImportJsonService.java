package com.medilabosolutions.service;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medilabosolutions.model.Note;
import com.medilabosolutions.repository.NoteRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ImportJsonService {
   
    private final NoteRepository noteRepository;

    private List<Note> generateNotes(List<String> lines) {

        ObjectMapper mapper = new ObjectMapper();
        List<Note> notes = new ArrayList<>();

        for (String json : lines) {
            try {
                Note readValueNote = mapper.readValue(json, Note.class);
                notes.add(readValueNote);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
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

    public String importTo(List<String> jsonLines) {
        List<Note> notes = generateNotes(jsonLines);
        int inserts = insertInto(notes);
        return inserts + "/" + jsonLines.size();
    }
}
