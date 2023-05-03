package com.uno.recon;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.IOException;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    ImageView imageview1;
    Button add2;
    ImageView add;
    String uriString = "";
    boolean doubleBackToExitPressedOnce = false;
    private TextToSpeech textToSpeech;
    boolean isSpeaking = false;
    private BottomSheetDialog bottomSheetDialog;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getSupportActionBar().hide();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomSheetDialog = new BottomSheetDialog(MainActivity.this, R.style.bottom_theme);
        //bottomSheetDialog.setContentView(R.layout.popup);
        //bottomSheetDialog = new BottomSheetDialog(MainActivity.this);
        bottomSheetDialog.setContentView(R.layout.popup);



        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = textToSpeech.setLanguage(Locale.getDefault());
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        // Language not supported
                    }
                } else {
                    // TextToSpeech initialization failed
                }
            }
        });

        Button button2 = findViewById(R.id.button2);
        imageview1 = findViewById(R.id.imageView);
        add = findViewById(R.id.add);
        add2 = findViewById(R.id.add2);
        add2.setVisibility(View.INVISIBLE);

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGetContent.launch("image/*");
                imageview1.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        add2.setVisibility(View.VISIBLE);
                        add.setVisibility(View.INVISIBLE);
                    }
                }, 1500);
            }
        });


        add2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGetContent.launch("image/*");

            }
        });


        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mExtractTextFromUri(MainActivity.this, Uri.parse(uriString));
            }
        });

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGetContent.launch("image/*");
                imageview1.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        add2.setVisibility(View.VISIBLE);
                        add.setVisibility(View.INVISIBLE);
                    }
                }, 1500);
            }
        });


        add2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGetContent.launch("image/*");

            }
        });


        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mExtractTextFromUri(MainActivity.this, Uri.parse(uriString));
            }
        });;

        TextView resultText = new TextView(this);
    }

    ActivityResultLauncher<String> mGetContent = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri uri) {
                    imageview1.setImageURI(uri);
                    uriString = uri.toString();
                }
            });

    public void mExtractTextFromUri(Context context, Uri _uri) {
        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        try {
            InputImage image = InputImage.fromFilePath(context, _uri);
            Task<Text> result =
                    recognizer.process(image)
                            .addOnSuccessListener(new OnSuccessListener<Text>() {
                                @Override
                                public void onSuccess(Text visionText) {
                                    // Task completed successfully
                                    showBottomSheetDialog(visionText.getText());
                                }
                            })
                            .addOnFailureListener(
                                    new OnFailureListener() {
                                        @Override
                                        public void onFailure(Exception e) {
                                            // Task failed with an exception
                                        }
                                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void showBottomSheetDialog(String text) {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this, R.style.bottom_theme);
        View bottomSheetView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.popup, findViewById(R.id.bottom_sheet_layout));
        bottomSheetDialog.setContentView(bottomSheetView);


        // set up text and buttons inside the bottom sheet
        TextView popupText = bottomSheetView.findViewById(R.id.popup_text);
        popupText.setText(text);

        Button copyButton = bottomSheetView.findViewById(R.id.copy_button);
        copyButton.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Extracted Text", text);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "Text copied to clipboard", Toast.LENGTH_SHORT).show();
        });

        Button speakButton = bottomSheetView.findViewById(R.id.speak_button);
        speakButton.setOnClickListener(v -> {
            if (!isSpeaking) {
                String[] sentences = text.split("\\. ");
                for (String sentence : sentences) {
                    textToSpeech.speak(sentence, TextToSpeech.QUEUE_ADD, null, null);
                    textToSpeech.playSilence(500, TextToSpeech.QUEUE_ADD, null);
                }
                isSpeaking = true;
            } else {
                textToSpeech.stop();
                isSpeaking = false;
            }
        });
        bottomSheetDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                isSpeaking = false;
                textToSpeech.stop();
            }
        });
        bottomSheetView.findViewById(R.id.popup_ok_button).setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
        });

        // make the bottom sheet draggable and expandable
        View bottomSheet = bottomSheetDialog.findViewById(R.id.bottom_sheet_layout);
        BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);

        // set the height of the bottom sheet in the collapsed state
        int peekHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 600, getResources().getDisplayMetrics());
        bottomSheetBehavior.setPeekHeight(peekHeight);

        // expand the bottom sheet when it is dragged to the top
        BottomSheetBehavior.BottomSheetCallback bottomSheetCallback = new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                // no-op
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                // calculate the new height based on the slide offset
                int newHeight = (int) (slideOffset * (getResources().getDisplayMetrics().heightPixels - peekHeight)) + peekHeight;

                // set the new height of the bottom sheet
                ViewGroup.LayoutParams layoutParams = bottomSheet.getLayoutParams();
                layoutParams.height = newHeight;
                bottomSheet.setLayoutParams(layoutParams);
            }
        };

        bottomSheetDialog.show();
    }

    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }
    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Swipe back again to exit", Toast.LENGTH_SHORT).show();

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }



}

