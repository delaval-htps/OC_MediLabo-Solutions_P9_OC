package com.medilabosolutions.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.crypto.password.PasswordEncoder;
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
import com.medilabosolutions.dto.PatientDto;
import com.medilabosolutions.model.RestPage;
import com.medilabosolutions.model.UserCredential;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;


@Controller
@Slf4j
public class UiController {

        @Value("${path.patient.service}")
        private String pathPatientService;

        private final WebClient webclient;
        private final PasswordEncoder passwordEncoder;
        private final ModelMapper modelMapper;

        @Autowired
        public UiController(WebClient webclient, ModelMapper modelMapper, PasswordEncoder passwordEncoder) {
                this.webclient = webclient;
                this.modelMapper = modelMapper;
                this.passwordEncoder = passwordEncoder;
        }

        private static final String ERROR_MESSAGE = "errorMessage";
        private static final String SUCCESS_MESSAGE = "successMessage";

        private static final String CREATION = "created !";
        private static final String UPDATE = "updated !";
        private static final String DELETE = "deleted !";

        /**
         * endpoint to show form login
         * 
         * @return view login
         */
        @GetMapping("/")
        public Mono<Rendering> getLogin(Model model, WebSession session) {

                // Retrieve jwt if it is present
                String jwtValue = session.getAttributeOrDefault("jwtoken", "");


                if (!jwtValue.equals("")) {
                        /*
                         * case user has a jwt : redirect to "/patients" : Gateway has a filter in routes to validate the token, if jwt is not valid then we have a redirection to GET "/login" with loginError (cf GET
                         * ("/patients"))
                         */
                        return Mono.just(Rendering.redirectTo("/patients").build());
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
                                                session.getAttributes().put("jwtoken", response.headers().header("jwtoken").get(0));
                                                return Mono.just(Rendering.redirectTo("/patients").build());
                                        } else {
                                                return redirectToLoginPage(model, true);
                                        }
                                });
        }

        /**
         * Endpoint to display the index page of medilabo-solution with the list of all registred PatientDtos (never get error , just only a avoid list of patients)
         * 
         * @param model model to add PatientDtos to the view
         * @return the view "index.html" //
         */
        @GetMapping("/patients")
        public Mono<Rendering> index(@RequestParam(value = "page") Optional<Integer> page,
                        @RequestParam(value = "size") Optional<Integer> size,
                        Model model, WebSession session) {

                int currentPage = page.orElse(0);
                int pageSize = size.orElse(10);

                // check if jwt token is present
                String jwtValue = session.getAttributeOrDefault("jwtoken", "");


                return webclient.get().uri(pathPatientService + "/{page}/{size}", currentPage, pageSize)
                                .headers(h -> h.add("jwtoken", jwtValue))
                                .exchangeToMono(response -> {
                                        if (response.statusCode().equals(HttpStatus.UNAUTHORIZED)) {
                                                return redirectToLoginPage(model, true);
                                        }
                                        return response.bodyToMono(RestPage.class)
                                                        .flatMap(restPage -> {
                                                                restPage.getContent().stream().map(p -> modelMapper.map(p, PatientDto.class));

                                                                if (restPage.getTotalPages() > 0) {
                                                                        List<Integer> pageNumbers = IntStream.rangeClosed(1, restPage.getTotalPages())
                                                                                        .boxed().collect(Collectors.toList());
                                                                        model.addAttribute("pageNumbers", pageNumbers);
                                                                }

                                                                model.addAttribute("fieldsOnError", new HashMap<String, String>());
                                                                model.addAttribute("patientToCreate", new PatientDto());
                                                                model.addAttribute("patientPages", restPage);

                                                                // in case of bindingResult , we have to add to model fieldsOnError and to override patientwith fields filled
                                                                // in by user to be able to display his errors
                                                                addAttributeSessionToModel(model, session, ERROR_MESSAGE, SUCCESS_MESSAGE, "fieldsOnError", "patientToCreate");

                                                                log.info("request GET all patients");

                                                                return Mono.just(Rendering.view("index").build());
                                                        });

                                });
        }

        /**
         * Endpoint to display record of Patient
         * 
         * @param patientId the id of Patient
         * @param model model to return to the view
         * @return the view of the record of Patient (personnal informations for the moment)
         */
        @GetMapping("/patients/{id}")
        public Mono<Rendering> getPatientRecord(@PathVariable(value = "id") Long patientId, WebSession session, Model model) {

                // check if jwt token is present
                String jwtValue = session.getAttributeOrDefault("jwtoken", "");

                return webclient.get().uri(pathPatientService + "/{id}", patientId)
                                .headers(h -> h.add("jwtoken", jwtValue))
                                .exchangeToMono(response -> {
                                        if (response.statusCode().equals(HttpStatus.UNAUTHORIZED)) {
                                                return redirectToLoginPage(model, true);
                                        }
                                        return response.bodyToMono(PatientDto.class)
                                                        .flatMap(body -> {
                                                                log.info("GET patient-record with id {} = {}", patientId, body);

                                                                model.addAttribute("fieldsOnError", new HashMap<String, String>());
                                                                model.addAttribute("patient", body);

                                                                // in case of bindingResult , we have to add to model fieldsOnError and
                                                                // to override patient with fields filled in by user to be able to display his errors
                                                                addAttributeSessionToModel(model, session, ERROR_MESSAGE, SUCCESS_MESSAGE, "fieldsOnError");

                                                                return Mono.just(Rendering.view("patient-record").build());
                                                        });

                                });
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
                String jwtValue = session.getAttributeOrDefault("jwtoken", "");


                return webclient.post().uri(pathPatientService)
                                .headers(h -> h.add("jwtoken", jwtValue))
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(Mono.just(patientToCreate), PatientDto.class)

                                .exchangeToMono(response -> {

                                        if (response.statusCode().equals(HttpStatus.UNAUTHORIZED)) {
                                                return redirectToLoginPage(model, true);
                                        }

                                        return (response.statusCode().isError()
                                                        ? response.bodyToMono(ProblemDetail.class)
                                                        : response.bodyToMono(PatientDto.class))
                                                                        .flatMap(body -> {
                                                                                setSessionAttribute(body, session, CREATION, Optional.of(patientToCreate));
                                                                                log.info("request POST createPatient");
                                                                                return Mono.just(Rendering.redirectTo("/patients").build());
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
         * @return Mono<Rendering> with view to index if success or redirection to the same page (form) if error
         */
        @PostMapping("/patients/update/{id}")
        public Mono<Rendering> updatePatient(@PathVariable(value = "id") Long patientId,
                        @ModelAttribute(value = "patient") PatientDto updatedPatient,
                        WebSession session, Model model) {

                // check if jwt token is present
                String jwtValue = session.getAttributeOrDefault("jwtoken", "");

                // delete id of patientDto to have a id null (must to have a correct validation in patient-service)
                updatedPatient.setId(null);

                return webclient.put().uri(pathPatientService + "/{id}", patientId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .headers(h -> h.add("jwtoken", jwtValue))
                                .body(Mono.just(updatedPatient), PatientDto.class)

                                .exchangeToMono(response -> {

                                        if (response.statusCode().equals(HttpStatus.UNAUTHORIZED)) {
                                                return redirectToLoginPage(model, true);
                                        }

                                        return (response.statusCode().isError()
                                                        ? response.bodyToMono(ProblemDetail.class)
                                                        : response.bodyToMono(PatientDto.class))
                                                                        .flatMap(body -> {
                                                                                log.info("request POST updatePatient with id {}", patientId);

                                                                                return setSessionAttribute(body, session, UPDATE, Optional.of(updatedPatient)).equals(SUCCESS_MESSAGE)
                                                                                                ? Mono.just(Rendering.redirectTo("/patients").build())
                                                                                                : Mono.just(Rendering.redirectTo("/patients/" + patientId).build());
                                                                        });
                                });

        }

        /**
         * endpoint to delete a patient with given id in request
         * 
         * @param patientId the given id of patient to delete
         * @param session the websession to redirect to same view "/"
         * @return Mono<Renderring> for redirection
         */
        @GetMapping("/patients/delete/{id}")
        public Mono<Rendering> deletePatient(@PathVariable(value = "id") Long patientId, Model model, WebSession session) {

                // check if jwt token is present
                String jwtValue = session.getAttributeOrDefault("jwtoken", "");



                return webclient.delete().uri(pathPatientService + "/{id}", patientId)
                                .headers(h -> h.add("jwtoken", jwtValue))
                                .exchangeToMono(response -> {

                                        if (response.statusCode().equals(HttpStatus.UNAUTHORIZED)) {
                                                return redirectToLoginPage(model, true);
                                        }

                                        return (response.statusCode().isError()
                                                        ? response.bodyToMono(ProblemDetail.class)
                                                        : response.bodyToMono(PatientDto.class))

                                                                        .flatMap(body -> {
                                                                                log.info("request GET deletePatient");
                                                                                setSessionAttribute(body, session, DELETE, Optional.empty());

                                                                                return Mono.just(Rendering.redirectTo("/patients").build());
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
         * method to add an error (with bindingResult, if exists) or success message to websession for a futur redirection (cause spring webflux not supports redirectAttributes)
         * 
         * @param body body content of a response from webclient request to add to message
         * @param session the websession of the request of endpoint
         * @param typeOfOperation String to modify message in toast in case of success message according to type of operation: create/update or delete a patient
         * @param userProvidedPatient optional parameter of user-provided patient in form to create one or update a existing one
         * @return String : if body is ProblemDetail then return is ERRORMESSAGE (with if exists bindingResult and user-provided patient ) else customized SUCCESSMESSAGE with type of operation ( created,
         *         updated or deleted !)
         */
        private String setSessionAttribute(Object body, WebSession session, String typeOfOperation, Optional<PatientDto> userProvidedPatient) {

                if (body instanceof ProblemDetail) {

                        ProblemDetail pb = (ProblemDetail) body;

                        Map<String, Object> properties = pb.getProperties();

                        // in case of existence of bindingResult in problemDetail , we have to add
                        // it to session
                        // to be retrieve in redirection and add fieldsOnError in model
                        if (properties != null && properties.containsKey("bindingResult")) {
                                session.getAttributes().put("fieldsOnError",
                                                properties.get("bindingResult"));
                        }

                        // add into session , the fields that user filled in that represents
                        // patientDto
                        if (userProvidedPatient.isPresent()) {
                                session.getAttributes().put("patient", userProvidedPatient);
                        }

                        log.error("error of type {} : {} \t {}", typeOfOperation, pb.getTitle(),
                                        pb.getDetail());
                        session.getAttributes().put(ERROR_MESSAGE, "<h6><ins>A problem occurs, "
                                        + Objects.requireNonNull(pb.getTitle()).split("-")[1] + "</ins></h6>"
                                        + pb.getDetail() + ".");

                        return ERROR_MESSAGE;

                } else {

                        PatientDto patient = (PatientDto) body;
                        session.getAttributes().put(SUCCESS_MESSAGE, "<h6><ins>Sucessful action!</ins></h6>" +
                                        "Patient " + patient.getLastName() + " was correctly "
                                        + typeOfOperation);
                        log.info("success : {} {}", body, typeOfOperation);

                        return SUCCESS_MESSAGE;
                }
        }


        /**
         * method to remove attributes of a websession and add them to model of view
         * 
         * @param model the model to add attribute
         * @param session the attribute to remove and add to model of view
         * @param messages list of (String)messages (name of attribute in session)to remove from websession and add them to model
         */
        private void addAttributeSessionToModel(Model model, WebSession session,
                        String... messages) {

                for (String message : messages) {

                        if (session.getAttribute(message) != null) {

                                model.addAttribute(message, session.getAttribute(message));

                                session.getAttributes().remove(message);
                        }
                }
        }

        /**
         * method to just verify if token is correctly saved in cookie session
         * 
         * @param session
         * @return
         */
        @GetMapping("/websession")
        public Mono<String> getWebsession(WebSession session) {
                if (session.getAttribute("jwtoken") != null) {
                        return Mono.just("session = " + session.getAttribute("jwtoken"));
                } else {
                        return Mono.just("le token n'est pas enregistré");
                }
        }


}
