package com.weiz.familia.api.controllers;

import com.weiz.familia.api.requests.PersonaRequest;
import com.weiz.familia.api.responses.PersonaResponse;
import com.weiz.familia.infrastructure.services.imp.IPersonaService;
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
        Set<PersonaResponse> response = personaService.readAll();
        return response.isEmpty()
                ? ResponseEntity.noContent().build()
                : ResponseEntity.ok(response);
    }

    @GetMapping(path = "{numeroDocumento}")
    public ResponseEntity<PersonaResponse> get(@PathVariable String numeroDocumento) {
        return ResponseEntity.ok(personaService.readById(numeroDocumento));
    }

    @PostMapping
    public ResponseEntity<PersonaResponse> create(@Valid @RequestBody PersonaRequest request) {
        return ResponseEntity.ok(personaService.create(request));
    }

    @PutMapping(path = "{numeroDocumento}")
    public ResponseEntity<PersonaResponse> put(@Valid @PathVariable String numeroDocumento, @RequestBody PersonaRequest request) throws InvocationTargetException, IllegalAccessException {
        return ResponseEntity.ok(personaService.update(request, numeroDocumento));
    }

    @DeleteMapping(path = "{numeroDocumento}")
    public ResponseEntity<Void> delete(@PathVariable String numeroDocumento){
        personaService.delete(numeroDocumento);
        return ResponseEntity.noContent().build();
    }
    
}
