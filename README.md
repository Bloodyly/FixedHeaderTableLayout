<div align="center">
    <h2>Fixed Header Table Layout for Android</h2>
</div>

FixedHeaderTableLayout is a powerful Android library for displaying complex data structures and rendering tabular data composed of rows, columns and cells with multi direction scrolling and zooming.

This repository also contains a sample app that is designed to show you how to create your own FixedHeaderTableLayout in your application.

FixedHeaderTableLayout is similar in construction and use as to Android's TableLayout  

<p align="center">
      <img src="https://raw.githubusercontent.com/Zardozz/FixedHeaderTableLayout/master/art/FixedHeaderTableLayout.gif">
</p>

# Note
<h1>This Library is current in development and is considered in an Alpha state</h1>

## Features
  - [x] 1 to X number of rows can be fixed as column headers at the top of the table.
  - [x] 1 to X number of rows can be fixed as row headers at the left of the table.
  - [x] Multi direction scrolling is available if the table is larger than the screen.
  - [x] Pinch Zoom is available.
  - [x] Standard scrollbars are available.
  - [x] Clicks are passed to children views.
  - [x] Each column width value will be automatically adjusted to fit the largest cell in the column.
  - [x] Each row height value will be automatically adjusted to fit the largest cell in the row.
  - [x] Support for API 16 upwards

## Bonus Feature
The FixedHeaderSubTableLayout behaves like a normal TableLayout But it gives you direct access to the cell sizes.

Thus it is possible to align cells between independent tables allowing you to build more complicated tables with borders easier,
easily split tables across print pages, viewpager, etc by using modular groups of aligned tables next to each other.
It also allows a form of column spanning, a column in one table can be made to be the size of multiple columns
in the adjacent table, so it looks like column spanning.

Some examples in the MultiTableExample in the Example App.

<p align="center">
      <img src="https://raw.githubusercontent.com/Zardozz/FixedHeaderTableLayout/master/art/MultiTableExample.png">
</p>

## Feature TODO list
  - [x] Scale around pinch center.
  - [x] Corner layout location and layout direction to support Right to Left Languages.
  - [x] Some type of column span (Nested Tables) support.
  - [x] Making the fixed headers optional (at least one of each is required at the moment).
  - [x] Documentation.
  - [x] Automated Tests.
  - [x] Probably lots more.

## Limitations
  - [x] As per Android's TableLayout constructing/drawing very large tables takes some time.


## What's new

You can check new implementations of `TableView` on the [release page](https://github.com/Zardozz/FixedHeaderTableLayout/releases).

## Installation

To use this library in your Android project

Add Maven Central to the project's `build.gradle` :
```
allprojects {
    repositories {
        google()
        mavenCentral()
    }
}
```

Add the following dependency into your module's `build.gradle`:
```
dependencies {
    implementation 'com.github.Zardozz:fixedheadertablelayout:0.0.0.5'
}
```

## Documentation

Please check out the [project's wiki](https://github.com/Zardozz/FixedHeaderTableLayout/wiki).

## Quick example: sheet-style stacking with sticky rows/columns

Use `FixedHeaderTableContainer` to vertically stack independent `FixedHeaderTableLayout` instances
while sharing a single pan/zoom/inertia controller. Each subtable can declare its own sticky rows
and columns.

```xml
<!-- layout/activity_sheet.xml -->
<com.github.zardozz.FixedHeaderTableLayout.FixedHeaderTableContainer
    android:id="@+id/tableContainer"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="#FFFFFF"/>
```

```java
FixedHeaderTableContainer container = findViewById(R.id.tableContainer);
container.setSheetBackgroundColor(Color.WHITE);

FixedHeaderTableLayout revenueTable = buildRevenueTable();
revenueTable.setStickyRowIndices(0, 3, 11);   // progressively sticky header rows
revenueTable.setStickyColumnIndices(0);       // keep first column frozen

FixedHeaderTableLayout expenseTable = buildExpenseTable();
expenseTable.setStickyRowIndices(0, 5);
expenseTable.setStickyColumnIndices(0, 1);

container.addSubTable(revenueTable);
container.addSubTable(expenseTable);
```

Each `build*Table()` call constructs a `FixedHeaderTableLayout`, provides its
`addViews(...)` configuration, and optionally uses per-column width overrides. The container
handles all gestures (pan, diagonal scroll, pinch-to-zoom, and inertia) and forwards a single
viewport to every child table so the sheet behaves like one continuous surface. Any unused space is
painted with the container's `sheetBackgroundColor` to reinforce the single-sheet illusion.

### Assumptions, ideas and potential improvements
- Cloning sticky header rows/columns copies basic `TextView` appearance; highly custom cells may
  need bespoke cloning logic.
- Viewport bounds are not yet constrained to content size; clamping based on measured subtable
  dimensions would prevent over-panning.
- Divider spacing is painted by the container; expose a dedicated divider drawable if stronger
  visual separation is needed.
parentLayout.addView(container)
```

Each subtable can freely set explicit column widths (via `SparseIntArray` on the table or
`setExplicitColumnWidths` on individual rows) and use `mergeCells` to combine adjacent cells.
When no width is provided, the content-driven measurement is kept for that column.

## Contributors

Contributions of any kind are welcome!

If you wish to contribute to this project, please refer to our [contributing guide](.github/CONTRIBUTING.md).

## License

```
MIT License

Copyright (c) 2021 Andrew Beck

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
