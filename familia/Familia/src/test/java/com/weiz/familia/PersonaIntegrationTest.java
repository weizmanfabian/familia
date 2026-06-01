package com.weiz.familia;

import com.weiz.familia.domain.entities.CiudadEntity;
import com.weiz.familia.domain.repositories.CiudadRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class PersonaIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private CiudadRepository ciudadRepository;

    @Test
    @Transactional
    @DisplayName("POST /personas con datos válidos crea la persona y calcula su viabilidad")
    void postPersonas_conDatosValidos_creaPersonaYCalculaViabilidad() throws Exception {
        CiudadEntity ciudad = ciudadRepository.save(
                CiudadEntity.builder().nombre("Bogotá").departamento("Cundinamarca").build());

        String requestBody = """
                {
                  "numeroDocumento": "1234567890",
                  "nombre": "Juan",
                  "apellidos": "Pérez Gómez",
                  "fechaNacimiento": "1990-01-15",
                  "correoElectronico": "juan.perez@example.com",
                  "telefono": "3001234567",
                  "idCiudad": %d,
                  "ocupacion": "INDEPENDIENTE"
                }
                """.formatted(ciudad.getId());

        mockMvc.perform(post("/personas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numeroDocumento").value("1234567890"))
                .andExpect(jsonPath("$.nombre").value("Juan"))
                .andExpect(jsonPath("$.esViable").value(true))
                .andExpect(jsonPath("$.ciudad.nombre").value("Bogotá"));
    }
}
