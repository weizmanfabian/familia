package com.weiz.familia.api.requests;

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
