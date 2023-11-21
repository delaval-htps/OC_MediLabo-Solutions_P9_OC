package com.medilabosolutions.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.medilabosolutions.model.Note;
import com.medilabosolutions.service.NoteService;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/notes")
public class NoteController {

    private final NoteService noteService;

    @GetMapping
    public ResponseEntity<Flux<Note>> getAllNotes() {
        return ResponseEntity.ok(noteService.getAllNotes());
    }
}
