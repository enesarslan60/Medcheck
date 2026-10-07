# Bachelorarbeit Medcheck (LaTeX)

## Kompilieren

Benötigt eine TeX-Distribution mit `latexmk` und `biber` (z. B. TeX Live oder MiKTeX).

```bash
cd thesis
latexmk -pdf main.tex
```

Ergebnis: `main.pdf`. Aufräumen mit `latexmk -c`.

## Aufbau

| Datei | Inhalt |
|---|---|
| `main.tex` | Präambel, Titelseite, Angaben zur Arbeit, Einbindung der Kapitel |
| `literatur.bib` | Literaturquellen (biblatex/biber) |
| `kapitel/00-abstract.tex` | Abstract |
| `kapitel/01-einleitung.tex` | Einleitung |
| `kapitel/02-state-of-the-art.tex` | State of the Art |
| `kapitel/03-methodik.tex` | Methodik |
| `kapitel/04-implementierung.tex` | Implementierung |
| `kapitel/05-evaluation.tex` | Evaluation |
| `kapitel/06-konklusion.tex` | Konklusion |

## Vor der Abgabe

- Platzhalter in `main.tex` ausfüllen (`\autor`, `\matrikel`, `\hochschule`, Betreuung, Abgabedatum).
- Satzspiegel und Zeilenabstand stehen in `main.tex` und lassen sich dort an die Vorgaben der Hochschule anpassen. Der Seitenumfang ändert sich dann entsprechend.
- Optionaler Anhang mit den Rohdaten der Befragung: Datei als `kapitel/anhang-umfrage.tex` ablegen. Sie wird automatisch als Anhang eingebunden, und in Kapitel 3 erscheint dann der Verweis auf den Anhang. Die Datei ist per `.gitignore` vom Repository ausgeschlossen, weil sie personenbezogene Interviewdaten enthält.
