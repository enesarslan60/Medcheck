import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { InteractionExplanationResponse } from '../../shared/models/interaction.model';
import { DEFAULT_LANGUAGE_CODE } from '../../shared/models/language.model';

@Injectable({ providedIn: 'root' })
export class InteractionService {
  private readonly baseUrl = 'http://localhost:8080/api/interactions';

  private lastResult: InteractionExplanationResponse | null = null;

  constructor(private http: HttpClient) {}

  /**
   * Ask the backend to analyse the given drug list.
   *
   * @param drugNames drugs picked from the RxNorm autocomplete
   * @param language  ISO 639-1 code — defaults to German if omitted
   */
  check(
    drugNames: string[],
    language: string = DEFAULT_LANGUAGE_CODE
  ): Observable<InteractionExplanationResponse> {
    return this.http.post<InteractionExplanationResponse>(
      `${this.baseUrl}/check`,
      { drugNames, language }
    );
  }

  setResult(result: InteractionExplanationResponse): void {
    this.lastResult = result;
  }

  getResult(): InteractionExplanationResponse | null {
    return this.lastResult;
  }
}
