package com.example.final_project;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.final_project.ml.BestFloat32;

import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.label.Category;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Date;
import java.util.List;

public class ResultPage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.result_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        String helm = getIntent().getStringExtra("helm_result");
        String vest = getIntent().getStringExtra("vest_result");
        String boots = getIntent().getStringExtra("boots_result");

        String[] helmParts = helm.split(",");
        String helmResult = helmParts[0].split(":")[1].trim();

        String[] vestParts = vest.split(",");
        String vestResult = vestParts[0].split(":")[1].trim();

        String[] bootsParts = boots.split(",");
        String bootsResult = bootsParts[0].split(":")[1].trim();

        TextView statusHelmTextView = findViewById(R.id.statusHelm);
        TextView statusVestTextView = findViewById(R.id.statusVest);
        TextView statusSepatuTextView = findViewById(R.id.statusSepatu);


        if ("helm".equals(helmResult)) {
            statusHelmTextView.setText("OK");
            statusHelmTextView.setTextColor(Color.GREEN);
        } else {
            statusHelmTextView.setText("GAGAL");
            statusHelmTextView.setTextColor(Color.RED);
        }

        if ("rompi".equals(vestResult)) {
            statusVestTextView.setText("OK");
            statusVestTextView.setTextColor(Color.GREEN);
        } else {
            statusVestTextView.setText("GAGAL");
            statusVestTextView.setTextColor(Color.RED);
        }

        if ("sepatu".equals(bootsResult)) {
            statusSepatuTextView.setText("OK");
            statusSepatuTextView.setTextColor(Color.GREEN);
        } else {
            statusSepatuTextView.setText("GAGAL");
            statusSepatuTextView.setTextColor(Color.RED);
        }

        Button scanButton = findViewById(R.id.scanLagi);
        Button backButton = findViewById(R.id.kembali);

        View.OnClickListener buttonClickListener = view -> {
            if (view.getId() == R.id.scanLagi) {
                startActivity(new Intent(ResultPage.this, HelmPage.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            } else if (view.getId() == R.id.kembali) {
                startActivity(new Intent(ResultPage.this, MainActivity.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        };

        scanButton.setOnClickListener(buttonClickListener);
        backButton.setOnClickListener(buttonClickListener);


    }


}
