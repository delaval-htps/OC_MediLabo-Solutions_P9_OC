package com.medilabosolutions.controller;

import java.util.List;
import java.util.Locale;
import org.modelmapper.ModelMapper;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
import com.medilabosolutions.dto.SumTermTriggersDto;
import com.medilabosolutions.exception.NoteCreationException;
import com.medilabosolutions.exception.NoteNotFoundException;
import com.medilabosolutions.model.Note;
import com.medilabosolutions.model.SumTermTriggers;
import com.medilabosolutions.service.NoteService;
import jakarta.validation.Valid;
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
    public ResponseEntity<Flux<NoteDto>> getAllNotes() {
        return ResponseEntity.ok(noteService.getAllNotes().map(note -> modelMapper.map(note, NoteDto.class)));
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<NoteDto>> getNoteById(@PathVariable("id") String id) {
        return noteService.findById(id)
                .map(note -> new ResponseEntity<NoteDto>(modelMapper.map(note, NoteDto.class), HttpStatus.OK))
                .switchIfEmpty(Mono.error(new NoteNotFoundException(messageSource.getMessage(NOT_FOUND, new Object[] {id}, Locale.ENGLISH))));
    }

    @PostMapping
    public Mono<ResponseEntity<NoteDto>> createNote(@Valid @RequestBody NoteDto noteToCreate) {
        return noteService.createNote(modelMapper.map(noteToCreate, Note.class))
                .map(note -> new ResponseEntity<NoteDto>(modelMapper.map(note, NoteDto.class), HttpStatus.CREATED))
                .onErrorMap(t -> new NoteCreationException(messageSource.getMessage("note.not.created", null, Locale.ENGLISH)));
    }

    @PutMapping(value = "/{id}")
    public Mono<ResponseEntity<NoteDto>> updateNote(@Valid @RequestBody NoteDto noteUpdated, @PathVariable(value = "id") String id) {
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

    /**
     * Return all notes for a given id of patient. If id of patient doesn't exist or is not in the
     * collections, return a empty Flux.
     * 
     * @param patientId th egiven id of patient
     * @return ResponseEntity with Flux of all notes that patient with id ownes
     */
    @GetMapping("/patient_id/{id}")
    public ResponseEntity<Flux<NoteDto>> getNoteByPatientId(@PathVariable("id") Long patientId) {
        return ResponseEntity.ok(noteService.findByPatientId(patientId)
                .map(note -> modelMapper.map(note, NoteDto.class)));
    }

    @GetMapping("/patient_id/{id}/{page}/{size}")
    public ResponseEntity<Mono<Page<NoteDto>>> getAllNotesPageableByPatientId(@PathVariable("id") Long patientId,
            @PathVariable(value = "page") int pageNumber,
            @PathVariable(value = "size") int pageSize) {

        return ResponseEntity.ok(noteService.findByPatientIdPageable(patientId, PageRequest.of(pageNumber, pageSize)));
    }

    /**
     * Delete all notes related to patient with given id
     * 
     * @param patientId the given id of patient
     * @return Flux of all notes deleted
     */
    @DeleteMapping("/patient_id/{id}")
    public ResponseEntity<Flux<NoteDto>> deleteNotesByPatientId(@PathVariable("id") Long patientId) {
        return ResponseEntity.ok(noteService.deleteNoteByPatientId(patientId)
                .map(note -> modelMapper.map(note, NoteDto.class)));
    }

    @PostMapping("/triggers/patient_id/{id}")
    public ResponseEntity<Mono<SumTermTriggersDto>> countTriggersIntoPatientNotes(@PathVariable(value = "id") Long patientId,
            @RequestBody List<String> triggers) {
        return ResponseEntity.ok(noteService.countTriggers(patientId, triggers).map(sum -> modelMapper.map(sum,SumTermTriggersDto.class)));
    }

}
