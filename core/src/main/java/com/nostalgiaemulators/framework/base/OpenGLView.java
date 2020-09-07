// Copyright (c) 2013, 2014, 2015, 2016, 2017, 2018, 2019, 2020 Radek Lzicar & Ales Lanik
//
// This file is part of Nostalgia Emulator Framework.
//
// Nostalgia Emulator Framework is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// Nostalgia Emulator Framework is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with Nostalgia Emulator Framework. If not, see <http://www.gnu.org/licenses/>.

package com.nostalgiaemulators.framework.base;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.view.View;

import com.nostalgiaemulators.framework.Emulator;
import com.nostalgiaemulators.framework.FrameListener;
import com.nostalgiaemulators.framework.GfxProfile;
import com.nostalgiaemulators.framework.R;
import com.nostalgiaemulators.framework.ui.preferences.PreferenceUtil;
import com.nostalgiaemulators.framework.utils.FileUtils;
import com.nostalgiaemulators.framework.utils.GLSLParser;
import com.nostalgiaemulators.framework.utils.Log;

class OpenGLView extends GLSurfaceView implements EmulatorView, FrameListener {
	private static final String TAG = "com.nostalgiaemulators.framework.base.OpenGLView";

	public OpenGLView(EmulatorActivity context, Emulator emulator,
			int defaultTopPadding) {
		super(context);

		setEGLContextClientVersion(2);
		renderer = new Renderer(context, emulator, defaultTopPadding);
		emulator.setFrameListener(this);
		setRenderer(renderer);
		setRenderMode(RENDERMODE_CONTINUOUSLY);
	}

	@Override
	public void onFrameReady() {
		if (quality == 2) {
			renderer.onFrameReady();
		}
	}

	@Override
	public View asView() {
		return this;
	}

	public ViewPort getViewPort() {
		return renderer.getViewPort();
	}

	public void setBenchmark(Benchmark benchmark) {
		renderer.benchmark = benchmark;
	}

	@Override
	public void onResume() {
		super.onResume();
		renderer.onResume();

	}

	int quality;

	@Override
	public void setQuality(int quality) {
		this.quality = quality;
		renderer.setQuality(quality);

	}

	private final Renderer renderer;

	static class Renderer implements GLSurfaceView.Renderer {
		public Renderer(EmulatorActivity context, Emulator emulator,
				int paddingTop) {
			this.emulator = emulator;

			this.texType = context.getTextureType();
			this.context = context.getApplicationContext();
			textureBounds = context.getTextureBounds(emulator);

			this.paddingTop = paddingTop;
		}

		int texType;
		Benchmark benchmark = null;

		public void onResume() {
			if (benchmark != null) {
				benchmark.reset();
			}
		}

		public void setQuality(int quality) {
			this.delayPerFrame = quality == 2 ? 0 : 40;
		}

		private boolean first = true;

		public void onFrameReady() {
			synchronized (lck) {
				lck.notify();
			}
		}

		private Object lck = new Object();

		@Override
		public void onDrawFrame(GL10 unused) {

			if (benchmark != null) {
				benchmark.notifyFrameEnd();
			}
			long endTime = System.currentTimeMillis();
			if (delayPerFrame > 0) {
				long delay = delayPerFrame - (endTime - startTime);
				if (delay > 0) {
					try {
						Thread.sleep(delay);
					} catch (InterruptedException e) {
					}
				}
			} else {
				synchronized (lck) {
					try {
						lck.wait(33);
					} catch (Exception e) {
					}
				}
			}
			if (benchmark != null) {
				benchmark.notifyFrameStart();
			}
			startTime = System.currentTimeMillis();
			if (first && inited) {
				guardedRender();
				first = false;
			} else {
				render();
			}
		}

		private void guardedRender() {
			try {
				render();
			} catch (RuntimeException e) {
				PreferenceUtil.setDefaultShader(context);
				throw e;
			}
		}

		private Context context;
		private int surfaceWidth;
		private int surfaceHeight;
		private boolean inited = false;

		@Override
		public void onSurfaceChanged(GL10 unused, int width, int height) {
			surfaceWidth = width;
			surfaceHeight = height;
			inited = false;
		}

		int vpx;
		int vpy;
		int vpw;
		int vph;

		int glslResId;

		boolean initialize() {
			if (!emulator.isReady()) {
				return false;
			}
			try {
				ViewPort vp = ViewUtils.loadOrComputeViewPort(context,
						emulator, surfaceWidth, surfaceHeight, 0, paddingTop,
						false);

				viewPort = vp;

				Matrix.orthoM(projMatrix, 0, -vp.width / 2, +vp.width / 2,
						-vp.height / 2, +vp.height / 2, -2f, 2f);

				int nvpy = (surfaceHeight - vp.y - vp.height);
				GLES20.glViewport(vp.x, nvpy, vp.width, vp.height);
				vpx = vp.x;
				vpy = nvpy;
				vpw = vp.width;
				vph = vp.height;
				initQuadCoordinates(emulator, vp.width, vp.height);

				checkGlError("initquad");

				GLES20.glUseProgram(defaultProgram);

				checkGlError("use program");

				positionHandle = GLES20.glGetAttribLocation(defaultProgram,
						"aPosition");
				textureHandle = GLES20.glGetUniformLocation(defaultProgram,
						"rubyTexture");
				texCoordHandle = GLES20.glGetAttribLocation(defaultProgram,
						"aTexCoord");

				if (usesNonDefaultShader) {
					positionHandle2 = GLES20.glGetAttribLocation(
							firstPassProgram, "VertexCoord");
					outputSizeHandle2 = GLES20.glGetAttribLocation(
							firstPassProgram, "OutputSize");
					inputSizeHandle2 = GLES20.glGetAttribLocation(
							firstPassProgram, "InputSize");
					textureHandle2 = GLES20.glGetUniformLocation(
							firstPassProgram, "Texture");
					frameCountHandle2 = GLES20.glGetUniformLocation(
							firstPassProgram, "FrameCount");
					textureSizeHandle2 = GLES20.glGetUniformLocation(
							firstPassProgram, "TextureSize");
					texCoordHandle2 = GLES20.glGetAttribLocation(
							firstPassProgram, "TexCoord");
				}

				GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mainTextureId);

				checkGlError("bind texture");

				GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
						GLES20.GL_TEXTURE_MIN_FILTER, firstPassFiltering);
				GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
						GLES20.GL_TEXTURE_MAG_FILTER, firstPassFiltering);

				GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
						GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
				GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
						GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
				GLES20.glPixelStorei(GLES20.GL_PACK_ALIGNMENT, 1);
				GLES20.glPixelStorei(GLES20.GL_UNPACK_ALIGNMENT, 1);

				checkGlError("params");

				startTime = System.currentTimeMillis();
			} catch (RuntimeException e) {
				PreferenceUtil.setDefaultShader(context);
				throw e;
			}
			return true;
		}

		@Override
		public void onSurfaceCreated(GL10 unused, EGLConfig config) {
			try {
				PreferenceUtil.Shader shader = PreferenceUtil
						.getShader(context);
				firstPassFiltering = GLES20.GL_NEAREST;
				filtering = GLES20.GL_LINEAR;
				usesNonDefaultShader = true;

				switch (shader) {
				case NEAREST:
					usesNonDefaultShader = false;
					firstPassFiltering = GLES20.GL_NEAREST;
					scale = 1;
					useFBO = false;
					break;
				case LINEAR:
					usesNonDefaultShader = false;
					firstPassFiltering = GLES20.GL_LINEAR;
					scale = 1;
					useFBO = false;
					break;
				case SUPER_EAGLE:
					glslResId = R.raw.supereagle;
					firstPassFiltering = GLES20.GL_NEAREST;
					filtering = GLES20.GL_LINEAR;
					scale = 2;
					useFBO = true;
					break;
				case SCALE2X:
					glslResId = R.raw.scale2x;
					firstPassFiltering = GLES20.GL_NEAREST;
					filtering = GLES20.GL_LINEAR;
					scale = 2;
					useFBO = true;
					break;
				case SCALE2X_HQ:
					glslResId = R.raw.scale2xhq;
					firstPassFiltering = GLES20.GL_NEAREST;
					filtering = GLES20.GL_LINEAR;
					scale = 2;
					useFBO = true;
					break;
				case SUPER2XSAI:

					glslResId = R.raw.super2xsai;
					firstPassFiltering = GLES20.GL_NEAREST;
					filtering = GLES20.GL_LINEAR;
					scale = 2;
					useFBO = true;
					break;

				default:
					throw new RuntimeException();
				}

				GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

				String vertex = FileUtils.loadTextFromResource(context,
						R.raw.default_vertex);
				String fragment = FileUtils.loadTextFromResource(context,
						R.raw.default_fragment);

				int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertex);
				int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER,
						fragment);

				defaultProgram = GLES20.glCreateProgram();
				GLES20.glAttachShader(defaultProgram, fragmentShader);
				GLES20.glAttachShader(defaultProgram, vertexShader);
				GLES20.glLinkProgram(defaultProgram);

				int[] linkStatus = new int[1];
				GLES20.glGetProgramiv(defaultProgram, GLES20.GL_LINK_STATUS,
						linkStatus, 0);
				if (linkStatus[0] != GLES20.GL_TRUE) {
					String log = GLES20.glGetProgramInfoLog(defaultProgram);
					throw new RuntimeException("glLinkProgram failed. " + log
							+ "#");
				}

				if (usesNonDefaultShader) {
					firstPassProgram = GLES20.glCreateProgram();

					String glsl = FileUtils.loadTextFromResource(context,
							glslResId);
					GLSLParser parser = new GLSLParser(glsl);
					String v = parser.getVertexShader();
					String f = parser.getFragmentShader();

					int vShader = loadShader(GLES20.GL_VERTEX_SHADER, v);
					int fShader = loadShader(GLES20.GL_FRAGMENT_SHADER, f);

					firstPassProgram = GLES20.glCreateProgram();
					GLES20.glAttachShader(firstPassProgram, fShader);
					GLES20.glAttachShader(firstPassProgram, vShader);
					GLES20.glLinkProgram(firstPassProgram);

					int[] linkStatus2 = new int[1];
					GLES20.glGetProgramiv(firstPassProgram,
							GLES20.GL_LINK_STATUS, linkStatus2, 0);
					if (linkStatus2[0] != GLES20.GL_TRUE) {
						String log = GLES20
								.glGetProgramInfoLog(firstPassProgram);
						throw new RuntimeException("glLinkProgram failed. "
								+ log + "#");
					}
				}

				initTextures();
			} catch (RuntimeException e) {
				PreferenceUtil.setDefaultShader(context);
				throw e;
			}
		}

		public ViewPort getViewPort() {
			return viewPort;
		}

		int scale = 2;

		private boolean usesNonDefaultShader = true;
		private boolean useFBO = false;
		int frameCount = 0;

		private void render() {
			if (!inited) {
				inited = initialize();
			}
			if (!inited) {
				return;
			}

			if (usesNonDefaultShader) {
				frameCount++;
				if (useFBO) {
					GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fboId);
					GLES20.glViewport(0, 0, maxTexX * scale, maxTexY * scale);
				}
				GLES20.glUseProgram(firstPassProgram);
				GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

				GLES20.glEnableVertexAttribArray(positionHandle2);
				GLES20.glEnableVertexAttribArray(texCoordHandle2);

				GLES20.glVertexAttribPointer(positionHandle2,
						COORDS_PER_VERTEX, GLES20.GL_FLOAT, false,
						VERTEX_STRIDE, useFBO ? vertexBuffer : vertexBuffer2);

				GLES20.glVertexAttribPointer(texCoordHandle2,
						COORDS_PER_TEXTURE, GLES20.GL_FLOAT, false,
						TEXTURE_STRIDE, useFBO ? textureBuffer : textureBuffer2);

				mvpMatrixHandle = GLES20.glGetUniformLocation(firstPassProgram,
						"MVPMatrix");

				GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false,
						useFBO ? fboProjMatrix : projMatrix, 0);

				GLES20.glActiveTexture(GLES20.GL_TEXTURE1);

				GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mainTextureId);

				GLES20.glUniform1i(frameCountHandle2, frameCount);
				GLES20.glUniform1i(textureHandle2, 1);

				GLES20.glUniform2i(outputSizeHandle2, vpw, vph);
				GLES20.glUniform2i(inputSizeHandle2, maxTexX, maxTexY);

				GLES20.glUniform2f(textureSizeHandle2, TEXTURE_SIZE,
						TEXTURE_SIZE);

				GLES20.glActiveTexture(GLES20.GL_TEXTURE1);

				emulator.renderGfxGL();
				GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length,
						GLES20.GL_UNSIGNED_SHORT, drawListBuffer);
				GLES20.glDisableVertexAttribArray(positionHandle2);
				GLES20.glDisableVertexAttribArray(texCoordHandle2);

				if (useFBO) {
					GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
					GLES20.glUseProgram(defaultProgram);
					GLES20.glViewport(vpx, vpy, vpw, vph);
				}
			}
			boolean defaultShader = !usesNonDefaultShader;
			if (defaultShader || useFBO) {
				GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

				GLES20.glEnableVertexAttribArray(positionHandle);
				GLES20.glEnableVertexAttribArray(texCoordHandle);

				GLES20.glVertexAttribPointer(positionHandle, COORDS_PER_VERTEX,
						GLES20.GL_FLOAT, false, VERTEX_STRIDE, vertexBuffer2);

				GLES20.glVertexAttribPointer(texCoordHandle,
						COORDS_PER_TEXTURE, GLES20.GL_FLOAT, false,
						TEXTURE_STRIDE, textureBuffer2);

				mvpMatrixHandle = GLES20.glGetUniformLocation(defaultProgram,
						"uMVPMatrix");

				GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false,
						projMatrix, 0);

				GLES20.glActiveTexture(GLES20.GL_TEXTURE0);

				int texId = useFBO ? fboTexId : mainTextureId;

				GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texId);

				GLES20.glUniform1i(textureHandle, 0);

				GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
				if (defaultShader) {
					emulator.renderGfxGL();
				}
				GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length,
						GLES20.GL_UNSIGNED_SHORT, drawListBuffer);
				GLES20.glDisableVertexAttribArray(positionHandle);
				GLES20.glDisableVertexAttribArray(texCoordHandle);
			}
		}

		private static void checkGlError(String glOperation) {
			int error;
			while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
				Log.e(TAG, glOperation + ": glError " + error);
				throw new RuntimeException(glOperation + ": glError " + error);
			}
		}

		public static int loadShader(int type, String shaderCode) {
			int shader = GLES20.glCreateShader(type);
			GLES20.glShaderSource(shader, shaderCode);
			GLES20.glCompileShader(shader);
			int[] compiled = new int[1];
			GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
			if (compiled[0] == 0) {
				String log = GLES20.glGetShaderInfoLog(shader);
				throw new RuntimeException("glCompileShader failed. t: " + type
						+ " " + log + "#");
			}
			return shader;
		}

		private int[] textureBounds;

		final int TEXTURE_SIZE = 256;
		int maxTexX;
		int maxTexY;

		private void initQuadCoordinates(Emulator emulator, int vpWidth,
				int vpHeight) {

			if (textureBounds == null) {
				GfxProfile gfx = emulator.getActiveGfxProfile();
				maxTexX = gfx.originalScreenWidth;
				maxTexY = gfx.originalScreenHeight;
			} else {
				maxTexX = textureBounds[0];
				maxTexY = textureBounds[1];
			}

			if (useFBO) {
				Matrix.orthoM(fboProjMatrix, 0, -maxTexX / 2 * scale, +maxTexX
						/ 2 * scale, -maxTexY / 2 * scale,
						+maxTexY / 2 * scale, -2f, 2f);

				quadCoords2 = new float[] { -maxTexX / 2f * scale,
						-maxTexY / 2f * scale, 0,
						-maxTexX / 2f * scale, maxTexY / 2f * scale, 0,
						maxTexX / 2f * scale, maxTexY / 2f * scale, 0,
						maxTexX / 2f * scale, -maxTexY / 2f * scale, 0 };

				textureCoords2 = new float[] { 0,
						0,

						0,
						(maxTexY / (float) (TEXTURE_SIZE)),

						maxTexX / (float) (TEXTURE_SIZE),
						(maxTexY / (float) (TEXTURE_SIZE)),

						(maxTexX / (float) (TEXTURE_SIZE)), 0,

				};
			}
			quadCoords = new float[] { -vpWidth / 2f, -vpHeight / 2f, 0,
					-vpWidth / 2f, vpHeight / 2f, 0,
					vpWidth / 2f, vpHeight / 2f, 0,
					vpWidth / 2f, -vpHeight / 2f, 0 };

			textureCoords = new float[] {
					0,
					(maxTexY / (float) (TEXTURE_SIZE)),
					0,
					0,
					(maxTexX / (float) (TEXTURE_SIZE)),
					0,
					maxTexX / (float) (TEXTURE_SIZE),
					(maxTexY / (float) (TEXTURE_SIZE)),
			};

			if (useFBO) {
				if (vertexBuffer == null) {
					ByteBuffer bb1 = ByteBuffer
							.allocateDirect(quadCoords2.length * 4);
					bb1.order(ByteOrder.nativeOrder());
					vertexBuffer = bb1.asFloatBuffer();
				}
				vertexBuffer.rewind();
				vertexBuffer.put(quadCoords2);
				vertexBuffer.position(0);
			}
			if (vertexBuffer2 == null) {
				ByteBuffer bb1 = ByteBuffer
						.allocateDirect(quadCoords.length * 4);
				bb1.order(ByteOrder.nativeOrder());
				vertexBuffer2 = bb1.asFloatBuffer();
			}

			vertexBuffer2.rewind();
			vertexBuffer2.put(quadCoords);
			vertexBuffer2.position(0);
			if (useFBO) {

				if (textureBuffer == null) {
					ByteBuffer bb2 = ByteBuffer
							.allocateDirect(textureCoords2.length * 4);
					bb2.order(ByteOrder.nativeOrder());
					textureBuffer = bb2.asFloatBuffer();
				}
				textureBuffer.rewind();
				textureBuffer.put(textureCoords2);
				textureBuffer.position(0);
			}
			if (textureBuffer2 == null) {
				ByteBuffer bb2 = ByteBuffer
						.allocateDirect(textureCoords.length * 4);
				bb2.order(ByteOrder.nativeOrder());
				textureBuffer2 = bb2.asFloatBuffer();
			}
			textureBuffer2.rewind();
			textureBuffer2.put(textureCoords);
			textureBuffer2.position(0);

			if (drawListBuffer == null) {
				ByteBuffer dlb = ByteBuffer
						.allocateDirect(drawOrder.length * 2);
				dlb.order(ByteOrder.nativeOrder());
				drawListBuffer = dlb.asShortBuffer();
			}
			drawListBuffer.rewind();
			drawListBuffer.put(drawOrder);
			drawListBuffer.position(0);

		}

		private ViewPort viewPort;

		int fboTexId;
		int fboId;
		int firstPassFiltering = GLES20.GL_NEAREST;
		int filtering = GLES20.GL_NEAREST;

		private void initTextures() {
			int numTextures = 1;

			int[] textureIds = new int[numTextures];

			int textureWidth = TEXTURE_SIZE;
			int textureHeight = TEXTURE_SIZE;

			GLES20.glGenTextures(numTextures, textureIds, 0);

			mainTextureId = textureIds[0];
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mainTextureId);

			GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, texType, textureWidth,
					textureHeight, 0, texType, GLES20.GL_UNSIGNED_BYTE, null);
			checkGlError("textures");

			if (useFBO) {
				int[] fboTextureIds = new int[1];
				int[] fboIds = new int[1];
				GLES20.glGenFramebuffers(1, fboIds, 0);
				GLES20.glGenTextures(1, fboTextureIds, 0);
				fboTexId = fboTextureIds[0];

				fboId = fboIds[0];
				GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, fboTexId);
				GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA,
						TEXTURE_SIZE * scale, TEXTURE_SIZE * scale, 0,
						GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);

				GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
						GLES20.GL_TEXTURE_MIN_FILTER, filtering);
				GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
						GLES20.GL_TEXTURE_MAG_FILTER, filtering);
				GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
						GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
				GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
						GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

				GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fboIds[0]);
				GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER,
						GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D,
						fboTexId, 0);
				GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
			}
		}

		private int textureHandle;
		private int texCoordHandle;
		private int positionHandle;

		private int outputSizeHandle2;
		private int inputSizeHandle2;
		private int textureHandle2;
		private int frameCountHandle2;
		private int texCoordHandle2;
		private int positionHandle2;
		private int textureSizeHandle2;
		private int mvpMatrixHandle;

		private int mainTextureId;

		private long startTime;

		private int defaultProgram;
		private int firstPassProgram;

		private Emulator emulator;

		private float[] quadCoords2;
		private float[] textureCoords2;

		private float[] quadCoords;
		private float[] textureCoords;

		private final short[] drawOrder = { 0, 1, 2, 0, 2, 3 };

		private float[] projMatrix = new float[16];
		private float[] fboProjMatrix = new float[16];

		private FloatBuffer vertexBuffer;
		private FloatBuffer textureBuffer;

		private FloatBuffer vertexBuffer2;
		private FloatBuffer textureBuffer2;

		private ShortBuffer drawListBuffer;

		private int delayPerFrame = 40;

		static final int COORDS_PER_VERTEX = 3;
		static final int COORDS_PER_TEXTURE = 2;

		public final int VERTEX_STRIDE = COORDS_PER_VERTEX * 4;
		public final int TEXTURE_STRIDE = COORDS_PER_TEXTURE * 4;

		private int paddingLeft;
		private int paddingTop;
	}

}
