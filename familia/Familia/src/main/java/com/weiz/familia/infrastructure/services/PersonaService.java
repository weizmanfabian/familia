package com.weiz.familia.infrastructure.services;

import com.weiz.familia.api.requests.PersonaRequest;
import com.weiz.familia.api.responses.PersonaResponse;
import com.weiz.familia.domain.entities.PersonaEntity;
import com.weiz.familia.domain.repositories.CiudadRepository;
import com.weiz.familia.domain.repositories.PersonaRepository;
import com.weiz.familia.infrastructure.mappers.PersonaMapper;
import com.weiz.familia.infrastructure.services.contracts.Creatable;
import com.weiz.familia.infrastructure.services.contracts.Deletable;
import com.weiz.familia.infrastructure.services.contracts.Listable;
import com.weiz.familia.infrastructure.services.contracts.Readable;
import com.weiz.familia.infrastructure.services.contracts.Updatable;
import com.weiz.familia.shared.exceptions.CustomException;
import com.weiz.familia.shared.exceptions.IdNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
@Slf4j
public class PersonaService implements
        Listable<PersonaResponse>,
        Readable<PersonaResponse, String>,
        Creatable<PersonaRequest, PersonaResponse>,
        Updatable<PersonaRequest, PersonaResponse, String>,
        Deletable<String> {

    private final CiudadRepository ciudadRepository;
    private final PersonaRepository personaRepository;
    private final PersonaMapper personaMapper;

    @Override
    public Set<PersonaResponse> consultarTodas() {
        return StreamSupport.stream(personaRepository.findAll().spliterator(), false)
                .map(personaMapper::toResponse)
                .collect(Collectors.toSet());
    }

    @Override
    public PersonaResponse crear(PersonaRequest request) {
        personaRepository.findById(request.getNumeroDocumento())
                .ifPresent(p -> {
                    throw new CustomException(String.format("La persona con documento %s ya existe", request.getNumeroDocumento()));
                });

        var ciudad = ciudadRepository.findById(request.getIdCiudad()).orElseThrow(() -> new IdNotFoundException("Ciudad"));
        PersonaEntity personaPrePersist = personaMapper.toEntity(request);
        personaPrePersist.setCiudad(ciudad);
        personaPrePersist.validarViabilidad();
        PersonaEntity personaPersisted = personaRepository.save(personaPrePersist);
        return personaMapper.toResponse(personaPersisted);
    }

    @Override
    public PersonaResponse consultarPorId(String numeroDocumento) {
        PersonaEntity persona = personaRepository.findById(numeroDocumento).orElseThrow(() -> new IdNotFoundException("Persona"));
        return personaMapper.toResponse(persona);
    }

    @Override
    public PersonaResponse actualizar(PersonaRequest request, String numeroDocumento) {
        PersonaEntity personaSaved = personaRepository.findById(numeroDocumento).orElseThrow(() -> new IdNotFoundException("Persona"));
        var ciudad = ciudadRepository.findById(request.getIdCiudad()).orElseThrow(() -> new IdNotFoundException("Ciudad"));
        personaMapper.actualizar(personaSaved, request);
        personaSaved.setCiudad(ciudad);
        personaSaved.validarViabilidad();
        PersonaEntity personaPersisted = personaRepository.save(personaSaved);
        return personaMapper.toResponse(personaPersisted);
    }

    @Override
    public void eliminar(String numeroDocumento) {
        personaRepository.findById(numeroDocumento).orElseThrow(() -> new IdNotFoundException("Persona"));
        personaRepository.deleteById(numeroDocumento);
    }
}
