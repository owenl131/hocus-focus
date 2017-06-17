package com.nushhacks.angelhackapp;

import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;

import com.nushhacks.angelhackapp.NotificationReader.NotificationHandler;
import com.nushhacks.angelhackapp.NotificationReader.NotificationListener;
import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    // Demo
    NotificationHandler notificationHandler;

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
				Intent intent = new Intent(MainActivity.this, BoostActivity.class);
				ActivityOptions options = ActivityOptions
						.makeSceneTransitionAnimation(MainActivity.this,
								Pair.create(findViewById(R.id.ring), "ring"),
								Pair.create(findViewById(R.id.ring1), "ring1"));
				startActivity(intent, options.toBundle());
			}
		});
    }
}
