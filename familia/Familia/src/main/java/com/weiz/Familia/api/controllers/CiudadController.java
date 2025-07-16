package com.weiz.Familia.api.controllers;

import com.weiz.Familia.api.responses.CiudadResponse;
import com.weiz.Familia.infraestructure.services.imp.ICiudadService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
@RequestMapping("/ciudades")
@AllArgsConstructor
@Slf4j
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class CiudadController {
    private final ICiudadService ciudadService;

    @GetMapping
    public ResponseEntity<Set<CiudadResponse>> readAll(){
        Set<CiudadResponse> res = ciudadService.readAll();
        return res.isEmpty()
                ? ResponseEntity.noContent().build()
                : ResponseEntity.ok(res);
    }
}
