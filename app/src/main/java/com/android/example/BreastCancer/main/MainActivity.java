package com.android.example.BreastCancer.main;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.DisplayMetrics;
import android.widget.EditText;
import android.widget.Toast;
import android.net.Uri;
import android.view.View;

import com.android.example.BreastCancer.models.Result;
import com.android.example.BreastCancer.R;
import com.android.example.BreastCancer.ml.Classifier;
import com.android.example.BreastCancer.ml.ModelConfig;
import com.android.example.BreastCancer.utils.ImageUtils;
import com.google.android.material.button.MaterialButton;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;

public class MainActivity extends AppCompatActivity {

    private AppCompatImageView myChosenImageView;
    private AppCompatTextView mResultTextView;
    private MaterialButton mChooseButton, mClassifyButton,
                            retrieveEntryButton, mySubmitButton;
    private Uri imageUri;
    private Classifier mClassifier;
    private EditText yourName, yourAge, phoneNumber;
    private String saveId;
    private static int RESULT_LOAD_IMAGE = 1, flag = 0;

    public static Intent getIntent(Context packageContext) {
        return new Intent(packageContext, MainActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        loadClassifier();   //Here I need to put my model
        wireUpWidgets();       //initialise variables for button
        setListeners();  //on click choose and set button

    }

    private void loadClassifier() {
        try {
            mClassifier = Classifier.createClassifier(getAssets(), ModelConfig.MODEL_FILENAME);
        } catch (IOException e) {
            Toast.makeText(this, "Couldn't load model", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void setListeners() {
        mChooseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                Intent i = new Intent(
                        Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI); //might want to use internal


                startActivityForResult(i, RESULT_LOAD_IMAGE);
            }
        });


        mClassifyButton.setOnClickListener(v -> detectImage());
        mySubmitButton.setOnClickListener((v -> submitEntry()));
        retrieveEntryButton.setOnClickListener((v -> retrieveEntry()));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            imageUri = data.getData();
            myChosenImageView.setImageURI(imageUri);

        }


    }

    private void detectImage() {
        Bitmap bitmap = ((BitmapDrawable) myChosenImageView.getDrawable()).getBitmap();
        bitmap = ThumbnailUtils.extractThumbnail(bitmap, getScreenWidth(), getScreenWidth());
        bitmap = ImageUtils.prepareImageForClassification(bitmap);

        List<Result> recognitions = mClassifier.recognizeImage(bitmap);
        Result result = recognitions.get(0);
        String confidence = String.format(Locale.getDefault(), "%.2f %%", result.mConfidence * 100);
        Cursor returnCursor =
                getContentResolver().query(imageUri, null, null, null, null);
        /*
         * Get the column indexes of the data in the Cursor,
         * move to the first row in the Cursor, get the data,
         * and display it.
         */
        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
        returnCursor.moveToFirst();
        String actualLabel = returnCursor.getString(nameIndex);   // actualLabel string contains the name of file selected.
        if (result.mConfidence*100 > 85) { // If confidence is lower than 85%, I assume it's not a cell! (Deadline is near :P)
            mResultTextView.setText(getString(R.string.result_string, result.mTitle, confidence));
            flag = 1;
        }
        else{
            mResultTextView.setText("Please enter image of a cell!");
        }
    }

    private void submitEntry() {
        if(flag == 0){
            Toast.makeText(this, "First Classify an image!", Toast.LENGTH_SHORT).show();
            return;
        }
        if((yourAge.getText() == null) || (yourName.getText() == null) || (phoneNumber.getText() == null)) {
            Toast.makeText(this, "Please fill all the details!!", Toast.LENGTH_SHORT).show();
            return;
        }
        if(flag == 1){
            saveId = phoneNumber.getText().toString(); //to retrieve/save Result
            String saveId2 = saveId + "0";  //to retrieve/save name
            String saveId3 = saveId + "1"; //to retrieve/save Age


            SharedPreferences user_details = getApplicationContext().getSharedPreferences("user_details", 0);
            SharedPreferences.Editor editor = user_details.edit();
            editor.putString(saveId, mResultTextView.getText().toString());
            editor.putString(saveId2, yourName.getText().toString());
            editor.putString(saveId3, yourAge.getText().toString());

            // Apply the edits!
            editor.apply();
            Toast.makeText(this, "Successful!!", Toast.LENGTH_SHORT).show();

        }


    }

    private void retrieveEntry() {
            saveId = phoneNumber.getText().toString();
            String saveId2 = saveId + "0";
            String saveId3 = saveId + "1";

            SharedPreferences user_details = getApplicationContext().getSharedPreferences("user_details", 0);
            String def = "Not found!";
            String aResult = user_details.getString(saveId, def);
            String userName = user_details.getString(saveId2, def);
            String userAge =  user_details.getString(saveId3, def);
            mResultTextView.setText(aResult);
            yourName.setText(userName);
            yourAge.setText(userAge);

    }

    private void wireUpWidgets() {
        myChosenImageView = findViewById(R.id.plant_image_view); //name changed!
        mResultTextView = findViewById(R.id.result_text_view);
        mChooseButton = findViewById(R.id.random_choose_button);
        mClassifyButton = findViewById(R.id.classifiy_button);


        //Add-ons!!
        mySubmitButton = findViewById(R.id.create_button);
        yourName = findViewById(R.id.editText);
        yourAge = findViewById(R.id.editText2);
        phoneNumber = findViewById(R.id.editText3);
        retrieveEntryButton = findViewById(R.id.create_button2);
    }

    private int getScreenWidth() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.widthPixels;
    }
}
