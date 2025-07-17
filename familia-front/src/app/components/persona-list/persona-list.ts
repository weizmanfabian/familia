import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { NgbModal, NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { PersonaModel } from '../../interfaces/PersonaModel';
import { PersonaService } from '../../services/persona-service';
import { ErrorResponse } from '../../interfaces/errors';
import { CommonModule } from '@angular/common';
import { PersonaForm } from '../persona-form/persona-form';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-persona-list',
  standalone: true,
  imports: [CommonModule, NgbModule],
  templateUrl: './persona-list.html',
  styleUrl: './persona-list.css'
})
export class PersonaList implements OnInit {
  personas: PersonaModel[] = [];
  loading = false;
  error: string | null = null;

  constructor(
    private personaService: PersonaService,
    private modalService: NgbModal,
    private cdr: ChangeDetectorRef
  ){}

  ngOnInit(): void {
    this.loadPersonas();
  }

  loadPersonas(): void{
    this.loading = true;
    this.error = null;
    this.cdr.detectChanges();

    this.personaService.getPersonas().subscribe({
      next: (personas) => {
        this.personas = [...personas]; // Crear una nueva referencia del array
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: (error: ErrorResponse) => {
        this.error = error.message || 'Error al cargar las personas';
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  openCreateModal(): void{
    const modalRef = this.modalService.open(PersonaForm,{
      size: 'lg',
      backdrop: 'static',
      keyboard: false
    });

    modalRef.componentInstance.idEdit = false;
    modalRef.componentInstance.persona = null;
    modalRef.componentInstance.loadPersonas = () => {
      this.loadPersonas();
      this.cdr.detectChanges();
    };

    modalRef.result.then(
      (result) => {
        if (result === 'saved') {
          this.loadPersonas();
        }
      }
    ).catch(() => {
      // Modal cerrado o cancelado
    });
  }

  openEditModal(persona: PersonaModel): void{
    const modalRef = this.modalService.open(PersonaForm,{
      size: 'lg',
      backdrop: 'static',
      keyboard: false
    });

    modalRef.componentInstance.isEdit = true;
    modalRef.componentInstance.persona = { ...persona };
    modalRef.componentInstance.loadPersonas = () => {
      this.loadPersonas();
      this.cdr.detectChanges();
    };

    modalRef.result.then(
      (result) => {
        if (result === 'saved') {
          this.loadPersonas();
        }
      }
    ).catch(() => {
      // Modal cerrado o cancelado
    });
  }

  deletePersona(persona: PersonaModel): void {
    //if (confirm(`¿Estás seguro de que quieres eliminar a ${persona.nombre} ${persona.apellidos}?`)) {
      Swal.fire({
        title: "Está seguro?",
        text: `Va a eliminar definitivamente a ${persona.nombre} ${persona.apellidos}. Desea continuar?`,
        icon: "warning",
        showCancelButton: true,
        confirmButtonColor: "#3085d6",
        cancelButtonColor: "#d33",
        confirmButtonText: "Si, eliminar!"
      }).then((result) => {
        if (result.isConfirmed) {
          this.personaService.deletePersona(persona.numero_documento).subscribe({
            next: () => {
              Swal.fire({
                toast: true,
                position: "top-end",
                icon: "success",
                title: "Registro eliminado!",
                showConfirmButton: false,
                timer: 1500
              });
              this.loadPersonas();
            },
            error: (error: ErrorResponse) => {
              this.error = error.message || 'Error al eliminar la persona';
            }
          });
        }
      });
    //}
  }

}
