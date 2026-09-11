export interface Language {
  code: string;
  displayName: string;
}

/**
 * Mirrors backend's SupportedLanguage enum.
 * The `code` is the ISO 639-1 identifier sent to /api/interactions/check
 * in the `language` field.
 */
export const SUPPORTED_LANGUAGES: Language[] = [
  { code: 'de', displayName: 'Deutsch' },
  { code: 'en', displayName: 'English' },
  { code: 'es', displayName: 'Español' },
  { code: 'fr', displayName: 'Français' },
  { code: 'it', displayName: 'Italiano' },
  { code: 'pt', displayName: 'Português' },
  { code: 'tr', displayName: 'Türkçe' },
  { code: 'nl', displayName: 'Nederlands' },
  { code: 'pl', displayName: 'Polski' },
  { code: 'ar', displayName: 'العربية' }
];

export const DEFAULT_LANGUAGE_CODE = 'de';
