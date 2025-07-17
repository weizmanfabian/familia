import { CiudadModel } from "./CiudadModel";

export interface PersonaModel {
  numero_documento: string;
  nombre: string;
  apellidos: string;
  fecha_nacimiento: string;
  correo_electronico: string;
  telefono: string;
  ocupacion: string;
  ciudad: CiudadModel;
  esViable?: boolean;
}