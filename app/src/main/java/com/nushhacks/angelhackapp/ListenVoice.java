package com.nushhacks.angelhackapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import android.app.VoiceInteractor;
import android.app.VoiceInteractor.PickOptionRequest.Option;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.nushhacks.angelhackapp.SpeechRecognizer.Recognizer;
import com.nushhacks.angelhackapp.TextToSpeech.TTS;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Locale;

import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;
import edu.cmu.pocketsphinx.SpeechRecognizerSetup;

public class ListenVoice extends AppCompatActivity
{
    TextToSpeech t1;
	TextView mSpeechTextView;
	TextView mInstructionsTextView;

	/* Used to handle permission request */
	private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 1;

	private Recognizer recognizer;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listen_voice);

		/* Text to speech */
        t1 = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    t1.setLanguage(Locale.UK);
                }
            }
        });
		mSpeechTextView = (TextView) findViewById(R.id.speech_text);
		mInstructionsTextView = (TextView) findViewById(R.id.instruction_text);

        ((Button)findViewById(R.id.voicebutton)).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                // t1.Say(""+((EditText)findViewById(R.id.text)).getText());
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

		recognizer = new Recognizer(getApplicationContext());

		/* Speech recognition */
		// Check if user has given permission to record audio, if not request first and return out of function
		int permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO);
		if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
			ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSIONS_REQUEST_RECORD_AUDIO);
			return;
		}
		// Initialize for the first time
        if(savedInstanceState == null)
            recognizer.runRecognizerSetup();
    }

	/**
	 * If in onCreate permissions are not previously granted and is requested for inside the if
	 * statement, this will run runRecognizerSetup() if permissions are granted
	 *
	 * @param requestCode
	 * @param permissions
	 * @param grantResults
	 */
	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);

		if (requestCode == PERMISSIONS_REQUEST_RECORD_AUDIO) {
			if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				recognizer.runRecognizerSetup();
			} else {
				finish();
			}
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		// Be good programmers and clean up when finish
		if (recognizer != null) {
			recognizer.cleanup();
		}
	}


}
