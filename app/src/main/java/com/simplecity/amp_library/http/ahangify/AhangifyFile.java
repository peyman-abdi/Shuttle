package com.simplecity.amp_library.http.ahangify;

import com.google.gson.annotations.SerializedName;

/**
 * Created by peyman on 4/16/18.
 */
public class AhangifyFile {

    public static final int ASK = 0;
    public static final int BEST = 1;
    public static final int MEDIUM = 2;
    public static final int SMALLEST = 3;

    @SerializedName("id")
    public long id;
    @SerializedName("label")
    public String label;
    @SerializedName("size")
    public long size;
    @SerializedName("quality")
    public String quality;
}
