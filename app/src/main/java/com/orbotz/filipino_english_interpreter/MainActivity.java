package com.orbotz.filipino_english_interpreter;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.drawable.BitmapDrawable;
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
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

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
import com.orbotz.filipino_english_interpreter.Services.SharedPref;
import com.orbotz.filipino_english_interpreter.Services.TTS;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    public static final Integer RecordAudioRequestCode = 1;
    public static int CAMERA_REQUEST_CODE = 123;
    public static int STORAGE_REQUEST_CODE = 100;
    Handler handler;
    SharedPref prefs;
    TTS tts;
    LoadingDataActivity loadingDataActivity;

    RelativeLayout relativeLayout;
    LinearLayout layoutTranslated;
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
        loadingDataActivity = new LoadingDataActivity(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        relativeLayout = findViewById(R.id.parentRelative);
        layoutTranslated = findViewById(R.id.layoutTranslated);
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
            if (prefs.loadLanguageState().equals("fil")) {
                tts.TTSFilipino(text);
            } else {
                tts.TTSEnglish(text);
            }
        });
        btnSpeakerEng.setOnClickListener(view -> {
            String text = tvTranslatedEng.getText().toString();
            if (prefs.loadLanguageState().equals("en")) {
                tts.TTSFilipino(text);
            } else {
                tts.TTSEnglish(text);
            }
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

        String loading, noInput;
        if (prefs.loadLanguageState().equals("en")) {
            loading = "Loading the result...";
            noInput = "Please Input.";
        } else {
            loading = "Naglo-load ang resulta...";
            noInput = "Maglagay ka nang teksto.";
        }

        btnEnter.setOnClickListener(view -> {
            if (editTextInputFilipino.getText().toString().isEmpty()){
                tvTranslatedEng.setHint(noInput);
                return;
            }
            tvTranslatedEng.setHint(loading);
            String text = editTextInputFilipino.getText().toString();
            if (prefs.loadLanguageState().equals("en")) {
                translate_english(text);
            } else {
                translate_tagalog(text);
            }
        });
    }

    private void Translated() {
        loadingDataActivity.StartLoadingDialog();
        TranslatorOptions options = new TranslatorOptions.Builder()
                .setSourceLanguage(TranslateLanguage.TAGALOG)
                .setTargetLanguage(TranslateLanguage.ENGLISH)
                .build();
        tagalogTranslator = Translation.getClient(options);

        options_2 = new TranslatorOptions.Builder()
                .setSourceLanguage(TranslateLanguage.ENGLISH)
                .setTargetLanguage(TranslateLanguage.TAGALOG)
                .build();

        DownloadConditions conditions = new DownloadConditions.Builder()
                .requireWifi()
                .build();

        tagalogTranslator.downloadModelIfNeeded(conditions)
                .addOnSuccessListener(
                        v -> {
                            // Model downloaded successfully. Okay to start translating.
                            download_data();
                        })
                .addOnFailureListener(
                        e -> {
                            // Model couldn’t be downloaded or other internal error.
                            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
    }

    private void translate_tagalog(String text) {
        tagalogTranslator.translate(text)
                .addOnSuccessListener(
                        translatedText -> {
                            // Translation successful.
                            tvTranslatedEng.setText(translatedText);
                            btnFavorite.setVisibility(View.VISIBLE);
                        })
                .addOnFailureListener(
                        e -> {
                            // Error.
                            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
    }

    private void translate_english(String text) {
        englishTranslator.translate(text)
                .addOnSuccessListener(
                        translatedText -> {
                            // Translation successful.
                            tvTranslatedEng.setText(translatedText);
                            btnFavorite.setVisibility(View.VISIBLE);
                        })
                .addOnFailureListener(
                        e -> {
                            // Error.
                            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
    }

    private void download_data() {
        englishTranslator = Translation.getClient(options_2);
        englishTranslator.downloadModelIfNeeded()
                .addOnSuccessListener(
                        v -> {
                        })
                .addOnFailureListener(
                        e -> Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show());
        loadingDataActivity.DismissDialog();
    }

    private void SetTheme(){
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        ChangeLanguage(prefs.loadLanguageState());

        if (prefs.loadNightModeState()) {
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.dark_black));
            window.setNavigationBarColor(ContextCompat.getColor(this, R.color.dark_blue));
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            relativeLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.dark_black));
            layoutTranslated.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.dark_blue)));

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
        }
    }

    MenuItem itemDark, itemAbout, itemHistory, itemFavorites, itemLanguage;
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
        itemLanguage = menu.findItem(R.id.changeLanguage);
        itemDark = menu.findItem(R.id.darkMode);
        itemAbout = menu.findItem(R.id.about);
        itemFavorites = menu.findItem(R.id.favorites);
        itemHistory = menu.findItem(R.id.history);

        if(prefs.loadNightModeState()) {
            itemDark.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_light));

            if (prefs.loadLanguageState().equals("en")) {
                itemDark.setTitle("Light Mode");
            } else {
                itemDark.setTitle("Malinaw na tema");
            }
            ColorStateList iconTint = ContextCompat.getColorStateList(this, R.color.white);
            itemLanguage.setIconTintList(iconTint);
            itemDark.setIconTintList(iconTint);
            itemAbout.setIconTintList(iconTint);
            itemFavorites.setIconTintList(iconTint);
            itemHistory.setIconTintList(iconTint);
        }
        return true;
    }
    @SuppressLint("NonConstantResourceId")
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection

        switch (item.getItemId()) {
            case R.id.darkMode:
                prefs.setNightModeState(!prefs.loadNightModeState());
                restartApp();
                return true;
            case R.id.changeLanguage:
                if (item.getTitle().toString().equals("Paltan sa Wikang Ingles")){
                    ChangeLanguage("en");
                    prefs.setLanguageLaunch("en");
                } else {
                    ChangeLanguage("fil");
                    prefs.setLanguageLaunch("fil");
                }
                restartApp();
                return true;
            case R.id.favorites:
            case R.id.history:
                Toast.makeText(this, item.getTitle(), Toast.LENGTH_SHORT).show();
                return true;
            case R.id.about:
                showPopupAbout(item.getActionView());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void ChangeLanguage(String Lang){
        Locale locale = new Locale(Lang);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        if (Lang.equals("en")){
            config.locale = locale;
            Objects.requireNonNull(getSupportActionBar()).setTitle("English - Filipino Translator");
        } else {
            config.locale = Locale.getDefault();
            Objects.requireNonNull(getSupportActionBar()).setTitle("Filipino - Ingles Tagasalin");
        }
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
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
            if (prefs.loadLanguageState().equals("en")) {
                speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.ENGLISH);
                speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to text");
            } else {
                speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "fil_PH");
                speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Magsalita sa Filipino");
            }

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

                if (prefs.loadLanguageState().equals("en")) {
                    translate_english(Objects.requireNonNull(result).get(0));
                } else {
                    translate_tagalog(Objects.requireNonNull(result).get(0));
                }
            }
        }
    }

    private void checkPermission() {
        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.RECORD_AUDIO},RecordAudioRequestCode);
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
    public void showPopupAbout(final View view) {
        LayoutInflater inflater = (LayoutInflater) view.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_about, null);

        //Specify the length and width through constants
        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;

        //Create a window with our parameters
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, true);

        //Set the location of the window on the screen
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setFocusable(true);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());

        TextView tvVersion, tvTitle, tvDescription;
        tvVersion = popupView.findViewById(R.id.tv_Version);
        tvTitle = popupView.findViewById(R.id.tvAbout);
        tvDescription = popupView.findViewById(R.id.textViewDescription);
        com.google.android.material.floatingactionbutton.FloatingActionButton btnShare, btnLink, btnDev;
        btnDev = popupView.findViewById(R.id.btn_Dev);
        btnLink = popupView.findViewById(R.id.btn_Link);
        btnShare = popupView.findViewById(R.id.btn_Share);
        String version = "Version: "+BuildConfig.VERSION_NAME;
        tvVersion.setText(version);

        if (prefs.loadNightModeState()) {
            tvTitle.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.white_smoke)));
            tvDescription.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.white_smoke)));
            btnDev.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.dark_blue)));
            btnDev.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.white_smoke)));
            btnLink.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.dark_blue)));
            btnLink.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.white_smoke)));
            btnShare.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.dark_blue)));
            btnShare.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.white_smoke)));
        }

        btnDev.setOnClickListener(view1 -> {
            Intent browser = new Intent(Intent.ACTION_VIEW, Uri.parse("https://albertzz.vercel.app/"));
            startActivity(browser);
        });
        btnLink.setOnClickListener(view1 -> {
            Intent browser = new Intent(Intent.ACTION_VIEW, Uri.parse("https://filengtranslator.carrd.co/"));
            startActivity(browser);
        });
        btnShare.setOnClickListener(view1 -> shareApplication());
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

    private void shareApplication() {
        ApplicationInfo app = getApplicationContext().getApplicationInfo();
        String filePath = app.sourceDir;

        Intent intent = new Intent(Intent.ACTION_SEND);

        // MIME of .apk is "application/vnd.android.package-archive".
        // but Bluetooth does not accept this. Let's use "*/*" instead.
        intent.setType("*/*");

        // Append file and send Intent
        File originalApk = new File(filePath);

        try {
            //Make new directory in new location
            File tempFile = new File(getExternalCacheDir() + "/ExtractedApk");
            //If directory doesn't exists create new
            if (!tempFile.isDirectory())
                if (!tempFile.mkdirs())
                    return;
            //Get application's name and convert to lowercase
            tempFile = new File(tempFile.getPath() + "/" + getString(app.labelRes).replace(" ", "").toLowerCase() + "_v" + BuildConfig.VERSION_NAME + ".apk");
            //If file doesn't exists create new
            if (!tempFile.exists()) {
                if (!tempFile.createNewFile()) {
                    return;
                }
            }
            //Copy file to new location
            InputStream in = new FileInputStream(originalApk);
            OutputStream out = new FileOutputStream(tempFile);

            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
            System.out.println("File copied.");
            Uri photoURI = FileProvider.getUriForFile(Objects.requireNonNull(getApplicationContext()),
                    BuildConfig.APPLICATION_ID + ".provider", tempFile);
            intent.putExtra(Intent.EXTRA_STREAM, photoURI);
            startActivity(Intent.createChooser(intent, "Share app via"));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static long back_pressed;
    @Override
    public void onBackPressed() {
        if (back_pressed + 2000 > System.currentTimeMillis()){
            overridePendingTransition(0, 0);
            finishAffinity();
            System.exit(0);
            super.onBackPressed();
        }
        else
        if (prefs.loadLanguageState().equals("en")) {
            Toast.makeText(getBaseContext(), "Press once again to exit!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getBaseContext(), "Pindutin muli para lumabas!", Toast.LENGTH_SHORT).show();
        }

        back_pressed = System.currentTimeMillis();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (loadingDataActivity !=null){
            loadingDataActivity.DismissDialog();
        }
        Runtime.getRuntime().gc();
    }
}