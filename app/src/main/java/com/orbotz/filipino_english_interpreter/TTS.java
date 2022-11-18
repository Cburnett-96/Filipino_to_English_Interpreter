package com.orbotz.filipino_english_interpreter;

import android.content.Context;
import android.speech.tts.TextToSpeech;

import java.util.Locale;

public class TTS {
    TextToSpeech textToSpeechFil, textToSpeechEng;
    public boolean textToSpeechIsInitialized = false;

    public TTS(Context context){
        textToSpeechFil = new TextToSpeech(context, status -> {
            if(status == TextToSpeech.SUCCESS){
                textToSpeechIsInitialized = true;
                int result = textToSpeechFil.setLanguage(new Locale("fil_PH"));

                if(result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED ){
                    System.out.println("Language Not Supported");
                }
            }
        });

        textToSpeechEng = new TextToSpeech(context, status -> {
            if(status == TextToSpeech.SUCCESS){
                textToSpeechIsInitialized = true;
                int result = textToSpeechEng.setLanguage(Locale.ENGLISH);

                if(result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED ){
                    System.out.println("Language Not Supported");
                }
            }
        });
    }


    public void TTSFilipino(String text){
        textToSpeechFil.speak(text, TextToSpeech.QUEUE_FLUSH,null);
        textToSpeechFil.setSpeechRate(0.7f);
    }

    public void TTSEnglish(String text){
        textToSpeechEng.speak(text, TextToSpeech.QUEUE_FLUSH,null);
        textToSpeechEng.setSpeechRate(0.7f);
    }
}
