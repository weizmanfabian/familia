package com.weiz.familia.api.controllers;

import com.weiz.familia.api.requests.PersonaRequest;
import com.weiz.familia.api.responses.PersonaResponse;
import com.weiz.familia.infrastructure.services.PersonaService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/personas")
@AllArgsConstructor
@Slf4j
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class PersonaController {

    private final PersonaService personaService;

    @GetMapping
    public ResponseEntity<Set<PersonaResponse>> consultarTodas() {
        Set<PersonaResponse> response = personaService.consultarTodas();
        return response.isEmpty()
                ? ResponseEntity.noContent().build()
                : ResponseEntity.ok(response);
    }

    @GetMapping(path = "{numeroDocumento}")
    public ResponseEntity<PersonaResponse> consultarPorDocumento(@PathVariable String numeroDocumento) {
        return ResponseEntity.ok(personaService.consultarPorId(numeroDocumento));
    }

    @PostMapping
    public ResponseEntity<PersonaResponse> crear(@Valid @RequestBody PersonaRequest request) {
        return ResponseEntity.ok(personaService.crear(request));
    }

    @PutMapping(path = "{numeroDocumento}")
    public ResponseEntity<PersonaResponse> actualizar(@Valid @PathVariable String numeroDocumento, @RequestBody PersonaRequest request) {
        return ResponseEntity.ok(personaService.actualizar(request, numeroDocumento));
    }

    @DeleteMapping(path = "{numeroDocumento}")
    public ResponseEntity<Void> eliminar(@PathVariable String numeroDocumento){
        personaService.eliminar(numeroDocumento);
        return ResponseEntity.noContent().build();
    }

}
