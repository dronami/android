package com.dronami.gametemplate;

// Based on the framework described in
// Beginning Android Games (Second Edition)
// by Mario Zechner and Robert Green

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.Environment;
import android.preference.PreferenceManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class AndroidFileIO implements FileIO {
    Context context;
    AssetManager assetManager;
    String externalStoragePath;

    public AndroidFileIO(Context context) {
        this.context = context;
        this.assetManager = context.getAssets();
        this.externalStoragePath = Environment.getExternalStorageDirectory().getAbsolutePath() +
                File.separator;
    }

    public InputStream readAsset(String fileName) throws IOException {
        return assetManager.open(fileName);
    }

    public InputStream readFile(String fileName) throws IOException {
        return new FileInputStream(externalStoragePath + fileName);
    }

    public OutputStream writeFile(String fileName) throws IOException {
        return new FileOutputStream(externalStoragePath + fileName);
    }

    public SharedPreferences getPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }
}
