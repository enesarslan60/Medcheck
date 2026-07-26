import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { Drug } from '../../shared/models/drug.model';
import { RxNormCandidate } from '../../shared/models/rxnorm.model';

@Injectable({ providedIn: 'root' })
export class DrugService {
  private readonly baseUrl = 'http://localhost:8080/api/drugs';

  constructor(private http: HttpClient) {}

  /** Local dev/test drugs from H2. */
  getAll(): Observable<Drug[]> {
    return this.http.get<Drug[]>(this.baseUrl);
  }

  /** Autocomplete: fuzzy RxNorm lookup. */
  search(name: string): Observable<RxNormCandidate[]> {
    const params = new HttpParams().set('name', name);
    return this.http.get<RxNormCandidate[]>(`${this.baseUrl}/search`, { params });
  }
}
