package com.medilabosolutions.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.reactive.function.client.WebClient;
import com.medilabosolutions.record.Patient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Controller
public class UiController {

    @Value("${gateway.base.url}")
    private String gatewayBaseUrl;

    @Autowired
    private WebClient webclient;

    /**
     * display the index page of medilabo-solution with the list of all registred patients
     * 
     * @param model model to add patients to the view
     * @return the view "index.html"
     */
    @GetMapping("/")
    public String index(Model model) {
        Flux<Patient> patients = webclient.get().uri(gatewayBaseUrl + "/patients")
                .retrieve().bodyToFlux(Patient.class);

        // TODO gestion des erreurs en récupérant le flux de patients (flux.onErrorResume)

        model.addAttribute("patients", patients);
        return "index";
    }

    /**
     * display record of patient 
     * @param patientId the id of patient 
     * @param model model to return to the view
     * @return the view of the record of patient (personnal informations for the moment)
     */
    @GetMapping("/patient-record/{id}")
    public String getPatientRecord(@PathVariable(value = "id") Long patientId, Model model) {

        Mono<Patient> patient = webclient.get().uri(gatewayBaseUrl + "/patients/{id}", patientId)
                .retrieve().bodyToMono(Patient.class);

        // TODO gestion du retour mono vide

        model.addAttribute("patient", patient);
        return "patient-record";
    }
}
