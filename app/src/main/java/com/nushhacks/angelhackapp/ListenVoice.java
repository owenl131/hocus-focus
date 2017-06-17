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

public class ListenVoice extends AppCompatActivity implements RecognitionListener
{
    TextToSpeech t1;
	TextView mSpeechTextView;
	TextView mInstructionsTextView;

	/* Keyword we are looking for to activate menu */
	private static final String KEYPHRASE = "no what";

	/* Used to handle permission request */
	private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 1;

	private SpeechRecognizer recognizer;

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

		/* Speech recognition */
		// Check if user has given permission to record audio, if not request first and return out of function
		int permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO);
		if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
			ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSIONS_REQUEST_RECORD_AUDIO);
			return;
		}

		// Initialize for the first time
        if(savedInstanceState == null)
            runRecognizerSetup();
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
				runRecognizerSetup();
			} else {
				finish();
			}
		}
	}

	/**
	 * Setup the stuff
	 */
	private void runRecognizerSetup() {
		// Recognizer initialization is a time-consuming and it involves IO,
		// so we execute it in async task
		new AsyncTask<Void, Void, Exception>() {
			@Override
			protected Exception doInBackground(Void... params) {
				try {
					Assets assets = new Assets(ListenVoice.this);
					File assetDir = assets.syncAssets();
					setupRecognizer(assetDir);
				} catch (IOException e) {
					return e;
				}
				return null;
			}

			@Override
			protected void onPostExecute(Exception result) {
				if (result != null) {
					Toast.makeText(getApplicationContext(), "Failed to init recongnizer " + result, Toast.LENGTH_SHORT).show();
				} else {
					switchSpeech("wakeup");
				}
			}
		}.execute();
	}

	/**
	 * Initialize the recognizer with the models and stuff
	 *
	 * @param assetsDir
	 * @throws IOException
	 */
	private void setupRecognizer(File assetsDir) throws IOException {
		// The recognizer can be configured to perform multiple searches
		// of different kind and switch between them

		recognizer = SpeechRecognizerSetup.defaultSetup()
				.setAcousticModel(new File(assetsDir, "en-us-ptm"))
				.setDictionary(new File(assetsDir, "cmudict-en-us.dict"))

				.setRawLogDir(assetsDir) // To disable logging of raw audio comment out this call (takes a lot of space on the device)

				.getRecognizer();
		recognizer.addListener(this);

		/** In your application you might not need to add all those searches.
		 * They are added here for demonstration. You can leave just one.
		 */

		// Create keyword-activation search.
		recognizer.addKeyphraseSearch("wakeup", KEYPHRASE);
		File menuGrammar = new File(assetsDir, "menu.gram");
		recognizer.addGrammarSearch("menu", menuGrammar);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		// Be good programmers and clean up when finish
		if (recognizer != null) {
			recognizer.cancel();
			recognizer.shutdown();
		}
	}

    @Override
    public void onBeginningOfSpeech() {
    }

	@Override
    public void onEndOfSpeech() {
		switchSpeech("wakeup");
    }

    private void switchSpeech(String searchName) {
		recognizer.stop();
		if(searchName.equals("wakeup")) {
			recognizer.startListening(searchName);
			mInstructionsTextView.setText("Say nowhat to continue");
		}
		else {
			// if the user says nowhat, let him give an instruction with a timeout
			recognizer.startListening(searchName, 10000);
			mInstructionsTextView.setText("Tell me something to do");
		}
	}

	@Override
    public void onPartialResult(Hypothesis hypothesis) {
		if(hypothesis == null)
			return;
		String text = hypothesis.getHypstr();
		mSpeechTextView.setText(text);

		if(recognizer.getSearchName().equals("wakeup") && text.equals(KEYPHRASE)) {
			t1.speak("Ya what", TextToSpeech.QUEUE_FLUSH, null);
			switchSpeech("menu");
		}
    }

	@Override
    public void onResult(Hypothesis hypothesis) {
		if(hypothesis == null)
			return;
		String text = hypothesis.getHypstr();
		mSpeechTextView.setText(text);
    }

    @Override
    public void onError(Exception e) {
		Toast.makeText(getApplicationContext(), "There is error! Go fix before pitching!!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onTimeout() {

    }
}
