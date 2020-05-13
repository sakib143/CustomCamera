package com.example.customcameratemp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.SecureRandom;


public class MainActivity extends AppCompatActivity implements
        View.OnClickListener {

    public static final String TAG = MainActivity.class.getSimpleName();
    private Camera mCamera;
    private int PERMISSION_CALLBACK_CONSTANT = 1000;
    private ImageView ivCapturePhoto;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageView ivCapture = (ImageView) findViewById(R.id.ivCapture);
        ivCapturePhoto = findViewById(R.id.ivCapturePhoto);

        checkAndGivePermission();
        ivCapture.setOnClickListener(this);
    }

    private void checkAndGivePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSION_CALLBACK_CONSTANT);
        } else {
            initialize();
        }
    }

    private void initialize() {
        mCamera = getCameraInstance();
        CameraPreview mPreview = new CameraPreview(this, mCamera);
        FrameLayout rlCameraPreview = (FrameLayout) findViewById(R.id.rlCameraPreview);
        if (rlCameraPreview != null) {
            rlCameraPreview.addView(mPreview);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CALLBACK_CONSTANT) {
            boolean allgranted = false;
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    allgranted = true;
                } else {
                    allgranted = false;
                    break;
                }
            }


            if (allgranted) {
                initialize();
            } else if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.CAMERA)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, PERMISSION_CALLBACK_CONSTANT);
                }
            } else if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE
                    }, PERMISSION_CALLBACK_CONSTANT);
                }
            } else {
                Toast.makeText(MainActivity.this, "Permission is mandatory, Try giving it from App Settings", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open(1);// Open Front camera
        } catch (Exception e) {
            e.printStackTrace();
        }
        return c;
    }

    public static Bitmap RotateBitmap(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            File pictureFile = getOutputMediaFile();
            if (pictureFile == null) {
                Log.d(TAG, "Error creating media file, check storage permissions: ");
                return;
            }

            MediaScannerConnection.scanFile(MainActivity.this,
                    new String[]{pictureFile.toString()}, null,
                    new MediaScannerConnection.OnScanCompletedListener() {
                        public void onScanCompleted(String path, Uri uri) {
                            mCamera.startPreview();
                        }
                    });
            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();

                Bitmap bitmap = BitmapFactory.decodeFile(pictureFile.getPath());
                ivCapturePhoto.setImageBitmap(RotateBitmap(bitmap,270));


            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    private static File getOutputMediaFile() {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "WellnessUser");
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                System.out.println("Directory not created");
                return null;
            }
        }

        SecureRandom random = new SecureRandom();
        int num = random.nextInt(1000000);
        return new File(mediaStorageDir.getAbsolutePath() + File.separator +
                "IMG_" + num + ".jpg");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ivCapture:
                mCamera.takePicture(null, null, mPicture);
                break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }
}