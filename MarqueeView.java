import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Choreographer;
import android.view.View;
import android.view.animation.LinearInterpolator;

/**
 * 跑马灯
 */

public class MarqueeView extends View implements Choreographer.FrameCallback {
    private static final String TAG = "MarqueeView";
    private TextPaint tvPaint;
    private int textColor = Color.BLACK;
    private int textSize = 12;
    private String marqueeText;
    private long marqueeTime;

    private int marqueeX;
    private int marqueeEndX;
    private int marqueeWidth;

    private ValueAnimator marqueeAnimator;

    private Choreographer mChoreographer;
    private OnMarqueeListener onMarqueeListener;

    public MarqueeView(Context context) {
        super(context);
        init();
    }

    public MarqueeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MarqueeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setWillNotDraw(false);
        tvPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        tvPaint.setColor(textColor);
        tvPaint.setTextSize(dip2px(textSize));
        mChoreographer = Choreographer.getInstance();
    }

    public void setMarqueeText(String marqueeText) {
        this.marqueeText = marqueeText;
        invalidate();
    }

    public void setOnMarqueeListener(OnMarqueeListener onMarqueeListener) {
        this.onMarqueeListener = onMarqueeListener;
    }

    public boolean canMarquee() {
        if (TextUtils.isEmpty(marqueeText)) {
            return false;
        }
        int marqueeNewWidth = (int) tvPaint.measureText(marqueeText);
        return marqueeNewWidth > marqueeWidth;
    }

    public void startMarquee() {
        startMarqueeAnimator();
    }

    public void stopMarquee() {
        if (marqueeAnimator != null) {
            marqueeAnimator.end();
            marqueeAnimator = null;
        }
        marqueeX = 0;
        marqueeEndX = 0;
        invalidate();
        mChoreographer.removeFrameCallback(this);
        Log.d(TAG, "--stop marquee--");
    }

    private void startMarqueeAnimator() {
        int marqueeNewWidth = (int) tvPaint.measureText(marqueeText);
        if (onMarqueeListener != null) {
            onMarqueeListener.onMarqueeStart();
        }
        marqueeEndX = marqueeNewWidth - marqueeWidth;
        marqueeAnimator = ValueAnimator.ofInt(0, -marqueeEndX);
        if (marqueeTime <= 0) {
            marqueeTime = 3000;
        }
        marqueeAnimator.setDuration(marqueeTime);
        marqueeAnimator.setRepeatCount(0);
        marqueeAnimator.setInterpolator(new LinearInterpolator());
        marqueeAnimator.start();
        mChoreographer.postFrameCallback(this);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        marqueeWidth = MeasureSpec.getSize(widthMeasureSpec);
        int marqueeHeight = getMarqueeHeight();
        int newHeightSpec = MeasureSpec.makeMeasureSpec(marqueeHeight, MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, newHeightSpec);
    }

    public int getMarqueeHeight() {
        return (int) (tvPaint.getTextSize() * 1.2f);
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (TextUtils.isEmpty(marqueeText)) {
            return;
        }
        int baseY = (int) (getMarqueeHeight() / 2 - (tvPaint.descent() + tvPaint.ascent()) / 2);
        canvas.drawText(marqueeText, marqueeX, baseY, tvPaint);
    }

    private float sp2px(int spValue) {
        float fontScale = getResources().getDisplayMetrics().scaledDensity;
        return spValue * fontScale + 0.5f;
    }

    public int dip2px(int dpValue) {
        float scale = getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    @Override
    public void doFrame(long frameTimeNanos) {
        mChoreographer.removeFrameCallback(this);
        if (marqueeAnimator == null) {
            return;
        }
        Integer integer = (Integer) marqueeAnimator.getAnimatedValue();
        if (integer != null) {
            marqueeX = integer;
            if (marqueeX == -marqueeEndX) {
                stopMarquee();
                if (onMarqueeListener != null) {
                    onMarqueeListener.onMarqueeEnd();
                }
            } else {
                invalidate();
                mChoreographer.postFrameCallback(this);
            }
        }
    }

    public interface OnMarqueeListener {
        void onMarqueeStart();

        void onMarqueeEnd();
    }

}
