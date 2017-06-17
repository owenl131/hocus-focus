package com.nushhacks.angelhackapp;


import android.app.ActivityOptions;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.NotificationCompat;
import android.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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

		findViewById(R.id.mainButton).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
                startClicked();
			}
		});
        tts = new TTS(getApplicationContext());
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
        final ArrayList<Pair<String, Integer> > arr = TasksIO.getAllTasksNameAndDuration(this);
        final TTS tts = new TTS(this);
        Handler handler = new Handler();
        long elapsed = 0;
        long minToMs = 60*1000;
        for (int i = 0; i < arr.size(); i++)
        {
            final String name = arr.get(i).first;
            final long dur = arr.get(i).second;
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
}
