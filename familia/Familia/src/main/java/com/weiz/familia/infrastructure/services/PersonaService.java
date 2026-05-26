package com.weiz.familia.infrastructure.services;

import com.weiz.familia.api.requests.PersonaRequest;
import com.weiz.familia.api.responses.PersonaResponse;
import com.weiz.familia.domain.entities.PersonaEntity;
import com.weiz.familia.domain.repositories.CiudadRepository;
import com.weiz.familia.domain.repositories.PersonaRepository;
import com.weiz.familia.infrastructure.services.imp.IPersonaService;
import com.weiz.familia.shared.exceptions.CustomException;
import com.weiz.familia.shared.exceptions.IdNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
@Slf4j
public class PersonaService implements IPersonaService {

    private final CiudadRepository ciudadRepository;
    private final PersonaRepository personaRepository;

    @Override
    public Set<PersonaResponse> readAll() {
        return StreamSupport.stream(personaRepository.findAll().spliterator(), false)
                .map(PersonaEntity::entityToResponse)
                .collect(Collectors.toSet());
    }

    @Override
    public PersonaResponse create(PersonaRequest request) {
        personaRepository.findById(request.getNumeroDocumento())
                .ifPresent(p -> {
                    throw new CustomException(String.format("La persona con documento %s ya existe", request.getNumeroDocumento()));
                });

        var ciudad = ciudadRepository.findById(request.getIdCiudad()).orElseThrow(() -> new IdNotFoundException("Ciudad"));
        var personaPrePersist = PersonaEntity.requestToEntity(request);
        personaPrePersist.setCiudad(ciudad);
        personaPrePersist.validarViabilidad();
        var personaPersisted = personaRepository.save(personaPrePersist);
        return PersonaEntity.entityToResponse(personaPersisted);
    }

    @Override
    public PersonaResponse readById(String numeroDocumento) {
        var persona = personaRepository.findById(numeroDocumento).orElseThrow(()-> new IdNotFoundException("Persona"));
        return PersonaEntity.entityToResponse(persona);
    }

    @Override
    public PersonaResponse update(PersonaRequest request, String numeroDocumento) throws InvocationTargetException, IllegalAccessException {
        var personaSaved = personaRepository.findById(numeroDocumento).orElseThrow(() -> new IdNotFoundException("Persona"));
        var ciudad = ciudadRepository.findById(request.getIdCiudad()).orElseThrow(() -> new IdNotFoundException("Ciudad"));
        var personaCurrent = PersonaEntity.requestToEntity(request);
        personaCurrent.setCiudad(ciudad);
        personaSaved.merge(personaCurrent);
        personaSaved.validarViabilidad();
        var personaPersisted = personaRepository.save(personaSaved);
        return PersonaEntity.entityToResponse(personaPersisted);
    }

    @Override
    public void delete(String numeroDocumento) {
        personaRepository.findById(numeroDocumento).orElseThrow(() -> new IdNotFoundException("Persona"));
        personaRepository.deleteById(numeroDocumento);
    }
}
