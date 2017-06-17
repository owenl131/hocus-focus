package com.nushhacks.angelhackapp.TextToSpeech;

import android.content.Context;
import android.os.Build;
import android.speech.tts.TextToSpeech;

import org.w3c.dom.Text;

import java.util.Locale;

/**
 * Created by Owen Leong on 17/6/2017.
 */

public class TTS
{
    TextToSpeech t;
    public TTS(Context c)
    {
        t = new TextToSpeech(c, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    t.setLanguage(Locale.UK);
                }
            }
        });
    }
    public void Say(String s)
    {
        if (Build.VERSION.SDK_INT >= 21)
            t.speak(s, TextToSpeech.QUEUE_FLUSH, null, "");
        else
            t.speak(s, TextToSpeech.QUEUE_FLUSH, null);
    }

}
