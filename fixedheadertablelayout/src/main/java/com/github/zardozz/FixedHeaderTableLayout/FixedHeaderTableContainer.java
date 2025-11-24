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
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.OverScroller;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * FixedHeaderTableContainer stacks {@link FixedHeaderTableLayout} children vertically and manages a
 * single shared viewport (pan/zoom) for all subtables. The container owns the gesture detectors and
 * inertial scrolling to provide a sheet-like experience.
 */
public class FixedHeaderTableContainer extends ViewGroup {

    private final List<FixedHeaderTableLayout> subtables = new ArrayList<>();
    private final GestureDetector gestureDetector;
    private final ScaleGestureDetector scaleGestureDetector;
    private final OverScroller scroller;

    private float globalPanX = 0f;
    private float globalPanY = 0f;
    private float globalScale = 1f;
    private float minScale = 0.5f;
    private float maxScale = 2.0f;

    private float contentWidth = 0f;
    private float contentHeight = 0f;

    private float lastTouchX;
    private float lastTouchY;
    private boolean isScrolling = false;
    private final int touchSlop;

    private int dividerHeightPx = 8;
    private int dividerColor = Color.TRANSPARENT;
    private @ColorInt int sheetBackgroundColor = Color.WHITE;
    private final Paint dividerPaint = new Paint();

    private static final float OVERSCROLL_MARGIN = 0f;

    public FixedHeaderTableContainer(Context context) {
        this(context, null);
    }

    public FixedHeaderTableContainer(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FixedHeaderTableContainer(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setWillNotDraw(false);
        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        scroller = new OverScroller(context);
        gestureDetector = new GestureDetector(context, new GestureListener());
        scaleGestureDetector = new ScaleGestureDetector(context, new ScaleListener());
        dividerPaint.setStyle(Paint.Style.FILL);
        dividerPaint.setColor(dividerColor);
    }

    /**
     * Add a {@link FixedHeaderTableLayout} to the sheet. The table is configured to use external
     * viewport mode and will be updated whenever the container viewport changes.
     */
    public void addSubTable(@NonNull FixedHeaderTableLayout tableLayout) {
        tableLayout.setUseExternalViewport(true);
        applyViewport(tableLayout);
        subtables.add(tableLayout);
        addView(tableLayout, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        updateContentBounds();
        clampPan();
        applyViewportToChildren();
        requestLayout();
    }

    /**
     * Sets the sheet background color used to fill empty areas around the stacked tables.
     */
    public void setSheetBackgroundColor(@ColorInt int color) {
        this.sheetBackgroundColor = color;
        invalidate();
    }

    /**
     * Sets the minimum scale factor allowed during pinch gestures.
     */
    public void setMinScale(float minScale) {
        this.minScale = minScale;
    }

    /**
     * Sets the maximum scale factor allowed during pinch gestures.
     */
    public void setMaxScale(float maxScale) {
        this.maxScale = maxScale;
    }

    /**
     * Configure the height of the divider placed between stacked tables.
     */
    public void setDividerHeightPx(int dividerHeightPx) {
        this.dividerHeightPx = dividerHeightPx;
        requestLayout();
    }

    public void setDividerColor(@ColorInt int dividerColor) {
        this.dividerColor = dividerColor;
        dividerPaint.setColor(dividerColor);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(sheetBackgroundColor);
        int top = getPaddingTop();
        for (int i = 0; i < getChildCount() - 1; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() == GONE) continue;
            top += child.getMeasuredHeight();
            if (dividerHeightPx > 0) {
                canvas.drawRect(getPaddingLeft(), top, getWidth() - getPaddingRight(), top + dividerHeightPx, dividerPaint);
                top += dividerHeightPx;
            }
        }
        super.onDraw(canvas);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = 0;
        int height = getPaddingTop() + getPaddingBottom();
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child.getVisibility() == GONE) continue;
            measureChild(child, widthMeasureSpec, heightMeasureSpec);
            width = Math.max(width, child.getMeasuredWidth());
            height += child.getMeasuredHeight();
            if (i < getChildCount() - 1) {
                height += dividerHeightPx;
            }
        }
        width += getPaddingLeft() + getPaddingRight();
        setMeasuredDimension(resolveSize(width, widthMeasureSpec), resolveSize(height, heightMeasureSpec));
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int top = getPaddingTop();
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child.getVisibility() == GONE) continue;
            int childHeight = child.getMeasuredHeight();
            child.layout(getPaddingLeft(), top, getPaddingLeft() + child.getMeasuredWidth(), top + childHeight);
            top += childHeight;
            if (i < getChildCount() - 1) {
                // add spacing for the divider; the color is painted in onDraw
                top += dividerHeightPx;
            }
        }
        updateContentBounds();
        clampPan();
        applyViewportToChildren();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleGestureDetector.onTouchEvent(event);
        gestureDetector.onTouchEvent(event);

        final int action = event.getActionMasked();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                scroller.forceFinished(true);
                lastTouchX = event.getX();
                lastTouchY = event.getY();
                isScrolling = false;
                break;
            case MotionEvent.ACTION_MOVE:
                float dx = event.getX() - lastTouchX;
                float dy = event.getY() - lastTouchY;
                if (!isScrolling) {
                    if (Math.hypot(dx, dy) > touchSlop) {
                        isScrolling = true;
                    }
                }
                if (isScrolling && !scaleGestureDetector.isInProgress()) {
                    panBy(dx, dy);
                }
                lastTouchX = event.getX();
                lastTouchY = event.getY();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                isScrolling = false;
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (scroller.computeScrollOffset()) {
            float newX = scroller.getCurrX();
            float newY = scroller.getCurrY();
            updateViewport(newX, newY, globalScale);
            postInvalidateOnAnimation();
        }
    }

    private void panBy(float dx, float dy) {
        updateViewport(globalPanX + dx, globalPanY + dy, globalScale);
    }

    private void updateViewport(float panX, float panY, float scale) {
        globalPanX = panX;
        globalPanY = panY;
        globalScale = Math.max(minScale, Math.min(maxScale, scale));
        clampPan();
        applyViewportToChildren();
        invalidate();
    }

    private void updateContentBounds() {
        float maxWidth = 0f;
        float totalHeight = 0f;

        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (!(child instanceof FixedHeaderTableLayout) || child.getVisibility() == GONE) {
                continue;
            }

            FixedHeaderTableLayout table = (FixedHeaderTableLayout) child;
            maxWidth = Math.max(maxWidth, table.getContentWidth());
            totalHeight += table.getContentHeight();

            if (i < getChildCount() - 1) {
                totalHeight += dividerHeightPx;
            }
        }

        contentWidth = maxWidth;
        contentHeight = totalHeight;
    }

    private void clampPan() {
        float viewWidth = getWidth();
        float viewHeight = getHeight();

        float scaledWidth = contentWidth * globalScale;
        float scaledHeight = contentHeight * globalScale;

        float minPanX = Math.min(0f, viewWidth - scaledWidth) - OVERSCROLL_MARGIN;
        float maxPanX = OVERSCROLL_MARGIN;

        float minPanY = Math.min(0f, viewHeight - scaledHeight) - OVERSCROLL_MARGIN;
        float maxPanY = OVERSCROLL_MARGIN;

        if (globalPanX < minPanX) globalPanX = minPanX;
        if (globalPanX > maxPanX) globalPanX = maxPanX;

        if (globalPanY < minPanY) globalPanY = minPanY;
        if (globalPanY > maxPanY) globalPanY = maxPanY;
    }

    private void applyViewport(FixedHeaderTableLayout table) {
        table.setExternalViewport(globalPanX, globalPanY, globalScale);
    }

    private void applyViewportToChildren() {
        for (FixedHeaderTableLayout table : subtables) {
            applyViewport(table);
        }
    }

    private void fling(float velocityX, float velocityY) {
        scroller.fling((int) globalPanX, (int) globalPanY,
                (int) velocityX, (int) velocityY,
                Integer.MIN_VALUE, Integer.MAX_VALUE,
                Integer.MIN_VALUE, Integer.MAX_VALUE);
        postInvalidateOnAnimation();
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (!scaleGestureDetector.isInProgress()) {
                panBy(-distanceX, -distanceY);
                return true;
            }
            return false;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            fling(velocityX, velocityY);
            return true;
        }
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float newScale = globalScale * detector.getScaleFactor();
            updateViewport(globalPanX, globalPanY, newScale);
            return true;
        }
    }
}
