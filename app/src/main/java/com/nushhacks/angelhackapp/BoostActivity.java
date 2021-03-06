package com.nushhacks.angelhackapp;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;
import android.widget.Toast;

import com.github.lzyzsd.circleprogress.ArcProgress;
import com.nushhacks.angelhackapp.NotificationReader.NotificationHandler;
import com.nushhacks.angelhackapp.NotificationReader.NotificationListener;
import com.nushhacks.angelhackapp.SpeechRecognizer.Recognizer;
import com.nushhacks.angelhackapp.TextToSpeech.TTS;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by Jeff Sieu on 17/6/2017.
 */

public class BoostActivity extends AppCompatActivity {

	private TextView mTimerView, mSpeechTextView, mTaskNameView;
    private View mGiveUpView, mSpeechCardView;
    private ArcProgress progress;
	private long previousMillis = -1;

	TTS tts;
	NotificationHandler notificationHandler;
	Recognizer recognizer;

	private int duration = 50000;
	private int tick = 1;
	private int updateTick = 500;
    private Date startTime;
    private boolean done = false;
    private boolean startedTimer = false;

    private static long prevRequest = -1;

    @TargetApi(21)
	@Override
	public void onEnterAnimationComplete() {
		super.onEnterAnimationComplete();

        startup();
	}

	public void startup() {

        if (!startedTimer) {
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
                    if (!startedTimer) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                startedTimer = true;
                                new CountDownTimer(duration, tick) {
                                    public void onTick(long millisUntilFinished) {
                                        if (previousMillis == -1)
                                            previousMillis = millisUntilFinished;
                                        if (previousMillis - millisUntilFinished > updateTick) {
                                            previousMillis = millisUntilFinished;
                                            int oldProgress = progress.getProgress();
                                            int newProgress = (int) ((100) * (1 - (float) millisUntilFinished / duration));
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
                                        mGiveUpView.setVisibility(View.GONE);
                                        done = true;
//                        ((TextView) findViewById(R.id.giveuptext)).setText("RETURN TO HOME");
//                        mGiveUpView.setOnClickListener(new View.OnClickListener() {
//                            @Override
//                            public void onClick(View v) {
//                                finish();
//                            }
//                        });
                                    }
                                }.start();
                            }
                        });
                    }
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
    }

    @Override
    protected void onStart() {
        super.onStart();
        startTime = new Date();
    }

    private void setupSpeechListener()
	{
		recognizer = new Recognizer(getApplicationContext(), new Recognizer.SpeechProcessor() {
			@Override
			public void f(String text) {
				Log.d("recognizer", text);

				switch (text.toUpperCase()) {
					case "NOTIFICATION":
					case "UPDATES":
					case "MESSAGES":
					    Date date = new Date();
                        if (date.getTime() - prevRequest < 60*1000)
                        {
                            tts.Say("Focus...");
                            return;
                        }
                        prevRequest  = date.getTime();
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
					case "TIME NOW":
						Date date2 = new Date();
						SimpleDateFormat format = new SimpleDateFormat("h:mm a");
						tts.Say("It is now " + format.format(date2) + ".");
						break;
                    case "TIME LEFT":
                        long left = startTime.getTime() + duration - new Date().getTime();
                        left /= 1000; // seconds
                        tts.Say("You have " + left/60 + " minutes and " + left%60 + " seconds left.");
                        break;
					default:
						// tts.Say("I don't know what you are saying.");
						break;
				}
			}
		});
		recognizer.runRecognizerSetup();
        recognizer.setBoostActivity(this);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_boost);
		duration = getIntent().getIntExtra("Duration", 30) * 60 * 1000;
		progress = (ArcProgress) findViewById(R.id.progress);
        mTaskNameView = (TextView) findViewById(R.id.taskname);
        mTaskNameView.setText(getIntent().getStringExtra("Name"));
        mTaskNameView.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/Dosis-Bold.ttf"));
        mTimerView = (TextView) findViewById(R.id.timerView);
        mTimerView.setText(formatMillis(duration));
        mGiveUpView = findViewById(R.id.giveup);
        mTimerView.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/AnonymousPro-Bold.ttf"));
        mGiveUpView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Give up this challenge?", Snackbar.LENGTH_LONG).setAction("Yes", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(getApplicationContext(), "You gave up your challenge.", Toast.LENGTH_LONG).show();
                        finish();
                    }
                }).show();
            }
        });

		notificationHandler = new NotificationHandler();
		LocalBroadcastManager.getInstance(this).registerReceiver(notificationHandler, new IntentFilter("Notification"));
		startService(new Intent(this, NotificationListener.class));

		tts = new TTS(getApplicationContext());
		setupSpeechListener();


        mSpeechCardView = findViewById(R.id.speechcard);
        mSpeechTextView = (TextView) findViewById(R.id.speechtext);

        if (Build.VERSION.SDK_INT < 21)
            startup();

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
        if (done)
            super.onBackPressed();
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

	public void onSpeechStart() {
        //mSpeechCardView.setVisibility(View.VISIBLE);
        //mSpeechCardView.setAlpha(0f);
        //mSpeechCardView.animate().alpha(1).start();
        //mSpeechTextView.setText("");
    }

    public void onSpeechPartial(String partial) {

        //mSpeechTextView.setText(partial);
    }

    public void onSpeechStop() {

        //mSpeechCardView.animate().alpha(0).start();
    }

}
