package com.medilabosolutions.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;

@Controller
@RequestMapping("/")
public class GatewayController {
    
    @GetMapping("")
    public Mono<Rendering> home() {
        return Mono.just(Rendering.redirectTo("/front/").build());
    }
}
