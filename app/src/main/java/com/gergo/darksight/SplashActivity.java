package com.gergo.darksight;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.splashscreenmaker);
        final ImageView logoView = (ImageView) findViewById(R.id.logo);
        final Animation animation_logo_1 = AnimationUtils.loadAnimation(getBaseContext(),R.anim.rotate);
        final Animation animation_logo_2 = AnimationUtils.loadAnimation(getBaseContext(),R.anim.antirotate);
        final ImageView backgroundView = (ImageView) findViewById(R.id.background);
        final Animation animation_background_1 = AnimationUtils.loadAnimation(getBaseContext(),R.anim.move_down);
        final Animation animation_background_2 = AnimationUtils.loadAnimation(getBaseContext(),R.anim.move_up);

        logoView.startAnimation(animation_logo_2);
        animation_logo_2.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                logoView.startAnimation(animation_logo_1);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        animation_logo_1.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                finish();
                Intent mainActivity = new Intent(getBaseContext(),MainActivity.class);
                startActivity(mainActivity);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        backgroundView.startAnimation(animation_background_1);
        animation_background_1.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                backgroundView.startAnimation(animation_background_2);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        animation_background_2.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }
}
