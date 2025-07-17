import { Component, OnInit } from '@angular/core';
import { NgIf, NgFor, CommonModule } from '@angular/common';
import { PersonaModel } from '../../interfaces/PersonaModel';
import { PersonaCreateModel } from '../../interfaces/PersonaCreateModel';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { CiudadModel } from '../../interfaces/CiudadModel';
import { PersonaService } from '../../services/persona-service';
import { CiudadService } from '../../services/ciudad-service';
import { NgbActiveModal, NgbModule } from '@ng-bootstrap/ng-bootstrap';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-persona-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, NgbModule, NgIf, NgFor, FormsModule],
  templateUrl: './persona-form.html',
  styleUrl: './persona-form.css',
})
export class PersonaForm implements OnInit {
  personaForm: FormGroup;
  isEdit = false;
  persona: PersonaModel | null = null;
  loading = false;
  validationErrors: ValidationErrors = {};
  ciudades: CiudadModel[] = [];
  loadPersonas: () => void = () => {}; //función pasada desde el componente padre para recargar la lista de personas

  loadingCiudades: boolean = true;

  // Campos del formulario
  // Estos campos deben coincidir con los nombres de los controles en el formulario
  formControls: string[] = ['numero_documento', 'nombre', 'apellidos', 'fecha_nacimiento', 
                         'correo_electronico', 'telefono', 'ocupacion', 'ciudad_id'];


  //Ocupaciones disponibles
  ocupaciones = ['EMPLEADO', 'INDEPENDIENTE', 'ESTUDIANTE', 'JUBILADO'];

  constructor(
    private fb: FormBuilder,
    private activeModal: NgbActiveModal,
    private personaService: PersonaService,
    private ciudadService: CiudadService
  ) {
    this.personaForm = this.createForm();
  }

  ngOnInit(): void {
    if (this.isEdit && this.persona) {
      this.loadPersonaData();
    }
    this.loadCiudades();
  }

  loadCiudades(): void {
    this.loadingCiudades = true;
    this.ciudadService.getCiudades().subscribe({
      next: (ciudades) => {
        this.ciudades = ciudades;
        this.loadingCiudades = false;
      },
      error: (error) => {
        console.error('Error al cargar las ciudades', error);
        this.loadingCiudades = false;
      }
    });
  }

  // Método para crear el formulario con sus validaciones
  // Este método se puede llamar desde el constructor o desde ngOnInit
  createForm(): FormGroup {
    return this.fb.group({
      numero_documento: ['', [Validators.required, Validators.minLength(8)]],
      nombre: ['', [Validators.required, Validators.minLength(0), Validators.maxLength(50)]],
      apellidos: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(100)]],
      fecha_nacimiento: ['', [Validators.required]],
      correo_electronico: ['', [Validators.required, Validators.email]],
      telefono: ['', [Validators.required, Validators.minLength(10)]],
      ocupacion: ['', [Validators.required]],
      ciudad_id: ['', [Validators.required]]
    });
  }



  loadPersonaData(): void {
    if (this.persona) {
      this.personaForm.patchValue({
        numero_documento: this.persona.numero_documento,
        nombre: this.persona.nombre,
        apellidos: this.persona.apellidos,
        fecha_nacimiento: this.persona.fecha_nacimiento,
        correo_electronico: this.persona.correo_electronico,
        telefono: this.persona.telefono,
        ocupacion: this.persona.ocupacion,
        ciudad_id: this.persona.ciudad.id
      })
    }
  }

  get f() {
    return this.personaForm.controls;
  }

  getFieldError(fieldName: string): string | null {
    return this.validationErrors[fieldName] || null;
  }

  hasFieldError(fieldName: string): boolean {
    const field = this.personaForm.get(fieldName);
    return !!this.validationErrors[fieldName] || (field?.invalid && field?.touched) || false;
  }

  clearValidationErrors(): void {
    this.validationErrors = {};
  }

  onSubmit(): void {
    if (this.personaForm.invalid) {
      this.personaForm.markAllAsTouched();
      return;
    }

    this.loading = true;
    this.clearValidationErrors();

    const formData = this.personaForm.value;

    const personaToSend: PersonaCreateModel = {
      numero_documento: formData.numero_documento,
      nombre: formData.nombre,
      apellidos: formData.apellidos,
      fecha_nacimiento: formData.fecha_nacimiento,
      correo_electronico: formData.correo_electronico,
      telefono: formData.telefono,
      ocupacion: formData.ocupacion,
      idCiudad: parseInt(formData.ciudad_id)
    }

    const operation = this.isEdit
      ? this.personaService.updatePersona(this.persona!.numero_documento, personaToSend)
      : this.personaService.createPersona(personaToSend);

    operation.subscribe({
      next: (response: any) => {
        if (response.code === 400 || response.status === 500) {
          if (response.error && response.error.message) {
            this.validationErrors['numero_documento'] = response.error.message;
          } else if (response.errors) {
            this.validationErrors = response.errors;
          } else {
            this.validationErrors['numero_documento'] = response.message;
          }
          this.markFieldsWithError();
          this.focusFirstError();
        } else {
          this.loadPersonas(); // Llamamos a la función antes de cerrar el modal
          this.activeModal.close('saved');
          Swal.fire({
            toast: true,
            position: "top-end",
            icon: "success",
            title: this.isEdit ? "Persona actualizada correctamente" : "Persona creada correctamente",
            showConfirmButton: false,
            timer: 1500
          });
        }
        this.loading = false;
      },
      error: (error: any) => {
        this.loading = false;
        alert('Ocurrió un error al procesar la solicitud');
        console.error('Error en PersonaForm: ', error);
      }
    });
  }

  private focusFirstError(): void {
    
    
    // Buscar el primer campo que tenga un error
    const firstErrorField = this.formControls.find(field => 
      this.validationErrors[field] || this.personaForm.get(field)?.errors
    );

    if (firstErrorField) {
      // Usar setTimeout para asegurar que el DOM se ha actualizado
      setTimeout(() => {
        const element = document.getElementById(firstErrorField);
        if (element) {
          element.focus();
        }
      });
    }
  }

  private markFieldsWithError(): void {
    
    // Marcar todos los campos con errores
    this.formControls.forEach(field => {
      const control = this.personaForm.get(field);
      if (control && (this.validationErrors[field] || control.errors)) {
        control.markAsTouched();
        if (this.validationErrors[field]) {
          control.setErrors({ ...control.errors, serverError: this.validationErrors[field] });
        }
      }
    });
  }

  onCancel(): void {
    this.activeModal.dismiss('cancel');
  }

  get modalTitle(): string {
    return this.isEdit ? 'Editar Persona' : 'Crear Persona';
  }

  get submitButtonText(): string {
    return this.isEdit ? 'Actualizar' : 'Crear';
  }

}
