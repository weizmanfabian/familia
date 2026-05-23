package com.weiz.Familia.infraestructure.services.imp;

import com.weiz.Familia.api.responses.CiudadResponse;

import java.util.Set;


public interface ICiudadService {
    Set<CiudadResponse> readAll();
}
