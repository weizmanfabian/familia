package com.weiz.familia.infrastructure.services.contracts;

import java.util.Set;

public interface Listable<Res> {
    Set<Res> consultarTodas();
}
