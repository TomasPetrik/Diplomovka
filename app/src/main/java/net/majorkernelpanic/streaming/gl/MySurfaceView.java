/*
 * Copyright (C) 2011-2014 GUIGUI Simon, fyhertz@gmail.com
 * 
 * This file is part of libstreaming (https://github.com/fyhertz/libstreaming)
 * 
 * Spydroid is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This source code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this source code; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package net.majorkernelpanic.streaming.gl;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.SurfaceTexture.OnFrameAvailableListener;
import android.graphics.Typeface;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;

import com.pop24.androidapp.MainActivity;
import com.pop24.androidapp.MjpegInputStream;
import com.pop24.androidapp.MyApp;

import net.majorkernelpanic.streaming.MediaStream;
import net.majorkernelpanic.streaming.video.VideoStream;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.concurrent.Semaphore;

import javax.microedition.khronos.opengles.GL10;

/**
 * An enhanced SurfaceView in which the camera preview will be rendered. 
 * This class was needed for two reasons. <br /> 
 * 
 * First, it allows to use to feed MediaCodec with the camera preview 
 * using the surface-to-buffer method while rendering it in a surface 
 * visible to the user. To force the surface-to-buffer method in 
 * libstreaming, call {@link MediaStream#setStreamingMethod(byte)}
 * with {@link MediaStream#MODE_MEDIACODEC_API_2}. <br /> 
 * 
 * Second, it allows to force the aspect ratio of the SurfaceView 
 * to match the aspect ratio of the camera preview, so that the 
 * preview do not appear distorted to the user of your app. To do 
 * that, call {@link MySurfaceView#setAspectRatioMode(int)} with
 * {@link MySurfaceView#ASPECT_RATIO_PREVIEW} after creating your
 * {@link MySurfaceView}. <br />
 * 
 */
public class MySurfaceView extends GLSurfaceView implements Runnable, OnFrameAvailableListener, SurfaceHolder.Callback{

	public final static String TAG = "MySurfaceView";

	/** 
	 * The aspect ratio of the surface view will be equal 
	 * to the aspect ration of the camera preview.
	 **/
	public static final int ASPECT_RATIO_PREVIEW = 0x01;
	
	/** The surface view will fill completely fill its parent. */
	public static final int ASPECT_RATIO_STRETCH = 0x00;
	
	private Thread mThread = null;
	private Handler mHandler = null;
	private boolean mFrameAvailable = false; 
	private boolean mRunning = true;
	private int mAspectRatioMode = ASPECT_RATIO_STRETCH;

	// The surface in which the preview is rendered
	private SurfaceManager mViewSurfaceManager = null;
	
	// The input surface of the MediaCodec
	private SurfaceManager mCodecSurfaceManager = null;
	
	// Handles the rendering of the SurfaceTexture we got 
	// from the camera, onto a Surface
	private TextureManager mTextureManager = null;

	private Surface mSurface = null;

	private final Semaphore mLock = new Semaphore(0);
	private final Object mSyncObject = new Object();

	// Allows to force the aspect ratio of the preview
	private ViewAspectRatioMeasurer mVARM = new ViewAspectRatioMeasurer();

	private boolean otgActive = false;

	
	public MySurfaceView(Context context, AttributeSet attrs) {
		super(context, attrs);
		Log.d(TAG, "SurfaceView contructor called");

		/* FOR OTG
		setRenderer(this);
		setRenderMode(RENDERMODE_WHEN_DIRTY);

		*/
		//getHolder().setFormat(PixelFormat.TRANSLUCENT);

		mHandler = new Handler();
		getHolder().addCallback(this);

	}

	public void setAspectRatioMode(int mode) {
		mAspectRatioMode = mode;
	}

	public SurfaceTexture getSurfaceTexture() {
		return mTextureManager.getSurfaceTexture();
	}

	public void addMediaCodecSurface(Surface surface) {
		synchronized (mSyncObject) {
			mCodecSurfaceManager = new SurfaceManager(surface,mViewSurfaceManager);
		}
	}

	public void removeMediaCodecSurface() {
		synchronized (mSyncObject) {
			if (mCodecSurfaceManager != null) {
				mCodecSurfaceManager.release();
				mCodecSurfaceManager = null;
			}
		}
	}



	public void startGLThread() {
		Log.d(TAG, "Thread started.");
		if (mTextureManager == null) {
			mTextureManager = new TextureManager();
		}
		if (mTextureManager.getSurfaceTexture() == null) {
			mThread = new Thread(MySurfaceView.this);
			mRunning = true;
			mThread.start();
			mLock.acquireUninterruptibly();
		}
	}



	/*
	private boolean externalCam = false;

	public void setExternalCam(boolean externalCam){
		this.externalCam = externalCam;
	}

	*/

	@Override
	public void run() {

		mViewSurfaceManager = new SurfaceManager(getHolder().getSurface());
		mViewSurfaceManager.makeCurrent();
		mTextureManager.createTexture().setOnFrameAvailableListener(this);
		Log.d(TAG, "mTextureManager id : " +  mTextureManager.getTextureId());

		mLock.release();

		try {
			long ts = 0, oldts = 0;
			while (mRunning) {
				synchronized (mSyncObject) {
					mSyncObject.wait(2500);
					if (mFrameAvailable) {
						mFrameAvailable = false;

						mViewSurfaceManager.makeCurrent();

						mTextureManager.updateFrame();

						if(otgActive)
							mTextureManager.drawFrame(image, txW, txH);
						else {
							mTextureManager.drawFrame();
						}


						mViewSurfaceManager.swapBuffer();



						if (mCodecSurfaceManager != null) {
							Log.d(TAG, "drawing on mediacodec surface");
							mCodecSurfaceManager.makeCurrent();
							if(otgActive)
								mTextureManager.drawFrame(image, txW, txH);
							else
								mTextureManager.drawFrame();
							oldts = ts;
							ts = mTextureManager.getSurfaceTexture().getTimestamp();
							//Log.d(TAG,"FPS: "+(1000000000/(ts-oldts)));
							mCodecSurfaceManager.setPresentationTime(ts);
							mCodecSurfaceManager.swapBuffer();
						}



					} else {
						Log.e(TAG,"No frame received !");
					}
				}
			}
		} catch (InterruptedException ignore) {
		} finally {
			mViewSurfaceManager.release();
			mTextureManager.release();
		}
	}

	@Override
	public void onFrameAvailable(SurfaceTexture surfaceTexture) {
		//Log.d(TAG, "onFrameAvailable() called");
		synchronized (mSyncObject) {
			mFrameAvailable = true;
			mSyncObject.notifyAll();
		}
	}

	public void setOtgActive(boolean otgActive){
		this.otgActive = otgActive;
	}


	private void drawFrame()
	{
		Log.d("SurfaceView", "drawFrame");
		mTextureManager.drawFrame();

		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

		if (image == null)
			return;

		if(mTextureManager == null) {
			Log.d("SurfaceView", "mTextureManager is null");
			return;
		}

		Log.d("SurfaceView", "drawing image on texture id " + mTextureManager.getTextureId());


		//gl.glLoadIdentity();
		//gl.glTranslatef(0.0f, 0.0f, -2.0f);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureManager.getTextureId());
		GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGB,
				txW, txH, 0, GLES20.GL_RGB,
				GLES20.GL_UNSIGNED_SHORT_5_6_5,
				ByteBuffer.wrap(image));
		//gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		//gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
		GLES20.glFrontFace(GLES20.GL_CW);
		//gl.glVertexPointer(3, GL10.GL_FLOAT, 0, squareVerticesBuf);
		//gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, textureCoordsBuf);
		GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
		//gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
		//gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
	}


	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		Log.d(TAG, "surfaceChanged() called");

		/*
		if (thread != null) {
			thread.setSurfaceSize(width, height);
		}
		*/

	}




	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		Log.d(TAG, "surfaceCreated() called");
		surfaceCreated = true;
		surfaceDone = true;
	}


	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.d(TAG, "surfaceDestroyed() called");
		if (mThread != null) {
			mThread.interrupt();
		}
		mRunning = false;


		surfaceDone = false;
		stopPlayback();

	}


	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		if (mVARM.getAspectRatio() > 0 && mAspectRatioMode == ASPECT_RATIO_PREVIEW) {
			mVARM.measure(widthMeasureSpec, heightMeasureSpec);
			setMeasuredDimension(mVARM.getMeasuredWidth(), mVARM.getMeasuredHeight());
		} else {
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		}
	}

	/**
	 * Requests a certain aspect ratio for the preview. You don't have to call this yourself,
	 * the {@link VideoStream} will do it when it's needed.
	 */
	public void requestAspectRatio(double aspectRatio) {
		if (mVARM.getAspectRatio() != aspectRatio) {
			mVARM.setAspectRatio(aspectRatio);
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					if (mAspectRatioMode == ASPECT_RATIO_PREVIEW) {
						requestLayout();
					}
				}
			});
		}
	}

	/**
	 * This class is a helper to measure views that require a specific aspect ratio.
	 * @author Jesper Borgstrup
	 */
	public class ViewAspectRatioMeasurer {

		private double aspectRatio;

		public void setAspectRatio(double aspectRatio) {
			this.aspectRatio = aspectRatio;
		}

		public double getAspectRatio() {
			return this.aspectRatio;
		}

		/**
		 * Measure with the aspect ratio given at construction.<br />
		 * <br />
		 * After measuring, get the width and height with the {@link #getMeasuredWidth()}
		 * and {@link #getMeasuredHeight()} methods, respectively.
		 * @param widthMeasureSpec The width <tt>MeasureSpec</tt> passed in your <tt>View.onMeasure()</tt> method
		 * @param heightMeasureSpec The height <tt>MeasureSpec</tt> passed in your <tt>View.onMeasure()</tt> method
		 */
		public void measure(int widthMeasureSpec, int heightMeasureSpec) {
			measure(widthMeasureSpec, heightMeasureSpec, this.aspectRatio);
		}

		/**
		 * Measure with a specific aspect ratio<br />
		 * <br />
		 * After measuring, get the width and height with the {@link #getMeasuredWidth()}
		 * and {@link #getMeasuredHeight()} methods, respectively.
		 * @param widthMeasureSpec The width <tt>MeasureSpec</tt> passed in your <tt>View.onMeasure()</tt> method
		 * @param heightMeasureSpec The height <tt>MeasureSpec</tt> passed in your <tt>View.onMeasure()</tt> method
		 * @param aspectRatio The aspect ratio to calculate measurements in respect to
		 */
		public void measure(int widthMeasureSpec, int heightMeasureSpec, double aspectRatio) {
			int widthMode = MeasureSpec.getMode(widthMeasureSpec);
			int widthSize = widthMode == MeasureSpec.UNSPECIFIED ? Integer.MAX_VALUE : MeasureSpec.getSize(widthMeasureSpec);
			int heightMode = MeasureSpec.getMode(heightMeasureSpec);
			int heightSize = heightMode == MeasureSpec.UNSPECIFIED ? Integer.MAX_VALUE : MeasureSpec.getSize(heightMeasureSpec);

			if ( heightMode == MeasureSpec.EXACTLY && widthMode == MeasureSpec.EXACTLY ) {
				/*
				 * Possibility 1: Both width and height fixed
				 */
				measuredWidth = widthSize;
				measuredHeight = heightSize;

			} else if ( heightMode == MeasureSpec.EXACTLY ) {
				/*
				 * Possibility 2: Width dynamic, height fixed
				 */
				measuredWidth = (int) Math.min(widthSize, heightSize * aspectRatio);
				measuredHeight = (int) (measuredWidth / aspectRatio);

			} else if ( widthMode == MeasureSpec.EXACTLY ) {
				/*
				 * Possibility 3: Width fixed, height dynamic
				 */
				measuredHeight = (int) Math.min(heightSize, widthSize / aspectRatio);
				measuredWidth = (int) (measuredHeight * aspectRatio);

			} else {
				/*
				 * Possibility 4: Both width and height dynamic
				 */
				if ( widthSize > heightSize * aspectRatio ) {
					measuredHeight = heightSize;
					measuredWidth = (int)( measuredHeight * aspectRatio );
				} else {
					measuredWidth = widthSize;
					measuredHeight = (int) (measuredWidth / aspectRatio);
				}

			}
		}

		private Integer measuredWidth = null;
		/**
		 * Get the width measured in the latest call to <tt>measure()</tt>.
		 */
		public int getMeasuredWidth() {
			if ( measuredWidth == null ) {
				throw new IllegalStateException( "You need to run measure() before trying to get measured dimensions" );
			}
			return measuredWidth;
		}

		private Integer measuredHeight = null;
		/**
		 * Get the height measured in the latest call to <tt>measure()</tt>.
		 */
		public int getMeasuredHeight() {
			if ( measuredHeight == null ) {
				throw new IllegalStateException( "You need to run measure() before trying to get measured dimensions" );
			}
			return measuredHeight;
		}

	}

	public void internalCameraRelease(){
		if(mCodecSurfaceManager != null)
			mCodecSurfaceManager.release();
		if(mViewSurfaceManager != null)
			mViewSurfaceManager.release();
		if(mTextureManager != null)
			mTextureManager.release();
		getHolder().removeCallback(this);

		//mLock.release();
		if (mThread != null) {
			mThread.interrupt();
		}
	}



/* EXTERNAL OTG CAMERA */


	private boolean textureGen, sizeSet, surfaceCreated;
	private int textureId;
	private int frW, frH, txW, txH, drW, drH, drX, drY;
	private int[] intAry1 = new int[1];
	private FloatBuffer squareVerticesBuf, textureCoordsBuf;
	private int[] textureRect;

	//private boolean fullScreen = false;


	/*
	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig eglConfig) {
		Log.d(TAG, "OTG onSurfaceCreated() called");

		if (frW > 0)
			calcDrawRect(getHolder().getSurfaceFrame());

		createTexture(gl);

		gl.glEnable(GL10.GL_TEXTURE_2D);
		gl.glClearColor(0.0f, 0.0f, 0.0f, 0.5f);
		gl.glClearDepthf(1.0f);
		// gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);

		surfaceCreated = true;
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		Log.d(TAG, "OTG onSurfaceChanged() called");

		Rect r = getHolder().getSurfaceFrame();
		if (r.right > 0)
			calcDrawRect(r);
		if (fullScreen) {
			surfaceChangedFullScreen(gl, width, height);
		} else {
			surfaceChanged(gl, width, height);
		}
	}

	@Override
	public void onDrawFrame(GL10 gl) {
		Log.d(TAG, "OTG onDrawFrame() called");

		if (fullScreen) {
			drawFrameFullScreen(gl);
		} else {
			drawFrame(gl);
		}
	}

	*/

	public boolean getReady()
	{
		Log.d("SurfaceView", "sizeSet : " + sizeSet + " , surfaceCreated : " + surfaceCreated) ;
		return sizeSet && surfaceCreated;
	}

	private void setVertices(float[] textureCoords, float[] squareVertices)		//during create rect for texture
	{
		ByteBuffer byteBuffer = ByteBuffer
				.allocateDirect(squareVertices.length * 4);
		byteBuffer.order(ByteOrder.nativeOrder());
		squareVerticesBuf = byteBuffer.asFloatBuffer();
		squareVerticesBuf.put(squareVertices);
		squareVerticesBuf.position(0);

		byteBuffer = ByteBuffer.allocateDirect(textureCoords.length * 4);
		byteBuffer.order(ByteOrder.nativeOrder());
		textureCoordsBuf = byteBuffer.asFloatBuffer();
		textureCoordsBuf.put(textureCoords);
		textureCoordsBuf.position(0);
	}

	private void calcDrawRect(Rect r)		//during create texture
	{
		float tw = (float)frW / (float)txW;
		float th = (float)frH / (float)txH;

		int rw = r.right - r.left;
		int rh = r.bottom - r.top;
		float sw, sh;

		if (rw > rh) {
			if ((frH != 0) && frW * 9 / frH == 16) {
				sw = 1.35f;
				sh = 0.76f;
			} else {
				sw = 1.0f;
				sh = (float)frH / (float)frW;
			}
		} else {
			sw = rw / rh;
			sh = sw * ((float)frH / (float)frW) * 0.9f;
		}

		if (rw > rh &&
				(float)rw / (float)rh > (float)frW / (float)frH) {
			drH = rh;
			drW = (int)((float)frW / frH * drH);
			drX = (rw - drW) / 2;
			drY = 0;
		} else {
			drW = rw;
			drH = (int)((float)frH / frW * drW);
			drX = 0;
			drY = (rh - drH) / 2;
		}

		float[] textureCoords = new float[] {
				// top left (V2)
				0.0f, th,
				// bottom left (V1)
				0.0f, 0.0f,
				// top right (V4)
				tw, th,
				// bottom right (V3)
				tw, 0.0f
		};

		float[] squareVertices = new float[] {
				// V1 - bottom left
				-sw, -sh, 0.0f,
				// V2 - top left
				-sw, sh, 0.0f,
				// V3 - bottom right
				sw, -sh, 0.0f,
				// V4 - top right
				sw, sh, 0.0f
		};

		setVertices(textureCoords, squareVertices);		//during create rect for texture
	}

	public void setImageSize(int width, int height, int texWidth, int texHeight)
	{
		Log.d("SurfaceView", "OTG setImageSize() called : width : " + width + " , height : " + height );
		this.frW = width;
		this.frH = height;
		this.txW = texWidth;
		this.txH = texHeight;
		textureRect = new int[] { 0, frH, frW, -frH };

		Rect r = getHolder().getSurfaceFrame();
		if (r.right > 0)
			calcDrawRect(r);

		sizeSet = true;
	}

	public int getTextureWidth()
	{
		return txW;
	}

	public int getTextureHeight()
	{
		return txH;
	}

	private void createTexture(GL10 gl)		//already created for camera, but with GLE20
	{
		if (!textureGen) {
			intAry1[0] = textureId;
			gl.glDeleteTextures(1, intAry1, 0);
		}

		gl.glGenTextures(1, intAry1, 0);
		for (int i = 0; i < intAry1.length; i++) {
			textureId = intAry1[0];
			gl.glBindTexture(GL10.GL_TEXTURE_2D, textureId);
			gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER,
					GL10.GL_NEAREST);
			gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER,
					GL10.GL_LINEAR);
			gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S,
					GL10.GL_CLAMP_TO_EDGE);
			gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T,
					GL10.GL_CLAMP_TO_EDGE);
			gl.glTexEnvf(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE,
					GL10.GL_REPLACE);
		}

		textureGen = true;
	}

	public void render(byte[] image)
	{
		this.image = image;
		requestRender();

	}

	private void surfaceChanged(GL10 gl, int width, int height)
	{
		if (height == 0)
			height = 1;

		gl.glViewport(0, 0, width, height);
		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glLoadIdentity();

		GLU.gluPerspective(gl, 45.0f, (float) width / (float) height, 1f,
				100.0f);

		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glLoadIdentity();
		GLU.gluLookAt(gl, 0, 0, 5.1f, 0, 0, 0, 0, 1, 0);
		gl.glNormal3f(0, 0, 1);
	}

	/*
	private void drawFrame(GL10 gl)
	{
		Log.d("SurfaceView", "drawFrame");

		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

		if (image == null)
			return;

		gl.glLoadIdentity();
		gl.glTranslatef(0.0f, 0.0f, -2.0f);
		gl.glBindTexture(GL10.GL_TEXTURE_2D, textureId);
		gl.glTexImage2D(GL10.GL_TEXTURE_2D, 0, GL10.GL_RGB,
				txW, txH, 0, GL10.GL_RGB,
				GL10.GL_UNSIGNED_SHORT_5_6_5,
				ByteBuffer.wrap(image));
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
		gl.glFrontFace(GL10.GL_CW);
		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, squareVerticesBuf);
		gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, textureCoordsBuf);
		gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);
		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
	}
	*/




	/*
	private void surfaceChangedFullScreen(GL10 gl, int width, int height)
	{
		if (height == 0)
			return;

		gl.glViewport(0, 0, width, height);
		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glLoadIdentity();
		GLU.gluOrtho2D(gl, 0.0f, (float) width, 0.0f, (float) height);

		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glLoadIdentity();
	}
	*/

	/*

	private void drawFrameFullScreen(GL10 gl)
	{
		if (textureRect == null)
			return;

		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

		if (image == null)
			return;

		gl.glLoadIdentity();
		gl.glEnable(GL10.GL_TEXTURE_2D);
		gl.glBindTexture(GL10.GL_TEXTURE_2D, textureId);
		gl.glTexImage2D(GL10.GL_TEXTURE_2D, 0, GL10.GL_RGB,
				txW, txH, 0, GL10.GL_RGB, GL10.GL_UNSIGNED_SHORT_5_6_5,
				ByteBuffer.wrap(image));
		((GL11)gl).glTexParameteriv(GL10.GL_TEXTURE_2D,
				GL11Ext.GL_TEXTURE_CROP_RECT_OES, textureRect, 0);
		((GL11Ext)gl).glDrawTexfOES(drX, drY, 0f, drW, drH);
	}

	*/

/*
	public void setFullScreen(boolean fullScreen)
	{
		this.fullScreen = fullScreen;
	}
*/






















/* 	EXTERNAL IP CAMERA */

	/* MJPEG INIT */

	public final static int POSITION_UPPER_LEFT = 9;
	public final static int POSITION_UPPER_RIGHT = 3;
	public final static int POSITION_LOWER_LEFT = 12;
	public final static int POSITION_LOWER_RIGHT = 6;

	public final static int SIZE_STANDARD = 1;
	public final static int SIZE_BEST_FIT = 4;
	public final static int SIZE_FULLSCREEN = 8;

	SurfaceHolder holder;
	Context saved_context;

	//private MjpegViewThread thread;
	private MjpegInputStream mIn = null;
	private boolean mRun = false;
	private boolean surfaceDone = false;

	private Paint overlayPaint;
	private int overlayTextColor;
	private int overlayBackgroundColor;
	private int ovlPos;
	private int dispWidth;
	private int dispHeight;
	private int displayMode;
	private byte[] image;

	private boolean suspending = false;

	private Bitmap bmp = null;

	// image size

	public int IMG_WIDTH = 640;
	public int IMG_HEIGHT = 480;

	private final static int HEADER_MAX_LENGTH = 100;

	public void updateOTGImage(byte[] image)
	{
		synchronized (mSyncObject) {
			this.image = image;
			mFrameAvailable = true;
			mSyncObject.notifyAll();
		}
		//drawImage()
		//drawFrame();
	}

	public int readMjpegFrameFromBytes(byte[] imageData, Bitmap bmp) throws IOException {
		if (imageData != null && imageData.length > 0) {
			//Bitmap bitmap = BitmapFactory.decodeStream(new ByteArrayInputStream(imageData, 0, imageData.length+HEADER_MAX_LENGTH));
			Log.d(TAG,  " lenght " + bytesToHex(imageData).length() +  " , 2frameData : " + bytesToHex(imageData));
			return ((MainActivity) MyApp.content).pixeltobmp(imageData, imageData.length+HEADER_MAX_LENGTH, bmp);	//Bitmap size differs !
		} else {
			return 255;
		}
	}

	final protected char[] hexArray = "0123456789ABCDEF".toCharArray();
	public  String bytesToHex(byte[] bytes) {
		char[] hexChars = new char[bytes.length * 2];
		int j=0;
		for (; j < bytes.length; j++ ) {
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];

			//if(hexArray[v >>> 4] == '0' && hexArray[v & 0x0F] == '0')
			//   break;
			//Log.d(TAG, "j index char : " + j + " = " + hexArray[v >>> 4] +  hexArray[v & 0x0F]);
		}

		//Log.d(TAG,  "hexChars : " + new String(hexChars));

		return new String(hexChars);
	}




	public void setResolution(int w, int h) {
		IMG_WIDTH = w;
		IMG_HEIGHT = h;
	}

	public void setSource(MjpegInputStream source) {
		mIn = source;
		if (!suspending) {
			startPlayback();
		} else {
			resumePlayback();
		}
	}

	public void setDisplayMode(int s) {
		displayMode = s;
	}


	public void initMjpegView(Context context) {
		Log.d(TAG, "MJPEG init called()");

		internalCameraRelease();
		//holder.unlockCanvasAndPost();
		//mLock.release();

		//SurfaceHolder holder = getHolder();

		holder = getHolder();
		holder.removeCallback(this);
		saved_context = context;
		//holder.addCallback(this);
		//thread = new MjpegViewThread(holder);
		setFocusable(true);
		overlayPaint = new Paint();
		overlayPaint.setTextAlign(Paint.Align.LEFT);
		overlayPaint.setTextSize(12);
		overlayPaint.setTypeface(Typeface.DEFAULT);
		overlayTextColor = Color.WHITE;
		overlayBackgroundColor = Color.BLACK;
		ovlPos = MySurfaceView.POSITION_LOWER_RIGHT;
		displayMode = MySurfaceView.SIZE_BEST_FIT;
		dispWidth = getWidth();
		dispHeight = getHeight();
	}

	/* MJPEG START AND STOP PLAYBACK */

	public void stopCameraThread(){
		if (mThread != null) {
			mThread.interrupt();
		}
	}

	public void startPlayback() {
		Log.d(TAG, "startPlayback");
		//first must turn off thread from built camera !
		if (mThread != null) {
			mThread.interrupt();
		}
		mRunning = false;

		//after try start with preview from external camera !
		//if (mIn != null) {
			mRun = true;
		/*
			if (thread == null) {
				thread = new MjpegViewThread(holder);
			}
			thread.start();
			*/
		//}

	}

	public void resumePlayback() {
		if (suspending) {
			//if (mIn != null) {
				mRun = true;
				SurfaceHolder holder = getHolder();
				holder.addCallback(this);
			/*
				thread = new MjpegViewThread(holder);
				thread.start();
				*/
				suspending = false;
			//}
		}
	}

	public void stopPlayback() {
		if (mRun) {
			suspending = true;
		}
		mRun = false;
		/*
		if (thread != null) {
			boolean retry = true;
			while (retry) {
				try {
					thread.join();
					retry = false;
				} catch (InterruptedException e) {
				}
			}
			thread = null;
		}
		*/

		/*
		if (mIn != null) {
			try {
				mIn.close();
			} catch (IOException e) {
			}
			mIn = null;
		}
		*/

	}


	/* MJPEG THREAD */


/*
	public class MjpegViewThread extends Thread implements SurfaceHolder.Callback{

		private SurfaceHolder mSurfaceHolder;
		private String tag = "MjpegViewThread";


		public MjpegViewThread(SurfaceHolder surfaceHolder) {
			this.mSurfaceHolder = surfaceHolder;
			this.mSurfaceHolder.addCallback(this);
		}

*/
		private Rect destRect(int bmw, int bmh) {
			int tempx;
			int tempy;
			if (displayMode == MySurfaceView.SIZE_STANDARD) {
				tempx = (dispWidth / 2) - (bmw / 2);
				tempy = (dispHeight / 2) - (bmh / 2);
				return new Rect(tempx, tempy, bmw + tempx, bmh + tempy);
			}
			if (displayMode == MySurfaceView.SIZE_BEST_FIT) {
				float bmasp = (float) bmw / (float) bmh;
				bmw = dispWidth;
				bmh = (int) (dispWidth / bmasp);
				if (bmh > dispHeight) {
					bmh = dispHeight;
					bmw = (int) (dispHeight * bmasp);
				}
				tempx = (dispWidth / 2) - (bmw / 2);
				tempy = (dispHeight / 2) - (bmh / 2);
				return new Rect(tempx, tempy, bmw + tempx, bmh + tempy);
			}
			if (displayMode == MySurfaceView.SIZE_FULLSCREEN)
				return new Rect(0, 0, dispWidth, dispHeight);
			return null;
		}

		public void setSurfaceSize(int width, int height) {
			synchronized (holder) {
				dispWidth = width;
				dispHeight = height;
			}
		}



		public void drawImage() {
			PorterDuffXfermode mode = new PorterDuffXfermode(PorterDuff.Mode.DST_OVER);

			int width;
			int height;
			Paint p = new Paint();
			Bitmap ovl = null;

			//while (mRun) {
				Log.d(TAG, "thread cycle begin");
				Rect destRect = null;
				Canvas c = null;

				if (surfaceDone) {
					try {
						if (bmp == null) {
							bmp = Bitmap.createBitmap(IMG_WIDTH, IMG_HEIGHT, Bitmap.Config.ARGB_8888);
						}

						int ret = readMjpegFrameFromBytes(image, bmp);
						Log.d(TAG, "readMjpegFrameFromBytes : ret " + ret);

						if (ret == -1) {
							Log.d(TAG, "MJPEG image error !");
							//((MjpegActivity) saved_context).setImageError();
							return;
						}
						destRect = destRect(bmp.getWidth(), bmp.getHeight());
						Log.d(TAG, "destRect : btm.width " + bmp.getWidth() + " , bmp.getHeight() " + bmp.getHeight());

						c = holder.lockCanvas();
						Log.d(TAG, "canvas locked");
						synchronized (holder) {
							//c.drawColor(Color.BLACK);
							c.drawBitmap(bmp, null, destRect, p);
							Log.d(TAG, "drawingBitmap size : " + bmp.getByteCount());
							//mFrameAvailable = true;
							//mSyncObject.notifyAll();
						}

					} catch (IOException e) {

					} finally {
						if (c != null) holder.unlockCanvasAndPost(c);
					}
				}
			//}
		}


	//}



}
