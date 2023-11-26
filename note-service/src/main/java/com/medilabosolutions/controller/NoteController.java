package com.medilabosolutions.controller;

import java.util.Locale;
import org.modelmapper.ModelMapper;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.medilabosolutions.dto.NoteDto;
import com.medilabosolutions.exception.NoteCreationException;
import com.medilabosolutions.exception.NoteNotFoundException;
import com.medilabosolutions.model.Note;
import com.medilabosolutions.service.NoteService;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/notes")
public class NoteController {

    private final NoteService noteService;
    private final ModelMapper modelMapper;
    private final MessageSource messageSource;

    private static final String NOT_FOUND = "note.not.found";
    @GetMapping
    public ResponseEntity<Flux<Note>> getAllNotes() {
        return ResponseEntity.ok(noteService.getAllNotes());
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<NoteDto>> getNoteById(@PathVariable("id") String id) {
        return noteService.findById(id)
                .map(note -> new ResponseEntity<NoteDto>(modelMapper.map(note, NoteDto.class), HttpStatus.OK))
                .switchIfEmpty(Mono.error(new NoteNotFoundException(messageSource.getMessage(NOT_FOUND, new Object[] {id}, Locale.ENGLISH))));
    }

    @PostMapping
    public Mono<ResponseEntity<NoteDto>> createNote(@RequestBody NoteDto noteToCreate) {
        return noteService.createNote(modelMapper.map(noteToCreate, Note.class))
                .map(note -> new ResponseEntity<NoteDto>(modelMapper.map(note, NoteDto.class), HttpStatus.CREATED))
                .onErrorMap(t -> new NoteCreationException(messageSource.getMessage("note.not.created", null, Locale.ENGLISH)));
    }

    @PutMapping(value = "/{id}")
    public Mono<ResponseEntity<NoteDto>> updateNote(@RequestBody NoteDto noteUpdated, @PathVariable(value = "id") String id) {
        return noteService.updateNote(id, modelMapper.map(noteUpdated, Note.class))
                .map(note -> ResponseEntity.ok(modelMapper.map(note, NoteDto.class)))
                .switchIfEmpty(Mono.error(new NoteNotFoundException((messageSource.getMessage(NOT_FOUND, new Object[] {id}, Locale.ENGLISH)))));
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<NoteDto>> deleteNote(@PathVariable(value = "id") String id) {
        return noteService.deleteNote(id)
                .map(note -> ResponseEntity.ok(modelMapper.map(note, NoteDto.class)))
                .switchIfEmpty(Mono.error(new NoteNotFoundException(messageSource.getMessage(NOT_FOUND, new Object[] {id}, Locale.ENGLISH))));
    }
}
