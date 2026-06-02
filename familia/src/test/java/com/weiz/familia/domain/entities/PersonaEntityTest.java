package com.weiz.familia.domain.entities;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class PersonaEntityTest {

    @DisplayName("calcularViabilidad marca viable solo en el rango [18, 65] años inclusive")
    @ParameterizedTest(name = "edad {0} → esViable={1}")
    @CsvSource({
            "17, false",
            "18, true",
            "65, true",
            "66, false"
    })
    void calcularViabilidad_segunEdad_estableceEsViable(int edad, boolean esViableEsperado) {
        PersonaEntity persona = PersonaEntity.builder()
                .fechaNacimiento(LocalDate.now().minusYears(edad))
                .build();

        persona.calcularViabilidad();

        assertThat(persona.isEsViable()).isEqualTo(esViableEsperado);
    }
}
