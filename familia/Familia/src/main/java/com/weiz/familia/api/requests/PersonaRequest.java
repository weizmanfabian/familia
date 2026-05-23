package com.weiz.Familia.api.requests;

import com.weiz.Familia.domain.entities.CiudadEntity;
import com.weiz.Familia.util.Enums.OcupacionEnum;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
public class PersonaRequest {
    @Size(min = 5, max = 20, message = "El documento debe tener entre 5 y 20 caracteres")
    private String numero_documento;

    @NotBlank(message = "El nombre no puede estar vacío")
    @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
    @Pattern(regexp = "^[\\p{L}\\s'-]+$", message = "El nombre solo puede contener letras y espacios")
    private String nombre;

    @NotBlank(message = "Los apellidos no pueden estar vacíos")
    @Size(min = 2, max = 100, message = "Los apellidos deben tener entre 2 y 100 caracteres")
    private String apellidos;

    @NotNull(message = "La fecha de nacimiento es obligatoria")
    @Past(message = "La fecha de nacimiento debe ser en el pasado")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate fecha_nacimiento;

    @Email(message = "Debe ser un correo electrónico válido")
    @Size(max = 100, message = "El correo no puede exceder los 100 caracteres")
    private String correo_electronico;

    @Pattern(regexp = "^\\+?[0-9\\s()-]{7,15}$",
            message = "El teléfono debe ser un número válido con formato internacional")
    private String telefono;

    @NotNull(message = "La ocupación no puede ser nula")
    private OcupacionEnum ocupacion;

    @NotNull(message = "La ciudad no puede ser nula")
    private Integer idCiudad;
}
