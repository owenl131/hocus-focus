package com.nushhacks.angelhackapp;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Transformation;
import android.widget.TextView;
import android.widget.Toast;

import com.github.lzyzsd.circleprogress.ArcProgress;
import com.nushhacks.angelhackapp.NotificationReader.NotificationHandler;
import com.nushhacks.angelhackapp.NotificationReader.NotificationListener;
import com.nushhacks.angelhackapp.SpeechRecognizer.Recognizer;
import com.nushhacks.angelhackapp.TextToSpeech.TTS;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

/**
 * Created by Jeff Sieu on 17/6/2017.
 */

public class BoostActivity extends AppCompatActivity {

	private TextView mTimerView;
    private View mGiveUpView;
    private ArcProgress progress;
	private long previousMillis = -1;

	TTS tts;
	NotificationHandler notificationHandler;
	Recognizer recognizer;

	private int duration = 5000;
	private int tick = 1;
	private int updateTick = 500;

	@Override
	public void onEnterAnimationComplete() {
		super.onEnterAnimationComplete();

		findViewById(R.id.ringbackground).setVisibility(View.VISIBLE);
		findViewById(R.id.ringbackground).setAlpha(1);
		findViewById(R.id.ring).animate().alpha(0).setDuration(1000).start();
		progress.setVisibility(View.VISIBLE);
		ObjectAnimator anim = ObjectAnimator.ofInt(progress, "progress", 100, 0);
		anim.setInterpolator(new DecelerateInterpolator());
		anim.setDuration(1000);
		anim.addListener(new Animator.AnimatorListener() {
			@Override
			public void onAnimationStart(Animator animator) {

			}

			@Override
			public void onAnimationEnd(Animator animator) {
				new CountDownTimer(duration, tick) {
					public void onTick(long millisUntilFinished) {
						if (previousMillis == -1)
							previousMillis = millisUntilFinished;
						if (previousMillis - millisUntilFinished > updateTick) {
							previousMillis = millisUntilFinished;
							int oldProgress = progress.getProgress();
							int newProgress = (int)((100)*(1-(float)millisUntilFinished/duration));
							ObjectAnimator objectAnimator = ObjectAnimator.ofInt(progress, "progress", oldProgress, newProgress);
                            objectAnimator.setInterpolator(new DecelerateInterpolator());
                            objectAnimator.setDuration(updateTick);
                            objectAnimator.start();
						}
						mTimerView.setText(formatMillis(millisUntilFinished));
					}
					public void onFinish() {
						mTimerView.setText("DONE");
                        int oldProgress = progress.getProgress();
                        int newProgress = 100;
                        ObjectAnimator objectAnimator = ObjectAnimator.ofInt(progress, "progress", oldProgress, newProgress);
                        objectAnimator.setInterpolator(new DecelerateInterpolator());
                        objectAnimator.setDuration(updateTick);
                        objectAnimator.start();
					}
				}.start();
			}

			@Override
			public void onAnimationCancel(Animator animator) {

			}

			@Override
			public void onAnimationRepeat(Animator animator) {

			}
		});
		anim.start();
	}

	private void setupSpeechListener()
	{
		recognizer = new Recognizer(getApplicationContext(), new Recognizer.SpeechProcessor() {
			@Override
			public void f(String text) {
				Log.d("recognizer", text);

				switch (text.toUpperCase()) {
					case "NOTIFICATION":
						ArrayList<NotificationHandler.NotificationInfo> notifications = notificationHandler.getNotifications();
						String notificationCount = "Received " + Integer.toString(notifications.size());
						if(notifications.size() == 1)
							notificationCount += " notification.";
						else
							notificationCount += " notifications.";
						tts.Say(notificationCount);
						for (NotificationHandler.NotificationInfo info : notifications) {
							PackageManager pm = getApplicationContext().getPackageManager();
							ApplicationInfo ai;
							try {
								ai = pm.getApplicationInfo(info.packageName, 0);
							} catch (final PackageManager.NameNotFoundException e) {
								ai = null;
							}
							String appName = ai != null ? (String) pm.getApplicationLabel(ai) : "unknown application";
							tts.Say("Notification by ".concat(appName));
							tts.Say(info.title);
							tts.Say(info.text);
						}
						break;
					default:
						// tts.Say("I don't know what you are saying.");
						break;
				}
			}
		});
		recognizer.runRecognizerSetup();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_boost);
		duration = getIntent().getIntExtra("Duration", 30);
		progress = (ArcProgress) findViewById(R.id.progress);
		mTimerView = (TextView) findViewById(R.id.timerView);
        mTimerView.setText(formatMillis(duration));
        mGiveUpView = findViewById(R.id.giveup);
        mTimerView.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/AnonymousPro-Bold.ttf"));
        mGiveUpView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(view.getContext(), "You are a shitstain, noob", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

		notificationHandler = new NotificationHandler();
		LocalBroadcastManager.getInstance(this).registerReceiver(notificationHandler, new IntentFilter("Notification"));
		startService(new Intent(this, NotificationListener.class));

		tts = new TTS(getApplicationContext());
		setupSpeechListener();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)  {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			onBackPressed();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onBackPressed() {
		// Do nothing
	}

	public static String formatMillis(long millis) {
		return String.format("%02d:%02d",
				TimeUnit.MILLISECONDS.toMinutes(millis) -
						TimeUnit.MINUTES.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
				TimeUnit.MILLISECONDS.toSeconds(millis) -
						TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		recognizer.cleanup();
	}

}
