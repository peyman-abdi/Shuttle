package com.simplecity.amp_library.http.ahangify;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by peyman on 4/9/18.
 */
public class AhangifyPagination<T> {
    @SerializedName("data")
    public List<T> data;

    @SerializedName("current_page")
    public int current_page;

    @SerializedName("per_page")
    public int per_page;

    @SerializedName("total")
    public int total;

    @SerializedName("last_page")
    public int last_page;
}
