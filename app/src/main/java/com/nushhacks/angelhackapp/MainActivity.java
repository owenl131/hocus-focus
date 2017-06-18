package com.nushhacks.angelhackapp;

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
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nushhacks.angelhackapp.TextToSpeech.TTS;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    // Demo
    TTS tts;

    private static final float CIRCLE_FROM = 1.0f;
    private static final float CIRCLE_TO = 0.9f;
    private static final float TEXT_FROM = 1.0f;
    private static final float TEXT_TO = 1.2f;
    private int buttonState = 0;
    private Rect rect;
    private View mInnerCircle, mArrowView;
    private TextView mStartView, mCurrentTaskView;
    private BottomSheetBehavior bottomSheetBehavior;
    private RecyclerView recyclerView;
    private String selectedTaskName = null;
    public static List<Pair<String, Integer>> tasks = new ArrayList<>();


    class BottomSheetViewHolder extends RecyclerView.ViewHolder {
        protected TextView mTitleView, mDurationView;
        protected View mRootView;
        public BottomSheetViewHolder(View v) {
            super(v);
            mTitleView = (TextView) v.findViewById(R.id.title);
            mTitleView.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/Dosis-Bold.ttf"));
            mDurationView = (TextView) v.findViewById(R.id.duration);
            mRootView = v;
        }
    }

    public static String formatMinutes(int minutes) {
        return minutes + " min";
    }

    @Override
    protected void onResume() {
        super.onResume();
        tasks = TasksIO.getAllTasksNameAndDuration(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

		Typeface dosis = Typeface.createFromAsset(getAssets(), "fonts/Dosis-Bold.ttf");
		Typeface cabin = Typeface.createFromAsset(getAssets(), "fonts/Cabin-Bold.ttf");
        mCurrentTaskView = (TextView) findViewById(R.id.tasks);
		mCurrentTaskView.setTypeface(dosis);
        ((TextView) findViewById(R.id.t3)).setTypeface(dosis);

		((TextView) findViewById(R.id.appname)).setTypeface(cabin);
		((TextView) findViewById(R.id.start)).setTypeface(dosis);
        mInnerCircle = findViewById(R.id.ring1);
        mStartView = (TextView) findViewById(R.id.start);
        mArrowView = findViewById(R.id.arrow);
        recyclerView = (RecyclerView) findViewById(R.id.dustbin);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new RecyclerView.Adapter<BottomSheetViewHolder>() {
            @Override
            public BottomSheetViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                return new BottomSheetViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false));
            }

            @Override
            public void onBindViewHolder(BottomSheetViewHolder holder, int position) {
                final Pair<String, Integer> pair = tasks.get(position);
                holder.mTitleView.setText(pair.first);
                holder.mDurationView.setText(formatMinutes(pair.second));
                holder.mRootView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d("MainActivity", pair.first + " " + pair.second);
                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        selectedTaskName = pair.first;
                        mCurrentTaskView.setText(pair.first);
                    }
                });
            }

            @Override
            public int getItemCount() {
                return tasks.size();
            }

        });

        bottomSheetBehavior = BottomSheetBehavior.from(findViewById(R.id.bottomsheet));
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        bottomSheetBehavior.setHideable(false);
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {

            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                mArrowView.setRotation(180*slideOffset);
            }
        });

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
                        } else if (buttonState == 1 && inButton(v, motionEvent)) {
                            startClicked();
                        }
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

        tts = new TTS(getApplicationContext());
    }

    private void startClicked()
    {
        Intent intent = new Intent(MainActivity.this, BoostActivity.class);
        try {
            JSONObject obj = TasksIO.getFromFile(selectedTaskName, this);
            int duration = obj.getInt("duration");
            JSONArray arr = obj.getJSONArray("subtasks");
            ArrayList<Pair<String, Integer>> list = new ArrayList<>();
            for (int i = 0; i < arr.length(); i++)
                list.add(Pair.create(
                        ((JSONObject)arr.get(i)).getString("plan"),
                        ((JSONObject)arr.get(i)).getInt("duration")
                ));
            setupTaskCheckpoints(list);

            if (Build.VERSION.SDK_INT >= 21) {
                ActivityOptions options = ActivityOptions
                        .makeSceneTransitionAnimation(MainActivity.this,
                                Pair.create(findViewById(R.id.ring), "ring"),
                                Pair.create(findViewById(R.id.ring1), "ring1"));
                Bundle bundle = options.toBundle();
                intent.putExtra("Duration", 1000);
                startActivity(intent, bundle);
            } else {
                intent.putExtra("Duration", 1000);
                startActivity(intent);
            }
        }
        catch(Exception e)
        {

        }
    }

    private boolean inButton(View v, MotionEvent motionEvent) {
        return rect != null
                && rect.contains(v.getLeft() + (int) motionEvent.getX(),
                v.getTop() + (int) motionEvent.getY());
    }

    // get subtasks and schedule notifications
    private void setupTaskCheckpoints(final ArrayList<Pair<String, Integer>> arr)
    {
        if (arr == null) return;
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
