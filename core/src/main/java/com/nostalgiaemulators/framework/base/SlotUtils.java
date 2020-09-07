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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.net.Uri;

import com.nostalgiaemulators.framework.EmulatorApplication;
import com.nostalgiaemulators.framework.SlotInfo;
import com.nostalgiaemulators.framework.ui.gamegallery.GalleryActivity;
import com.nostalgiaemulators.framework.ui.gamegallery.GameDescription;
import com.nostalgiaemulators.framework.ui.gamegallery.SlotSelectionActivity;
import com.nostalgiaemulators.framework.utils.FileUtils;
import com.nostalgiaemulators.framework.utils.Log;

public class SlotUtils {
	private SlotUtils() {

	}

	public static boolean autoSaveExists(String baseDir, String md5) {
		String path = getSlotPath(baseDir, md5, 0);
		return new File(path).exists();
	}

	private static SlotInfo getAutoSaveSlot(String baseDir, String md5) {
		return getSlot(baseDir, md5, 0);
	}

	public static List<SlotInfo> getSlots(String baseDir, String md5) {
		ArrayList<SlotInfo> result = new ArrayList<SlotInfo>();
		for (int i = 1; i < (NUM_SLOTS + 1); i++) {
			SlotInfo slot = getSlot(baseDir, md5, i);
			result.add(slot);
		}

		return result;
	}

	public static String getSlotPath(String baseDir, String md5, int slot) {
		return getGameDataFilePrefix(baseDir, md5) + slot + SLOT_SUFFIX;
	}

	public static String getScreenshotPath(String baseDir, String md5, int slot) {
		return getGameDataFilePrefix(baseDir, md5) + slot + SCREENSHOT_SUFFIX;
	}

	public static String getGameDataFilePrefix(String baseDir, String md5) {
		return baseDir + "/" + md5 + ".";
	}

	public static final int NUM_SLOTS = 8;

	public static SlotInfo getSlot(String baseDir, String md5, int idx) {
		SlotInfo slot = new SlotInfo();
		String prefix = baseDir + "/" + md5 + ".";

		File file = new File(prefix + idx + SLOT_SUFFIX);
		slot.isUsed = file.exists();
		slot.lastModified = slot.isUsed ? file.lastModified() : -1;
		slot.path = file.getAbsolutePath();
		slot.id = idx;
		if (slot.isUsed) {
			File screenShot = new File(prefix + idx + SCREENSHOT_SUFFIX);
			if (screenShot.exists()) {

    // http://sakra.lanik-mt.eu/app/?page=error&hash=8df9fb90e7c5ba5370602ede165c4ff9
				try {
					Bitmap bitmap = BitmapFactory.decodeFile(screenShot
							.getAbsolutePath());
					if (bitmap != null) {

						Bitmap newScreenshot = Bitmap.createBitmap(
								bitmap.getWidth(), bitmap.getHeight(),
								bitmap.getConfig());
						if (newScreenshot != null) {

							Canvas c = new Canvas(newScreenshot);
							float[] matrix = new float[] {
									0.299f, 0.587f, 0.114f, 0, 0,
									0.299f, 0.587f, 0.114f, 0, 0,
									0.299f, 0.587f, 0.114f, 0, 0,
									0, 0, 0, 0.5f, 0 };
							Paint paint = new Paint();
							paint.setAntiAlias(true);
							paint.setFilterBitmap(true);
							paint.setColorFilter(new ColorMatrixColorFilter(
									new ColorMatrix(matrix)));
							c.drawBitmap(bitmap, 0, 0, paint);
							bitmap.recycle();
							slot.screenShot = newScreenshot;
						}
					}
				} catch (OutOfMemoryError e) {
					Log.e(TAG, "", e);
				}
			}
		}
		return slot;
	}

	private static final String SLOT_SUFFIX = ".state";
	private static final String SCREENSHOT_SUFFIX = ".png";
	private static final String TAG = "com.nostalgiaemulators.framework.base.SlotUtils";

	public static File createPackFile(Activity context, SlotInfo info,
			GameDescription desc) throws IOException, JSONException {
		try {
			String slotFilePath = info.path;

			String packFileSuffix = "."
					+ ((EmulatorApplication) context.getApplication())
							.getPackFileSuffix();

			File slotPath = new File(context.getFilesDir(), "shared");
			slotPath.mkdir();

			File outFile = new File(slotPath,
					desc.getCleanName() + packFileSuffix);

			outFile.createNewFile();
			DataOutputStream dos = new DataOutputStream(new FileOutputStream(
					outFile));
			dos.writeByte(0);
			File slotFile = new File(slotFilePath);

			JSONObject json = new JSONObject();
			JSONObject root = new JSONObject();
			root.put("format-description", "nostalgia slot fileformat");
			root.put("format-version", 2);
			root.put("game-name", desc.name);
			root.put("game-hash", desc.checksum);
			root.put("slot-size", slotFile.length());
			root.put("screenshot-included", false);
			json.put("nness", root);

			dos.writeUTF(json.toString());

			FileInputStream fis = new FileInputStream(slotFile);

			byte[] buffer = new byte[1024];
			int count = 0;
			while ((count = fis.read(buffer)) != -1) {
				dos.write(buffer, 0, count);
			}
			fis.close();
			dos.close();
			return outFile;
		} catch (JSONException e) {
			Log.e(TAG, "Tohle se nema jak stat");
			throw e;
		}

	}

	
	public static String unpackFile(String baseDir, Context context, Uri uri)
			throws IOException, JSONException {

		Log.i(TAG, "import file " + uri.getEncodedPath());
		InputStream is = context.getContentResolver().openInputStream(uri);
		is.read();
		DataInputStream dis = new DataInputStream(is);
		String jsonTxt = dis.readUTF();
		JSONObject json = new JSONObject(jsonTxt);
		JSONObject root = json.getJSONObject("nness");
		String gameName = root.getString("game-name");
		String gameHash = root.getString("game-hash");

		Log.i(TAG, "game:" + gameName + " gameHash:" + gameHash);

		if (!gameHash.startsWith("V2_")) {
			GameDescription game = GalleryActivity.findGameByOldHash(context,
					gameHash);
			if (game != null) {
				gameHash = game.checksum;
			}
		}

		SlotInfo slotInfo = SlotUtils.getAutoSaveSlot(baseDir, gameHash);
		File outFileName = new File(slotInfo.path);

		FileUtils.copyFile(is, outFileName);

		Log.i(TAG, "file copy to " + outFileName.getAbsolutePath());

		return gameHash;
	}
}
