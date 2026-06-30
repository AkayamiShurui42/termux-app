package com.termux.app.voice;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import com.termux.terminal.TerminalSession;
import java.util.ArrayList;

public class TerminalSpeechEngine {
    private SpeechRecognizer speechRecognizer;
    private Intent speechIntent;
    private TerminalSession mSession;
    private Context context;

    public TerminalSpeechEngine(Context context, TerminalSession session) {
        this.context = context;
        this.mSession = session;
        initSpeechEngine();
    }

    private void initSpeechEngine() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
        speechIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {}

            @Override
            public void onBeginningOfSpeech() {}

            @Override
            public void onRmsChanged(float rmsdB) {}

            @Override
            public void onBufferReceived(byte[] buffer) {}

            @Override
            public void onEndOfSpeech() {}

            @Override
            public void onError(int error) {}

            @Override
            public void onResults(Bundle results) {
                processSpeech(results);
            }

            @Override
            public void onPartialResults(Bundle partialResults) {
                processSpeech(partialResults);
            }

            @Override
            public void onEvent(int eventType, Bundle params) {}
        });
    }

    public void startListening() {
        speechRecognizer.startListening(speechIntent);
    }

    public void stopListening() {
        speechRecognizer.stopListening();
    }

    private void processSpeech(Bundle results) {
        ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        if (matches != null && !matches.isEmpty()) {
            String text = matches.get(0).toLowerCase();
            String processedText = translateTokens(text);
            if (mSession != null) {
                mSession.write(processedText);
            }
        }
    }

    private String translateTokens(String input) {
        String result = input;
        result = result.replace("open bracket", "[");
        result = result.replace("close bracket", "]");
        result = result.replace("pipe to grep", "| grep ");

        // Simple regex or loop to handle "snake case [word]" and "camel case [word]"
        result = result.replaceAll("snake case (\\w+)", "$1");
        // A robust implementation would parse the next word, but for simplicity we assume it captures the word correctly
        java.util.regex.Matcher snakeMatcher = java.util.regex.Pattern.compile("snake case (\\w+)").matcher(input);
        while (snakeMatcher.find()) {
            String word = snakeMatcher.group(1);
            result = result.replace(snakeMatcher.group(0), word.toLowerCase().replace(" ", "_")); // simplified
        }
        
        java.util.regex.Matcher camelMatcher = java.util.regex.Pattern.compile("camel case (\\w+)").matcher(input);
        while (camelMatcher.find()) {
            String word = camelMatcher.group(1);
            String camelWord = word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase();
            result = result.replace(camelMatcher.group(0), camelWord);
        }

        return result;
    }
}
