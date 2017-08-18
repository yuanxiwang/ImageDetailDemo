package com.imagedetaildemo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.*;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.Iterator;
import java.util.List;
import java.util.TimerTask;

/**
 * Created by Administrator on 2017/8/15.
 */

public class CameraActivity extends AppCompatActivity implements SurfaceHolder.Callback {
    private Button btnTakePhoto;
    private ImageView ivResult;
    private SurfaceView surfaceView;
    private Camera mCamera;

    private SurfaceHolder surfaceHolder;
    private boolean isView = false;
    private Camera.Parameters myParameters;
    private Camera.AutoFocusCallback mAutoFocusCallback;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int flag = WindowManager.LayoutParams.FLAG_FULLSCREEN;
        Window myWindow = this.getWindow();
        myWindow.setFlags(flag, flag);
        setContentView(R.layout.activity_camera);

        btnTakePhoto = (Button) findViewById(R.id.btnTakePhoto);
        ivResult = (ImageView) findViewById(R.id.ivResult);
        surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        surfaceView.setZOrderOnTop(true);
        mAutoFocusCallback = new Camera.AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean b, Camera camera) {
                if (b) {
                    mCamera.cancelAutoFocus();
                }
            }
        };
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        surfaceHolder.setFormat(PixelFormat.TRANSLUCENT);
        surfaceHolder.addCallback(this);

        surfaceView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return false;
            }
        });
        btnTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCamera.takePicture(myShutterCallback, myRawCallback, myjpegCalback);
            }
        });
    }

    Camera.ShutterCallback myShutterCallback = new Camera.ShutterCallback() {
        public void onShutter() {
        }
    };
    PictureCallback myRawCallback = new PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
        }
    };
    PictureCallback myjpegCalback = new PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            Bitmap bm = BitmapFactory.decodeByteArray(data, 0, data.length);
            FileUtil.saveBitmap(bm);
            isView = false;
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
            isView = false;
        }
    };

    public void initCamera() {
        if (mCamera == null && !isView) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
                mCamera = Camera.open(0);
            } else {
                mCamera = Camera.open();
            }
        }
        if (mCamera != null && !isView) {
            try {
                int PreviewWidth = 0;
                int PreviewHeight = 0;
                myParameters = mCamera.getParameters();
                myParameters.setPictureFormat(PixelFormat.JPEG);
                // 选择合适的预览尺寸
                List<Size> sizeList = myParameters.getSupportedPreviewSizes();

                // 如果sizeList只有一个我们也没有必要做什么了，因为就他一个别无选择
                if (sizeList.size() > 1) {
                    Iterator<Size> itor = sizeList.iterator();
                    while (itor.hasNext()) {
                        Camera.Size cur = itor.next();
                        if (cur.width >= PreviewWidth && cur.height >= PreviewHeight) {
                            PreviewWidth = cur.width;
                            PreviewHeight = cur.height;
                            break;
                        }
                    }
                }
                myParameters.setPreviewSize(PreviewWidth, PreviewHeight);
                //myParameters.setFocusMode("auto");

                myParameters.setPictureSize(PreviewWidth, PreviewHeight); //1280, 720
                mCamera.setDisplayOrientation(90);
                mCamera.setParameters(myParameters);
                mCamera.setPreviewDisplay(surfaceHolder);
                mCamera.startPreview();
                isView = true;
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(CameraActivity.this, "初始化相机失败", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        initCamera();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        mCamera.autoFocus(mAutoFocusCallback);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }
}
