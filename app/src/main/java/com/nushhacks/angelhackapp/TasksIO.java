package com.nushhacks.angelhackapp;

import android.support.v7.app.AppCompatActivity;

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

    public static boolean checkNameExist(String name, AppCompatActivity aca) {
        File[] curDir = aca.getFilesDir().listFiles();
        for (File file : curDir) {
            if (file.getName().equals(name + ".json")) return false;
        }
        return true;
    }

    public static void writeToFile(JSONObject obj, String name, AppCompatActivity aca) {
        //File f = aca.getFilesDir();
        File curFile = new File(aca.getFilesDir().getAbsolutePath() + "/" + name + ".json");

        try {
            OutputStreamWriter outputStream = null;
            outputStream = new OutputStreamWriter(new FileOutputStream(curFile));
            outputStream.write(obj.toString());
            outputStream.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static JSONObject getFromFile(String name, AppCompatActivity aca) {
        //File f = aca.getFilesDir();
        StringBuilder result = new StringBuilder();
        File curFile = new File(aca.getFilesDir().getAbsolutePath() + "/" + name + ".json");

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
        return null;
    }

    public static String[] getAllFileName(AppCompatActivity aca) {
        File[] curDir = aca.getFilesDir().listFiles();
        ArrayList<String> list = new ArrayList<>();
        for (File file : curDir) {
            list.add(file.getName().split(".json")[0]);
        }
        return (String[]) list.toArray();
    }
}
