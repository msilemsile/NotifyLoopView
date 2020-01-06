import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Choreographer;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;

import java.util.LinkedList;

public class NotifyLoopView extends FrameLayout implements Choreographer.FrameCallback {

    private MarqueeView topMarqueeView;
    private MarqueeView centerMarqueeView;
    private MarqueeView bottomMarqueeView;

    private ValueAnimator transTopAnimator;
    private ValueAnimator transCenterAnimator;
    private ValueAnimator transBottomAnimator;
    private Animator.AnimatorListener animatorListener;

    private LinkedList<String> marqueeStrList = new LinkedList<>();

    private Choreographer mChoreographer;
    private int loopType;

    private int lineSpacing = 20;
    private int lineHeight;
    private int transHeight;

    private boolean canMarquee;

    public NotifyLoopView(@NonNull Context context) {
        super(context);
        initView();
    }

    public NotifyLoopView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public NotifyLoopView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        Context context = getContext();
        topMarqueeView = new MarqueeView(context);
        centerMarqueeView = new MarqueeView(context);
        bottomMarqueeView = new MarqueeView(context);
        mChoreographer = Choreographer.getInstance();
        FrameLayout.LayoutParams topParams = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        addView(topMarqueeView, topParams);
        final FrameLayout.LayoutParams centerParams = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        addView(centerMarqueeView, centerParams);
        FrameLayout.LayoutParams bottomParams = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        addView(bottomMarqueeView, bottomParams);
        lineHeight = topMarqueeView.getMarqueeHeight();
        transHeight = lineHeight + lineSpacing;
        topMarqueeView.setTranslationY(0);
        centerMarqueeView.setTranslationY(transHeight);
        bottomMarqueeView.setTranslationY(transHeight * 2);
        animatorListener = new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mChoreographer.removeFrameCallback(NotifyLoopView.this);
                loopType++;
                if (loopType >= 3) {
                    loopType = 0;
                }
                String transMarquee = marqueeStrList.pollFirst();
                marqueeStrList.add(transMarquee);
                canMarquee = false;
                MarqueeView marqueeView = null;
                switch (loopType) {
                    case 0:
                        bottomMarqueeView.setMarqueeText(marqueeStrList.get(2));
                        bottomMarqueeView.setTranslationY(transHeight * 2);
                        topMarqueeView.setTranslationY(0);
                        centerMarqueeView.setTranslationY(transHeight);
                        canMarquee = topMarqueeView.canMarquee();
                        marqueeView = topMarqueeView;
                        break;
                    case 1:
                        topMarqueeView.setMarqueeText(marqueeStrList.get(2));
                        topMarqueeView.setTranslationY(transHeight * 2);
                        centerMarqueeView.setTranslationY(0);
                        bottomMarqueeView.setTranslationY(transHeight);
                        canMarquee = centerMarqueeView.canMarquee();
                        marqueeView = centerMarqueeView;
                        break;
                    case 2:
                        centerMarqueeView.setMarqueeText(marqueeStrList.get(2));
                        centerMarqueeView.setTranslationY(transHeight * 2);
                        bottomMarqueeView.setTranslationY(0);
                        topMarqueeView.setTranslationY(transHeight);
                        canMarquee = bottomMarqueeView.canMarquee();
                        marqueeView = bottomMarqueeView;
                        break;
                }
                if (!canMarquee) {
                    startLoopAnim();
                } else {
                    marqueeView.setOnMarqueeListener(new MarqueeView.OnMarqueeListener() {
                        @Override
                        public void onMarqueeStart() {

                        }

                        @Override
                        public void onMarqueeEnd() {
                            canMarquee = false;
                            startLoopAnim();
                        }
                    });
                    marqueeView.startMarquee();
                }

            }

            @Override
            public void onAnimationStart(Animator animation) {
                mChoreographer.postFrameCallback(NotifyLoopView.this);
            }
        };
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int newHeightSpec = MeasureSpec.makeMeasureSpec((lineHeight + lineSpacing) * 2, MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, newHeightSpec);
    }

    @Override
    public void doFrame(long frameTimeNanos) {
        mChoreographer.removeFrameCallback(this);
        if (canMarquee) {
            return;
        }
        Integer tempTopTransY = (Integer) transTopAnimator.getAnimatedValue();
        Integer tempCenterTransY = (Integer) transCenterAnimator.getAnimatedValue();
        Integer tempBottomTransY = (Integer) transBottomAnimator.getAnimatedValue();
        if (tempTopTransY == 0 && tempCenterTransY == 0 && tempBottomTransY == 0) {
            mChoreographer.postFrameCallback(this);
            return;
        }
        Log.d("doFrame", "topY=" + tempTopTransY + " centerY=" + tempCenterTransY + " bottomY=" + tempBottomTransY);
        switch (loopType) {
            case 0:
                topMarqueeView.setTranslationY(tempTopTransY);
                centerMarqueeView.setTranslationY(tempCenterTransY);
                bottomMarqueeView.setTranslationY(tempBottomTransY);
                break;
            case 1:
                topMarqueeView.setTranslationY(tempBottomTransY);
                centerMarqueeView.setTranslationY(tempTopTransY);
                bottomMarqueeView.setTranslationY(tempCenterTransY);
                break;
            case 2:
                topMarqueeView.setTranslationY(tempCenterTransY);
                centerMarqueeView.setTranslationY(tempBottomTransY);
                bottomMarqueeView.setTranslationY(tempTopTransY);
                break;
        }
        mChoreographer.postFrameCallback(this);
    }

    public void addMarqueeTxt(String marqueeTxt) {
        if (TextUtils.isEmpty(marqueeTxt)) {
            return;
        }
        marqueeStrList.add(marqueeTxt);
        int marqueeListSize = marqueeStrList.size();
        if (marqueeListSize == 1) {
            topMarqueeView.setMarqueeText(marqueeTxt);
        } else if (marqueeListSize == 2) {
            centerMarqueeView.setMarqueeText(marqueeTxt);
        } else if (marqueeListSize == 3) {
            bottomMarqueeView.setMarqueeText(marqueeTxt);
            startLoopAnim();
        }
    }

    private void startLoopAnim() {
        transTopAnimator = ValueAnimator.ofInt(0, -transHeight);
        transCenterAnimator = ValueAnimator.ofInt(transHeight, 0);
        transBottomAnimator = ValueAnimator.ofInt(transHeight * 2, transHeight);
        AnimatorSet transAnimatorSet = new AnimatorSet();
        transAnimatorSet.setDuration(1500);
        transAnimatorSet.setStartDelay(1000);
        transAnimatorSet.setInterpolator(new LinearInterpolator());
        transAnimatorSet.playTogether(transTopAnimator, transCenterAnimator, transBottomAnimator);
        transAnimatorSet.addListener(animatorListener);
        transAnimatorSet.start();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mChoreographer.removeFrameCallback(this);
    }
}
