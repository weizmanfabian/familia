package com.weiz.Familia.infraestructure.services.imp;

import com.weiz.Familia.api.requests.PersonaRequest;
import com.weiz.Familia.api.responses.PersonaResponse;
import com.weiz.Familia.infraestructure.abstractService.CrudService;

public interface IPersonaService extends CrudService<PersonaRequest, PersonaResponse, String> {
}
