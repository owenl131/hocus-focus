package com.nushhacks.angelhackapp;

import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
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
