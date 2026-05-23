package com.weiz.Familia.api.requests;

import jakarta.persistence.Column;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
public class CiudadRequest {
    String nombre;
    String departamento;
}
