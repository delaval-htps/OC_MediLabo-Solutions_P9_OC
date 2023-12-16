package com.medilabosolutions.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.result.view.Rendering;
import org.springframework.web.server.WebSession;
import com.medilabosolutions.dto.NoteDto;
import com.medilabosolutions.dto.PatientDataDto;
import com.medilabosolutions.dto.PatientDto;
import com.medilabosolutions.model.RestPage;
import com.medilabosolutions.model.UserCredential;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Controller
@Slf4j
public class UiController {

        private static final String AUTHORIZATION = "Authorization";
        private static final String FIELDS_ON_ERROR = "fieldsOnError";
        private static final String PATIENT_URL = "/patients";

        private static final String ERROR_MESSAGE = "errorMessage";
        private static final String SUCCESS_MESSAGE = "successMessage";

        private static final String CREATION = "created !";
        private static final String UPDATE = "updated !";
        private static final String DELETE = "deleted !";
        private static final String FIND = "found !";

        @Value("${path.patient.service}")
        private String pathPatientService;

        @Value("${path.note.service}")
        private String pathNoteService;

        private final WebClient webclient;
        private final ModelMapper modelMapper;

        public UiController(WebClient webclient, ModelMapper modelMapper) {
                this.webclient = webclient;
                this.modelMapper = modelMapper;
        }

        /**
         * endpoint to show form login
         * 
         * @return view login
         */
        @GetMapping("/")
        public Mono<Rendering> getLogin(Model model, WebSession session) {

                // Retrieve jwt if it is present
                String jwtValue = session.getAttributeOrDefault(AUTHORIZATION, "");

                if (!jwtValue.equals("")) {
                        /*
                         * case user has a jwt : redirect to "/patients" : Gateway has a filter in routes to validate the
                         * token, if jwt is not valid then we have a redirection to GET "/login" with loginError (cf GET
                         * ("/patients"))
                         */
                        return Mono.just(Rendering.redirectTo(PATIENT_URL).build());
                }
                /*
                 * case user doesn't have a jwt then he must pass by GET "/login" to fill it his credentials
                 */
                return redirectToLoginPage(model, false);

        }

        // TODO validation of credential with jakarta
        @PostMapping(value = "/login")
        public Mono<Rendering> postLogin(@ModelAttribute UserCredential credential, WebSession session, Model model) {

                // call auth-service to get jwtoken to identify user
                return webclient.post().uri("/login")
                                .contentType(MediaType.APPLICATION_JSON).body(BodyInserters.fromValue(credential))
                                .exchangeToMono(response -> {
                                        if (response.statusCode().equals(HttpStatus.OK)) {

                                                session.getAttributes().put(AUTHORIZATION, response.headers()
                                                                .header(AUTHORIZATION).get(0)
                                                                .replace("Bearer ", ""));

                                                return Mono.just(Rendering.redirectTo(PATIENT_URL).build());
                                        }

                                        return redirectToLoginPage(model, true);

                                });
        }

        /**
         * Endpoint to display the index page of medilabo-solution with the list of all registred
         * PatientDtos.
         * 
         * @param page page for pagination.
         * @param size number of patients in a page by default 10
         * @param session websession to add attribute for redirection because of no redirectAttribute in
         *        spring webflux
         * @param model model to add Patients to the view for thymeleaf
         * @return the view "index.html" //
         */
        @GetMapping("/patients")
        public Mono<Rendering> index(@RequestParam(value = "page") Optional<Integer> page,
                        @RequestParam(value = "size") Optional<Integer> size,
                        Model model, WebSession session) {

                int currentPage = page.orElse(0);
                int pageSize = size.orElse(10);

                // check if jwt token is present
                String jwtValue = session.getAttributeOrDefault(AUTHORIZATION, "");

                return webclient.get().uri(pathPatientService + "/{page}/{size}", currentPage, pageSize)
                                .headers(h -> h.setBearerAuth(jwtValue))
                                .exchangeToMono(response -> {
                                        if (response.statusCode().equals(HttpStatus.UNAUTHORIZED)) {
                                                return redirectToLoginPage(model, true);
                                        }

                                        return response.bodyToMono(RestPage.class)
                                                        .flatMap(restPage -> {
                                                                // TODO after change return patient Dto in pagination delete this row
                                                                restPage.getContent().stream().map(p -> modelMapper.map(p, PatientDto.class));

                                                                if (restPage.getTotalPages() > 0) {
                                                                        List<Integer> pageNumbers = IntStream.rangeClosed(1, restPage.getTotalPages())
                                                                                        .boxed().collect(Collectors.toList());
                                                                        model.addAttribute("pageNumbers", pageNumbers);
                                                                }

                                                                model.addAttribute(FIELDS_ON_ERROR, new HashMap<String, String>());
                                                                model.addAttribute("patient", new PatientDto());
                                                                model.addAttribute("patientPages", restPage);

                                                                // in case of bindingResult , we have to add to model fieldsOnError and to override patientwith
                                                                // fields fille in by user to be able to display his errors
                                                                transfertSessionAttributesIntoModel(model, session, ERROR_MESSAGE, SUCCESS_MESSAGE, FIELDS_ON_ERROR, "patient");

                                                                // if request from patient record page, we have to delete note in session to not propagate it
                                                                session.getAttributes().remove("note");

                                                                log.info("request GET all patients");

                                                                return Mono.just(Rendering.view("index").build());
                                                        });

                                });
        }

        /**
         * Endpoint to display record of Patient with his all medical notes (existed errors are display only
         * in a toast).
         * 
         * @param patientId the id of Patient
         * @param session websession to add attribute for redirection because of no redirectAttribute in
         *        spring webflux
         * @param model model to return attributes to the view for thymeleaf
         * @return view of the record of Patient (personnal informations and his notes).
         *         <ul>
         *         <li>If no jwt Token retrieved then redirection to login page.</li>
         *         <li>If no patient with given id is found then redirection to "/patients" with message of
         *         error "not found".</li>
         *         </ul>
         */
        @GetMapping("/patients/{id}")
        public Mono<Rendering> getPatientRecordAndNotes(@PathVariable(value = "id") Long patientId,
                        @RequestParam(value = "notePage") Optional<Integer> notePageNumber,
                        @RequestParam(value = "noteSize") Optional<Integer> noteSize,
                        WebSession session, Model model) {

                /* check if jwt token is present */
                String jwtValue = session.getAttributeOrDefault(AUTHORIZATION, "");

                if (jwtValue.isEmpty()) {
                        return redirectToLoginPage(model, true);
                }

                int currentPage = notePageNumber.orElse(0);
                int pageSize = noteSize.orElse(5);

                /*
                 * send request to patient API, return :Mono<Rendering> to specific endpoints in function of
                 * response
                 */
                return webclient.get().uri(pathPatientService + "/{id}", patientId)
                                .headers(h -> h.setBearerAuth(jwtValue))
                                .exchangeToMono(response -> {

                                        if (response.statusCode().isError()) {
                                                /* only in case of patient not found */
                                                return response.bodyToMono(ProblemDetail.class)
                                                                .flatMap(body -> {
                                                                        // add in session only errorMessage for redirection to index
                                                                        setSessionAttributes(body, session, FIND, Optional.empty());

                                                                        return Mono.just(Rendering.redirectTo(PATIENT_URL).build());
                                                                });
                                        }

                                        return response.bodyToMono(PatientDto.class)

                                                        // zip with request to notes API that return all notes for patient with pagination
                                                        .zipWith(webclient.get()
                                                                        .uri(pathNoteService + "/patient_id/{id}/{page}/{size}", patientId, currentPage, pageSize)
                                                                        .headers(h -> h.setBearerAuth(jwtValue))
                                                                        .exchangeToMono(r -> r.bodyToMono(RestPage.class)
                                                                                        .flatMap(restPage -> {
                                                                                                if (restPage.getTotalPages() > 0) {
                                                                                                        List<Integer> pageNumbers =
                                                                                                                        IntStream.rangeClosed(1, restPage.getTotalPages())
                                                                                                                                        .boxed()
                                                                                                                                        .collect(Collectors.toList());
                                                                                                        model.addAttribute("pageNumbers", pageNumbers);
                                                                                                }
                                                                                                return Mono.just(restPage.getContent());
                                                                                        })))
                                                        .flatMap(t -> {
                                                                model.addAttribute("patient", t.getT1());
                                                                model.addAttribute("notes", t.getT2());

                                                                model.addAttribute(FIELDS_ON_ERROR, new HashMap<String, String>());
                                                                NoteDto noteToCreate = new NoteDto();
                                                                noteToCreate.setPatient(new PatientDataDto());
                                                                noteToCreate.getPatient().setId(t.getT1().getId());
                                                                noteToCreate.getPatient().setName(t.getT1().getLastName());
                                                                model.addAttribute("note", noteToCreate);

                                                                // Override model (using existing attributes of session) with fieldsOnError,messages or note with
                                                                // fields filled in by
                                                                // user to be able to display after redirection
                                                                transfertSessionAttributesIntoModel(model, session, ERROR_MESSAGE, SUCCESS_MESSAGE, FIELDS_ON_ERROR, "note");

                                                                log.info("GET patient-record with id {} = {}", patientId, t.getT1());
                                                                return Mono.just(Rendering.view("patient-record").build());
                                                        });

                                });
        }

        /**
         * Endpoint to create a new patient.
         * 
         * @param patientToCreate the patient to create from form of index.html
         * @param model model to add attributes for thymeleaf
         * @param session session to add attribute to model when redirection because of no redirectAttribute
         *        in spring webflux
         * @return rendering with redirection to index.html with either errorMessage with patient's fields
         *         filled in by user in form either succesMessage with patient created
         */
        @PostMapping("/patients/create")
        public Mono<Rendering> createPatient(@ModelAttribute(value = "patient") PatientDto patientToCreate,
                        Model model, WebSession session) {

                // check if jwt token is present
                String jwtValue = session.getAttributeOrDefault(AUTHORIZATION, "");

                return webclient.post().uri(pathPatientService)
                                .headers(h -> h.setBearerAuth(jwtValue))
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(Mono.just(patientToCreate), PatientDto.class)

                                .exchangeToMono(response -> {

                                        if (response.statusCode().equals(HttpStatus.UNAUTHORIZED)) {
                                                return redirectToLoginPage(model, true);
                                        }

                                        // case of problem finding patient in REST API : example not find exception , etc...
                                        if (response.statusCode().isError()) {
                                                return response.bodyToMono(ProblemDetail.class)
                                                                .flatMap(body -> {
                                                                        // put fields of patient filled in by user and errorMessage to session before redirection
                                                                        setSessionAttributes(body, session, CREATION, Optional.of(patientToCreate));

                                                                        return Mono.just(Rendering.redirectTo(PATIENT_URL).build());
                                                                });
                                        }

                                        return response.bodyToMono(PatientDto.class)
                                                        .flatMap(body -> {
                                                                // put created patient and success message in session before redirection to index
                                                                setSessionAttributes(body, session, CREATION, Optional.empty());

                                                                log.info("request POST createPatient");
                                                                return Mono.just(Rendering.redirectTo(PATIENT_URL).build());
                                                        });
                                });

        }

        /**
         * endpoint to update a existed patient.
         * 
         * @param patientId id of patient to be updated
         * @param updatedPatient the patient with updated fields filled in by user
         * @param noteState state of note's form (= all || creation ||edition ||update) to specify after
         *        redirection to patient-recored in which type of state note's form was and display it in
         *        this state
         * @param session websession to add attribute for redirection because of no redirectAttribute in
         *        spring webflux
         * @param model model to pass attribute to thymeleaf
         * @return Mono<Rendering> with view to patient-record page (patient with his note)
         */
        @PostMapping("/patients/update/{id}")
        public Mono<Rendering> updatePatient(@PathVariable(value = "id") Long patientId,
                        @ModelAttribute(value = "patient") PatientDto updatedPatient,
                        @RequestParam(value = "note_state", required = false) String noteState,
                        WebSession session, Model model) {

                // check if jwt token is present
                String jwtValue = session.getAttributeOrDefault(AUTHORIZATION, "");

                // delete id of patientDto to have a id null (must to have a correct validation in patient-service)
                updatedPatient.setId(null);

                // add to session selectednote displayed when updating patient to not lost note and review it on
                // page. If State of note is creation , remove note in session
                if (!noteState.equals("creation") && session.getAttributes().containsKey("note")) {
                        session.getAttributes().put("note", session.getAttribute("note"));
                } else {
                        session.getAttributes().remove("note");
                }

                return webclient.put().uri(pathPatientService + "/{id}", patientId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .headers(h -> h.setBearerAuth(jwtValue))
                                .body(Mono.just(updatedPatient), PatientDto.class)

                                .exchangeToMono(response -> {

                                        if (response.statusCode().equals(HttpStatus.UNAUTHORIZED)) {
                                                return redirectToLoginPage(model, true);
                                        }
                                        // case of problem finding patient in REST API : example not find exception, bindingResult
                                        if (response.statusCode().isError()) {
                                                return response.bodyToMono(ProblemDetail.class)
                                                                .flatMap(body -> {
                                                                        // in case of bindignResult we don't send information of patient filled in by user =>
                                                                        // Optional.empty(). Fields of form after redirection are filled in with information of registred
                                                                        // patient. Only errorMessage is sended.
                                                                        setSessionAttributes(body, session, UPDATE, Optional.empty());

                                                                        return Mono.just(Rendering.redirectTo("/patients/" + patientId
                                                                                        + "?note_state=" + noteState + "&patient_update=true")
                                                                                        .build());

                                                                });
                                        }
                                        return response.bodyToMono(PatientDto.class)
                                                        .flatMap(body -> {
                                                                // Add patient updated (=body) to session with a success message
                                                                setSessionAttributes(body, session, UPDATE, Optional.empty());

                                                                log.info("request POST updatePatient with id {}", patientId);
                                                                return Mono.just(Rendering.redirectTo("/patients/" + patientId
                                                                                + "?note_state=" + noteState + "&patient_update=false")
                                                                                .build());
                                                        });
                                });

        }

        /**
         * endpoint to delete a patient with given id in request and his related notes too.
         * 
         * @param patientId the given id of patient to delete
         * @param session the websession to add attribute for redirection because of no redirectAttribute in
         *        spring webflux
         * @param model model to pass attribute to thymeleaf
         * @return Mono<Renderring> to index page. If deleted patient had notes, all notes was deleted too.
         */
        @GetMapping("/patients/delete/{id}")
        public Mono<Rendering> deletePatientWithNotes(@PathVariable(value = "id") Long patientId, Model model, WebSession session) {

                // check if jwt token is present
                String jwtValue = session.getAttributeOrDefault(AUTHORIZATION, "");

                return webclient.delete().uri(pathPatientService + "/{id}", patientId)
                                .headers(h -> h.setBearerAuth(jwtValue))
                                .exchangeToMono(response -> {
                                        if (response.statusCode().equals(HttpStatus.UNAUTHORIZED)) {
                                                return redirectToLoginPage(model, true);
                                        }

                                        if (response.statusCode().isError()) {
                                                // case of problem finding patient in REST API : example not find exception , etc...
                                                return response.bodyToMono(ProblemDetail.class)
                                                                .flatMap(body -> {
                                                                        // send a error message for redirection
                                                                        setSessionAttributes(body, session, UPDATE, Optional.empty());

                                                                        return Mono.just(Rendering.redirectTo(PATIENT_URL).build());
                                                                });
                                        }
                                        return response.bodyToMono(PatientDto.class)
                                                        .flatMap(body -> {
                                                                // add to session successMessage & patient for redirection
                                                                setSessionAttributes(body, session, DELETE, Optional.empty());

                                                                log.info("request GET deletePatient");

                                                                // delete all notes of deleted patient before redirection
                                                                return webclient.delete().uri(pathNoteService + "/patient_id/{id}", patientId)
                                                                                .headers(h -> h.setBearerAuth(jwtValue))
                                                                                .exchangeToMono(result -> Mono.just(Rendering.redirectTo(PATIENT_URL).build()));

                                                        });
                                });

        }

        /**
         * Endpoint to display note selected of a patient from list of his notes in the patient-record view.
         * 
         * @param noteId id of note selected in table of patient notes in patient record view by cliking the
         *        row
         * @param patientUpdate state of form of patient ( true : form is operational ; false form is in
         *        readonly state)
         * @param noteState state of note's form (= all || creation ||edition ||update) to specify after
         *        redirection to patient-record in which type of state note's form was and display it again
         *        in this state
         * @param model model to add attribut for thymeleaf
         * @param session web session to add attribute in case of redirection because of no
         *        redirectAttribute in spring webflux
         * @return redirection to the patient record view with information of note selected displayed in the
         *         form for note
         */
        @GetMapping("/notes/{note_id}")
        public Mono<Rendering> getNoteById(@PathVariable(value = "note_id") String noteId,
                        @RequestParam(value = "patient_update", required = false) String patientUpdate,
                        @RequestParam(value = "note_state", required = false) String noteState,
                        Model model, WebSession session) {
                // check if jwt token is present
                String jwtValue = session.getAttributeOrDefault(AUTHORIZATION, "");

                if (jwtValue.isEmpty()) {
                        return redirectToLoginPage(model, true);
                }

                return webclient.get().uri(pathNoteService + "/{id}", noteId)
                                .headers(h -> h.setBearerAuth(jwtValue))
                                .exchangeToMono(response -> {

                                        if (response.statusCode().isError()) {
                                                // case of note not found then redirection to index page cause it is not normal to have a
                                                // note not found in list of all patient's note in patient-record
                                                return response.bodyToMono(ProblemDetail.class)
                                                                .flatMap(errorBody -> {
                                                                        // send errorMessage and redirect to index page
                                                                        setSessionAttributes(errorBody, session, FIND, Optional.empty());
                                                                        return Mono.just(Rendering.redirectTo(PATIENT_URL).build());
                                                                });
                                        }
                                        return response.bodyToMono(NoteDto.class).flatMap(body -> {
                                                // send note and successMessage with session for redirection
                                                setSessionAttributes(body, session, FIND, Optional.empty());

                                                return Mono.just(Rendering.redirectTo("/patients/" + body.getPatient().getId()
                                                                + "?note_state=" + noteState + "&patient_update=" + patientUpdate)
                                                                .build());
                                        });
                                });

        }

        /**
         * Endpoint to create a note for a patient
         * 
         * @param noteToCreate the note to create fill in by user from form in patient-record view
         * @param patientUpdate state of form of patient ( true : form is operational ; false form is in
         *        readonly state)
         * @param model model to add attribute for thymeleaf
         * @param session session to pass attribute for redirection because of no redirectAttribute in
         *        spring webflux
         * @return Mono<Rendering> with redirection to patient-record view with message of success or error
         *         if failed to create the new note
         */
        @PostMapping("/notes/create")
        public Mono<Rendering> createNote(@ModelAttribute(value = "note") NoteDto noteToCreate,
                        @RequestParam(value = "patient_update", required = false) String patientUpdate,
                        Model model, WebSession session) {

                // check if jwt token is present
                String jwtValue = session.getAttributeOrDefault(AUTHORIZATION, "");

                if (jwtValue.isEmpty()) {
                        return redirectToLoginPage(model, true);
                }

                // retrieve the id of related patient for the new note to create
                Long relatedPatientId = noteToCreate.getPatient().getId();

                // creation of new note
                return webclient.post().uri(pathNoteService)
                                .headers(h -> h.setBearerAuth(jwtValue))
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(Mono.just(noteToCreate), NoteDto.class)
                                .exchangeToMono(noteResponse -> {

                                        /* case of problem created note in REST API : example bindingResult , etc... */
                                        if (noteResponse.statusCode().isError()) {
                                                return noteResponse.bodyToMono(ProblemDetail.class)
                                                                .flatMap(body -> {
                                                                        // send errorMessage & note in form filled in by user to session for redirection
                                                                        setSessionAttributes(body, session, CREATION, Optional.of(noteToCreate));

                                                                        return Mono.just(Rendering.redirectTo("/patients/" + relatedPatientId
                                                                                        + "?patient_update=" + patientUpdate)
                                                                                        .build());
                                                                });
                                        }

                                        return noteResponse.bodyToMono(NoteDto.class)
                                                        .flatMap(body -> {
                                                                // send success message & updated note in session for redirection
                                                                setSessionAttributes(body, session, CREATION, Optional.empty());

                                                                log.info("request POST create note for patient {}", relatedPatientId);
                                                                return Mono.just(Rendering.redirectTo("/patients/" + relatedPatientId
                                                                                + "?note_state=all&patient_update=" + patientUpdate)
                                                                                .build());
                                                        });
                                });


        }

        /**
         * Endpoint to update existing note with filled in fields form by user
         * 
         * @param noteId the given id of note to update
         * @param updatedNote the updated note filled in by user
         * @param patientUpdate state of form of patient ( true : form is operational ; false form is in
         *        readonly state)
         * @param model model to add Attribute for thymeleaf
         * @param session websession to add Attribute for redirection because of no redirectAttribute in
         *        spring webflux
         * @return Mono <Rendering> with redirection to patient record with message of success or error
         */
        @PostMapping("/notes/update/{note_id}")
        public Mono<Rendering> updateNote(@PathVariable(value = "note_id") String noteId,
                        @ModelAttribute(value = "note") NoteDto updatedNote,
                        @RequestParam(value = "patient_update", required = false) String patientUpdate,
                        Model model, WebSession session) {
                // check if jwt token is present
                String jwtValue = session.getAttributeOrDefault(AUTHORIZATION, "");

                if (jwtValue.isEmpty()) {
                        return redirectToLoginPage(model, true);
                }
                /* retrieve the id of related patient for the new note to create */
                Long relatedPatientId = updatedNote.getPatient().getId();

                return webclient.put().uri(pathNoteService + "/{id}", noteId)
                                .headers(h -> h.setBearerAuth(jwtValue))
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(Mono.just(updatedNote), NoteDto.class)
                                .exchangeToMono(response -> {

                                        /* case of not found or bindigResult error */
                                        if (response.statusCode().isError()) {
                                                return response.bodyToMono(ProblemDetail.class)
                                                                .flatMap(body -> {
                                                                        /*
                                                                         * need to add id of note to updated note given in form by user before sending it in session cause
                                                                         * NtoDto has validation @Null to its id's field. Send updated note to session is use, to be able to
                                                                         * retrieve it in model after redirection
                                                                         */
                                                                        updatedNote.setId(noteId);
                                                                        setSessionAttributes(body, session, UPDATE, Optional.of(updatedNote));

                                                                        return Mono.just(Rendering.redirectTo("/patients/" + relatedPatientId
                                                                                        + "?note_state=update&patient_update=" + patientUpdate)
                                                                                        .build());
                                                                });
                                        }

                                        return response.bodyToMono(NoteDto.class)
                                                        .flatMap(body -> {
                                                                // send sucessMessage and updated note (body) in session for redirection
                                                                setSessionAttributes(body, session, UPDATE, Optional.empty());

                                                                log.info("request POST update note for patient {}", relatedPatientId);
                                                                return Mono.just(Rendering.redirectTo("/patients/" + relatedPatientId
                                                                                + "?note_state=all&patient_update=" + patientUpdate)
                                                                                .build());
                                                        });
                                });

        }

        /**
         * endpoint to delete a note by given its id
         * 
         * @param noteId Given id of note to delete
         * @param patientUpdate state of form of patient ( true : form is operational ; false form is in
         *        readonly state)
         * @param model model to add attribute for thymeleaf
         * @param session websession to add attribute in case of redirection and retrieve them after
         * @return Mono<Rendering> in case of success : patient record page & in case of error to index
         */
        @GetMapping("/notes/delete/{note_id}")
        public Mono<Rendering> deleteNote(@PathVariable(value = "note_id") String noteId,
                        @RequestParam(value = "patient_update", required = false) String patientUpdate,
                        Model model, WebSession session) {

                // check if jwt token is present
                String jwtValue = session.getAttributeOrDefault(AUTHORIZATION, "");

                if (jwtValue.isEmpty()) {
                        return redirectToLoginPage(model, true);
                }
                return webclient.delete().uri(pathNoteService + "/{id}", noteId)
                                .headers(h -> h.setBearerAuth(jwtValue))
                                .exchangeToMono(response -> {
                                        // case of error redirection to index page
                                        if (response.statusCode().isError()) {
                                                return response.bodyToMono(ProblemDetail.class)
                                                                .flatMap(errorBody -> {
                                                                        // send error message
                                                                        setSessionAttributes(errorBody, session, DELETE, Optional.empty());

                                                                        return Mono.just(Rendering.redirectTo(PATIENT_URL).build());
                                                                });
                                        }
                                        return response.bodyToMono(NoteDto.class)
                                                        .flatMap(body -> {
                                                                // send success message and note deleted in session before redirection
                                                                setSessionAttributes(body, session, DELETE, Optional.empty());

                                                                log.info("request DELETE note with id {} of patient {}", body.getId(), body.getPatient().getId());
                                                                return Mono.just(Rendering.redirectTo("/patients/" + body.getPatient().getId()
                                                                                + "?note_state=all&patient_update=" + patientUpdate)
                                                                                .build());
                                                        });
                                });
        }


        private Mono<Rendering> redirectToLoginPage(Model model, boolean loginError) {
                if (loginError) {
                        model.addAttribute("loginError", true);
                }
                model.addAttribute("userCredential", new UserCredential());
                return Mono.just(Rendering.view("/login").build());
        }

        /**
         * method to add an error message (when invalid fields or errors are detected in result of webclient
         * request) or success message to websession for a futur redirection (cause spring webflux not
         * supports redirectAttributes). Using websession attribut allow us to retrieve them in another
         * request and add them to the model for thymeleaf with method
         * transfertSessionAttributesIntoModel().
         * 
         * @param body body content of a response from webclient request to add to message (problemDetail or
         *        Patient or Note)
         * @param session the websession of the request
         * @param typeOfOperation String to modify message in toast in case of success message according to
         *        type of operation: create/update or delete a patient
         * @param formObjectFilledInByUser optional parameter of patient or note filled in by user in form
         *        to create one or update a existing one
         * @return void
         */
        private void setSessionAttributes(Object body, WebSession session, String typeOfOperation, Optional<Object> formObjectFilledInByUser) {

                if (body instanceof ProblemDetail) {

                        ProblemDetail pb = (ProblemDetail) body;

                        Map<String, Object> properties = pb.getProperties();

                        /*
                         * In case of existence of bindingResult in problemDetail , we have to add it to session to be
                         * retrieve in redirection and add fieldsOnError in model
                         */
                        if (properties != null && properties.containsKey("bindingResult")) {
                                session.getAttributes().put(FIELDS_ON_ERROR,
                                                properties.get("bindingResult"));
                        }

                        /*
                         * Add into session , the fields that user filled in form that represents patientDto or NoteDto to
                         * retrieve it after redirection for thymeleaf
                         */
                        if (formObjectFilledInByUser.isPresent()) {

                                if (formObjectFilledInByUser.get() instanceof PatientDto) {
                                        session.getAttributes().put("patient", formObjectFilledInByUser.get());
                                } else {
                                        session.getAttributes().put("note", formObjectFilledInByUser.get());
                                }
                        }

                        log.error("error of type {} : {} \t {}", typeOfOperation, pb.getTitle(), pb.getDetail());

                        session.getAttributes().put(ERROR_MESSAGE, "<h6><ins>A problem occurs, "
                                        + Objects.requireNonNull(pb.getTitle()).split("-")[1] + "</ins></h6>"
                                        + pb.getDetail() + ".");
                }

                /* Case of success operation for a patient or a note */

                if (body instanceof PatientDto) {
                        PatientDto patient = (PatientDto) body;
                        session.getAttributes().put("patient", patient);
                        session.getAttributes().put(SUCCESS_MESSAGE, "<h6><ins>Sucessful action!</ins></h6>" +
                                        "Patient " + patient.getLastName() + " was correctly "
                                        + typeOfOperation);
                }

                if (body instanceof NoteDto) {
                        NoteDto note = (NoteDto) body;
                        session.getAttributes().put("note", note);
                        session.getAttributes().put(SUCCESS_MESSAGE, "<h6><ins>Sucessful action!</ins></h6>" +
                                        "Note of patient " + note.getPatient().getName() + " was correctly "
                                        + typeOfOperation);

                }
        }

        /**
         * method to remove attributes of a websession and add them to model of view Exception for message
         * "note" where we don't delete in session to keep it
         * 
         * @param model the model to add attribute
         * @param session the attribute to remove and add to model of view
         * @param messages list of (String)messages (name of attribute in session)to remove from websession
         *        and add them to model
         */
        private void transfertSessionAttributesIntoModel(Model model, WebSession session, String... messages) {

                for (String message : messages) {
                        if (session.getAttribute(message) != null) {
                                model.addAttribute(message, session.getAttribute(message));
                                if (!message.equals("note")) {
                                        session.getAttributes().remove(message);
                                }
                        }
                }
        }
}
