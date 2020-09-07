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

package com.nostalgiaemulators.framework.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;

import org.acra.ACRA;
import org.apache.http.conn.util.InetAddressUtils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.view.Display;

import com.nostalgiaemulators.framework.EmulatorApplication;
import com.nostalgiaemulators.framework.R;
import com.nostalgiaemulators.framework.base.EmulatorUtils;
import com.nostalgiaemulators.framework.base.SlotUtils;
import com.nostalgiaemulators.framework.ui.gamegallery.GameDescription;

public class Utils {

	private static final String TAG = "com.nostalgiaemulators.framework.utils.Utils";

	private Utils() {

	}

	private static final int MD5_BYTES_COUNT = 10240;

	public static boolean isAppInstalled(Context context, String packageName) {
		try {
			return (context != null)
					&& (context.getPackageManager().getPackageInfo(packageName,
							PackageManager.GET_ACTIVITIES) != null);
		} catch (Exception e) {
			return false;
		}
	}

	public static String stripExtension(String str) {
		if (str == null)
			return null;
		int pos = str.lastIndexOf(".");
		if (pos == -1)
			return str;
		return str.substring(0, pos);
	}

	public static String getMD5Checksum(File file, boolean useVersion2) {
		InputStream fis = null;
		try {
			fis = new FileInputStream(file);
			return countMD5(fis, useVersion2);
		} catch (IOException e) {
			Log.e(TAG, "", e);
		} finally {
			try {
				if (fis != null)
					fis.close();
			} catch (IOException e) {
			}
		}
		return "";
	}

	
	public static String getMD5Checksum(InputStream zis, boolean useVersion2)
			throws IOException {
		return countMD5(zis, useVersion2);
	}

	private static String countMD5(InputStream is, boolean useVersion2) {
		if (useVersion2) {
			return countMD5_New(is);
		} else {
			return countMD5_Old(is);
		}
	}
	private static String countMD5_New(InputStream is) {
		MessageDigest digest;
		try {
			digest = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			Log.e(TAG, "Exception while getting digest", e);
			return null;
		}

		byte[] buffer = new byte[8192];
		int read;
		try {
			while ((read = is.read(buffer)) > 0) {
				digest.update(buffer, 0, read);
			}
			byte[] md5sum = digest.digest();
			String result = "";

			for (int i = 0; i < md5sum.length; i++) {
				result += Integer.toString((md5sum[i] & 0xff) + 0x100, 16)
						.substring(1);
			}
			return "V2_" + result;

		} catch (IOException e) {
			throw new RuntimeException("Unable to process file for MD5", e);
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				Log.e(TAG, "Exception on closing MD5 input stream", e);
			}
		}

	}

	private static String countMD5_Old(InputStream is) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] buffer = new byte[MD5_BYTES_COUNT];
			int readCount = 0;
			int totalCount = 0;
			int updateCount = 0;

			while ((readCount = is.read(buffer)) != -1) {
				updateCount = readCount;
				if ((totalCount + readCount) > MD5_BYTES_COUNT) {
					updateCount = MD5_BYTES_COUNT - totalCount;
				}
				md.update(buffer, 0, updateCount);
				totalCount += updateCount;
				if (totalCount >= MD5_BYTES_COUNT)
					break;
			}

			if (totalCount >= MD5_BYTES_COUNT) {
				byte[] digest = md.digest();
				String result = "";
				for (int i = 0; i < digest.length; i++) {
					result += Integer.toString((digest[i] & 0xff) + 0x100, 16)
							.substring(1);
				}
				return result;
			} else {
				return "small file";
			}

		} catch (NoSuchAlgorithmException e) {
			Log.e(TAG, "", e);
		} catch (IOException e) {
			Log.e(TAG, "", e);
		}
		return "";
	}

	public static long getCrc(String dir, String entry) {
		try {
			ZipFile zf = new ZipFile(dir);
			ZipEntry ze = zf.getEntry(entry);
			long crc = ze.getCrc();
			return crc;
		} catch (Exception e) {
			return -1;
		}
	}

    // http://stackoverflow.com/questions/6450709/detect-if-opengl-es-2-0-is-available-or-not
	public static boolean checkGL20Support(Context context) {
		EGL10 egl = (EGL10) EGLContext.getEGL();
		EGLDisplay display = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);

		int[] version = new int[2];
		egl.eglInitialize(display, version);

		int EGL_OPENGL_ES2_BIT = 4;
		int[] configAttribs = { EGL10.EGL_RED_SIZE, 4, EGL10.EGL_GREEN_SIZE, 4,
				EGL10.EGL_BLUE_SIZE, 4, EGL10.EGL_RENDERABLE_TYPE,
				EGL_OPENGL_ES2_BIT, EGL10.EGL_NONE };

		EGLConfig[] configs = new EGLConfig[10];
		int[] num_config = new int[1];
		egl.eglChooseConfig(display, configAttribs, configs, 10, num_config);
		egl.eglTerminate(display);
		return num_config[0] > 0;
	}

	public static void extractFile(File zipFile, String entryName,
			File outputFile) throws ZipException, IOException {
		Log.i(TAG,
				"extract " + entryName + " from " + zipFile.getAbsolutePath()
						+ " to " + outputFile.getAbsolutePath());
		ZipFile zipFile2 = new ZipFile(zipFile);
		ZipEntry ze = zipFile2.getEntry(entryName);

    // http://sakra.lanik-mt.eu/app/?page=error&hash=c6cf085ec589d8891cefe9e1a0b7e63c&edit
		if (ze != null) {
			InputStream zis = zipFile2.getInputStream(ze);

			FileOutputStream fos = new FileOutputStream(outputFile);

			byte[] buffer = new byte[2048];
			int count = 0;
			while ((count = zis.read(buffer)) != -1) {
				fos.write(buffer, 0, count);
			}

			zis.close();
			zipFile2.close();
			fos.close();
		}
	}

	
	public static String removeExt(String fileName) {
		int idx = fileName.lastIndexOf('.');
		if (idx > 0) {
			return fileName.substring(0, idx);
		} else {
			return fileName;
		}
	}

	
	public static String getExt(String fileName) {
		int idx = fileName.lastIndexOf('.');
		if (idx > 0) {
			return fileName.substring(idx + 1);
		} else {
			return "";
		}
	}

	public enum ServerType {
		mobile, tablet, tv
	};

	public static ServerType getDeviceType(Context context) {
		if (context.getPackageManager().hasSystemFeature(
				"android.hardware.telephony")) {
			return ServerType.mobile;
		} else if (context.getPackageManager().hasSystemFeature(
				"android.hardware.touchscreen")) {
			return ServerType.tablet;
		} else {
			return ServerType.tv;
		}
	}

	private static Point size = new Point();

	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	public static int getDisplayWidth(Display display) {
		if (Build.VERSION.SDK_INT >= 13) {
			display.getSize(size);
			return size.x;
		} else {
			return display.getWidth();
		}
	}

	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	public static int getDisplayHeight(Display display) {
		if (Build.VERSION.SDK_INT >= 13) {
			display.getSize(size);
			return size.y;
		} else {
			return display.getHeight();
		}
	}

	
	public static boolean isDebuggable(Context ctx) {
		boolean debuggable = false;

		PackageManager pm = ctx.getPackageManager();
		try {
			ApplicationInfo appinfo = pm.getApplicationInfo(
					ctx.getPackageName(), 0);
			debuggable = (0 != (appinfo.flags &= ApplicationInfo.FLAG_DEBUGGABLE));
		} catch (NameNotFoundException e) {
			
		}

		return debuggable;
	}

	public static boolean isNetworkAvailable(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager
				.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}

	public static boolean isWifiAvailable(Context context) {
		WifiManager wifii = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);

		WifiInfo wifiInfo = wifii.getConnectionInfo();
		return (wifii.getWifiState() == WifiManager.WIFI_STATE_ENABLED)
				& (wifiInfo.getIpAddress() != 0);
	}

	public static InetAddress getBroadcastAddress(Context context) {
		String ip = getNetPrefix(context) + ".255";
		try {
			return InetAddress.getByName(ip);
		} catch (UnknownHostException e) {
			return null;
		}
	}

	
	public static class IpInfo {
		public String sAddress;
		public int address;
		public int netmask;

		public void setPrefixLen(int len) {
			netmask = 0;
			int n = 31;
			for (int i = 0; i < len; i++) {
				netmask |= ((int) 1) << (n);
				n--;
			}
			Log.e("netmask", len + "");
			Log.e("netmask", netmask + "");
		}
	}

	@SuppressLint("NewApi")
	private static IpInfo getIP() {
		if (Build.VERSION.SDK_INT > 9) {
			try {
				IpInfo result = new IpInfo();
				List<NetworkInterface> interfaces = Collections
						.list(NetworkInterface.getNetworkInterfaces());
				for (NetworkInterface intf : interfaces) {
					List<InetAddress> addrs = Collections.list(intf
							.getInetAddresses());

					int j = 0;

					for (InetAddress addr : addrs) {
						if (!addr.isLoopbackAddress()) {
							int prefixLen = Integer.MAX_VALUE;
							for (InterfaceAddress address : intf
									.getInterfaceAddresses()) {
								if (address.getNetworkPrefixLength() < prefixLen) {
									prefixLen = address
											.getNetworkPrefixLength();
								}
							}
							String sAddr = addr.getHostAddress().toUpperCase();
							byte[] ip = addr.getAddress();
							int iAddr = ((int) ip[0] << (24)) & 0xFF000000
									| ((int) ip[1] << (16)) & 0x00FF0000
									| ((int) ip[2] << (8)) & 0x0000FF00
									| ((int) ip[3] << (0)) & 0x000000FF;
							boolean isIPv4 = InetAddressUtils
									.isIPv4Address(sAddr);
							if (isIPv4) {
								result.sAddress = sAddr;
								result.address = iAddr;
								result.setPrefixLen(prefixLen);
								return result;
							}
						}
						j++;
					}
				}

			} catch (Exception ex) {
			}
			return new IpInfo();
		}
		return null;
	}

	public static String getNetPrefix(Context context) {
		if (Build.VERSION.SDK_INT < 9) {
			WifiManager wifii = (WifiManager) context
					.getSystemService(Context.WIFI_SERVICE);
			DhcpInfo d = wifii.getDhcpInfo();
			int prefix = d.netmask & d.ipAddress;
			return (prefix & 0xff) + "." + ((prefix >> 8) & 0xff) + "."
					+ ((prefix >> 16) & 0xff);
		} else {
			IpInfo info = getIP();

			int prefix = info.address & info.netmask;
			return ((prefix >> 24) & 0xff) + "." + ((prefix >> 16) & 0xff)
					+ "." + ((prefix >> 8) & 0xff);
		}
	}

	public static String getIpAddr(Context context) {
		if (Build.VERSION.SDK_INT < 9) {
			WifiManager wifiManager = (WifiManager) context
					.getSystemService(Context.WIFI_SERVICE);
			WifiInfo wifiInfo = wifiManager.getConnectionInfo();
			int ip = wifiInfo.getIpAddress();

			String ipString = String.format("%d.%d.%d.%d", (ip & 0xff),
					(ip >> 8 & 0xff), (ip >> 16 & 0xff), (ip >> 24 & 0xff));

			return ipString;
		} else {
			return getIP().sAddress;
		}

	}

	public static Bitmap createScreenshotBitmap(Context context,
			GameDescription game, boolean watermark) {

		String path = SlotUtils.getScreenshotPath(
				EmulatorUtils.getBaseDir(context), game.checksum, 0);
		Bitmap bitmap = BitmapFactory.decodeFile(path);
		int w = bitmap.getWidth();
		int h = bitmap.getHeight();
		int newW = w * 2;
		int newH = h * 2;
		Rect from = new Rect(0, 0, w, h);
		Rect to = new Rect(0, 0, newW, newH);

		Bitmap largeBitmap = Bitmap.createBitmap(bitmap.getWidth() * 2,
				bitmap.getHeight() * 2, Config.ARGB_8888);
		Canvas c = new Canvas(largeBitmap);
		Paint p = new Paint();
		p.setDither(false);
		p.setFilterBitmap(false);
		c.drawBitmap(bitmap, from, to, p);
		if (watermark) {

			p.setColor(context.getResources().getColor(R.color.main_color));
			p.setTextAlign(Align.RIGHT);
			p.setTextSize(30);
			Typeface font = FontUtil.createFontFace(context);
			p.setTypeface(font);
			p.setShadowLayer(4, 0, 0, 0xff000000);
			c.drawText(context.getText(R.string.app_name) + "",
					(float) (newW - 10), (float) (newH - 10), p);
		}
		bitmap.recycle();
		return largeBitmap;
	}

	
	public static boolean isAdvertisingVersion(Activity activity) {
		EmulatorApplication ea = (EmulatorApplication) activity
				.getApplication();
		return ea.isAdvertisingVersion();
	}

	
	public static boolean isIntentAvailable(Context context, String action) {
		final PackageManager packageManager = context.getPackageManager();
		final Intent intent = new Intent(action);
		List<ResolveInfo> list = packageManager.queryIntentActivities(intent,
				PackageManager.MATCH_DEFAULT_ONLY);
		return list.size() > 0;
	}

	@SuppressLint("DefaultLocale")
	public static boolean isBeta(Context context) {
		PackageInfo pInfo;
		try {
			pInfo = context.getPackageManager().getPackageInfo(
					context.getPackageName(), 0);
			return pInfo.versionName.toLowerCase().contains("beta");
		} catch (NameNotFoundException e) {
			return false;
		}

	}

	
	public static void sendSilentException(Context context, Exception e) {
		if (!Utils.isDebuggable(context)) {
			ACRA.getErrorReporter().handleSilentException(e);
		}
	}

}
