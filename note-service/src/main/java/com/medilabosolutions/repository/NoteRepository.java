package com.medilabosolutions.repository;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import com.medilabosolutions.model.Note;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@Repository
public interface NoteRepository extends ReactiveMongoRepository<Note, String> {

    @Query("{'patient.id': ?0}")
    Flux<Note> findByPatientId(Long patientId);

    @Query(value = "{'patient.id': ?0}", sort = "{'date':1}")
    Flux<Note> findByPatientId(Long patientId, Pageable pageable);

    @Query(value = "{'patient.id': ?0}", delete = true)
    Flux<Note> deleteNoteByPatientId(Long patientId);

    Mono<Long> countByPatientId(Long patientId);

    @Query(value = "{'patient.id':?1,'content':{'$regex':?0}}", count = true)
    Long countTriggersIntoPatientNotes(String triggers, Long patientId);

    /*
     * [ {$match: { 'patient._id':4 }}, {$addFields: { totalTriggers:{
     * $regexFindAll:{input:'$content',regex:'Hémoglobine|déclare|patient'}} } } ]
     */

}
