package com.weiz.Familia.domain.entities;

import com.weiz.Familia.api.responses.CiudadResponse;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.beans.BeanUtils;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@ToString
@Table(name = "ciudad")
public class CiudadEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    Integer id;

    @Column(name = "nombre")
    String nombre;

    @Column(name = "departamento")
    String departamento;

    public static CiudadResponse entityToResponse(CiudadEntity entity) {
        CiudadResponse response = new CiudadResponse();
        BeanUtils.copyProperties(entity, response);
        return response;
    }
}
