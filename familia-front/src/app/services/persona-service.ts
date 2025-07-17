import { Injectable } from '@angular/core';
import { environment } from '../enviroments/global-component';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError  } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { PersonaModel } from '../interfaces/PersonaModel';
import { PersonaCreateModel } from '../interfaces/PersonaCreateModel';

@Injectable({
  providedIn: 'root'
})
export class PersonaService {
  private apiUrl = 'http://localhost:8089/familia/personas';

  constructor(private http: HttpClient) {}

  getPersonas(): Observable<PersonaModel[]> {
    return this.http.get<PersonaModel[]>(this.apiUrl).pipe(
      catchError(this.handleError)
    );
  }

  getPersona(numeroDocumento: string): Observable<PersonaModel> {
    return this.http.get<PersonaModel>(`${this.apiUrl}/${numeroDocumento}`).pipe(
      catchError(this.handleError)
    );
  }

  createPersona(persona: PersonaCreateModel): Observable<PersonaModel> {
    return this.http.post<PersonaModel>(this.apiUrl, persona).pipe(
      catchError(this.handleError)
    );
  }

  updatePersona(numeroDocumento: string, persona: PersonaCreateModel): Observable<PersonaModel> {
    return this.http.put<PersonaModel>(`${this.apiUrl}/${numeroDocumento}`, persona).pipe(
      catchError(this.handleError)
    );
  }

  deletePersona(numeroDocumento: string): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${numeroDocumento}`).pipe(
      catchError(this.handleError)
    );
  }

  private handleError(error: HttpErrorResponse): Observable<never> {
    console.error('Error en PersonaService:', error);
    return throwError(() => error);
  }
}
