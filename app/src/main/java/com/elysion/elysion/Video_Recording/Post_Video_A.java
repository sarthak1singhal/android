package com.elysion.elysion.Video_Recording;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.elysion.elysion.Main_Menu.MainMenuActivity;
import com.elysion.elysion.R;
import com.elysion.elysion.Services.ServiceCallback;
import com.elysion.elysion.Services.Upload_Service;
import com.elysion.elysion.SimpleClasses.Functions;
import com.elysion.elysion.SimpleClasses.Variables;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class Post_Video_A extends AppCompatActivity implements ServiceCallback, View.OnClickListener {


    ImageView video_thumbnail;
    String video_path;
    ProgressDialog progressDialog;
    ServiceCallback serviceCallback;
    EditText description_edit;

    String draft_file;

    Spinner categorySpinner, languageSpinner;
    ArrayAdapter<String> categorySpinnerAdapter, languageSpinnerAdapter;
    List<String> categoryList = new ArrayList<>();
    List<String> languageList = new ArrayList<>();

    String selectedLanguage = "";
    String selectedCategory = "";
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {

            Upload_Service.LocalBinder binder = (Upload_Service.LocalBinder) service;
            mService = binder.getService();

            mService.setCallbacks(Post_Video_A.this);


        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {

        }
    };

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
                    Toast.makeText(Post_Video_A.this, "Please select language", Toast.LENGTH_SHORT).show();
                    return;
                }
                Start_Service();

            }
        });

        categorySpinner = findViewById(R.id.categorySpinner);
        languageSpinner = findViewById(R.id.languageSpinner);

        findViewById(R.id.save_draft_btn);
        findViewById(R.id.save_draft_btn).setOnClickListener(this);

        languageList.add("English");
        languageList.add("Hindi");
        languageList.add("Tamil");
        languageList.add("Telugu");
        languageList.add("Marathi");
        languageList.add("Gujarati");
        languageList.add("Kannada");
        languageList.add("Bengali");

        categoryList.add("Comedy");
        categoryList.add("Travel");
        categoryList.add("Food");
        categoryList.add("Fashion");
        categoryList.add("Movies");
        categoryList.add("TV");
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
                selectedLanguage = languageList.get(position);
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
        Stop_Service();

    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
    }

    // this will start the service for uploading the video into database
    public void Start_Service() {

        serviceCallback = this;

        Upload_Service mService = new Upload_Service(serviceCallback);
        if (!Functions.isMyServiceRunning(this, mService.getClass())) {
            Toast.makeText(this, "Video Upload Started", Toast.LENGTH_SHORT).show();

            Intent mServiceIntent = new Intent(this.getApplicationContext(), mService.getClass());
            mServiceIntent.setAction("startservice");
            mServiceIntent.putExtra("uri", "" + Uri.fromFile(new File(video_path)));
            mServiceIntent.putExtra("desc", "" + description_edit.getText().toString());
            mServiceIntent.putExtra("content_language", "" + selectedLanguage);
            mServiceIntent.putExtra("category", "" + selectedCategory);
            startService(mServiceIntent);


            Intent intent = new Intent(this, Upload_Service.class);
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
            startActivity(new Intent(Post_Video_A.this, MainMenuActivity.class));

        } else {
            Toast.makeText(this, "Please wait video already in uploading progress", Toast.LENGTH_LONG).show();
        }


    }


    // this is importance for binding the service to the activity
    Upload_Service mService;

    // when the video is uploading successfully it will restart the appliaction
    @Override
    public void ShowResponce(final String responce) {

        try {
            if (mConnection != null)
                unbindService(mConnection);
        } catch (Exception e) {

        }


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

        try {
            if (mConnection != null)

                unbindService(mConnection);
        } catch (Exception e) {

        }

    }


    // this function will stop the the ruuning service
    public void Stop_Service() {

        serviceCallback = this;

        Upload_Service mService = new Upload_Service(serviceCallback);

        if (Functions.isMyServiceRunning(this, mService.getClass())) {
            Intent mServiceIntent = new Intent(this.getApplicationContext(), mService.getClass());
            mServiceIntent.setAction("stopservice");
            startService(mServiceIntent);

        }


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
