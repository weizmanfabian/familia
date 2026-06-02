package com.weiz.familia.api.responses;

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
