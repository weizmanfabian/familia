package com.weiz.familia.infrastructure.services.contracts;

public interface Creatable<Req, Res> {
    Res crear(Req request);
}
