package com.elysion.elysion.Services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.Volley;
import com.elysion.elysion.R;
import com.elysion.elysion.SimpleClasses.Variables;
import com.elysion.elysion.Video_Recording.AnimatedGifEncoder;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static android.content.Context.MODE_PRIVATE;

public class UploadWorkRequest extends Worker {

    String video_base64 = "", thumb_base_64 = "", Gif_base_64 = "";
    String description;
    String content_language, category;
    SharedPreferences sharedPreferences;
    Context context;
    NotificationManager notificationManager;

    public UploadWorkRequest(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context;
        sharedPreferences = context.getSharedPreferences(Variables.pref_name, MODE_PRIVATE);

    }

    private static byte[] loadFile(File file) throws IOException {
        InputStream is = new FileInputStream(file);

        long length = file.length();
        if (length > Integer.MAX_VALUE) {
            // File is too large
        }
        byte[] bytes = new byte[(int) length];

        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length
                && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
            offset += numRead;
        }

        if (offset < bytes.length) {
            throw new IOException("Could not completely read file " + file.getName());
        }

        is.close();
        return bytes;
    }

    @NonNull
    @Override
    public Result doWork() {

        showNotification();

        String videoUri = getInputData().getString("uri");
        Uri uri = Uri.parse(videoUri);
        String description = getInputData().getString("desc");
        String category = getInputData().getString("category");
        String content_language = getInputData().getString("content_language");

        Bitmap bmThumbnail = ThumbnailUtils.createVideoThumbnail(uri.getPath(),
                MediaStore.Video.Thumbnails.FULL_SCREEN_KIND);

        Bitmap bmThumbnail_resized = Bitmap.createScaledBitmap(bmThumbnail, (int) (bmThumbnail.getWidth() * 0.4), (int) (bmThumbnail.getHeight() * 0.4), true);

        thumb_base_64 = Bitmap_to_base64(bmThumbnail_resized);

        try {

            video_base64 = encodeFileToBase64Binary(uri);

        } catch (IOException e) {
            e.printStackTrace();
        }

        /****************************/

        File myVideo = new File(uri.getPath());
        Uri myVideoUri = Uri.parse(myVideo.toString());

        final MediaMetadataRetriever mmRetriever = new MediaMetadataRetriever();
        mmRetriever.setDataSource(myVideo.getAbsolutePath());

        final MediaPlayer mp = MediaPlayer.create(getApplicationContext(), myVideoUri);

        final ArrayList<Bitmap> frames = new ArrayList<Bitmap>();


        for (int i = 1000000; i < 2000 * 1000; i += 100000) {
            Bitmap bitmap = mmRetriever.getFrameAtTime(i, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
            Bitmap resized = Bitmap.createScaledBitmap(bitmap, (int) (bitmap.getWidth() * 0.4), (int) (bitmap.getHeight() * 0.4), true);
            frames.add(resized);
        }

        Gif_base_64 = Base64.encodeToString(generateGIF(frames), Base64.DEFAULT);


        JSONObject parameters = new JSONObject();

        try {
            parameters.put("fb_id", sharedPreferences.getString(Variables.u_id, ""));
            if(Variables.Selected_sound_id == null)
                parameters.put("sound_id", "0");
            else if(Variables.Selected_sound_id.equals("null"))
                parameters.put("sound_id", "0");
            else
                parameters.put("sound_id", Variables.Selected_sound_id);

            parameters.put("description", description);
            parameters.put("content_language", content_language);
            parameters.put("category", category);

            Log.d("UploadService", content_language);
            Log.d("UploadService", category);

            JSONObject vidoefiledata = new JSONObject();
            vidoefiledata.put("file_data", video_base64);
            parameters.put("videobase64", vidoefiledata);



            JSONObject imagefiledata = new JSONObject();
            imagefiledata.put("file_data", thumb_base_64);
            parameters.put("picbase64", imagefiledata);


            JSONObject giffiledata = new JSONObject();
            giffiledata.put("file_data", Gif_base_64);
            parameters.put("gifbase64", giffiledata);


        } catch (JSONException e) {
            e.printStackTrace();
        }


        generateNoteOnSD("parameters", parameters.toString());

        RequestFuture<JSONObject> future = RequestFuture.newFuture();
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        //JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, Variables.uploadVideo, parameters, future, future);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST, Variables.uploadVideo, parameters, future, future) {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("fb-id", sharedPreferences.getString(Variables.u_id, "0"));
                headers.put("version", getApplicationContext().getResources().getString(R.string.version));
                headers.put("device", getApplicationContext().getResources().getString(R.string.device));
                headers.put("tokon", sharedPreferences.getString(Variables.api_token, ""));
                headers.put("deviceid", sharedPreferences.getString(Variables.device_id, ""));
                Log.d(Variables.tag, headers.toString());
                return headers;
            }
        };

        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(120000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.getCache().clear();
        requestQueue.add(jsonObjectRequest);

        try {
            JSONObject response = future.get(); // this will block
            int k= 0;
        } catch (InterruptedException e) {
            // exception handling
            e.printStackTrace();
        } catch (ExecutionException e) {
            // exception handling
            e.printStackTrace();
        } finally {

            notificationManager.cancel(101);

        }


        return Result.success();
    }

    public String Bitmap_to_base64(Bitmap imagebitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        imagebitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos);
        byte[] byteArray = baos.toByteArray();
        String base64 = Base64.encodeToString(byteArray, Base64.DEFAULT);
        return base64;
    }

    private String encodeFileToBase64Binary(Uri fileName)
            throws IOException {

        File file = new File(fileName.getPath());
        byte[] bytes = loadFile(file);
        String encodedString = Base64.encodeToString(bytes, Base64.DEFAULT);
        return encodedString;
    }

    //for video gif image
    public byte[] generateGIF(ArrayList<Bitmap> bitmaps) {

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        AnimatedGifEncoder encoder = new AnimatedGifEncoder();
        encoder.start(bos);
        for (Bitmap bitmap : bitmaps) {

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 10, out);
            Bitmap decoded = BitmapFactory.decodeStream(new ByteArrayInputStream(out.toByteArray()));

            encoder.addFrame(decoded);

        }

        encoder.finish();


        File filePath = new File(Variables.app_folder, "sample.gif");
        FileOutputStream outputStream;
        try {
            outputStream = new FileOutputStream(filePath);
            outputStream.write(bos.toByteArray());
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        }

        return bos.toByteArray();
    }


    public void generateNoteOnSD(String sFileName, String sBody) {
        try {
            File root = new File(Environment.getExternalStorageDirectory(), "Notes");
            if (!root.exists()) {
                root.mkdirs();
            }
            File gpxfile = new File(root, sFileName + ".txt");
            FileWriter writer = new FileWriter(gpxfile);
            writer.append(sBody);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // this will show the sticky notification during uploading video
    private void showNotification() {


        final String CHANNEL_ID = "default";
        final String CHANNEL_NAME = "Default";

        notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel defaultChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(defaultChannel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.stat_sys_upload)
                .setContentTitle("Uploading Video")
                .setContentText("Please wait! Video is uploading....")
                .setOngoing(true)
                .setAutoCancel(false)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),
                        android.R.drawable.stat_sys_upload));
        Notification notification = builder.build();
        notificationManager.notify(101, notification);

    }

    private void stopNotification() {
        if (notificationManager != null) {

            notificationManager.cancel(101);
        }
    }
}
