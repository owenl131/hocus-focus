package com.nushhacks.angelhackapp;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
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

/**
 * Created by User on 6/17/2017.
 */

public class TasksActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*StringBuilder result = new StringBuilder();

        try {
            InputStream inputStream = openFileInput("hi.json");
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            OutputStreamWriter outputStream = null;
            outputStream = new OutputStreamWriter(openFileOutput("hi.json", Context.MODE_PRIVATE));
            outputStream.write("hi");
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //OutputStreamWriter outputStream = new OutputStreamWriter(context.openFileOutput("hi.json", Context.MODE_PRIVATE);

        try {
            InputStream inputStream = openFileInput("hi.json");
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.d("hi", result.toString());*/

        setContentView(R.layout.activity_tasks_list);


       //((EditText)findViewById(R.id.sDateText)).setText("hrllo");

    }

    public void addLayout(View view){
        LinearLayout addedLayout = (LinearLayout) findViewById(R.id.added_layout);

        if(addedLayout.getChildCount()!=1) {
            LinearLayout childLayout = (LinearLayout) addedLayout.getChildAt(addedLayout.getChildCount() - 1);
            EditText etDur = childLayout.findViewById(R.id.dMinorDurText);
            EditText etPlan = childLayout.findViewById(R.id.dMinorPlanText);

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
            EditText etDur = curLayout.findViewById(R.id.dMinorDurText);
            EditText etPlan = curLayout.findViewById(R.id.dMinorPlanText);
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
            TasksIO.writeToFile(obj, name, this);
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
