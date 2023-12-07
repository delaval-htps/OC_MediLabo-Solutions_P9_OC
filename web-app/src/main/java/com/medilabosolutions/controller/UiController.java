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
                } else {
                        /*
                         * case user doesn't have a jwt then he must pass by GET "/login" to fill it his credentials
                         */
                        return redirectToLoginPage(model, false);
                }
        }

        // TODO validation of credential with jakarta
        @PostMapping(value = "/login")
        public Mono<Rendering> postLogin(@ModelAttribute UserCredential credential, WebSession session, Model model) {

                // call auth-service to get jwtoken to identify user
                return webclient.post().uri("/login").contentType(MediaType.APPLICATION_JSON).body(BodyInserters.fromValue(credential))
                                .exchangeToMono(response -> {
                                        if (response.statusCode().equals(HttpStatus.OK)) {
                                                session.getAttributes().put(AUTHORIZATION, response.headers().header(AUTHORIZATION).get(0).replace("Bearer ", ""));
                                                return Mono.just(Rendering.redirectTo(PATIENT_URL).build());
                                        } else {
                                                return redirectToLoginPage(model, true);
                                        }
                                });
        }

        /**
         * Endpoint to display the index page of medilabo-solution with the list of all registred
         * PatientDtos (never get error , just only a avoid list of patients)
         * 
         * @param model model to add PatientDtos to the view
         * @return the view "index.html" //
         */
        @GetMapping("/patients")
        public Mono<Rendering> index(
                        @RequestParam(value = "page") Optional<Integer> page, @RequestParam(value = "size") Optional<Integer> size,
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

                                        // TODO case of problem finding patients in REST API : ex 503 not available...

                                        return response.bodyToMono(RestPage.class)
                                                        .flatMap(restPage -> {
                                                                restPage.getContent().stream().map(p -> modelMapper.map(p, PatientDto.class));

                                                                if (restPage.getTotalPages() > 0) {
                                                                        List<Integer> pageNumbers = IntStream.rangeClosed(1, restPage.getTotalPages())
                                                                                        .boxed().collect(Collectors.toList());
                                                                        model.addAttribute("pageNumbers", pageNumbers);
                                                                }

                                                                model.addAttribute(FIELDS_ON_ERROR, new HashMap<String, String>());
                                                                model.addAttribute("patientToCreate", new PatientDto());
                                                                model.addAttribute("patientPages", restPage);

                                                                // in case of bindingResult , we have to add to model fieldsOnError and to override patientwith
                                                                // fields filled
                                                                // in by user to be able to display his errors
                                                                moveSessionAttributeIntoModel(model, session, ERROR_MESSAGE, SUCCESS_MESSAGE, FIELDS_ON_ERROR, "patientToCreate");

                                                                log.info("request GET all patients");

                                                                return Mono.just(Rendering.view("index").build());
                                                        });

                                });
        }

        /**
         * Endpoint to display record of Patient with his all medical notes (existed errors are display only
         * in a toast)
         * 
         * @param patientId the id of Patient
         * @param model model to return to the view
         * @return view of the record of Patient (personnal informations and his notes). If no jwt Token
         *         retrieved then redirection to login page. If no patient with given id is found then
         *         redirection to "/patients" with message of error "not found".
         */
        @GetMapping("/patients/{id}")
        public Mono<Rendering> getPatientRecordAndNotes(@PathVariable(value = "id") Long patientId, WebSession session, Model model) {

                /* check if jwt token is present */
                String jwtValue = session.getAttributeOrDefault(AUTHORIZATION, "");

                if (jwtValue.isEmpty()) {

                        return redirectToLoginPage(model, true);

                } else {
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
                                                                                setSessionAttribute(body, session, FIND, Optional.empty());
                                                                                return Mono.just(Rendering.redirectTo(PATIENT_URL).build());
                                                                        });
                                                } else {

                                                        return response.bodyToMono(PatientDto.class)
                                                                        /* zip with request to notes API that return all notes for patient */
                                                                        .zipWith(webclient.get().uri(pathNoteService + "/patient_id/{id}", patientId)
                                                                                        .headers(h -> h.setBearerAuth(jwtValue))
                                                                                        .exchangeToFlux(r -> r.bodyToFlux(NoteDto.class)).collectList())
                                                                        .flatMap(t -> {
                                                                                log.info("GET patient-record with id {} = {}", patientId, t.getT1());
                                                                                model.addAttribute(FIELDS_ON_ERROR, new HashMap<String, String>());
                                                                                model.addAttribute("patient", t.getT1());
                                                                                model.addAttribute("notes", t.getT2());

                                                                                NoteDto noteToCreate = new NoteDto();
                                                                                noteToCreate.setPatient(new PatientDataDto());
                                                                                noteToCreate.getPatient().setId(t.getT1().getId());
                                                                                noteToCreate.getPatient().setName(t.getT1().getLastName());
                                                                                model.addAttribute("note", noteToCreate);
                                                                                /*
                                                                                 * case of bindingResult: Add to model fieldsOnError and override patient with fields filled in by
                                                                                 * user to be able to display his errors
                                                                                 */
                                                                                moveSessionAttributeIntoModel(model, session, ERROR_MESSAGE, SUCCESS_MESSAGE, FIELDS_ON_ERROR);
                                                                                return Mono.just(Rendering.view("patient-record").build());
                                                                        });
                                                }
                                        });
                }
        }

        /**
         * Endpoint to create a new patient
         * 
         * @param patientToCreate the patient to create from form of index.html
         * @param model model to add attributes
         * @param session session to add attribute to model when redirect to index.html
         * @return rendering with redirection to index.html
         */
        @PostMapping("/patients/create")
        public Mono<Rendering> createPatient(@ModelAttribute(value = "patientToCreate") PatientDto patientToCreate, Model model, WebSession session) {

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
                                                                        setSessionAttribute(body, session, CREATION, Optional.empty());
                                                                        return Mono.just(Rendering.redirectTo(PATIENT_URL).build());
                                                                });
                                        }
                                        return response.bodyToMono(PatientDto.class)
                                                        .flatMap(body -> {
                                                                setSessionAttribute(body, session, CREATION, Optional.of(patientToCreate));
                                                                log.info("request POST createPatient");
                                                                return Mono.just(Rendering.redirectTo(PATIENT_URL).build());
                                                        });
                                });

        }

        /**
         * endpoint to update a existed patient
         * 
         * @param patientId id of patient to be updated
         * @param updatedPatient the patient with updated fields
         * @param session websession to add attribute if problem and redirect to the form
         * @param model model to add sucess attribute when patient correctly updated
         * @return Mono<Rendering> with view to index if success or redirection to the same page (form) if
         *         error
         */
        @PostMapping("/patients/update/{id}")
        public Mono<Rendering> updatePatient(@PathVariable(value = "id") Long patientId, @ModelAttribute(value = "patient") PatientDto updatedPatient,
                        WebSession session, Model model) {

                // check if jwt token is present
                String jwtValue = session.getAttributeOrDefault(AUTHORIZATION, "");

                // delete id of patientDto to have a id null (must to have a correct validation in patient-service)
                updatedPatient.setId(null);

                return webclient.put().uri(pathPatientService + "/{id}", patientId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .headers(h -> h.setBearerAuth(jwtValue))
                                .body(Mono.just(updatedPatient), PatientDto.class)

                                .exchangeToMono(response -> {

                                        if (response.statusCode().equals(HttpStatus.UNAUTHORIZED)) {
                                                return redirectToLoginPage(model, true);
                                        }
                                        // case of problem finding patient in REST API : example not find exception , etc...
                                        if (response.statusCode().isError()) {
                                                return response.bodyToMono(ProblemDetail.class)
                                                                .flatMap(body -> {
                                                                        setSessionAttribute(body, session, UPDATE, Optional.empty());
                                                                        return Mono.just(Rendering.redirectTo("/patients/" + patientId).build());

                                                                });
                                        }
                                        return response.bodyToMono(PatientDto.class)
                                                        .flatMap(body -> {
                                                                log.info("request POST updatePatient with id {}", patientId);
                                                                setSessionAttribute(body, session, UPDATE, Optional.of(updatedPatient));
                                                                return Mono.just(Rendering.redirectTo(PATIENT_URL).build());
                                                        });
                                });

        }

        /**
         * endpoint to delete a patient with given id in request and his related notes too.
         * 
         * @param patientId the given id of patient to delete
         * @param session the websession to redirect to same view "/"
         * @return Mono<Renderring> for redirection
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
                                        // case of problem finding patient in REST API : example not find exception , etc...
                                        if (response.statusCode().isError()) {
                                                return response.bodyToMono(ProblemDetail.class)
                                                                .flatMap(body -> {
                                                                        setSessionAttribute(body, session, UPDATE, Optional.empty());
                                                                        return Mono.just(Rendering.redirectTo(PATIENT_URL).build());
                                                                });
                                        }
                                        return response.bodyToMono(PatientDto.class)
                                                        .flatMap(body -> {
                                                                log.info("request GET deletePatient");
                                                                setSessionAttribute(body, session, DELETE, Optional.empty());
                                                                /* delete all notes of deleted patient */
                                                                return webclient.delete().uri(pathNoteService + "/patient_id/{id}", patientId)
                                                                                .headers(h -> h.setBearerAuth(jwtValue))
                                                                                .exchangeToMono(result -> Mono.just(Rendering.redirectTo(PATIENT_URL).build()));

                                                        });
                                });

        }

        /**
         * Endpoint to create a note for a patient
         * 
         * @param noteToCreate the note to create fill in by user from form in patient-record view
         * @param model model to add attribute for thymeleaf
         * @param session session to pass attribute for redirection because of non management in spring
         *        webflux
         * @return Mono<Rendering> with redirection to patient-record view with message of success or error
         *         if failed to create the new note
         */
        @PostMapping("/notes/create")
        public Mono<Rendering> createNote(@ModelAttribute(value = "noteToCreate") NoteDto noteToCreate, Model model, WebSession session) {
                // check if jwt token is present
                String jwtValue = session.getAttributeOrDefault(AUTHORIZATION, "");

                if (jwtValue.isEmpty()) {
                        return redirectToLoginPage(model, true);
                } else {
                        /* retrieve the id of related patient for the new note to create */

                        Long relatedPatientId = noteToCreate.getPatient().getId();

                        /* check if patient's note exists and retrieve its id */
                        return webclient.get().uri(pathPatientService + "/{id}", relatedPatientId)
                                        .exchangeToMono(patientResponse -> {

                                                if (patientResponse.statusCode().equals(HttpStatus.NOT_FOUND)) {
                                                        // case of patient with relatedPatientId is not Found
                                                        return Mono.just(Rendering.redirectTo("/patients/" + relatedPatientId).build());
                                                }
                                                /* creation of new note */
                                                return webclient.post().uri(pathNoteService)
                                                                .headers(h -> h.setBearerAuth(jwtValue))
                                                                .contentType(MediaType.APPLICATION_JSON)
                                                                .body(Mono.just(noteToCreate), NoteDto.class)
                                                                .exchangeToMono(noteResponse -> {

                                                                        /* case of problem created note in REST API : example bindingResult , etc... */
                                                                        if (noteResponse.statusCode().isError()) {
                                                                                return noteResponse.bodyToMono(ProblemDetail.class)
                                                                                                .flatMap(body -> {
                                                                                                        setSessionAttribute(body, session, CREATION, Optional.empty());
                                                                                                        return Mono.just(Rendering.redirectTo("/patients/" + relatedPatientId).build());
                                                                                                });
                                                                        }
                                                                        /* case creation of note ok */
                                                                        return noteResponse.bodyToMono(NoteDto.class)
                                                                                        .flatMap(body -> {
                                                                                                setSessionAttribute(body, session, CREATION, Optional.of(noteToCreate));
                                                                                                log.info("request POST create note for patient {}", relatedPatientId);
                                                                                                return Mono.just(Rendering.redirectTo("/patients/" + relatedPatientId).build());
                                                                                        });
                                                                });
                                        });
                }
        }


        private Mono<Rendering> redirectToLoginPage(Model model, boolean loginError) {
                if (loginError) {
                        model.addAttribute("loginError", true);
                }
                model.addAttribute("userCredential", new UserCredential());
                return Mono.just(Rendering.view("/login").build());
        }

        /**
         * method to add an error (with bindingResult, if exists) or success message to websession for a
         * futur redirection (cause spring webflux not supports redirectAttributes)
         * 
         * @param body body content of a response from webclient request to add to message
         * @param session the websession of the request of endpoint
         * @param typeOfOperation String to modify message in toast in case of success message according to
         *        type of operation: create/update or delete a patient
         * @param userProvidedModelAttribute optional parameter of user-provided patient or note in form to
         *        create one or update a existing one
         * @return String : if body is ProblemDetail then return is ERRORMESSAGE (with if exists
         *         bindingResult and user-provided patient ) else customized SUCCESSMESSAGE with type of
         *         operation ( created, updated or deleted !)
         */
        private String setSessionAttribute(Object body, WebSession session, String typeOfOperation, Optional<Object> userProvidedModelAttribute) {

                if (body instanceof ProblemDetail) {

                        ProblemDetail pb = (ProblemDetail) body;

                        Map<String, Object> properties = pb.getProperties();

                        // in case of existence of bindingResult in problemDetail , we have to add
                        // it to session to be retrieve in redirection and add fieldsOnError in model
                        if (properties != null && properties.containsKey("bindingResult")) {
                                session.getAttributes().put(FIELDS_ON_ERROR,
                                                properties.get("bindingResult"));
                        }

                        // add into session , the fields that user filled in that represents patientDto or NoteDto
                        if (userProvidedModelAttribute.isPresent()) {

                                if (userProvidedModelAttribute.get() instanceof PatientDto) {
                                        session.getAttributes().put("patient", userProvidedModelAttribute);
                                } else {
                                        session.getAttributes().put("note", userProvidedModelAttribute);
                                }
                        }

                        log.error("error of type {} : {} \t {}", typeOfOperation, pb.getTitle(), pb.getDetail());

                        session.getAttributes().put(ERROR_MESSAGE, "<h6><ins>A problem occurs, "
                                        + Objects.requireNonNull(pb.getTitle()).split("-")[1] + "</ins></h6>"
                                        + pb.getDetail() + ".");

                        return ERROR_MESSAGE;

                }

                if (body instanceof PatientDto) {
                        PatientDto patient = (PatientDto) body;
                        session.getAttributes().put(SUCCESS_MESSAGE, "<h6><ins>Sucessful action!</ins></h6>" +
                                        "Patient " + patient.getLastName() + " was correctly "
                                        + typeOfOperation);
                } else {
                        NoteDto note = (NoteDto) body;
                        session.getAttributes().put(SUCCESS_MESSAGE, "<h6><ins>Sucessful action!</ins></h6>" +
                                        "Note of patient " + note.getPatient().getName() + " was correctly "
                                        + typeOfOperation);
                }
                log.info("success : {} {}", body, typeOfOperation);

                return SUCCESS_MESSAGE;

        }

        /**
         * method to remove attributes of a websession and add them to model of view
         * 
         * @param model the model to add attribute
         * @param session the attribute to remove and add to model of view
         * @param messages list of (String)messages (name of attribute in session)to remove from websession
         *        and add them to model
         */
        private void moveSessionAttributeIntoModel(Model model, WebSession session, String... messages) {

                for (String message : messages) {
                        if (session.getAttribute(message) != null) {
                                model.addAttribute(message, session.getAttribute(message));
                                session.getAttributes().remove(message);
                        }
                }
        }
}
