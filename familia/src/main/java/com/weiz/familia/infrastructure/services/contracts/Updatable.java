package com.weiz.familia.infrastructure.services.contracts;

public interface Updatable<Req, Res, Id> {
    Res actualizar(Req request, Id id);
}
