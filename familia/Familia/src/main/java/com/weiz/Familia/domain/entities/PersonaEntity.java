package com.weiz.Familia.domain.entities;

import com.weiz.Familia.api.requests.PersonaRequest;
import com.weiz.Familia.api.responses.PersonaResponse;
import com.weiz.Familia.util.Enums.OcupacionEnum;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.beans.BeanUtils;

import java.lang.reflect.Field;
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
    @Id
    @Column(name = "numero_documento", nullable = false, unique = true)
    private String numero_documento;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "apellidos", nullable = false)
    private String apellidos;

    @Column(name = "fecha_nacimiento", nullable = false)
    private LocalDate fecha_nacimiento;

    @Column(name = "correo_electronico", nullable = false)
    private String correo_electronico;

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

    public static PersonaResponse entityToResponse(PersonaEntity entity){
        PersonaResponse res = new PersonaResponse();
        BeanUtils.copyProperties(entity, res);
        res.setCiudad(CiudadEntity.entityToResponse(entity.getCiudad()));
        return res;
    }

    public static PersonaEntity requestToEntity(PersonaRequest request){
        PersonaEntity res = new PersonaEntity();
        BeanUtils.copyProperties(request, res);
        return res;
    }

    @PostPersist
    public void postPersist(){
        validarViabilidad();
    }

    @PostUpdate
    public void postUpdate(){
        validarViabilidad();
    }

    private void validarViabilidad(){
        LocalDate now = LocalDate.now();
        int edad = Period.between(this.fecha_nacimiento, now).getYears();
        this.setEsViable(edad >= 18 && edad <= 65);
    }

    public void merge(PersonaEntity updateData){
        Class<?> clazz = this.getClass();
        Field[] fields = clazz.getDeclaredFields();

        for (Field field : fields) {
            // Excluir campos que no deben actualizarse
            if (field.getName().equals("numero_documento")) continue;
            try {
                field.setAccessible(true); //permitir el acceso a campos privados
                Object newValue = field.get(updateData);

                if(newValue != null){
                    if(newValue instanceof String strValue){
                        if(!strValue.isBlank()){
                            field.set(this, newValue);
                        }
                    } else {
                        field.set(this, newValue);
                    }
                }
            } catch (IllegalAccessException e) {
                System.err.println("Error actualizando campo: " + field.getName());
                e.printStackTrace();
            }
        }
    }

}
