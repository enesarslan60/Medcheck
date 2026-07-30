import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { InteractionExplanationResponse } from '../../shared/models/interaction.model';

@Injectable({ providedIn: 'root' })
export class InteractionService {
  private readonly baseUrl = 'http://localhost:8080/api/interactions';

  private lastResult: InteractionExplanationResponse | null = null;

  constructor(private http: HttpClient) {}

  check(drugNames: string[]): Observable<InteractionExplanationResponse> {
    return this.http.post<InteractionExplanationResponse>(
      `${this.baseUrl}/check`, { drugNames });
  }

  setResult(result: InteractionExplanationResponse): void {
    this.lastResult = result;
  }

  getResult(): InteractionExplanationResponse | null {
    return this.lastResult;
  }
}
