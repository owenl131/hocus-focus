package com.nushhacks.angelhackapp;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import android.app.VoiceInteractor;
import android.app.VoiceInteractor.PickOptionRequest;
import android.app.VoiceInteractor.PickOptionRequest.Option;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;
import edu.cmu.pocketsphinx.SpeechRecognizerSetup;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.V;

public class ListenVoice extends AppCompatActivity
{
    TextToSpeech t1;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listen_voice);
        t1 = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    t1.setLanguage(Locale.UK);
                }
            }
        });
        ((Button)findViewById(R.id.voicebutton)).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                t1.speak(""+((EditText)findViewById(R.id.text)).getText(), TextToSpeech.QUEUE_FLUSH, null);
                if (false && Build.VERSION.SDK_INT >= 23)
                {
                    Option option = new Option("hey", 0);
                    option.addSynonym("hi");
                    getVoiceInteractor().submitRequest(new VoiceInteractor.PickOptionRequest(new VoiceInteractor.Prompt("Hello"), new Option[]{option}, null){
                    });
                }
            }
        });

        runRecognizerSetup();
        //startTrigger();
    }

    private void startTrigger() {
        Log.d("voice2", "start trigger: ");
        String assetsDir = "models";
        try
        {
            getAssets();
            SpeechRecognizer speechRecognizer = SpeechRecognizerSetup.defaultSetup()
                    //.setAcousticModel(new File("en-us-ptm"))
                    .setDictionary(new File("cmudict-en-us.dict"))
                    .getRecognizer();
            speechRecognizer.addListener(new RecognitionListener()
            {
                @Override
                public void onBeginningOfSpeech()
                {

                }

                @Override
                public void onEndOfSpeech()
                {

                }

                @Override
                public void onPartialResult(Hypothesis hypothesis)
                {

                }

                @Override
                public void onResult(Hypothesis hypothesis)
                {
                    Log.d("voice", hypothesis.getHypstr());
                }

                @Override
                public void onError(Exception e)
                {

                }

                @Override
                public void onTimeout()
                {

                }
            });
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        /*if (Build.VERSION.SDK_INT >= 23)
        {
            Option option = new Option("hey", 0);
            option.addSynonym("hi");
            getVoiceInteractor().submitRequest(new VoiceInteractor.PickOptionRequest(new VoiceInteractor.Prompt("Hello"), new Option[]{option}, null){
            });
        }*/


    }


    private void runRecognizerSetup() {
        // Recognizer initialization is a time-consuming and it involves IO,
        // so we execute it in async task
        new AsyncTask<Void, Void, Exception>() {
            @Override
            protected Exception doInBackground(Void... params) {
                startTrigger();
                return null;
            }

            @Override
            protected void onPostExecute(Exception result) {
            }
        }.execute();
    }
}
