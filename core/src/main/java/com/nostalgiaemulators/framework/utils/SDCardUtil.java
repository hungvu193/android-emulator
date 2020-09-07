/**
 * Based on post in http://stackoverflow.com/questions/5694933/find-an-external-sd-card-location
 * author: http://stackoverflow.com/users/565319/richard
 * 
 */
package com.nostalgiaemulators.framework.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Scanner;

import android.content.Intent;
import android.os.Environment;

import com.coderplus.filepicker.FilePickerActivity;

public class SDCardUtil {

	public static final String SD_CARD = "sdCard";
	public static final String EXTERNAL_SD_CARD = "externalSdCard";
	private static final String TAG = "com.nostalgiaemulators.framework.utils.SDCardUtil";

	/**
	 * @return A map of all storage locations available
	 */
	public static HashSet<File> getAllStorageLocations() {
		HashSet<String> sdcards = new HashSet<String>();
		sdcards.add("/mnt/sdcard");

		try {
			File mountFile = new File("/proc/mounts");
			if (mountFile.exists()) {
				Scanner scanner = new Scanner(mountFile);
				while (scanner.hasNext()) {
					String line = scanner.nextLine();
					String lineLower = line.toLowerCase(); // neukladat lower
															// primo do
															// line, protoze
															// zbytek line je
															// case sensitive
					if (lineLower.contains("vfat")
							|| lineLower.contains("exfat")
							|| lineLower.contains("fuse")
							|| lineLower.contains("sdcardfs")) {
						String[] lineElements = line.split(" ");
						String path = lineElements[1];
						sdcards.add(path);
					}
				}
			}
		} catch (Exception e) {
			Log.e(TAG, "", e);
		}

		try {
			getSDCardsPaths1(sdcards);
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			getSDCardsPaths2(sdcards);
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			getSDCardsPaths3(sdcards);
		} catch (Exception e) {
			e.printStackTrace();
		}

		HashSet<File> result = new HashSet<File>(sdcards.size());

		for (String mount : sdcards) {
			if (mount == null) {
				continue;
			}

			File root = new File(mount);
			if (root.exists() && root.isDirectory() && root.canRead()) {
				result.add(root);
			}
		}
		return result;
	}

	// http://stackoverflow.com/questions/5694933/find-an-external-sd-card-location
	private static void getSDCardsPaths1(HashSet<String> set) {
		File mnt = new File("/storage");
		if (!mnt.exists()) {
			mnt = new File("/mnt");
		}
		File[] roots = mnt.listFiles(new FileFilter() {

			@Override
			public boolean accept(File pathname) {
				return pathname.isDirectory() && pathname.exists()
						&& pathname.canRead() && !pathname.isHidden()
						&& !isSymlink(pathname);
			}
		});
		for (File root : roots) {
			set.add(root.getAbsolutePath());
		}
	}

	// http://stackoverflow.com/questions/5694933/find-an-external-sd-card-location
	private static void getSDCardsPaths3(HashSet<String> set) {
		final String state = Environment.getExternalStorageState();

		if (Environment.MEDIA_MOUNTED.equals(state)
				|| Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) { // we
																		// can
																		// read
																		// the
																		// External
																		// Storage...
			// Retrieve the primary External Storage:
			final File primaryExternalStorage = Environment
					.getExternalStorageDirectory();

			// Retrieve the External Storages root directory:
			final String externalStorageRootDir;
			if ((externalStorageRootDir = primaryExternalStorage.getParent()) == null) { // no
																							// parent...
				// Log.d(TAG, "External Storage: " + primaryExternalStorage +
				// "\n");
				set.add(primaryExternalStorage.getAbsolutePath());
			} else {
				final File externalStorageRoot = new File(
						externalStorageRootDir);
				final File[] files = externalStorageRoot.listFiles();

				if(files!=null) {
					for (final File file : files) {
						if (file.isDirectory() && file.canRead()) {
							// Log.d(TAG,
							// "External Storage: " + file.getAbsolutePath()
							// + "\n");
							File[] fs = file.listFiles();
							if (fs != null && fs.length > 0) {
								// it is a real directory
								// (not a USB drive)...
								set.add(file.getAbsolutePath());
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Copy from
	 * http://www.javacodegeeks.com/2012/10/android-finding-sd-card-path.html
	 * 
	 * @return
	 */
	private static void getSDCardsPaths2(HashSet<String> set) {
		File file = new File("/system/etc/vold.fstab");
		FileReader fr = null;
		BufferedReader br = null;

		try {
			fr = new FileReader(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		try {
			if (fr != null) {
				String defaultExternalStorage = Environment
						.getExternalStorageDirectory().getAbsolutePath();
				br = new BufferedReader(fr);
				String s = br.readLine();
				while (s != null) {
					if (s.startsWith("dev_mount")) {
						String[] tokens = s.split("\\s");
						String path = tokens[2]; // mount_point
						if (!defaultExternalStorage.equals(path)) {
							set.add(path);
							// break;
						}
					}
					s = br.readLine();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (fr != null) {
					fr.close();
				}
				if (br != null) {
					br.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * http://svn.apache.org/viewvc/commons/proper/io/trunk/src/main/java/org/
	 * apache/commons/io/FileUtils.java?view=markup
	 * 
	 * @param file
	 * @return
	 * @throws IOException
	 */

	public static boolean isSymlink(File file) {
		File canon = null;
		if (file.getParent() == null) {
			canon = file;
		} else {
			try {
				File canonDir = file.getParentFile().getCanonicalFile();
				canon = new File(canonDir, file.getName());
				return !canon.getCanonicalFile()
						.equals(canon.getAbsoluteFile());
			} catch (Exception e) {
				return false;
			}
		}
		return false;
	}

	public static void prepareFilePickerIntent(Intent intent) {
		HashSet<File> locations = SDCardUtil.getAllStorageLocations();
		Iterator<File> it = locations.iterator();
		if (it.hasNext()) {
			intent.putExtra(FilePickerActivity.EXTRA_FILE_PATH, it.next()
					.getAbsolutePath());
		}
	}
}
