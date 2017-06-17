package com.nushhacks.angelhackapp.SpeechRecognizer;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.Toast;

import com.nushhacks.angelhackapp.ListenVoice;
import com.nushhacks.angelhackapp.TextToSpeech.TTS;

import java.io.File;
import java.io.IOException;

import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;
import edu.cmu.pocketsphinx.SpeechRecognizerSetup;

/**
 * Created by danie on 17/6/2017.
 */

public class Recognizer implements RecognitionListener {
	private SpeechRecognizer recognizer;

	/* Keyword we are looking for to activate menu */
	private static final String KEYPHRASE = "ok google";//"no what";

	private String currentSearchName = "wakeup";

	SpeechProcessor speechProcessor;
	TTS tts;

	boolean firstCall = true;

	Context context;
	public Recognizer(Context context, SpeechProcessor speechProcessor) {
		this.context = context;
		this.speechProcessor = speechProcessor;
		this.tts = new TTS(context);
	}

	/**
	 * Setup the stuff
	 */
	public void runRecognizerSetup() {
		// Recognizer initialization is a time-consuming and it involves IO,
		// so we execute it in async task
		new AsyncTask<Void, Void, Exception>() {
			@Override
			protected Exception doInBackground(Void... params) {
				try {
					Assets assets = new Assets(context);
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
					Toast.makeText(context, "Failed to init recongnizer " + result, Toast.LENGTH_SHORT).show();
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
	public void onBeginningOfSpeech() {
	}

	@Override
	public void onEndOfSpeech() {
		recognizer.stop();
	}

	public void switchSpeech(String searchName) {
		if(firstCall) {
			Log.i("Current Text", "I am ready");
			tts.Say("I am ready");
			firstCall = false;
		}
		if(searchName.equals("wakeup")) {
			Log.i("Current Text", "wakeup");
			recognizer.startListening(searchName);
		}
		else {
			Log.i("Current Text", "menu");
			recognizer.startListening(searchName, 2000);
		}
	}

	@Override
	public void onPartialResult(Hypothesis hypothesis) {
		if(hypothesis == null)
			return;
		String text = hypothesis.getHypstr();
		Log.d("Current Partial", text);
		//speechProcessor.f(text);
	}

	@Override
	public void onResult(Hypothesis hypothesis) {
		if(hypothesis == null) {
			switchSpeech("wakeup");
			return;
		}
		String text = hypothesis.getHypstr();
		Log.i("Current Text", text);

		if(recognizer.getSearchName().equals("wakeup") && text.contains(KEYPHRASE)) {
			Log.i("Current Text", "Tell me what you want");
			tts.Say("Tell me what you want");
		} else if (recognizer.getSearchName().equals("menu")) {
			speechProcessor.f(text);
		}
		switchSpeech("menu");
	}

	@Override
	public void onError(Exception e) {
		Toast.makeText(context, "There is error! Go fix before pitching!!", Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onTimeout() {
		Log.i("Current Text", "Timeout");
		switchSpeech("wakeup");
	}

	public void cleanup() {
		recognizer.cancel();
		recognizer.shutdown();
	}

	public interface SpeechProcessor {
		void f(String text);
	}
}
