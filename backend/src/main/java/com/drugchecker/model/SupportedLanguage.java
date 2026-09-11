package com.drugchecker.model;

/**
 * The languages the app can ask the LLM to respond in.
 *
 * <p>Each entry carries:
 * <ul>
 *   <li>{@code code} — ISO 639-1 short code, used by the frontend / API</li>
 *   <li>{@code displayName} — native label shown in the language selector</li>
 *   <li>{@code englishName} — the name to embed in the English system prompt
 *       ("Answer STRICTLY in {englishName}"), because Claude/Gemini follow
 *       English instructions more reliably than switch-language directives.</li>
 * </ul>
 */
public enum SupportedLanguage {
    GERMAN    ("de", "Deutsch",    "German"),
    ENGLISH   ("en", "English",    "English"),
    SPANISH   ("es", "Español",    "Spanish"),
    FRENCH    ("fr", "Français",   "French"),
    ITALIAN   ("it", "Italiano",   "Italian"),
    PORTUGUESE("pt", "Português",  "Portuguese"),
    TURKISH   ("tr", "Türkçe",     "Turkish"),
    DUTCH     ("nl", "Nederlands", "Dutch"),
    POLISH    ("pl", "Polski",     "Polish"),
    ARABIC    ("ar", "العربية",   "Arabic");

    public static final SupportedLanguage DEFAULT = GERMAN;

    private final String code;
    private final String displayName;
    private final String englishName;

    SupportedLanguage(String code, String displayName, String englishName) {
        this.code = code;
        this.displayName = displayName;
        this.englishName = englishName;
    }

    public String getCode() { return code; }
    public String getDisplayName() { return displayName; }
    public String getEnglishName() { return englishName; }

    /** Tolerant lookup — accepts {@code null}, code, or enum name (any case). */
    public static SupportedLanguage fromCode(String value) {
        if (value == null || value.isBlank()) return DEFAULT;
        String v = value.trim().toLowerCase();
        for (SupportedLanguage lang : values()) {
            if (lang.code.equalsIgnoreCase(v) || lang.name().equalsIgnoreCase(v)) {
                return lang;
            }
        }
        return DEFAULT;
    }
}
