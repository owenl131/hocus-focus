package com.nushhacks.angelhackapp.TextToSpeech;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.util.Log;

import org.w3c.dom.Text;

import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

/**
 * Created by Owen Leong on 17/6/2017.
 */

public class TTS
{
    TextToSpeech t;
    //Set<Voice> set;
    //Iterator<Voice> it;

    public TTS(Context c)
    {
        t = new TextToSpeech(c, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    t.setLanguage(Locale.UK);
                }
                /*if (Build.VERSION.SDK_INT >= 21)
                {
                    set = t.getVoices();
                    Log.d("tts", set.toString());
                    it = set.iterator();
                }*/
            }
        });
    }

    public void Say(String s)
    {
        if (Build.VERSION.SDK_INT >= 21)
            t.speak(s, TextToSpeech.QUEUE_FLUSH, null, "");
        else
            t.speak(s, TextToSpeech.QUEUE_FLUSH, null);
        /*new Handler().postDelayed(new Runnable(){
            @Override
            public void run()
            {
                if (Build.VERSION.SDK_INT >= 21)
                {
                    if (it.hasNext())
                        t.setVoice(it.next());
                }
            }
        }, 1000);*/

    }

}
