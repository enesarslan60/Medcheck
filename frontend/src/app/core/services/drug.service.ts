import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { Drug } from '../../shared/models/drug.model';

@Injectable({ providedIn: 'root' })
export class DrugService {
  private readonly baseUrl = 'http://localhost:8080/api/drugs';

  constructor(private http: HttpClient) {}

  getAll(): Observable<Drug[]> {
    return this.http.get<Drug[]>(this.baseUrl);
  }

  search(name: string): Observable<Drug[]> {
    const params = new HttpParams().set('name', name);
    return this.http.get<Drug[]>(`${this.baseUrl}/search`, { params });
  }

  openFda(name: string): Observable<Record<string, unknown>> {
    const params = new HttpParams().set('name', name);
    return this.http.get<Record<string, unknown>>(`${this.baseUrl}/openfda`, { params });
  }
}
