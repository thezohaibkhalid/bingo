package com.example.bingoarena.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.bingoarena.R;

/**
 * Base activity that handles edge-to-edge display, window insets, and activity transitions.
 */
public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Enable edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
    }

    /**
     * Start an activity with slide transition.
     */
    protected void startActivityWithTransition(Intent intent) {
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    /**
     * Start an activity with fade transition.
     */
    protected void startActivityWithFade(Intent intent) {
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    /**
     * Start an activity with no transition (for bottom nav).
     */
    protected void startActivityNoTransition(Intent intent) {
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    /**
     * Finish without animation (for bottom nav).
     */
    protected void finishNoTransition() {
        super.finish();
        overridePendingTransition(0, 0);
    }

    /**
     * Apply window insets to a view to add padding for status bar and navigation bar.
     * Call this after setContentView() with your root view.
     */
    protected void applyWindowInsets(View rootView) {
        ViewCompat.setOnApplyWindowInsetsListener(rootView, (view, windowInsets) -> {
            Insets systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets displayCutout = windowInsets.getInsets(WindowInsetsCompat.Type.displayCutout());
            
            // Combine system bars and display cutout insets
            int topInset = Math.max(systemBars.top, displayCutout.top);
            int leftInset = Math.max(systemBars.left, displayCutout.left);
            int rightInset = Math.max(systemBars.right, displayCutout.right);
            
            view.setPadding(leftInset, topInset, rightInset, 0);
            
            return WindowInsetsCompat.CONSUMED;
        });
    }

    /**
     * Apply window insets with custom bottom handling (for screens with bottom nav).
     */
    protected void applyWindowInsetsWithBottomNav(View rootView, View bottomNavView) {
        ViewCompat.setOnApplyWindowInsetsListener(rootView, (view, windowInsets) -> {
            Insets systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets displayCutout = windowInsets.getInsets(WindowInsetsCompat.Type.displayCutout());
            
            int topInset = Math.max(systemBars.top, displayCutout.top);
            int leftInset = Math.max(systemBars.left, displayCutout.left);
            int rightInset = Math.max(systemBars.right, displayCutout.right);
            
            view.setPadding(leftInset, topInset, rightInset, 0);
            
            return WindowInsetsCompat.CONSUMED;
        });
        
        // Apply bottom inset to navigation bar
        if (bottomNavView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(bottomNavView, (view, windowInsets) -> {
                Insets systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
                
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
                params.bottomMargin = systemBars.bottom;
                view.setLayoutParams(params);
                
                return WindowInsetsCompat.CONSUMED;
            });
        }
    }

    /**
     * Apply only top inset for scroll views that handle their own bottom padding.
     */
    protected void applyTopWindowInset(View view) {
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, windowInsets) -> {
            Insets systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets displayCutout = windowInsets.getInsets(WindowInsetsCompat.Type.displayCutout());
            
            int topInset = Math.max(systemBars.top, displayCutout.top);
            
            v.setPadding(v.getPaddingLeft(), topInset, v.getPaddingRight(), v.getPaddingBottom());
            
            return WindowInsetsCompat.CONSUMED;
        });
    }
}