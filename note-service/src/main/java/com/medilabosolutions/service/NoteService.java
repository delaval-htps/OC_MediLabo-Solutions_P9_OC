package com.medilabosolutions.service;

import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import com.medilabosolutions.model.Note;
import com.medilabosolutions.repository.NoteRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@RequiredArgsConstructor
@Service
public class NoteService {

    private final NoteRepository noteRepository;

    public Flux<Note> getAllNotes() {
        return noteRepository.findAll();
    }

    public Mono<Note> findById(String id) {
        return noteRepository.findById(new ObjectId(id));
    }

    public Mono<Note> createNote(Note note) {
        return noteRepository.save(note);
    }

    public Mono<Note> updateNote(String id, Note note) {
        return noteRepository.findById(new ObjectId(id)).flatMap(n -> {
            n.setDate(note.getDate());
            n.setPatient(note.getPatient());
            n.setContent(note.getContent());
            return noteRepository.save(note);
        });
    }

    public Mono<Note> deleteNote(String id) {
        return noteRepository.findById(new ObjectId(id))
                .flatMap(noteToDelete -> noteRepository.delete(noteToDelete)
                        .then(Mono.just(noteToDelete)));
    }
}
