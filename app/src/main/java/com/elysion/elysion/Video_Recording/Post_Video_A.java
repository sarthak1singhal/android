package com.elysion.elysion.Video_Recording;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.elysion.elysion.Main_Menu.MainMenuActivity;
import com.elysion.elysion.R;
import com.elysion.elysion.Services.ServiceCallback;
import com.elysion.elysion.Services.UploadWorkRequest;
import com.elysion.elysion.SimpleClasses.Functions;
import com.elysion.elysion.SimpleClasses.Variables;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Post_Video_A extends AppCompatActivity implements ServiceCallback, View.OnClickListener {


    ImageView video_thumbnail;
    String video_path;
    ProgressDialog progressDialog;
    ServiceCallback serviceCallback;
    EditText description_edit;
    Map<String, String> language
            = new HashMap<String, String>();

    String draft_file;

    Spinner categorySpinner, languageSpinner;
    ArrayAdapter<String> categorySpinnerAdapter, languageSpinnerAdapter;
    List<String> categoryList = new ArrayList<>();
    List<String> languageList = new ArrayList<>();

    String selectedLanguage = "";
    String selectedCategory = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_video);

        Intent intent = getIntent();
        if (intent != null) {
            draft_file = intent.getStringExtra("draft_file");
        }


        video_path = Variables.output_filter_file;
        video_thumbnail = findViewById(R.id.video_thumbnail);


        description_edit = findViewById(R.id.description_edit);

        // this will get the thumbnail of video and show them in imageview
        Bitmap bmThumbnail;
        bmThumbnail = ThumbnailUtils.createVideoThumbnail(video_path,
                MediaStore.Video.Thumbnails.FULL_SCREEN_KIND);

        if (bmThumbnail != null) {
            video_thumbnail.setImageBitmap(bmThumbnail);
        } else {
        }


        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait");
        progressDialog.setCancelable(false);


        findViewById(R.id.Goback).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });


        findViewById(R.id.post_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                //progressDialog.show();
                if (selectedCategory.isEmpty()) {
                    Toast.makeText(Post_Video_A.this, "Please select category", Toast.LENGTH_SHORT).show();
                    return;
                } else if (selectedLanguage.isEmpty()) {
                    Toast.makeText(Post_Video_A.this, "Please select content language", Toast.LENGTH_SHORT).show();
                    return;
                }
                Start_Service(v);

            }
        });

        categorySpinner = findViewById(R.id.categorySpinner);
        languageSpinner = findViewById(R.id.languageSpinner);

        findViewById(R.id.save_draft_btn);
        findViewById(R.id.save_draft_btn).setOnClickListener(this);

        languageList.add("English");
        languageList.add("हिन्दी");
        languageList.add("தமிழ்");
        languageList.add("తెలుగు");
        languageList.add("मराठी");
        languageList.add("ગુજરાતી");
        languageList.add("ಕನ್ನಡ");
        languageList.add("বাংলা");


        language.put("English","english");
        language.put("हिन्दी","hindi");
        language.put("தமிழ்","tamil");
        language.put("తెలుగు","telugu");
        language.put("मराठी","marathi");
        language.put("ગુજરાતી","gujarati");
        language.put("ಕನ್ನಡ","kannada");
        language.put("বাংলা","bengali");


        categoryList.add("Comedy");
        categoryList.add("Travel");
        categoryList.add("Food");
        categoryList.add("Fashion");
        categoryList.add("TV Show and Movies");
        categoryList.add("Personal Care");
        categoryList.add("Tech");
        categoryList.add("Recipes");
        categoryList.add("Beauty");
        categoryList.add("Entertainment");
        categoryList.add("Other");


        categorySpinnerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categoryList);
        categorySpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categorySpinnerAdapter);

        languageSpinnerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, languageList);
        languageSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        languageSpinner.setAdapter(languageSpinnerAdapter);

        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCategory = categoryList.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        languageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedLanguage = language.get(languageList.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.save_draft_btn:
                Save_file_in_draft();
                break;
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
    }

    // this will start the service for uploading the video into database
    public void Start_Service(View v) {
        v.setEnabled(false);

        serviceCallback = this;

        Data data = new Data.Builder()
                .putString("uri", "" + Uri.fromFile(new File(video_path)))
                .putString("desc", "" + description_edit.getText().toString())
                .putString("content_language", "" + selectedLanguage)
                .putString("category", "" + selectedCategory)
                .build();
        OneTimeWorkRequest oneTimeWorkRequest = new OneTimeWorkRequest.Builder(UploadWorkRequest.class)
                .setInputData(data).build();

        WorkManager.getInstance(this).enqueue(oneTimeWorkRequest);
        startActivity(new Intent(Post_Video_A.this, MainMenuActivity.class));

    }



    // when the video is uploading successfully it will restart the appliaction
    @Override
    public void ShowResponce(final String responce) {


        if (responce.equalsIgnoreCase("Your Video is uploaded Successfully")) {

            Delete_draft_file();

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(Post_Video_A.this, responce, Toast.LENGTH_LONG).show();
                    progressDialog.dismiss();

                    startActivity(new Intent(Post_Video_A.this, MainMenuActivity.class));

                }
            }, 1000);


        } else {
            Toast.makeText(Post_Video_A.this, responce, Toast.LENGTH_LONG).show();
            progressDialog.dismiss();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        /*try {
            if (mConnection != null)

                unbindService(mConnection);
        } catch (Exception e) {

        }*/

    }




    public void Save_file_in_draft() {
        File source = new File(video_path);
        File destination = new File(Variables.draft_app_folder + Functions.getRandomString() + ".mp4");
        try {
            if (source.exists()) {

                InputStream in = new FileInputStream(source);
                OutputStream out = new FileOutputStream(destination);

                byte[] buf = new byte[1024];
                int len;

                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }

                in.close();
                out.close();

                Toast.makeText(Post_Video_A.this, "File saved in Draft", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(Post_Video_A.this, MainMenuActivity.class));

            } else {
                Toast.makeText(Post_Video_A.this, "File failed to saved in Draft", Toast.LENGTH_SHORT).show();

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void Delete_draft_file() {
        try {
            if (draft_file != null) {
                File file = new File(draft_file);
                file.delete();
            }
        } catch (Exception e) {

        }


    }

}
