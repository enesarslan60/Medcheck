export type InteractionSource =
  | 'LOCAL_CACHE'
  | 'OPENFDA_RXCUI'
  | 'OPENFDA_GENERIC_NAME'
  | 'NOT_FOUND';

export interface DrugInteractionData {
  drugName: string;
  rxcui: string | null;
  interactionText: string | null;
  source: InteractionSource;
}
