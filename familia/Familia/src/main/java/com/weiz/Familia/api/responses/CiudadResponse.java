package com.weiz.Familia.api.responses;

import jakarta.persistence.Column;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
public class CiudadResponse {
    Integer id;
    String nombre;
    String departamento;
}
