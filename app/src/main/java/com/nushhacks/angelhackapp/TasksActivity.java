package com.nushhacks.angelhackapp;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by User on 6/17/2017.
 */

public class TasksActivity extends AppCompatActivity {
    List<JSONObject> tasks = new ArrayList<>();
    private RecyclerView recyclerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasks_list);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setAdapter(new RecyclerView.Adapter<TaskViewHolder>() {
            @Override
            public TaskViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                return null;
            }

            @Override
            public void onBindViewHolder(TaskViewHolder holder, int position) {
                holder.mDurationView.setText();
            }

            @Override
            public int getItemCount() {
                return tasks.size();
            }

        });

       //((EditText)findViewById(R.id.sDateText)).setText("hrllo");

    }

    class TaskViewHolder extends RecyclerView.ViewHolder {
        EditText mDurationView, mPlanView;
        public TaskViewHolder(View itemView) {
            super(itemView);
            mDurationView = (EditText) childLayout.findViewById(R.id.dMinorDurText);
            mPlanView = (EditText) childLayout.findViewById(R.id.dMinorPlanText);
        }
    }

    //TODO: implement for an X beside the particular Linearlayout (Should work not tried yet)
    public void removeLayout(View view){
        LinearLayout currentLayout = (LinearLayout)view.getParent();
        LinearLayout currentLayoutParent = (LinearLayout)currentLayout.getParent();
        currentLayoutParent.removeView(currentLayout);
    }

    public void addLayout(View view){
        LinearLayout addedLayout = (LinearLayout) findViewById(R.id.added_layout);

        if(addedLayout.getChildCount()!=1) {
            LinearLayout childLayout = (LinearLayout) addedLayout.getChildAt(addedLayout.getChildCount() - 1);
            EditText etDur = (EditText) childLayout.findViewById(R.id.dMinorDurText);
            EditText etPlan = (EditText) childLayout.findViewById(R.id.dMinorPlanText);

            if (!etDur.getText().toString().trim().equalsIgnoreCase("") && !etPlan.getText().toString().trim().equalsIgnoreCase("")) {
                getLayoutInflater().inflate(R.layout.activity_tasks_list_inner, addedLayout);
            }
        }
        else{
            getLayoutInflater().inflate(R.layout.activity_tasks_list_inner, addedLayout);
        }
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

        LinearLayout addedLayout = (LinearLayout) findViewById(R.id.added_layout);
        for (int i = 1; i < (addedLayout.getChildCount()); i++) {
            JSONObject tempObj = new JSONObject();
            LinearLayout curLayout = (LinearLayout) addedLayout.getChildAt(i);
            EditText etDur = (EditText) curLayout.findViewById(R.id.dMinorDurText);
            EditText etPlan = (EditText) curLayout.findViewById(R.id.dMinorPlanText);
            try {
                int duration = Integer.parseInt(etDur.getText().toString());
                tempObj.put("duration", duration);
                durationSum += duration;
                tempObj.put("plan", etPlan.getText().toString());
                arr.put(tempObj);
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (NumberFormatException e) {
                ((EditText) findViewById(R.id.dText)).setError("Please enter a valid number.");
                error = true;
            }
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
