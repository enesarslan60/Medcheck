export type Severity = 'SEVERE' | 'MODERATE' | 'MILD' | 'NONE';

export interface Interaction {
  id?: number;
  drug1: string;
  drug2: string;
  severity: Severity;
  description: string;
  llmExplanation: string;
  source?: 'LOCAL' | 'OPENFDA' | 'NONE';
}
