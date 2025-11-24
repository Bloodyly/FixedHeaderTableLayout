/*
 *  MIT License
 *
 * Copyright (c) 2021 Andrew Beck
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
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.util.ArrayList;

public class FixedHeaderTableRow extends LinearLayout {

    private ArrayList<Integer> mColumnWidths = new ArrayList<>();
    private ArrayList<Integer> mExplicitColumnWidths = new ArrayList<>();
    private ArrayList<CellSpan> mColumnSpans = new ArrayList<>();
    private int myWidth = 0;
    private int myHeight = 0;
    private int maxChildHeight = 0;
    private boolean preMeasured = false;

    private static final String LOG_TAG = FixedHeaderTableRow.class.getSimpleName();

    public FixedHeaderTableRow(Context context) {
        super(context);
        init();
    }

    public FixedHeaderTableRow(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FixedHeaderTableRow(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @SuppressWarnings({"UnusedDeclaration"})
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public FixedHeaderTableRow(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init(){
        // Log.d(LOG_TAG, "init: " + Integer.toHexString(System.identityHashCode(this)) );
        // Row are always horizontal
        super.setOrientation(HORIZONTAL);
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public ArrayList<Integer> getColumnWidths() {
        return mColumnWidths;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public void setColumnWidths(ArrayList<Integer> mColumnWidths) {
        this.mColumnWidths = mColumnWidths;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public void setExplicitColumnWidths(@Nullable ArrayList<Integer> explicitColumnWidths) {
        if (explicitColumnWidths == null) {
            this.mExplicitColumnWidths.clear();
        } else {
            this.mExplicitColumnWidths = explicitColumnWidths;
        }
        requestLayout();
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public ArrayList<Integer> getExplicitColumnWidths() {
        return mExplicitColumnWidths;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public void clearMergedCells() {
        mColumnSpans.clear();
        requestLayout();
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public void mergeCells(int startColumn, int span) {
        if (span < 1) {
            throw new IllegalArgumentException("Span must be 1 or greater");
        }
        mColumnSpans.add(new CellSpan(startColumn, span));
        requestLayout();
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public int getMaxChildHeight() {
        return maxChildHeight;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public void setMaxChildHeight(int maxChildHeight) {
        this.maxChildHeight = maxChildHeight;
    }

    /**
     * Changing the Orientation of this class is not supported.
     * Rows are always horizontal
     * @param orientation Ignored
     * @throws RuntimeException Throws exception
     */
    @Override
    public void setOrientation(int orientation) {
        throw new UnsupportedOperationException("Setting the Orientation is not supported");
    }

    /**
     * Measure the row
     * A row is either measured to full size of all it's children (UNSPECIFIED)
     * or measured to the size of having fixed size children to match other rows (EXACTLY)
     * @param widthMeasureSpec Ignored
     * @param heightMeasureSpec Ignored
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (preMeasured) {
            //Log.d(LOG_TAG, "fixedMeasure: " + Integer.toHexString(System.identityHashCode(this)) );
            fixedMeasure();
        } else {
            // For first Measure
            //Log.d(LOG_TAG, "preMeasure: " + Integer.toHexString(System.identityHashCode(this)));
            preMeasure();
        }

    }

    private int resolveColumnWidth(int columnIndex, View columnChild) {
        if (mExplicitColumnWidths.size() > columnIndex && mExplicitColumnWidths.get(columnIndex) > 0) {
            return mExplicitColumnWidths.get(columnIndex);
        }
        return columnChild.getMeasuredWidth();
    }

    private int findSpanForColumn(int columnIndex) {
        for (CellSpan span : mColumnSpans) {
            if (span.startColumn == columnIndex) {
                return span.spanLength;
            }
        }
        return 1;
    }

    private void preMeasure(){
        // Reset stored size as we are measuring again
        myWidth = 0;
        myHeight = 0;
        // Measure UNSPECIFIED
        int measureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        mColumnWidths.clear();
        maxChildHeight = 0;

        final int count = getChildCount();
        int columnIndex = 0;
        while (columnIndex < count) {
            final View child = getChildAt(columnIndex);
            if (child == null || child.getVisibility() == View.GONE) {
                columnIndex++;
                continue;
            }

            int span = findSpanForColumn(columnIndex);
            int spanWidth = 0;

            for (int offset = 0; offset < span && (columnIndex + offset) < count; offset++) {
                final View spanChild = getChildAt(columnIndex + offset);
                if (spanChild == null || spanChild.getVisibility() == View.GONE) {
                    continue;
                }
                measureChildWithMargins(spanChild, measureSpec, 0, measureSpec, 0);
                int childWidth = resolveColumnWidth(columnIndex + offset, spanChild);
                mColumnWidths.add(childWidth);
                spanWidth += childWidth;
                maxChildHeight = Math.max(maxChildHeight, spanChild.getMeasuredHeight());
            }

            myWidth += spanWidth;
            columnIndex += span;
        }

        // Add my padding
        myWidth = myWidth + getPaddingLeft() + getPaddingRight();
        myHeight = maxChildHeight + getPaddingTop() + getPaddingBottom();

        // Check against our minimum height and width
        myWidth = Math.max(myWidth, getSuggestedMinimumWidth());
        myHeight = Math.max(myHeight, getSuggestedMinimumHeight());

        setMeasuredDimension(myWidth, myHeight);
        //Log.d(LOG_TAG, "preMeasure:setMeasuredDimension:" + myWidth + "x" + myHeight);

        preMeasured = true;
    }

    private void fixedMeasure(){
        // Reset stored size as we are measuring again
        myWidth = 0;
        myHeight = 0;
        // Measure EXACTLY
        //Log.d(LOG_TAG, "fixed:Height of Row: " + maxChildHeight);
        int heightMeasureSpec = MeasureSpec.makeMeasureSpec(maxChildHeight, MeasureSpec.EXACTLY);

        final int count = getChildCount();
        int columnIndex = 0;
        while (columnIndex < count) {
            int span = findSpanForColumn(columnIndex);
            int spanWidth = 0;
            for (int offset = 0; offset < span && (columnIndex + offset) < mColumnWidths.size(); offset++) {
                spanWidth += mColumnWidths.get(columnIndex + offset);
            }

            int widthMeasureSpec = MeasureSpec.makeMeasureSpec(spanWidth, MeasureSpec.EXACTLY);
            View child = getChildAt(columnIndex);
            if (child != null && child.getVisibility() != View.GONE) {
                // Ask the child to match the parent so it fills out the whole cell
                LinearLayout.LayoutParams childLayoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
                child.setLayoutParams(childLayoutParams);

                measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
            }

            // Calculate new row width using the width we have set each column to
            myWidth += spanWidth;
            columnIndex += span;
        }

        // Add my padding
        myWidth = myWidth + getPaddingLeft() + getPaddingRight();
        myHeight = maxChildHeight + getPaddingTop() + getPaddingBottom();

        // Check against our minimum height and width
        myWidth = Math.max(myWidth, getSuggestedMinimumWidth());
        myHeight = Math.max(myHeight, getSuggestedMinimumHeight());

        setMeasuredDimension(myWidth, myHeight);
        //Log.d(LOG_TAG, "fixedMeasure:setMeasuredDimension:" + myWidth + "x" + myHeight);
    }

    private static class CellSpan {
        private final int startColumn;
        private final int spanLength;

        private CellSpan(int startColumn, int spanLength) {
            this.startColumn = startColumn;
            this.spanLength = spanLength;
        }
    }
}
