package gishan.volumemeasurementtool.customviews;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import gishan.volumemeasurementtool.R;

/**
 * Created by Gishan Don Ranasinghe
 */

public class ProgressCircleView extends View {

    private static final int STROKE_WIDTH = 60;
    private static final int MAX = 100;
    private static final int PROGRESS = 0;
    private final RectF mBounds = new RectF();
    private final Paint mProgressColorPaint = new Paint();
    private final Paint mBackgroundColorPaint = new Paint();
    private boolean mShouldShowBackground = true;
    private int mStrokeWidth = STROKE_WIDTH;
    private int mMax = MAX;
    private int mProgress = PROGRESS;

    public ProgressCircleView(Context context) {
        super(context);
        init();
    }

    public ProgressCircleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ProgressCircleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mProgressColorPaint.setAntiAlias(true);
        mProgressColorPaint.setStyle(Paint.Style.STROKE);
        mProgressColorPaint.setStrokeWidth(mStrokeWidth);

        mBackgroundColorPaint.setAntiAlias(true);
        mBackgroundColorPaint.setStyle(Paint.Style.STROKE);
        mBackgroundColorPaint.setStrokeWidth(mStrokeWidth);
        mBackgroundColorPaint.setColor(getResources().getColor(R.color.gray_light));

    }

    public void setBackgroundColour(int colour) {
        mBackgroundColorPaint.setColor(colour);
        invalidate();
    }

    public void setProgressColour(int colour) {
        mProgressColorPaint.setColor(colour);
        invalidate();
    }

    public void setMax(int max) {
        this.mMax = max;
        invalidate();
    }

    public void setStrokeWidth(int width) {
        this.mStrokeWidth = width;
        mBackgroundColorPaint.setStrokeWidth(mStrokeWidth);
        mProgressColorPaint.setStrokeWidth(mStrokeWidth);
        invalidate();
    }

    public void setProgress(int progress) {
        this.mProgress = progress;
        invalidate();
    }

    public void showBackground(boolean show) {
        mShouldShowBackground = show;
        invalidate();
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        final int height = getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        final int width = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        final int min = Math.min(width, height);
        setMeasuredDimension(min + 2 * STROKE_WIDTH, min + 2 * STROKE_WIDTH);

        mBounds.set(STROKE_WIDTH, STROKE_WIDTH, min + STROKE_WIDTH, min + STROKE_WIDTH);
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        if (mShouldShowBackground) {
            canvas.drawArc(mBounds, 0, 360, false, mBackgroundColorPaint);
        }

        float scale = mMax > 0 ? (mProgress * 360) / mMax : 0;
        canvas.drawArc(mBounds, 270, scale, false, mProgressColorPaint);

        super.onDraw(canvas);
    }
}