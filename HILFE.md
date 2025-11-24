# Hilfe: Verwendung von FixedHeaderTableLayout mit Subtabellen

Diese Datei fasst die neuen Funktionen zusammen, die Subtabellen nebeneinander,
fixierte Bereiche pro Tabelle, explizite Spaltenbreiten und Zellverbindungen
ermöglichen.

## Mehrere Tabellen nebeneinander
- Verwende `FixedHeaderTableContainer`, um mehrere `FixedHeaderTableLayout`-Instanzen
  horizontal anzuordnen.
- Konfiguriere den Trennbalken zwischen den Tabellen:
  - `setDividerWidthPx(int px)` für den Abstand.
  - `setDividerHeightPx(int px)` für die Höhe (Standard: MATCH_PARENT).
  - `setDividerColor(@ColorInt int color)` für die Farbe (z. B. `Color.TRANSPARENT` für
    nur Abstand ohne sichtbare Linie).

```kotlin
val container = FixedHeaderTableContainer(context).apply {
    setDividerWidthPx(8)
    setDividerHeightPx(LayoutParams.MATCH_PARENT)
    setDividerColor(Color.GRAY)
}
```

## Fixierte Kopf- und Spaltenbereiche pro Subtabelle
Jede `FixedHeaderTableLayout` kann eigene Header-Einstellungen erhalten:
```kotlin
val table = FixedHeaderTableLayout(context).apply {
    setFixedHeaderCounts(fixedHeaderRowCount = 2, fixedHeaderColumnCount = 1)
}
```
Die Werte gelten nur für die jeweilige Subtabelle und beeinflussen die anderen
Subtabellen nicht.

## Spaltenbreiten festlegen oder automatisch messen
- Subtabellenweite Vorgaben: `setColumnWidthOverrides(SparseIntArray)` setzt feste
  Spaltenbreiten für definierte Indizes. Alle anderen Spalten orientieren sich am Inhalt.
```kotlin
val widths = SparseIntArray().apply {
    put(0, 160) // erste Spalte fest auf 160px
    put(2, 200) // dritte Spalte fest auf 200px
}
table.setColumnWidthOverrides(widths)
```
- Zeilenweite Vorgaben: In einer `FixedHeaderTableRow` kann `setExplicitColumnWidths`
  verwendet werden, um nur für diese Zeile feste Breiten zu hinterlegen. Nicht gesetzte
  Einträge werden weiterhin automatisch gemessen.

## Zellen verbinden (Spans)
`FixedHeaderTableRow.mergeCells(startColumn, span)` verbindet Zellen ab der angegebenen
Startspalte über mehrere Spalten hinweg. Der Inhalt der Startzelle wird über die gesamte
Spanne gezogen.
```kotlin
val row = FixedHeaderTableRow(context).apply {
    mergeCells(startColumn = 0, span = 3) // verbindet Spalten 0-2
}
```

## Komplettes Beispiel
Das folgende Beispiel erstellt zwei Subtabellen mit unterschiedlichen Headern und
Spaltenbreiten und fügt sie dem Container hinzu. Die zweite Subtabelle verbindet dabei
Zellen in der Kopfzeile.
```kotlin
val container = FixedHeaderTableContainer(context).apply {
    setDividerWidthPx(12)
    setDividerColor(Color.LTGRAY)
}

val left = FixedHeaderTableLayout(context).apply {
    setFixedHeaderCounts(1, 1)
    setColumnWidthOverrides(SparseIntArray().apply { put(0, 180) })
    // addViews(...) mit deinen FixedHeaderSubTableLayout-Instanzen
}

val right = FixedHeaderTableLayout(context).apply {
    setFixedHeaderCounts(2, 0)
    // addViews(...)
}

val headerRow = FixedHeaderTableRow(context).apply {
    mergeCells(startColumn = 0, span = 2)
}

container.addSubTable(left)
container.addSubTable(right)
```

## Hinweise
- Werden keine festen Spaltenbreiten vergeben, bleibt die Breite inhaltssensitiv.
- Subtabellen können unterschiedliche Gesamtbreiten haben; der Container kümmert sich um die
  horizontale Ausrichtung und den definierten Abstand.
- Werden mehrere Container untereinander gestapelt, bleiben sie standardmäßig linksbündig,
  weil `FixedHeaderTableContainer` nur so breit wie sein Inhalt ist (`WRAP_CONTENT`). Eine
  Zentrierung entsteht nur, wenn das umgebende Layout explizit `gravity="center"` o. Ä. nutzt;
  für feste Linksbündigkeit kann dort `gravity="start"` gesetzt werden.
- `clearMergedCells()` entfernt zuvor gesetzte Zellverbindungen, falls eine Zeile neu
  konfiguriert werden soll.
