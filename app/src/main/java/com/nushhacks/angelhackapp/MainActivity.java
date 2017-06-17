package com.nushhacks.angelhackapp;


import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.ActivityOptions;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.NotificationCompat;
import android.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.nushhacks.angelhackapp.NotificationReader.NotificationHandler;
import com.nushhacks.angelhackapp.NotificationReader.NotificationListener;
import com.nushhacks.angelhackapp.SpeechRecognizer.Recognizer;
import com.nushhacks.angelhackapp.TextToSpeech.TTS;

import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    // Demo
    NotificationHandler notificationHandler;
    TTS tts;

    private static final float CIRCLE_FROM = 1.0f;
    private static final float CIRCLE_TO = 0.9f;
    private static final float TEXT_FROM = 1.0f;
    private static final float TEXT_TO = 1.2f;
    private int buttonState = 0;
    private Rect rect;
    private View mInnerCircle;
    private TextView mStartView;
    private BottomSheetBehavior bottomSheetBehavior;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        notificationHandler = new NotificationHandler();
		LocalBroadcastManager.getInstance(this).registerReceiver(notificationHandler, new IntentFilter("Notification"));
		startService(new Intent(this, NotificationListener.class));

		Typeface dosis = Typeface.createFromAsset(getAssets(), "fonts/Dosis-Bold.ttf");
		Typeface cabin = Typeface.createFromAsset(getAssets(), "fonts/Cabin-Bold.ttf");
		((TextView) findViewById(R.id.tasks)).setTypeface(dosis);
		((TextView) findViewById(R.id.appname)).setTypeface(cabin);
		((TextView) findViewById(R.id.start)).setTypeface(dosis);
        mInnerCircle = findViewById(R.id.ring1);
        mStartView = (TextView) findViewById(R.id.start);

        bottomSheetBehavior = BottomSheetBehavior.from(findViewById(R.id.bottomsheet));
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        bottomSheetBehavior.setHideable(false);

        findViewById(R.id.bottomsheettop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED)
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                else if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED)
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });

        findViewById(R.id.mainButton).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        rect = new Rect(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
                        if (buttonState == 0)
                            enterButton();
                        return true;
                    case MotionEvent.ACTION_UP:
                        if (!inButton(v, motionEvent)) {
                            if (buttonState == 1)
                                exitButton();
                        } else if (buttonState == 1 && inButton(v, motionEvent))
                            startClicked();
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        if (buttonState == 0 && inButton(v, motionEvent))
                            enterButton();
                        else if (buttonState == 1 && !inButton(v, motionEvent))
                            exitButton();

                }
                return false;
            }
        });
        findViewById(R.id.plus).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), TasksActivity.class);
                startActivity(intent);
            }
        });
    }

    private void startClicked()
    {
        Intent intent = new Intent(MainActivity.this, BoostActivity.class);
        setupTaskCheckpoints();
        setupSpeechListener();
        if (Build.VERSION.SDK_INT >= 21) {
            ActivityOptions options = ActivityOptions
                    .makeSceneTransitionAnimation(MainActivity.this,
                            Pair.create(findViewById(R.id.ring), "ring"),
                            Pair.create(findViewById(R.id.ring1), "ring1"));
            startActivity(intent, options.toBundle());
        }
        else {
            startActivity(intent);
        }
    }

    private boolean inButton(View v, MotionEvent motionEvent) {
        return rect != null
                && rect.contains(v.getLeft() + (int) motionEvent.getX(),
                v.getTop() + (int) motionEvent.getY());
    }

    private void setupSpeechListener()
    {
        Recognizer recognizer = new Recognizer(getApplicationContext(), new Recognizer.SpeechProcessor() {
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
                        tts.Say("I don't know what you are saying.");
                        break;
                }
            }
        });
        recognizer.runRecognizerSetup();
    }

    // get tasks and schedule notifications
    private void setupTaskCheckpoints()
    {
        final ArrayList<Pair<String, Integer>> arr = TasksIO.getLastSubtasks(this);
        //TasksIO.getAllTasksNameAndDuration(this);
        final TTS tts = new TTS(this);
        Handler handler = new Handler();
        long elapsed = 0;
        long minToMs = 60*1000;
        for (int i = 0; i < arr.size(); i++)
        {
            final String name = arr.get(i).first;
            final long dur = arr.get(i).second;
            Log.d("mainmain", name + "," + dur);
            if (i != 0) {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        tts.Say("Time to start " + name + ".");
                    }
                }, minToMs*elapsed);
            }
            if (i == arr.size() - 1) {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        tts.Say("You're almost done! Only " + dur + " minutes left.");
                    }
                }, (minToMs*elapsed + minToMs*dur*9/10));
            }
            else {
                final String nextName = arr.get(i+1).first;
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        tts.Say("You should be wrapping up " + name +
                                " and moving on to " + nextName +
                                " soon. Just " + dur + " minutes left on " + name + ".");
                    }
                }, (minToMs*elapsed + minToMs*dur*9/10));
            }
            elapsed += dur;
        }
    }

    private void animateButton(float from, float to) {
        ValueAnimator objectAnimator = ValueAnimator.ofFloat(from, to);
        objectAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float val = (float) valueAnimator.getAnimatedValue();
                mInnerCircle.setScaleX(CIRCLE_FROM + val*(CIRCLE_TO - CIRCLE_FROM));
                mInnerCircle.setScaleY(CIRCLE_FROM + val*(CIRCLE_TO - CIRCLE_FROM));
                mStartView.setScaleX(TEXT_FROM + val*(TEXT_TO - TEXT_FROM));
                mStartView.setScaleY(TEXT_FROM + val*(TEXT_TO - TEXT_FROM));
            }
        });
        objectAnimator.start();
    }

    private void enterButton() {
        buttonState = 1;
        animateButton(0, 1);
    }

    private void exitButton() {
        buttonState = 0;
        animateButton(1, 0);
    }
}
