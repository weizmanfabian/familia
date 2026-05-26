package com.weiz.familia.api.responses;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.weiz.familia.shared.enums.OcupacionEnum;
import lombok.*;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
public class PersonaResponse {
    private String numeroDocumento;
    private String nombre;
    private String apellidos;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate fechaNacimiento;
    private String correoElectronico;
    private String telefono;
    private OcupacionEnum ocupacion;
    private CiudadResponse ciudad;
    private boolean esViable;
}
