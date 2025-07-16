package com.weiz.Familia.infraestructure.services;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.weiz.Familia.domain.repositories.CiudadRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import com.weiz.Familia.api.responses.CiudadResponse;
import com.weiz.Familia.domain.entities.CiudadEntity;
import com.weiz.Familia.infraestructure.services.imp.ICiudadService;

@Service
@RequiredArgsConstructor
@Slf4j
public class CiudadService implements ICiudadService{

    private final CiudadRepository ciudadRepository;

    public Set<CiudadResponse> readAll(){
        return StreamSupport.stream(ciudadRepository.findAll().spliterator(),false)
                    .map(CiudadEntity::entityToResponse)
                    .collect(Collectors.toSet());
    }
}