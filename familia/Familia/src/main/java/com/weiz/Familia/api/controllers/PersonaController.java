package com.weiz.Familia.api.controllers;

import com.weiz.Familia.api.requests.PersonaRequest;
import com.weiz.Familia.api.responses.PersonaResponse;
import com.weiz.Familia.infraestructure.services.imp.IPersonaService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.InvocationTargetException;
import java.util.Set;

@RestController
@RequestMapping("/personas")
@AllArgsConstructor
@Slf4j
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class PersonaController {

    private final IPersonaService personaService;

    @GetMapping
    public ResponseEntity<Set<PersonaResponse>> readAll() {
        ResponseEntity.ok();
        Set<PersonaResponse> response = personaService.readAll();
        return response.isEmpty()
                ? ResponseEntity.noContent().build()
                : ResponseEntity.ok(response);
    }

    @GetMapping(path = "{numero_documento}")
    public ResponseEntity<PersonaResponse> get(@PathVariable String numero_documento) {
        return ResponseEntity.ok(personaService.readById(numero_documento));
    }

    @PostMapping
    public ResponseEntity<PersonaResponse> create(@Valid @RequestBody PersonaRequest request) {
        return ResponseEntity.ok(personaService.create(request));
    }

    @PutMapping(path = "{numero_documento}")
    public ResponseEntity<PersonaResponse> put(@Valid @PathVariable String numero_documento, @RequestBody PersonaRequest request) throws InvocationTargetException, IllegalAccessException {
        return ResponseEntity.ok(personaService.update(request, numero_documento));
    }

    @DeleteMapping(path = "{numero_documento}")
    public ResponseEntity<Void> delete(@PathVariable String numero_documento){
        personaService.delete(numero_documento);
        return ResponseEntity.noContent().build();
    }
    
}
