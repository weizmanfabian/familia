package com.weiz.familia.api.controllers;

import com.weiz.familia.api.responses.CiudadResponse;
import com.weiz.familia.infrastructure.services.contracts.Listable;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
@RequestMapping("/ciudades")
@AllArgsConstructor
@Slf4j
public class CiudadController {
    private final Listable<CiudadResponse> ciudadService;

    @GetMapping
    public ResponseEntity<Set<CiudadResponse>> consultarTodas(){
        Set<CiudadResponse> res = ciudadService.consultarTodas();
        return res.isEmpty()
                ? ResponseEntity.noContent().build()
                : ResponseEntity.ok(res);
    }
}
