package com.nushhacks.angelhackapp;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Pair;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

/**
 * Created by User on 6/17/2017.
 */

public class TasksIO {
    /* Files in the format of:
        {
            "name":,
            "duration":,
            "subtasks":[
                {
                    "duration":"",
                    "plan":""
                }
            ]
        }
     */

    public static final String FILE_NAME = "data.txt";

    public static boolean checkNameExist(String name, Context ctx){
        /*File[] curDir = aca.getFilesDir().listFiles();
        for (File file : curDir) {
            if (file.getName().equals(name + ".json")) return false;
        }
        return true;*/

        StringBuilder result = new StringBuilder();
        File curFile = new File(ctx.getFilesDir().getAbsolutePath() + "/" + FILE_NAME + ".json");

        try {
            InputStream inputStream = new FileInputStream(curFile);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
            JSONArray ja = new JSONArray(result.toString());
            for(int i = 0; i < ja.length(); i++){
                JSONObject jo = ja.getJSONObject(i);
                if(name.equals(jo.get("name"))) return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void writeToFile(JSONObject obj, Context ctx) {
        //File f = aca.getFilesDir();
        /*File curFile = new File(aca.getFilesDir().getAbsolutePath() + "/" + name + ".json");

        try {
            OutputStreamWriter outputStream = null;
            outputStream = new OutputStreamWriter(new FileOutputStream(curFile));
            outputStream.write(obj.toString());
            outputStream.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }*/
        JSONArray ja = getAllFromFile(ctx);

        if(ja!=null) {
            ja.put(obj);
        }
        else{
            ja = new JSONArray();
        }

        File curFile = new File(ctx.getFilesDir().getAbsolutePath() + "/" + FILE_NAME + ".json");

        try {
            OutputStreamWriter outputStream = new OutputStreamWriter(new FileOutputStream(curFile));
            outputStream.write(ja.toString());
            outputStream.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static JSONArray getAllFromFile(Context ctx) {
        //File f = aca.getFilesDir();
        StringBuilder result = new StringBuilder();
        File curFile = new File(ctx.getFilesDir().getAbsolutePath() + "/" + FILE_NAME + ".json");

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

        try {
            return new JSONArray(result.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static JSONObject getFromFile(String name, Context ctx) {
        //File f = aca.getFilesDir();
        /*StringBuilder result = new StringBuilder();
        File curFile = new File(aca.getFilesDir().getAbsolutePath() + "/" + FILE_NAME + ".json");

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

        try {
            return new JSONObject(result.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;*/

        JSONArray ja = getAllFromFile(ctx);
        if(ja == null) return null;
        for(int i = 0; i < ja.length(); i++){
            try {
                JSONObject jo = ja.getJSONObject(i);
                if(jo.getString("name").equals(name)){
                    return jo;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        return null;
    }

    public static ArrayList<Pair<String,Integer>> getAllTasksNameAndDuration(Context ctx) {
        /*File[] curDir = aca.getFilesDir().listFiles();
        ArrayList<Pair<String,Integer>> list = new ArrayList<>();
        for (File file : curDir) {
            try {
                Pair<String, Integer> curPair = Pair.create(file.getName().split(".json")[0], getFromFile(file.getName(), aca).getInt("duration"));
                list.add(curPair);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return (Pair<String,Integer>[]) list.toArray();*/
        ArrayList<Pair<String,Integer>> list = new ArrayList<>();
        JSONArray ja = getAllFromFile(ctx);
        if (ja == null)
            return list;
        Log.d("tasksio", ja.toString());
        try
        {
            Log.d("taskio", ja.getString(0));
        }catch (Exception e)
        {}
        for(int i = 0; i < ja.length(); i++){
            try {
                JSONObject jo = ja.getJSONObject(i);
                Pair<String, Integer> curPair = Pair.create(jo.getString("name"), jo.getInt("duration"));
                list.add(curPair);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        return list;
    }
}
