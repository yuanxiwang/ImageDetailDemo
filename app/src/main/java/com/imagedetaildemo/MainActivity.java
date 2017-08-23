package com.imagedetaildemo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import jp.co.cyberagent.android.gpuimage.GPUImage;
import jp.co.cyberagent.android.gpuimage.GPUImageFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageSepiaFilter;

public class MainActivity extends AppCompatActivity {
    private static final int PIC_REQUEST_CODE = 0x1901;
    private Button btnChoose;
    private Button btnDetail;
    private ImageView imageView;

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    private int REQUEST_CAMERA = 0x1911;
    private java.lang.String bitmapPath = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Example of a call to a native method
        btnChoose = (Button) findViewById(R.id.button);
        btnDetail = (Button) findViewById(R.id.button2);
        imageView = (ImageView) findViewById(R.id.imageView);
        TextView tv = (TextView) findViewById(R.id.sample_text);
        tv.setText(stringFromJNI());

        btnChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    checkPermission();
                } else {
                    Intent mIntent = new Intent(MainActivity.this, CameraActivity.class);
                    startActivityForResult(mIntent, PIC_REQUEST_CODE);
                }
            }
        });
        btnDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GPUImage gpuImage = new GPUImage(MainActivity.this);
                gpuImage.setImage(BitmapFactory.decodeFile(bitmapPath));
                gpuImage.setFilter(new GPUImageSepiaFilter(5));
                final long current = System.currentTimeMillis();
                Bitmap bitmap = gpuImage.getBitmapWithFilterApplied();
                imageView.setImageBitmap(bitmap);
            }
        });
    }

    public void checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA,
                                                                 Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                                                 Manifest.permission.READ_EXTERNAL_STORAGE},
                                                REQUEST_CAMERA);
        } else {
            Intent mIntent = new Intent(MainActivity.this, CameraActivity.class);
            startActivityForResult(mIntent, PIC_REQUEST_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PIC_REQUEST_CODE && resultCode == RESULT_OK && data.hasExtra("path")) {
            bitmapPath = data.getStringExtra("path");
            imageView.setImageBitmap(BitmapFactory.decodeFile(bitmapPath));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED
                    && grantResults[2] == PackageManager.PERMISSION_GRANTED) {
                Intent mIntent = new Intent(MainActivity.this, CameraActivity.class);
                startActivityForResult(mIntent, PIC_REQUEST_CODE);
            } else {
                checkPermission();
            }
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
//    public native int add(int a, int b);
}
