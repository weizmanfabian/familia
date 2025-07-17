import { Injectable } from '@angular/core';
import { environment } from '../enviroments/global-component';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError  } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { PersonaModel } from '../interfaces/PersonaModel';
import { CiudadModel } from '../interfaces/CiudadModel';

@Injectable({
  providedIn: 'root'
})
export class CiudadService {
  private apiUrl = `${environment.apiUrl}/ciudades`;

  constructor(private http: HttpClient) {}

  getCiudades(): Observable<CiudadModel[]> {
    return this.http.get<CiudadModel[]>(this.apiUrl).pipe(
      catchError(this.handleError)
    );
  }

  private handleError(error: HttpErrorResponse): Observable<never> {
    return throwError(() => error);
  }
}
