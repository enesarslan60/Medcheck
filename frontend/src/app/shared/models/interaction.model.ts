export type Severity = 'LOW' | 'MEDIUM' | 'HIGH' | 'UNKNOWN';

export type InteractionSource =
  | 'LOCAL_CACHE'
  | 'OPENFDA_RXCUI'
  | 'OPENFDA_GENERIC_NAME'
  | 'NOT_FOUND';

export interface DrugDetail {
  drugName: string;
  rxcui: string | null;
  aiSideEffectSummary: string | null;
  openFdaRawText: string | null;
  source: InteractionSource;
}

export interface InteractionExplanationResponse {
  severity: Severity;
  interactionSummary: string | null;
  drugs: DrugDetail[];
  llmAvailable: boolean;
}
