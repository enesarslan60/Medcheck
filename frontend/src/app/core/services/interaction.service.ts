import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { DrugInteractionData } from '../../shared/models/interaction.model';

@Injectable({ providedIn: 'root' })
export class InteractionService {
  private readonly baseUrl = 'http://localhost:8080/api/interactions';

  private lastResults: DrugInteractionData[] = [];

  constructor(private http: HttpClient) {}

  check(drugNames: string[]): Observable<DrugInteractionData[]> {
    return this.http.post<DrugInteractionData[]>(`${this.baseUrl}/check`, { drugNames });
  }

  setResults(results: DrugInteractionData[]): void {
    this.lastResults = results;
  }

  getResults(): DrugInteractionData[] {
    return this.lastResults;
  }
}
