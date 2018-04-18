package com.simplecity.amp_library.utils;

import android.content.ContextWrapper;

import com.simplecity.amp_library.ShuttleApplication;

import java.io.File;

/**
 * Created by peyman on 4/17/18.
 */
public class TempStorage {

    public static final String TAG = "TempStorage";

    private static TempStorage sInstance;
    public static synchronized TempStorage getInstance() {
        if (sInstance == null) {
            sInstance = new TempStorage();
        }
        return sInstance;
    }

    String persistentTemp;

    private TempStorage() {
        ContextWrapper wrapper = new ContextWrapper(ShuttleApplication.getInstance().getApplicationContext());
        persistentTemp =  wrapper.getFilesDir().getAbsolutePath() + "/temp/";
        File temp = new File(persistentTemp);
        if (!temp.exists()) {
            temp.mkdirs();
        }
    }

    public String getRandomTempFile(String extension) {
        return persistentTemp + StringUtils.getRandomFilename(extension, 20);
    }
}
