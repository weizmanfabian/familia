package com.weiz.Familia.api.responses;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.weiz.Familia.util.Enums.OcupacionEnum;
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
    private String numero_documento;
    private String nombre;
    private String apellidos;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate fecha_nacimiento;
    private String correo_electronico;
    private String telefono;
    private OcupacionEnum ocupacion;
    private CiudadResponse ciudad;
    private boolean esViable;
}
