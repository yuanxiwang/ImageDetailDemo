package com.imagedetaildemo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by Administrator on 2017/8/23.
 */

public class CameraSurfaceView extends SurfaceView implements SurfaceHolder.Callback{
    protected SurfaceHolder holder;
    public CameraSurfaceView(Context context) {
        super(context);
    }

    public CameraSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        holder = getHolder();
        holder.addCallback(this);
        holder.setFormat(PixelFormat.TRANSPARENT);
        setZOrderOnTop(true);
    }

    public CameraSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
    void clearDraw() {
        Canvas canvas = holder.lockCanvas();
        Paint paint = new Paint();
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        canvas.drawPaint(paint);
        holder.unlockCanvasAndPost(canvas);
    }
    public void drawLine(int x, int y) {
        Canvas canvas = holder.lockCanvas();
        canvas.drawColor(Color.TRANSPARENT);
        Paint p = new Paint();
        Paint paint = new Paint();
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        canvas.drawPaint(paint);
        p.setAntiAlias(true);
        p.setColor(Color.GREEN);
        p.setStrokeWidth(2);
        p.setStyle(Paint.Style.STROKE);
        //canvas.drawPoint(100.0f, 100.0f, p);
//        canvas.drawLine(0,110, 500, 110, p);
        canvas.drawCircle(x, y, 100.0f, p);
        holder.unlockCanvasAndPost(canvas);
    }
}
