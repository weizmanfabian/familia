package com.weiz.familia.domain.entities;

import com.weiz.familia.shared.enums.OcupacionEnum;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.Period;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@ToString
@Table(name = "persona")
public class PersonaEntity {
    private static final int EDAD_VIABLE_MIN = 18;
    private static final int EDAD_VIABLE_MAX = 65;

    @Id
    @Column(name = "numero_documento", nullable = false, unique = true)
    private String numeroDocumento;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "apellidos", nullable = false)
    private String apellidos;

    @Column(name = "fecha_nacimiento", nullable = false)
    private LocalDate fechaNacimiento;

    @Column(name = "correo_electronico", nullable = false)
    private String correoElectronico;

    @Column(name = "telefono", nullable = false)
    private String telefono;

    @Enumerated(EnumType.STRING)
    @Column(name = "ocupacion", nullable = false)
    private OcupacionEnum ocupacion;

    @Column(name = "esviable")
    private boolean esViable;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ciudad_id", referencedColumnName = "id")
    private CiudadEntity ciudad;

    public void calcularViabilidad() {
        int edad = Period.between(this.fechaNacimiento, LocalDate.now()).getYears();
        this.esViable = edad >= EDAD_VIABLE_MIN && edad <= EDAD_VIABLE_MAX;
    }
}
