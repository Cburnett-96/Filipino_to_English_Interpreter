package com.orbotz.filipino_english_interpreter;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    public static final Integer RecordAudioRequestCode = 1;
    public static int CAMERA_REQUEST_CODE = 123;
    public static int STORAGE_REQUEST_CODE = 100;
    Handler handler;
    SharedPref prefs;
    TTS tts;
    LoadingData loadingData;

    RelativeLayout relativeLayout;
    com.google.android.material.floatingactionbutton.FloatingActionButton
            btnMic, btnSpeaker, btnSpeakerEng, btnCopy, btnCopyEng, btnFavorite, btnPhotoCam, btnEnter;
    EditText editTextInputFilipino;
    TextView tvTranslatedEng;

    Uri imageurl = null;
    TextRecognizer recognizer;
    Translator tagalogTranslator, englishTranslator;
    TranslatorOptions options_2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        prefs = new SharedPref(this);
        tts = new TTS(this);
        handler = new Handler();
        loadingData = new LoadingData(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        relativeLayout = findViewById(R.id.parentRelative);
        btnMic = findViewById(R.id.btn_Mic);
        btnSpeaker = findViewById(R.id.btn_Speaker);
        btnSpeakerEng = findViewById(R.id.btn_SpeakerEng);
        btnCopy = findViewById(R.id.btn_Copy);
        btnCopyEng = findViewById(R.id.btn_CopyEng);
        btnFavorite = findViewById(R.id.btn_Favorite);
        btnPhotoCam = findViewById(R.id.btn_PhotoCamera);
        btnEnter = findViewById(R.id.btn_Enter);
        editTextInputFilipino = findViewById(R.id.editTextInputFilipino);
        tvTranslatedEng = findViewById(R.id.tv_translatedEng);

        SetTheme();
        Translated();

        btnMic.setOnClickListener(view -> SpeakToText());

        btnSpeaker.setOnClickListener(view -> {
            String text = editTextInputFilipino.getText().toString();
            tts.TTSFilipino(text);
        });
        btnSpeakerEng.setOnClickListener(view -> {
            String text = tvTranslatedEng.getText().toString();
            tts.TTSEnglish(text);
        });

        btnCopy.setOnClickListener(view -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Filipino text",editTextInputFilipino.getText().toString());
            clipboard.setPrimaryClip(clip);
        });
        btnCopyEng.setOnClickListener(view -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("English text",tvTranslatedEng.getText().toString());
            clipboard.setPrimaryClip(clip);
        });

        btnFavorite.setOnClickListener(view -> {
            Toast.makeText(MainActivity.this, "Hindi pa gawa!", Toast.LENGTH_SHORT).show();
        });
        recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        btnPhotoCam.setOnClickListener(this::showPopupWindow);

        btnEnter.setOnClickListener(view -> {
            String loading = "Naglo-load ang resulta...";
            String NoInput = "Maglagay ka nang teksto.";
            if (editTextInputFilipino.getText().toString().isEmpty()){
                tvTranslatedEng.setHint(NoInput);
                return;
            }
            tvTranslatedEng.setHint(loading);
            String text = editTextInputFilipino.getText().toString();
            translate_tagalog(text);
        });
    }

    private void Translated(){
        loadingData.StartLoadingDialog();
        TranslatorOptions options = new TranslatorOptions.Builder()
                .setSourceLanguage(TranslateLanguage.TAGALOG)
                .setTargetLanguage(TranslateLanguage.ENGLISH)
                .build();
        tagalogTranslator= Translation.getClient(options);

        options_2 = new TranslatorOptions.Builder()
                .setSourceLanguage(TranslateLanguage.ENGLISH)
                .setTargetLanguage(TranslateLanguage.TAGALOG)
                .build();

        DownloadConditions conditions = new DownloadConditions.Builder()
                .requireWifi()
                .build();

        tagalogTranslator.downloadModelIfNeeded(conditions)
                .addOnSuccessListener(
                        new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void v) {
                                // Model downloaded successfully. Okay to start translating.
                                download_data();
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Model couldn’t be downloaded or other internal error.
                                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
    }

    private void translate_tagalog(String text) {
        tagalogTranslator.translate(text)
                .addOnSuccessListener(
                        new OnSuccessListener<String>() {
                            @Override
                            public void onSuccess(@NonNull String translatedText) {
                                // Translation successful.
                                tvTranslatedEng.setText(translatedText);
                                btnFavorite.setVisibility(View.VISIBLE);
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Error.
                                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
    }

    private void download_data() {
        englishTranslator = Translation.getClient(options_2);
        englishTranslator.downloadModelIfNeeded()
                .addOnSuccessListener(
                        new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void v) {
                                // Model downloaded successfully. Okay to start translating.
                                // (Set a flag, unhide the translation UI, etc.)
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Model couldn’t be downloaded or other internal error.
                                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
        loadingData.DismissDialog();
    }

    private void SetTheme(){
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        if (prefs.loadNightModeState()) {
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.dark_black));
            window.setNavigationBarColor(ContextCompat.getColor(this, R.color.dark_blue));
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            relativeLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.dark_black));
            Drawable background = AppCompatResources.getDrawable(this, R.drawable.rounded_card);
            GradientDrawable changeBackground = (GradientDrawable) background;
            changeBackground.setColor(ContextCompat.getColor(this, R.color.dark_blue));
            btnSpeakerEng.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.dark_black)));
            btnCopyEng.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.dark_black)));
            btnFavorite.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.dark_black)));

            btnMic.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.dark_blue)));
            btnMic.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.white_smoke)));
            btnSpeaker.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.dark_blue)));
            btnSpeaker.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.white_smoke)));
            btnCopy.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.dark_blue)));
            btnCopy.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.white_smoke)));
            btnPhotoCam.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.dark_blue)));
            btnPhotoCam.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.white_smoke)));
            btnEnter.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.dark_blue)));
            btnEnter.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.white_smoke)));
        } else {
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.violet));
            window.setNavigationBarColor(ContextCompat.getColor(this, R.color.white_smoke));
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            Drawable background = AppCompatResources.getDrawable(this, R.drawable.rounded_card);
            GradientDrawable changeBackground = (GradientDrawable) background;
            changeBackground.setColor(ContextCompat.getColor(this, R.color.white_smoke));
        }
    }

    MenuItem itemDark, itemAbout, itemHistory, itemFavorites;
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);
        if(menu instanceof MenuBuilder){
            MenuBuilder m = (MenuBuilder) menu;
            //noinspection RestrictedApi
            m.setOptionalIconsVisible(true);
        }
        itemDark = menu.findItem(R.id.darkMode);
        itemAbout = menu.findItem(R.id.about);
        itemFavorites = menu.findItem(R.id.favorites);
        itemHistory = menu.findItem(R.id.history);
        if(prefs.loadNightModeState()) {
            itemDark.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_light));
            itemDark.setTitle("Malinaw na tema");
            itemDark.setIconTintList(ContextCompat.getColorStateList(this, R.color.white));
            itemAbout.setIconTintList(ContextCompat.getColorStateList(this, R.color.white));
            itemFavorites.setIconTintList(ContextCompat.getColorStateList(this, R.color.white));
            itemHistory.setIconTintList(ContextCompat.getColorStateList(this, R.color.white));
        }
        return true;
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.darkMode:
                prefs.setNightModeState(!prefs.loadNightModeState());
                restartApp();
                return true;
            case R.id.favorites:
            case R.id.history:
            case R.id.about:
                Toast.makeText(this, item.getTitle(), Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void restartApp(){
        Intent i = new Intent(getApplicationContext(),MainActivity.class);
        startActivity(i);
        finish();
    }

    private void SpeakToText(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
            checkPermission();
        }
        else {
            Intent speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "fil_PH");
            speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Magsalita sa Filipino");
            try {
                startActivityForResult(speechRecognizerIntent, RecordAudioRequestCode);
            }
            catch (Exception e) {
                Toast.makeText(this,"Your device does not have a Voice Recognition Service installed.",Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RecordAudioRequestCode) {
            if (resultCode == RESULT_OK && data != null) {
                ArrayList<String> result = data.getStringArrayListExtra(
                        RecognizerIntent.EXTRA_RESULTS);
                editTextInputFilipino.setText(
                        Objects.requireNonNull(result).get(0));
                translate_tagalog(Objects.requireNonNull(result).get(0));
            }
        }
    }

    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.RECORD_AUDIO},RecordAudioRequestCode);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RecordAudioRequestCode && grantResults.length > 0 ){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
                Toast.makeText(this,"Permission Granted",Toast.LENGTH_SHORT).show();
        }
    }

    public void showPopupWindow(final View view) {
        view.getContext();
        LayoutInflater inflater = (LayoutInflater) view.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_select, null);

        //Specify the length and width through constants
        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;

        //Create a window with our parameters
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, true);

        //Set the location of the window on the screen
        popupWindow.showAtLocation(view, Gravity.BOTTOM, 0, 0);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setFocusable(true);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());

        Button buttonCamera = popupView.findViewById(R.id.btn_Camera);
        Button buttonGallery = popupView.findViewById(R.id.btn_Gallery);
        recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        if (prefs.loadNightModeState()) {
            buttonCamera.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.dark_black)));
            buttonGallery.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.dark_black)));
        }

        buttonCamera.setOnClickListener(view1 -> {
            //Check permission
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                //Grant permission
                requestPermissions(new String[]{Manifest.permission.CAMERA},CAMERA_REQUEST_CODE);
            } else {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.TITLE, "Title");
                values.put(MediaStore.Images.Media.DESCRIPTION, "Descriptions");

                imageurl = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageurl);
                cameraActivityResultLauncher.launch(intent);
                popupWindow.dismiss();
            }
        });

        buttonGallery.setOnClickListener(view1 -> {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                //Grant permission
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},STORAGE_REQUEST_CODE);
            } else {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");

                galleryActivityResultLauncher.launch(intent);

                popupWindow.dismiss();
            }
        });
    }
    private final ActivityResultLauncher<Intent> galleryActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == Activity.RESULT_OK){
                if (result.getData() != null) {
                    imageurl = result.getData().getData();
                    try {
                        InputImage image = InputImage.fromFilePath(MainActivity.this, imageurl);

                        Task<Text> resultTask = recognizer.process(image).addOnSuccessListener(new OnSuccessListener<Text>() {
                            @Override
                            public void onSuccess(Text text) {
                                String resultText = text.getText();
                                editTextInputFilipino.setText(resultText);
                            }
                        }).addOnFailureListener(e -> Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show());

                    } catch (IOException e) {
                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this,"NO IMAGE SELECTED!",Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(MainActivity.this,"Cancelled!",Toast.LENGTH_SHORT).show();
            }
        }
    });

    private final ActivityResultLauncher<Intent> cameraActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == Activity.RESULT_OK){
                try {
                    InputImage image = InputImage.fromFilePath(MainActivity.this, imageurl);

                    Task<Text> resultTask = recognizer.process(image).addOnSuccessListener(new OnSuccessListener<Text>() {
                        @Override
                        public void onSuccess(Text text) {
                            String resultText = text.getText();
                            editTextInputFilipino.setText(resultText);
                        }
                    }).addOnFailureListener(e -> Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show());

                } catch (IOException e) {
                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(MainActivity.this,"Cancelled!",Toast.LENGTH_SHORT).show();
            }
        }
    });

    private static long back_pressed;
    @Override
    public void onBackPressed() {
        if (back_pressed + 2000 > System.currentTimeMillis()){
            overridePendingTransition(0, 0);
            finishAffinity();
            super.onBackPressed();
        }
        else Toast.makeText(getBaseContext(), "Press once again to exit!", Toast.LENGTH_SHORT).show();
        back_pressed = System.currentTimeMillis();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (loadingData!=null){
            loadingData.DismissDialog();
        }
        Runtime.getRuntime().gc();
    }
}