package com.nushhacks.angelhackapp;

import android.os.Build;
import android.os.Message;
import android.speech.SpeechRecognizer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import android.app.VoiceInteractor;
import android.app.VoiceInteractor.PickOptionRequest;
import android.app.VoiceInteractor.PickOptionRequest.Option;
import android.view.Gravity;
import android.widget.TextView;
import java.util.Timer;
import java.util.TimerTask;

public class ListenVoice extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listen_voice);
        startTrigger();
    }

    private void startTrigger() {
        Log.d("voice", "start trigger: ");

        if (Build.VERSION.SDK_INT >= 23)
        {
            Option option = new Option("hey", 0);
            option.addSynonym("hi");
            getVoiceInteractor().submitRequest(new PickOptionRequest(new VoiceInteractor.Prompt("Hello there!"), new Option[]{option}, null) {
                @Override
                public void onPickOptionResult(boolean finished, Option[] selections, Bundle result)
                {
                    if (finished && selections.length == 1)
                    {
                        Message message = Message.obtain();
                        message.obj = result;
                        Log.d("voice", "Hello");
                    }
                }
            });
        }


    }
}
