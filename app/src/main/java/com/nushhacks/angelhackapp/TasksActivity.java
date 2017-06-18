package com.nushhacks.angelhackapp;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by User on 6/17/2017.
 */

public class TasksActivity extends AppCompatActivity {
    List<JSONObject> subtasks = new ArrayList<>();
    private RecyclerView recyclerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasks_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.abl);
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int i) {
                if (i == 0) {
                    //expanded
                } else if (Math.abs(i) >= appBarLayout.getTotalScrollRange()) {
                    //collapsed
                } else {
                    //in between
                    View extra = findViewById(R.id.extra);
                    float normalised = (float)Math.abs(i) / appBarLayout.getTotalScrollRange();
                    extra.setAlpha(1f-normalised);
                }
            }
        });

        ((TextView) findViewById(R.id.addtask)).setTypeface(Typeface.createFromAsset(getAssets(), "fonts/Cabin-Bold.ttf"));
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setAdapter(new RecyclerView.Adapter<TaskViewHolder>() {
            @Override
            public TaskViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                TaskTextWatcher tw1 = new TaskTextWatcher();
                tw1.setKey("duration");
                TaskTextWatcher tw2 = new TaskTextWatcher();
                tw2.setKey("plan");
                return new TaskViewHolder(LayoutInflater.from(TasksActivity.this).inflate(R.layout.activity_tasks_list_inner, parent, false), tw1, tw2);
            }

            @Override
            public void onBindViewHolder(TaskViewHolder holder, final int position) {
                final JSONObject jsonObject = subtasks.get(position);
                try {
                    holder.tw1.updatePosition(holder.getAdapterPosition());
                    holder.tw2.updatePosition(holder.getAdapterPosition());
                    //holder.mEditText.setText(mDataset[holder.getAdapterPosition()]);
                    holder.mDurationView.setText(subtasks.get(holder.getAdapterPosition()).getString("duration"));
                    holder.mPlanView.setText(Integer.toString(subtasks.get(holder.getAdapterPosition()).getInt("plan")));

                    //holder.mDurationView.setText(Integer.toString(jsonObject.getInt("duration")));
                    //holder.mPlanView.setText(jsonObject.getString("plan"));
                    holder.mCancelView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            subtasks.remove(jsonObject);
                            notifyItemRemoved(position);
                        }
                    });

                } catch (JSONException e){
                    e.printStackTrace();
                }
            }

            @Override
            public int getItemCount() {
                return subtasks.size();
            }
        });
        //TODO: Test whether editing works
        Intent intent = getIntent();
        boolean newTask = intent.getBooleanExtra("new_task", false);
        if(newTask){
            Bundle b = intent.getExtras();
            JSONObject jo = (JSONObject)b.get("json_obj");

            try {
                if(jo==null) return;
                ((EditText)findViewById(R.id.nText)).setText(jo.getString("plan"));
                ((EditText)findViewById(R.id.dText)).setText("" + jo.getInt("duration"));

                JSONArray ja = jo.getJSONArray("subtasks");
                if(ja==null) return;
                for(int i = 0; i < ja.length(); i++){
                    JSONObject joSub = ja.getJSONObject(i);
//                    LinearLayout subtaskLL = addNewSubtask(null);
//                    ((EditText)subtaskLL.findViewById(R.id.dMinorDurText)).setText(joSub.getInt("duration"));
//                    ((EditText)subtaskLL.findViewById(R.id.dMinorPlanText)).setText(joSub.getString("plan"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    class TaskViewHolder extends RecyclerView.ViewHolder {
        EditText mDurationView, mPlanView;
        View mCancelView;
        TaskTextWatcher tw1;
        TaskTextWatcher tw2;
        public TaskViewHolder(View v, TaskTextWatcher tw1, TaskTextWatcher tw2) {
            super(v);
            mDurationView = (EditText) v.findViewById(R.id.dMinorDurText);
            mPlanView = (EditText) v.findViewById(R.id.dMinorPlanText);
            mCancelView = v.findViewById(R.id.cancel);
            this.tw1 = tw1;
            this.tw2 = tw2;
            mDurationView.addTextChangedListener(tw1);
            mPlanView.addTextChangedListener(tw2);
        }
    }

    class TaskTextWatcher implements TextWatcher {
            private int position;
        private String key;
            public void updatePosition(int position) {
                this.position = position;
            }
            public void setKey(String key){
                this.key = key;
            }
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                try {
                    JSONObject jo = subtasks.get(position);
                    Object o = null;
                    if(jo.has(key)) {
                         o = jo.get(key);
                    }
                    if(key.equals("duration") || o instanceof Integer){
                        subtasks.get(position).put(key, charSequence.toString().trim().equalsIgnoreCase("")?0:Integer.parseInt(charSequence.toString()));
                    }
                    else if (o instanceof String){
                        subtasks.get(position).put(key, charSequence.toString());
                    }
                    Log.i(key,subtasks.get(position).toString());
                    Log.i(key,""+position);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void afterTextChanged(Editable editable) {
            }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.tick) {
            Log.d("TaskActivity", "Save to file");
            saveToFile(null);
            return true;
        } else if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void addNewSubtask(View view) throws JSONException{
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("plan", "");
        jsonObject.put("duration", null);
        subtasks.add(jsonObject);
        recyclerView.getAdapter().notifyDataSetChanged();
    }

    public void saveToFile(View view) {
        JSONObject obj;
        StringBuilder result = new StringBuilder();
        obj = new JSONObject();
        boolean error = false;
        int totalDuration = 0;
        String name = "";

        try {
            //obj.put("s_date", System.currentTimeMillis());
            name = ((EditText) findViewById(R.id.nText)).getText().toString();
            if(name.trim().equalsIgnoreCase("")){
                ((EditText) findViewById(R.id.nText)).setError("Please enter a name.");
                error = true;
            }
            obj.put("name", name);
            totalDuration = Integer.parseInt(((EditText) findViewById(R.id.dText)).getText().toString());
            obj.put("duration", totalDuration);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (NumberFormatException e) {
            ((EditText) findViewById(R.id.dText)).setError("Please enter a valid number.");
            error = true;
        }
        JSONArray arr = new JSONArray();

        int durationSum = 0;
        for (int i = 0; i < subtasks.size(); i++) {
            JSONObject tempObj = subtasks.get(i);
            Log.i("subtask", tempObj.toString());
            arr.put(tempObj);
//            try {
//                int duration = Integer.parseInt(etDur.getText().toString());
//                tempObj.put("duration", duration);
//                durationSum += duration;
//                tempObj.put("plan", etPlan.getText().toString());
//                arr.put(tempObj);
//            } catch (JSONException e) {
//                e.printStackTrace();
//            } catch (NumberFormatException e) {
//                ((EditText) findViewById(R.id.dText)).setError("Please enter a valid number.");
//                error = true;
//            }
        }

        if(totalDuration < durationSum){
            AlertDialog.Builder ad = new AlertDialog.Builder(this);
            ad.setMessage("Your total plan time exceeds your intended time by " + (durationSum-totalDuration) + " minutes. Please reallocate your time to fit the original time.");
            ad.show();
            return;
        }

        try {
            obj.put("subtasks", arr);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if(TasksIO.checkNameExist(name, this)){
            ((EditText) findViewById(R.id.nText)).setError("Please enter another name.");
            error = true;
        }

        if (!error){
            TasksIO.writeToFile(obj, this);
            finish();
        }
        /*File f = getFilesDir();
        File curFile = new File(getFilesDir().getAbsolutePath() + "/" + name + ".json");

        if (!error) {
            try {
                OutputStreamWriter outputStream = null;
                outputStream = new OutputStreamWriter(new FileOutputStream(curFile));
                outputStream.write(obj.toString());
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                InputStream inputStream = new FileInputStream(curFile);
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }


            Log.d("HIHIHIHI", result.toString());

        }*/
    }
}
