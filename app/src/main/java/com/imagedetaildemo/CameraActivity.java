package com.imagedetaildemo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.*;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Administrator on 2017/8/15.
 */

public class CameraActivity extends AppCompatActivity implements SurfaceHolder.Callback {
    private Button btnTakePhoto;
    private ImageView ivResult;
    private SurfaceView surfaceView;
    private CameraSurfaceView viewArea;
    private Camera mCamera;
    int PreviewWidth = 0;
    int PreviewHeight = 0;

    private SurfaceHolder surfaceHolder;
    private boolean isView = false;
    private Camera.Parameters myParameters;
    private Camera.AutoFocusCallback mAutoFocusCallback;
    public final static int BEHIND = 0;
    public final static int FRONT = 1;

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
        viewArea = (CameraSurfaceView) findViewById(R.id.viewArea);
        surfaceView.setZOrderOnTop(true);
        mAutoFocusCallback = new Camera.AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean b, Camera camera) {
                if (b) {
                    mCamera.cancelAutoFocus();
                    viewArea.clearDraw();
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
                focusOnTouch((int) motionEvent.getX(), (int) motionEvent.getY());
                return false;
            }
        });
        btnTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCamera.takePicture(myShutterCallback, null, myjpegCalback);
            }
        });
    }

    private void focusOnTouch(int x, int y) {
        Rect rect = new Rect(x - 100, y - 100, x + 100, y + 100);
        int left = rect.left * 2000 / surfaceView.getWidth() - 1000;
        int top = rect.top * 2000 / surfaceView.getHeight() - 1000;
        int right = rect.right * 2000 / surfaceView.getWidth() - 1000;
        int bottom = rect.bottom * 2000 / surfaceView.getHeight() - 1000;
        viewArea.drawLine(x, y);
        // 如果超出了(-1000,1000)到(1000, 1000)的范围，则会导致相机崩溃
        left = left < -1000 ? -1000 : left;
        top = top < -1000 ? -1000 : top;
        right = right > 1000 ? 1000 : right;
        bottom = bottom > 1000 ? 1000 : bottom;
        focusOnRect(new Rect(left, top, right, bottom));
    }

    protected void focusOnRect(Rect rect) {
        if (mCamera != null) {
            Parameters parameters = mCamera.getParameters(); // 先获取当前相机的参数配置对象
            parameters.setFocusMode(Parameters.FOCUS_MODE_AUTO); // 设置聚焦模式
            Log.d("CameraActivity", "parameters.getMaxNumFocusAreas() : " + parameters.getMaxNumFocusAreas());
            if (parameters.getMaxNumFocusAreas() > 0) {
                List<Camera.Area> focusAreas = new ArrayList<Area>();
                focusAreas.add(new Camera.Area(rect, 1000));
                parameters.setFocusAreas(focusAreas);
            }
            mCamera.cancelAutoFocus(); // 先要取消掉进程中所有的聚焦功能
            mCamera.setParameters(parameters); // 一定要记得把相应参数设置给相机
            mCamera.autoFocus(mAutoFocusCallback);
        }
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
            Matrix matrix = new Matrix();
            // 缩放原图
            matrix.postScale(1f, 1f);
            // 向左旋转45度，参数为正则向右旋转
            matrix.postRotate(90);
            //bmp.getWidth(), 500分别表示重绘后的位图宽高
            Bitmap dstbmp = Bitmap.createBitmap(bm, 0, 0, PreviewWidth, PreviewHeight, matrix, false);
            isView = false;
            String path = FileUtil.saveBitmap(dstbmp);
            Intent mIntent = new Intent();
            mIntent.putExtra("path", path);
            CameraActivity.this.setResult(RESULT_OK, mIntent);
            CameraActivity.this.finish();
        }
    };

    public void initCamera(int type) {
        if (mCamera == null && !isView) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
                mCamera = Camera.open(type);
            } else {
                mCamera = Camera.open();
            }
        }
        if (mCamera != null && !isView) {
            try {
                myParameters = mCamera.getParameters();
                myParameters.setPictureFormat(PixelFormat.JPEG);
                myParameters.setJpegQuality(100);
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
        initCamera(BEHIND);
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
