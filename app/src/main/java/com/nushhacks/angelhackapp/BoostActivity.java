package com.nushhacks.angelhackapp;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Transformation;
import android.widget.TextView;

import com.github.lzyzsd.circleprogress.ArcProgress;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Jeff Sieu on 17/6/2017.
 */

public class BoostActivity extends AppCompatActivity {
	private TextView mTimerView;
	@Override
	public void onEnterAnimationComplete() {
		super.onEnterAnimationComplete();

		final ArcProgress progress = (ArcProgress) findViewById(R.id.progress);
		findViewById(R.id.ringbackground).setVisibility(View.VISIBLE);
		findViewById(R.id.ringbackground).setAlpha(1);
		findViewById(R.id.ring).animate().alpha(0).setDuration(1000).start();
		progress.setVisibility(View.VISIBLE);
		ObjectAnimator anim = ObjectAnimator.ofInt(progress, "progress", 100, 0);
		anim.setInterpolator(new DecelerateInterpolator());
		anim.setDuration(1000);
		anim.setStartDelay(1000);
		anim.start();
		final int duration = 5000;
		new CountDownTimer(duration, 10) {

			public void onTick(long millisUntilFinished) {
				mTimerView.setText(millisUntilFinished + "s");
				progress.setProgress((int)((100)*(1-(float)millisUntilFinished/duration)));
			}

			public void onFinish() {
				mTimerView.setText("DONE");
			}
		}.start();

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_boost);

		final ArcProgress progress = (ArcProgress) findViewById(R.id.progress);
		mTimerView = (TextView) findViewById(R.id.timerView);
	}
}
