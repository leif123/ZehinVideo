package com.zehin.videosdk.view;

import android.content.Context;
import android.opengl.GLException;
import android.opengl.GLSurfaceView.Renderer;
import android.util.Log;

import com.zehin.videosdk.utils.APPScreen;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static com.zehin.videosdk.constants.VideoConstants.LOG;

public class SimpleRenderer implements Renderer {
	private int mScreenWidth, mScreenHeight;
	private FloatBuffer vertices;
	private FloatBuffer texture;
	private ShortBuffer indices;
	private int textureId;
	private int[] textureIds = new int[1];
	private int mVideoWidth;
	private int mVideoHeight;
	private ByteBuffer buffer;
	private APPScreen screen = null;
	
	public SimpleRenderer(Context content) {
		screen = new APPScreen(content);
		mScreenWidth = screen.getAPPScreenWidth();
		mScreenHeight = screen.getAPPScreenHeight();

		ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * 2 * 4);
		byteBuffer.order(ByteOrder.nativeOrder());
		vertices = byteBuffer.asFloatBuffer();
		float x = mScreenWidth / 2;
		float y = mScreenHeight / 2;
		vertices.put(new float[] { -x, -y, x, -y, -x, y, x, y });

		ByteBuffer indicesBuffer = ByteBuffer.allocateDirect(6 * 2);
		indicesBuffer.order(ByteOrder.nativeOrder());
		indices = indicesBuffer.asShortBuffer();
		indices.put(new short[] { 0, 1, 2, 1, 2, 3 });

		ByteBuffer textureBuffer = ByteBuffer.allocateDirect(4 * 2 * 4);
		textureBuffer.order(ByteOrder.nativeOrder());
		texture = textureBuffer.asFloatBuffer();
		texture.put(new float[] { 0, 1f, 1f, 1f, 0f, 0f, 1f, 0f });

		indices.position(0);
		vertices.position(0);
		texture.position(0);
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		gl.glGenTextures(1, textureIds, 0);
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		Log.d("GLSurfaceViewTest", "surface Changed");
		gl.glViewport(0, 0, width, height);
	}

	@Override
	public void onDrawFrame(GL10 gl) {
		if (buffer != null) {
			synchronized (this) {
				buffer.position(0);
				try {
					textureId = loadTexture(gl, textureIds[0]);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				// 定义显示在屏幕上的什么位置(opengl 自动转换)
				gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
				gl.glMatrixMode(GL10.GL_PROJECTION);
				gl.glLoadIdentity();
				gl.glOrthof(-mScreenWidth / 2, mScreenWidth / 2,
						-mScreenHeight / 2, mScreenHeight / 2, 1, -1);
				gl.glEnable(GL10.GL_TEXTURE_2D);
				// 绑定纹理ID
				gl.glBindTexture(GL10.GL_TEXTURE_2D, textureId);
				gl.glTexSubImage2D(GL10.GL_TEXTURE_2D, 0, 0, 0, mVideoWidth,
						mVideoHeight, GL10.GL_RGB, GL10.GL_UNSIGNED_SHORT_5_6_5,
						buffer);				
				gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
				gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
				gl.glVertexPointer(2, GL10.GL_FLOAT, 0, vertices);
				gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, texture);
				gl.glDrawElements(GL10.GL_TRIANGLE_STRIP, 6,
						GL10.GL_UNSIGNED_SHORT, indices);
			}
		}
	}

	public int loadTexture(GL10 gl, int textureId) throws IOException {
//		Log.d("GLSurfaceViewTest", "surface loadTexture");
		gl.glBindTexture(GL10.GL_TEXTURE_2D, textureId);
		gl.glTexImage2D(GL10.GL_TEXTURE_2D, 0, GL10.GL_RGB, mVideoWidth,
				mVideoHeight, 0, GL10.GL_RGB, GL10.GL_UNSIGNED_SHORT_5_6_5,
				null);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER,
				GL10.GL_LINEAR);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER,
				GL10.GL_LINEAR);
		return textureId;
	}
	
	public void updata(int w, int h, byte[] b){
//		Log.d("GLSurfaceViewTest", "surface updata");
		try {
			if (w > 0 && h > 0 && b.length > 10) {
				// 初始化容器
				if (w != mVideoWidth && h != mVideoHeight) {
					mVideoWidth = w;
					mVideoHeight = h;
					synchronized (this) {
						buffer = ByteBuffer.allocateDirect(b.length);
					}
				}
			}
			synchronized (this) {
				buffer.clear();
				buffer.put(b, 0, b.length);
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			Log.e(LOG, e.toString());
		} catch (GLException e) {
			Log.e(LOG, e.toString());
		} catch (Exception e) {
			// TODO: handle exception
			Log.e(LOG, e.toString());
		}
	}
}
