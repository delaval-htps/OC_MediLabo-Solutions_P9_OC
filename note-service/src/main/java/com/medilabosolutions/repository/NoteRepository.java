package com.medilabosolutions.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import com.medilabosolutions.model.Note;

@Repository
public interface NoteRepository extends ReactiveMongoRepository<Note,String>{
    
}
