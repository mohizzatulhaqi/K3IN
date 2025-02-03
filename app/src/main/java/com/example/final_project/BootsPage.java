package com.example.final_project;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import com.google.android.material.snackbar.Snackbar;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;


public class BootsPage extends AppCompatActivity {

    private ProcessCameraProvider cameraProvider;
    private ImageCapture imageCapture;
    private ImageView imageView;
    private File imageFile;
    String classValue;
    private static final int MY_PERMISSIONS_REQUEST_WRITE = 223;

    private static final String API_KEY = "xGN6YojLgOgSpQireQaH";
    private static final String MODEL_ENDPOINT = "occupational-health-and-safety/2";
    private static final String UPLOAD_URL = "https://detect.roboflow.com/" + MODEL_ENDPOINT + "?api_key=" + API_KEY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.boots_page);

        Button next = findViewById(R.id.btnLanjutkan);
        Button capture = findViewById(R.id.bCapturePhoto);
        Button retake = findViewById(R.id.retake);
        imageView = findViewById(R.id.capturedImageView);

        String helmResult = getIntent().getStringExtra("helm_result");
        String vestResult = getIntent().getStringExtra("vest_result");

        next.setOnClickListener(view -> {
            Intent intent = new Intent(BootsPage.this, ResultPage.class);
            intent.putExtra("helm_result", helmResult);
            intent.putExtra("vest_result", vestResult);
            intent.putExtra("boots_result", classValue);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        capture.setOnClickListener(view -> capturePhoto());

        retake.setOnClickListener(view -> retakePhoto());

        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                startCamera(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void capturePhoto() {
        if (imageCapture == null) return;

        try {
            imageFile = createImageFile();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        Uri uriSavedImage = FileProvider.getUriForFile(this, getPackageName() + ".provider", imageFile);

        ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(imageFile).build();
        imageCapture.takePicture(outputFileOptions, ContextCompat.getMainExecutor(this), new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                displayCapturedImage(uriSavedImage);

                Snackbar.make(findViewById(android.R.id.content), "Tunggu Sebentar.....", Snackbar.LENGTH_LONG).show();

                new Thread(() -> {
                    try {
                        String result = sendMultipartRequest(UPLOAD_URL, imageFile);
                        classValue = result;
                        runOnUiThread(() -> {
                            Log.d("BootsPage", "Hasil Prediksi: " + result);
                            Snackbar.make(findViewById(android.R.id.content), "Deteksi Selesai. Silahkan Lanjutkan", Snackbar.LENGTH_LONG).show();
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }).start();
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                exception.printStackTrace();
                Toast.makeText(BootsPage.this, "Gagal mengambil gambar", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    private void displayCapturedImage(Uri imageUri) {
        try {
            Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
            imageView.setImageBitmap(bitmap);
            imageView.setVisibility(View.VISIBLE);

            Button capture = findViewById(R.id.bCapturePhoto);
            capture.setVisibility(View.GONE);

            Button retake = findViewById(R.id.retake);
            retake.setVisibility(View.VISIBLE);

            androidx.camera.view.PreviewView previewView = findViewById(R.id.pvPreview);
            previewView.setVisibility(View.GONE);

            Button next = findViewById(R.id.btnLanjutkan);
            next.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startCamera(ProcessCameraProvider cameraProvider) {
        if (imageFile != null && imageFile.exists()) {
            boolean deleted = imageFile.delete();
            if (deleted) {
                Log.d("HelmPage", "Temporary image file deleted.");
            }
        }

        askWritePermission();

        CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
        Preview preview = new Preview.Builder().build();
        androidx.camera.view.PreviewView previewView = findViewById(R.id.pvPreview);
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        imageCapture = new ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build();

        try {
            cameraProvider.unbindAll();
            cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void retakePhoto() {
        startCamera(cameraProvider);

        imageView.setVisibility(View.GONE);

        Button capture = findViewById(R.id.bCapturePhoto);
        capture.setVisibility(View.VISIBLE);

        Button retake = findViewById(R.id.retake);
        retake.setVisibility(View.GONE);

        androidx.camera.view.PreviewView previewView = findViewById(R.id.pvPreview);
        previewView.setVisibility(View.VISIBLE);

        Button next = findViewById(R.id.btnLanjutkan);
        next.setVisibility(View.GONE);
    }

    private void askWritePermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            int cameraPermission = this.checkSelfPermission(Manifest.permission.CAMERA);
            int writePermission = this.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);

            if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_WRITE);
            }

            if (writePermission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_WRITE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSIONS_REQUEST_WRITE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Izin diberikan", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Izin diperlukan untuk menyimpan foto", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String sendMultipartRequest(String requestURL, File file) throws IOException {
        String boundary = UUID.randomUUID().toString();
        String LINE_FEED = "\r\n";
        HttpURLConnection connection = null;
        StringBuilder filteredResult = new StringBuilder();

        try {
            URL url = new URL(requestURL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setUseCaches(false);
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

            OutputStream outputStream = connection.getOutputStream();
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, "UTF-8"), true);

            writer.append("--").append(boundary).append(LINE_FEED);
            writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"")
                    .append(file.getName()).append("\"").append(LINE_FEED);
            writer.append("Content-Type: ").append("image/jpeg").append(LINE_FEED).append(LINE_FEED).flush();

            FileInputStream inputStream = new FileInputStream(file);
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.flush();
            inputStream.close();

            writer.append(LINE_FEED).flush();
            writer.append("--").append(boundary).append("--").append(LINE_FEED);
            writer.close();

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                reader.close();

                JSONObject jsonResponse = new JSONObject(result.toString());
                JSONArray predictions = jsonResponse.getJSONArray("predictions");

                try {
                    for (int i = 0; i < predictions.length(); i++) {
                        JSONObject prediction = predictions.getJSONObject(i);
                        String detectedClass = prediction.getString("class");
                        double confidence = prediction.getDouble("confidence");

                        if ("sepatu".equals(detectedClass)) {
                            filteredResult.append("Class: ").append(detectedClass)
                                    .append(", Confidence: ").append(confidence)
                                    .append("\n");
                        } else {
                            filteredResult.append("Class: Not Found")
                                    .append(", Confidence: -1")
                                    .append("\n");
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        return filteredResult.toString();
    }

}
