package com.weiz.familia.infrastructure.services.imp;

import com.weiz.familia.api.responses.CiudadResponse;

import java.util.Set;


public interface ICiudadService {
    Set<CiudadResponse> readAll();
}
