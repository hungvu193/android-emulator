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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;

import com.nostalgiaemulators.framework.R;
import com.nostalgiaemulators.framework.utils.DialogUtils;
import com.nostalgiaemulators.framework.utils.Log;
import com.nostalgiaemulators.framework.utils.ZipUtils;

import java.io.File;
import java.util.ArrayList;

import androidx.core.content.FileProvider;

public class LiteExportActivity extends Activity {

    private static final String TAG = "com.nostalgiaemulators.framework.base.LiteExportActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "Start exporting");
        exportTask.execute();
    }

    boolean isUsingExternalDir;

    public static String getExportDir(Context context, boolean externalFolder) {
        if (!externalFolder) {
            File sTarget = new File(context.getFilesDir(), "shared");
            return new File(sTarget, EmulatorInfoHolder.getInfo()
                    .getName() + "_Export").getAbsolutePath();
        } else {
            String sTarget = Environment.getExternalStorageDirectory()
                    .getAbsolutePath();
            return new File(sTarget, EmulatorInfoHolder.getInfo()
                    .getName() + "_Export").getAbsolutePath();
        }
    }

    public static boolean export(Context context, String targetPath, boolean isUsingExternalDir) {
        try {
            File[] fs = new File(targetPath).listFiles();
            if (fs != null) {
                for (File f : fs) {
                    f.delete();
                }
            }
            MigrationManager.doExport(context, targetPath);
            File[] files = new File(targetPath).listFiles();
            ArrayList<String> paths = new ArrayList<>();
            if (files != null) {
                for (File f : files) {
                    paths.add(f.getAbsolutePath());
                }
            }
            if (!isUsingExternalDir) {
                ZipUtils.zip(paths.toArray(new String[paths.size()]), targetPath + "/data.zip");
            }
            Thread.sleep(1000);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    @SuppressLint("StaticFieldLeak")
    AsyncTask<Void, Void, Boolean> exportTask = new AsyncTask<Void, Void, Boolean>() {

        String targetPath;
        ProgressDialog pd;

        protected void onPreExecute() {

            isUsingExternalDir = true;
            targetPath = getExportDir(getApplicationContext(), true);
            new File(targetPath).mkdirs();


            if (!new File(targetPath).exists()) {
                targetPath = getExportDir(getApplicationContext(), false);
                isUsingExternalDir = false;
                new File(targetPath).mkdirs();
            }

            pd = new ProgressDialog(LiteExportActivity.this);
            pd.setMessage(getString(R.string.export_lite_dialog));
            pd.show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            return export(LiteExportActivity.this, targetPath, isUsingExternalDir);
        }

        protected void onCancelled() {
            DialogUtils.dismiss(pd);
            LiteExportActivity.this.setResult(Activity.RESULT_CANCELED);
            finish();
        }

        protected void onPostExecute(Boolean result) {
            DialogUtils.dismiss(pd);
            if (result) {
                Intent data = new Intent();
                if (!isUsingExternalDir) {
                    File dataFile = new File(targetPath + "/data.zip");
                    Uri uri = FileProvider.getUriForFile(getApplicationContext(), getApplicationContext().getPackageName() + ".provider", dataFile);
                    String toPackage = getApplicationContext().getPackageName().replace("lite", "full");
                    grantUriPermission(toPackage, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    data.setData(uri);
                } else {
                    data.putExtra("PATH", targetPath);
                }
                LiteExportActivity.this.setResult(Activity.RESULT_OK, data);
            } else {
                LiteExportActivity.this.setResult(Activity.RESULT_CANCELED);
            }
            finish();
        }

    };

}
