/*
 *  MIT License
 *
 * Copyright (c) 2024 Andrew Beck
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.github.zardozz.FixedHeaderTableLayout;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * FixedHeaderTableContainer hosts multiple {@link FixedHeaderTableLayout} instances next to each
 * other, allowing independent fixed header definitions per table while keeping a consistent gap
 * (divider) between them.
 */
public class FixedHeaderTableContainer extends HorizontalScrollView {

    private final LinearLayout container;
    private int dividerHeightPx = LayoutParams.MATCH_PARENT;
    private int dividerWidthPx = 8;
    private @ColorInt int dividerColor = Color.TRANSPARENT;
    private final List<FixedHeaderTableLayout> subtables = new ArrayList<>();

    public FixedHeaderTableContainer(Context context) {
        super(context);
        container = buildContainer(context);
    }

    public FixedHeaderTableContainer(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        container = buildContainer(context);
    }

    public FixedHeaderTableContainer(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        container = buildContainer(context);
    }

    private LinearLayout buildContainer(Context context) {
        LinearLayout inner = new LinearLayout(context);
        inner.setOrientation(LinearLayout.HORIZONTAL);
        addView(inner, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        return inner;
    }

    /**
     * Adds a {@link FixedHeaderTableLayout} to the container. A divider view is inserted before the
     * table after the first entry to maintain the requested separation.
     *
     * @param tableLayout the table layout to add
     */
    @SuppressWarnings({"UnusedDeclaration"})
    public void addSubTable(FixedHeaderTableLayout tableLayout) {
        if (tableLayout == null) {
            return;
        }
        if (!subtables.isEmpty()) {
            container.addView(buildDivider());
        }
        subtables.add(tableLayout);
        container.addView(tableLayout, new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
    }

    /**
     * Returns the currently added subtables.
     */
    @SuppressWarnings({"UnusedDeclaration"})
    public List<FixedHeaderTableLayout> getSubtables() {
        return subtables;
    }

    /**
     * Sets the divider height in pixels. By default it matches the height of the table.
     */
    @SuppressWarnings({"UnusedDeclaration"})
    public void setDividerHeightPx(int dividerHeightPx) {
        this.dividerHeightPx = dividerHeightPx;
        requestLayout();
    }

    /**
     * Sets the divider width in pixels. This controls the space between subtables.
     */
    @SuppressWarnings({"UnusedDeclaration"})
    public void setDividerWidthPx(int dividerWidthPx) {
        this.dividerWidthPx = dividerWidthPx;
        requestLayout();
    }

    /**
     * Sets the divider color. Use {@link Color#TRANSPARENT} to make the separation invisible while
     * maintaining spacing.
     */
    @SuppressWarnings({"UnusedDeclaration"})
    public void setDividerColor(@ColorInt int dividerColor) {
        this.dividerColor = dividerColor;
        invalidate();
    }

    private View buildDivider() {
        View divider = new View(getContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dividerWidthPx, dividerHeightPx);
        divider.setLayoutParams(params);
        divider.setBackgroundColor(dividerColor);
        return divider;
    }
}
