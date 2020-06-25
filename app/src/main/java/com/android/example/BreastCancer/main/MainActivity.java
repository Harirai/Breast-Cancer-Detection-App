package com.android.example.BreastCancer.main;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.media.ThumbnailUtils;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.DisplayMetrics;
import android.widget.Button;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.app.ActivityCompat;

import com.itextpdf.text.Document;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;


public class MainActivity extends AppCompatActivity {

    private AppCompatImageView myChosenImageView;
    private AppCompatTextView mResultTextView;
    private MaterialButton mChooseButton, mClassifyButton,
            retrieveEntryButton, mySubmitButton;
    private Button generateReport;
    private Uri imageUri;
    private Classifier mClassifier;
    private EditText yourName, yourAge, phoneNumber, yourSex;
    private String saveId;
    private String saveId2 = saveId + "0", saveId3 = saveId + "1",
            saveId4 = saveId + "2", saveId5 = saveId + "3";
    private static int RESULT_LOAD_IMAGE = 1, flag = 0, retr = 0;

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
        generateReport.setOnClickListener((v -> generatePDF()));
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
        if (myChosenImageView.getDrawable() == null) {
            Toast.makeText(this, "Please select an Image before classifying!", Toast.LENGTH_SHORT).show();
            return;
        }
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
        if (result.mConfidence * 100 > 0) { // If confidence is lower than 85%, I assume it's not a cell!
            mResultTextView.setText(getString(R.string.result_string, result.mTitle, confidence));
            flag = 1;
        } else {
            mResultTextView.setText("Please enter image of a cell!");
        }
    }

    private void submitEntry() {
        if (flag == 0) {
            Toast.makeText(this, "First Classify an image!", Toast.LENGTH_SHORT).show();
            return;
        }
        if ((yourAge.getText().toString().isEmpty()) || (yourName.getText().toString().isEmpty()) || (phoneNumber.getText().toString().isEmpty())) {
            Toast.makeText(this, "Please fill all the details!!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (flag == 1) {
            saveId = phoneNumber.getText().toString(); //to retrieve/save Result

            saveId2 = saveId + "0";
            saveId3 = saveId + "1";
            saveId4 = saveId + "2";
            saveId5 = saveId + "3";

            SharedPreferences user_details = getApplicationContext().getSharedPreferences("user_details", 0);
            SharedPreferences.Editor editor = user_details.edit();
            editor.putString(saveId, mResultTextView.getText().toString());
            editor.putString(saveId2, yourName.getText().toString());
            editor.putString(saveId3, yourAge.getText().toString());
            editor.putString(saveId4, yourSex.getText().toString());
            editor.putString(saveId5, imageUri.toString());
            // Apply the edits!
            editor.apply();
            Toast.makeText(this, "Successful!!", Toast.LENGTH_SHORT).show();

        }


    }

    private void retrieveEntry() {
        saveId = phoneNumber.getText().toString();
        if (saveId.isEmpty()) {
            Toast.makeText(this, "Please Enter a valid number!", Toast.LENGTH_SHORT).show();
            return;
        }
        saveId2 = saveId + "0";
        saveId3 = saveId + "1";
        saveId4 = saveId + "2";
        saveId5 = saveId + "3";

        SharedPreferences user_details = getApplicationContext().getSharedPreferences("user_details", 0);
        String def = "Not found!";
        String aResult = user_details.getString(saveId, def);
        String userName = user_details.getString(saveId2, def);
        String userAge = user_details.getString(saveId3, def);
        String userSex = user_details.getString(saveId4, def);
        String userImage = user_details.getString(saveId5, def);

        if (aResult == def || userName == def || userAge == def || userSex == def) {
            Toast.makeText(this, "No record found! :(", Toast.LENGTH_SHORT).show();
            return;
        }
        mResultTextView.setText(aResult);
        yourName.setText(userName);
        yourAge.setText(userAge);
        yourSex.setText(userSex);
        imageUri = Uri.parse(userImage);
        myChosenImageView.setImageURI(imageUri);
        retr = 1;
    }

    public boolean haveStoragePermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
//                Log.e("Permission error","You have permission");
                return true;
            } else {

//                Log.e("Permission error","You have asked for permission");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else { //you dont need to worry about these stuff below api level 23
//            Log.e("Permission error","You already have the permission");
            return true;
        }

    }

    private String getRealPathFromURI(Uri contentURI) {
        String result;
        Cursor cursor = getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) { // Source is Dropbox or other similar local file path
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }

    private void generatePDF() {
        if (retr == 0) {
            Toast.makeText(this, "First retrieve an entry!", Toast.LENGTH_SHORT).show();
            return;
        }

        //create object of Document class
        if (!haveStoragePermission()) {
            Toast.makeText(this, "Permission Denied!", Toast.LENGTH_SHORT).show();
            return;
        }
        Document mDoc = new Document();
        //pdf file name
        String mFileName = new SimpleDateFormat("yyyyMMdd",
                Locale.getDefault()).format(System.currentTimeMillis());
        //pdf file path
        String dateText = "Date of Report : " + new SimpleDateFormat("yyyy/MM/dd",
                Locale.getDefault()).format(System.currentTimeMillis());
        ;

        mFileName = phoneNumber.getText().toString() + "_" + mFileName;
        String mFilePath = Environment.getExternalStorageDirectory() + "/" + mFileName + ".pdf";

        try {
            //create instance of PdfWriter class
            PdfWriter.getInstance(mDoc, new FileOutputStream(mFilePath));
            //open the document for writing
            mDoc.open();
            // collect all the strings
            String phoneNumberText = "Mobile No.:  " + phoneNumber.getText().toString();
            String nameText = "Name:  " + yourName.getText().toString();
            String ageText = "Age:  " + yourAge.getText().toString();
            String sexText = "Sex:  " + yourSex.getText().toString();
            String resultText = "Result: " + mResultTextView.getText().toString();
            String patientIdText = "Patient ID : " + "BC" + phoneNumber.getText().toString();

            mDoc.addTitle("Breast Cancer Detection App - IIT BHU");
            //add author of the document (optional)

            mDoc.addAuthor("Harirai and Ayush");

            //add paragraphs to the document
            mDoc.add(new Paragraph("Breast Cancer Detection App - IIT BHU", FontFactory.getFont(FontFactory.TIMES_BOLD, 20, Font.BOLD)));

            mDoc.add(new Paragraph(dateText));
            mDoc.add(new Paragraph(patientIdText));
            mDoc.add(new Paragraph(nameText));
            mDoc.add(new Paragraph(ageText));
            mDoc.add(new Paragraph(sexText));
            mDoc.add(new Paragraph(phoneNumberText));
            mDoc.add(new Paragraph(resultText));
            String reqpath = getRealPathFromURI(imageUri);
            Image image1 = Image.getInstance(reqpath);
            image1.scaleToFit(224, 224);  // Scale image to fit properly!
            mDoc.add(image1);
            //close the document
            mDoc.close();
            //show message that file is saved, it will show file name and file path too
            Toast.makeText(this, mFileName + ".pdf\nis saved to\n" + mFilePath, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            //if any thing goes wrong causing exception, get and show exception message
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }


    }

    private void wireUpWidgets() {
        myChosenImageView = findViewById(R.id.pic_image_view); //name changed!
        mResultTextView = findViewById(R.id.result_text_view);
        mChooseButton = findViewById(R.id.random_choose_button);
        mClassifyButton = findViewById(R.id.classifiy_button);


        //Add-ons!!
        generateReport = findViewById(R.id.generateReport);
        yourSex = findViewById(R.id.editText4);
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
