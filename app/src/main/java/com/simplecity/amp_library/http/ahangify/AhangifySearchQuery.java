package com.simplecity.amp_library.http.ahangify;

import com.google.gson.annotations.SerializedName;

/**
 * Created by peyman on 4/8/18.
 */

public class AhangifySearchQuery {
    @SerializedName("term")
    public String term;

    @SerializedName("flags")
    public int flags;

    public AhangifySearchQuery(String term) {
        this.term = term;
        this.flags = 0;
    }
    public AhangifySearchQuery(String term, int flags) {
        this.term = term;
        this.flags = flags;
    }
}
