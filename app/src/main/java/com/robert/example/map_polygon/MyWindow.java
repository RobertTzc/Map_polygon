package com.robert.example.map_polygon;

import java.io.IOException;

import android.app.Service;
import android.content.Context;
import android.hardware.Camera;
import android.graphics.SurfaceTexture;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.TextureView.SurfaceTextureListener;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;

public class MyWindow extends LinearLayout implements SurfaceTextureListener {

	private TextureView textureView;

	/**
	 * 相机类
	 */
	private Camera myCamera;
	private Context context;

	private WindowManager mWindowManager;

	public MyWindow(Context context) {
		super(context);
		LayoutInflater.from(context).inflate(R.layout.window, this);
		this.context = context;
		
		initView();
	}

	private void initView() {

		textureView = (TextureView) findViewById(R.id.textureView);
		textureView.setSurfaceTextureListener(this);
		mWindowManager = (WindowManager) context.getSystemService(Service.WINDOW_SERVICE);
	}

	@Override
	public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {

		if (myCamera == null) {
			// 创建Camera实例
			myCamera = Camera.open();
			try {
				// 设置预览在textureView上
				myCamera.setPreviewTexture(surface);
				myCamera.setDisplayOrientation(SetDegree(MyWindow.this));

				// 开始预览
				myCamera.startPreview();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private int SetDegree(MyWindow myWindow) { 
		// 获得手机的方向
		int rotation = mWindowManager.getDefaultDisplay().getRotation();
		int degree = 0;
		// 根据手机的方向计算相机预览画面应该选择的角度
		switch (rotation) {
		case Surface.ROTATION_0:
			degree = 90;
			break;
		case Surface.ROTATION_90:
			degree = 0;
			break;
		case Surface.ROTATION_180:
			degree = 270;
			break;
		case Surface.ROTATION_270:
			degree = 180;
			break;
		}
		return degree;
	}

	@Override
	public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
		myCamera.stopPreview(); //停止预览
		myCamera.release();     // 释放相机资源
		myCamera = null;

		return false;
	}

	@Override
	public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

	}

	@Override
	public void onSurfaceTextureUpdated(SurfaceTexture surface) {

	}

}
