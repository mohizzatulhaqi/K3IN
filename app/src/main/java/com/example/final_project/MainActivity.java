package com.example.final_project;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;
import java.io.IOException;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private static final int kodekamera = 222;
    private static final int MY_PERMISSIONS_REQUEST_WRITE = 223;
    String nmFile;
    ImageView iv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        LinearLayout homeButton = findViewById(R.id.homeButton);
        LinearLayout scanButton = findViewById(R.id.scanButton);
        LinearLayout tutorialButton = findViewById(R.id.tutorialButton);

        View.OnClickListener buttonClickListener = view -> {
            if (view.getId() == R.id.homeButton) {
                startActivity(new Intent(MainActivity.this, MainActivity.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            } else if (view.getId() == R.id.scanButton) {
                startActivity(new Intent(MainActivity.this, HelmPage.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            } else if (view.getId() == R.id.tutorialButton) {
                startActivity(new Intent(MainActivity.this, TutorialPage.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        };

        homeButton.setOnClickListener(buttonClickListener);
        scanButton.setOnClickListener(buttonClickListener);
        tutorialButton.setOnClickListener(buttonClickListener);

    }

    private void openCamera() {
        askWritePermission();
        File photoFile = createImageFile();
        if (photoFile != null) {
            nmFile = photoFile.getAbsolutePath();
            Uri photoURI = FileProvider.getUriForFile(this,
                    getApplicationContext().getPackageName() + ".provider",
                    photoFile);
            Intent it = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            it.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            startActivityForResult(it, kodekamera);
        }
    }

    private File createImageFile() {
        String timeStamp = DateFormat.format("yyyyMMdd_HHmmss", new Date()).toString();
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = null;
        try {
            image = File.createTempFile(
                    imageFileName,
                    ".jpg",
                    storageDir
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }

    private void askWritePermission() {
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            int cameraPermission = this.checkSelfPermission(android.Manifest.permission.CAMERA);
            int writePermission = this.checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);

            if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_WRITE);
            }

            if (writePermission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_WRITE);
            }
        }
    }

}