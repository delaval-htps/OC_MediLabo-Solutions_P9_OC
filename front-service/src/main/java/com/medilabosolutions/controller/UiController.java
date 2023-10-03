package com.medilabosolutions.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Role;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.result.view.Rendering;
import org.springframework.web.server.WebSession;
import com.medilabosolutions.dto.PatientDto;
import com.medilabosolutions.model.RestPage;
import jakarta.annotation.security.RolesAllowed;
import reactor.core.publisher.Mono;

@Controller
public class UiController {

        @Value("${patient.service.url.from.gateway}")
        private String patientServiceUrl;

        @Autowired
        private WebClient webclient;

        @Autowired
        private ModelMapper modelMapper;

        private Logger logger = LogManager.getLogger(UiController.class);

        private static final String ERROR_MESSAGE = "errorMessage";
        private static final String SUCCESS_MESSAGE = "successMessage";

        private static final String CREATION = "created !";
        private static final String UPDATE = "updated !";
        private static final String DELETE = "deleted !";

        /**
         * Endpoint to display the index page of medilabo-solution with the list of all registred PatientDtos (never get error , just only a avoid list of patients)
         * 
         * @param model model to add PatientDtos to the view
         * @return the view "index.html" //
         */
        @GetMapping("/")
        public Mono<Rendering> index(@RequestParam(value = "page") Optional<Integer> page,
                        @RequestParam(value = "size") Optional<Integer> size,
                        Model model, WebSession session) {

                int currentPage = page.orElse(0);
                int pageSize = size.orElse(10);

                return webclient.get().uri(patientServiceUrl + "/{page}/{size}", currentPage, pageSize)
                                .retrieve()
                                .bodyToMono(RestPage.class)
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

                                        logger.info("request GET all patients");

                                        return Mono.just(Rendering.view("index").build());
                                });
        }

        /**
         * Endpoint to display record of Patient
         * 
         * @param patientId the id of Patient
         * @param model model to return to the view
         * @return the view of the record of Patient (personnal informations for the moment)
         */
        @GetMapping("/patient-record/{id}")
        public Mono<Rendering> getPatientRecord(@PathVariable(value = "id") Long patientId, WebSession session, Model model) {

                return webclient.get().uri(patientServiceUrl + "/{id}", patientId)
                                .retrieve()
                                .bodyToMono(PatientDto.class)
                                .flatMap(body -> {
                                        logger.info("GET patient-record with id {} = {}", patientId, body);

                                        model.addAttribute("fieldsOnError", new HashMap<String, String>());
                                        model.addAttribute("patient", body);

                                        // in case of bindingResult , we have to add to model fieldsOnError and
                                        // to override patient with fields filled in by user to be able to display his errors
                                        addAttributeSessionToModel(model, session, ERROR_MESSAGE, SUCCESS_MESSAGE, "fieldsOnError");

                                        return Mono.just(Rendering.view("patient-record").build());
                                });

                // TODO gestion du retour mono vide
        }

        /**
         * Endpoint to create a new patient
         * 
         * @param patientToCreate the patient to create from form of index.html
         * @param model model to add attributes
         * @param session session to add attribute to model when redirect to index.html
         * @return rendering with redirection to index.html
         */
        @PostMapping("/patient")
        public Mono<Rendering> createPatient(
                        @ModelAttribute(value = "patientToCreate") PatientDto patientToCreate,
                        Model model, WebSession session) {

                return webclient.post().uri(patientServiceUrl)
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(Mono.just(patientToCreate), PatientDto.class)

                                .exchangeToMono(response -> response.statusCode().isError()
                                                ? response.bodyToMono(ProblemDetail.class)
                                                : response.bodyToMono(PatientDto.class))

                                .flatMap(body -> {
                                        logger.info("request POST createPatient");
                                        setSessionAttribute(body, session, CREATION, Optional.of(patientToCreate));

                                        return Mono.just(Rendering.redirectTo("/").build());
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
        @PostMapping("/patient/{id}")
        public Mono<Rendering> updatePatient(@PathVariable(value = "id") Long patientId,
                        @ModelAttribute(value = "patient") PatientDto updatedPatient,
                        WebSession session,
                        Model model) {

                // delete id of patientDto to have a id null (must to have a correct validation in
                // patient-service)
                updatedPatient.setId(null);

                return webclient.put().uri(patientServiceUrl + "/{id}", patientId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(Mono.just(updatedPatient), PatientDto.class)

                                .exchangeToMono(response -> response.statusCode().isError()
                                                ? response.bodyToMono(ProblemDetail.class)
                                                : response.bodyToMono(PatientDto.class))

                                .flatMap(body -> {
                                        logger.info("request POST updatePatient with id {}", patientId);

                                        return setSessionAttribute(body, session, UPDATE, Optional.of(updatedPatient)).equals(SUCCESS_MESSAGE)
                                                        ? Mono.just(Rendering.redirectTo("/").build())
                                                        : Mono.just(Rendering.redirectTo("/patient-record/" + patientId).build());
                                });

        }

        /**
         * endpoint to delete a patient with given id in request
         * 
         * @param patientId the given id of patient to delete
         * @param session the websession to redirect to same view "/"
         * @return Mono<Renderring> for redirection
         */
        @GetMapping("/delete-patient/{id}")
        public Mono<Rendering> deletePatient(@PathVariable(value = "id") Long patientId,
                        WebSession session) {

                return webclient.delete().uri(patientServiceUrl + "/{id}", patientId)
                                .exchangeToMono(response -> response.statusCode().isError()
                                                ? response.bodyToMono(ProblemDetail.class)
                                                : response.bodyToMono(PatientDto.class))

                                .flatMap(body -> {
                                        logger.info("request GET deletePatient");
                                        setSessionAttribute(body, session, DELETE, Optional.empty());

                                        return Mono.just(Rendering.redirectTo("/").build());
                                });

        }

        /**
         * method to add an error (with bindingResult, if exists) or success message to websession for a futur redirection (cause spring webflux not supports redirectAttributes)
         * 
         * @param body body content of a response from webclient request to add to message
         * @param session the websession of the request of endpoint
         * @param typeOfOperation String to modify message in toast in case of success message according to type of operation: create/update or delete a patient
         * @param PatientDto... optional parameter of user-provided patient in form to create one or update a existing one
         * @return String : if body is ProblemDetail then return is ERRORMESSAGE (with if exists bindingResult and user-provided patient ) else customized SUCCESSMESSAGE with type of operation ( created,
         *         updated or deleted !)
         */
        private String setSessionAttribute(Object body, WebSession session, String typeOfOperation,
                        Optional<PatientDto> userProvidedPatient) {

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

                        logger.error("error of type {} : {} \t {}", typeOfOperation, pb.getTitle(),
                                        pb.getDetail());
                        session.getAttributes().put(ERROR_MESSAGE, "<h6><ins>A problem occurs, "
                                        + pb.getTitle().split("-")[1] + "</ins></h6>"
                                        + pb.getDetail() + ".");

                        return ERROR_MESSAGE;

                } else {

                        PatientDto patient = (PatientDto) body;
                        session.getAttributes().put(SUCCESS_MESSAGE, "<h6><ins>Sucessful action!</ins></h6>" +
                                        "Patient " + patient.getLastName() + " was correctly "
                                        + typeOfOperation);
                        logger.info("success : {} {}", body, typeOfOperation);

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

}
