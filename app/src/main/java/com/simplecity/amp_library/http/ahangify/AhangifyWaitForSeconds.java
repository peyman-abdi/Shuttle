package com.simplecity.amp_library.http.ahangify;

import com.google.gson.annotations.SerializedName;

/**
 * Created by peyman on 4/15/18.
 */
public class AhangifyWaitForSeconds {
    @SerializedName("message")
    public String message;

    @SerializedName("wait")
    public long wait;

    @SerializedName("status")
    public int status;

    @SerializedName("route")
    public String route;
}
