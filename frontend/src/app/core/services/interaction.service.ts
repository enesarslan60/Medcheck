import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { Interaction } from '../../shared/models/interaction.model';

@Injectable({ providedIn: 'root' })
export class InteractionService {
  private readonly baseUrl = 'http://localhost:8080/api/interactions';

  private lastResults: Interaction[] = [];

  constructor(private http: HttpClient) {}

  check(drugNames: string[]): Observable<Interaction[]> {
    return this.http.post<Interaction[]>(`${this.baseUrl}/check`, { drugNames });
  }

  setResults(results: Interaction[]): void {
    this.lastResults = results;
  }

  getResults(): Interaction[] {
    return this.lastResults;
  }
}
