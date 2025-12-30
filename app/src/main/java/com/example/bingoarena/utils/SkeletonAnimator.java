package com.example.bingoarena.utils;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.ViewGroup;
import java.util.ArrayList;
import java.util.List;


public class SkeletonAnimator {
    private final List<ObjectAnimator> animators = new ArrayList<>();


    public void startAnimation(ViewGroup container) {
        stopAnimation();
        findAndAnimateSkeletonViews(container);
    }
    
    private void findAndAnimateSkeletonViews(ViewGroup container) {
        for (int i = 0; i < container.getChildCount(); i++) {
            View child = container.getChildAt(i);
            
            if (child instanceof ViewGroup) {
                findAndAnimateSkeletonViews((ViewGroup) child);
            } else if (isSkeletonView(child)) {
                animateView(child, i * 100L);
            }
        }
    }
    
    private boolean isSkeletonView(View view) {
        // Check if this is a skeleton placeholder (no specific content)
        return view.getBackground() != null && 
               view.getId() == View.NO_ID &&
               view.getContentDescription() == null;
    }
    
    private void animateView(View view, long delay) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "alpha", 0.3f, 0.7f, 0.3f);
        animator.setDuration(1500);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setStartDelay(delay);
        animator.start();
        animators.add(animator);
    }

    public void animateViews(View... views) {
        stopAnimation();
        for (int i = 0; i < views.length; i++) {
            ObjectAnimator animator = ObjectAnimator.ofFloat(views[i], "alpha", 0.3f, 0.7f, 0.3f);
            animator.setDuration(1500);
            animator.setRepeatCount(ValueAnimator.INFINITE);
            animator.setStartDelay(i * 100L);
            animator.start();
            animators.add(animator);
        }
    }

    public void stopAnimation() {
        for (ObjectAnimator animator : animators) {
            animator.cancel();
        }
        animators.clear();
    }
}