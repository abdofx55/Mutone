package com.music.mutone.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.music.mutone.MediaViewModel;
import com.music.mutone.R;
import com.music.mutone.databinding.ActivityMainBinding;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MAIN_ACTIVITY_LOG_TAG";

    private static final int STORAGE_PERMISSION_CODE = 1;
    ActivityMainBinding binding;
    MediaViewModel viewModel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        viewModel = new ViewModelProvider(this).get(MediaViewModel.class);

        checkStoragePermissionState();

    }

    public void checkStoragePermissionState() {
        // Check for permission
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            // Permission granted

            Log.d(TAG, "Storage Permission is granted");
            viewModel.setStoragePermissionGranted(true);

            // Read Media Files
            viewModel.readDataFromRepository();

        } else {
            // You can directly ask for the permission.
            // The registered ActivityResultCallback gets the result of this request.
            Log.d(TAG, "Storage Permission is not granted , request it");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    STORAGE_PERMISSION_CODE);
        }
    }


    // This function is called when user accept or decline the permission.
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted.

                Log.d(TAG, "Storage Permission is granted");
                viewModel.setStoragePermissionGranted(true);

                // Read Media Files
                viewModel.readDataFromRepository();

            } else {
                // Permission is denied
                Log.d(TAG, "Storage Permission is denied");
                viewModel.setStoragePermissionGranted(false);
            }
        }
    }
}


