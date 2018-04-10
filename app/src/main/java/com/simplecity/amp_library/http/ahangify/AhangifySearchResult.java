package com.simplecity.amp_library.http.ahangify;

import com.google.gson.annotations.SerializedName;

/**
 * Created by peyman on 4/8/18.
 */
public class AhangifySearchResult {

    @SerializedName("songs")
    public TrackPagination songs;

    public static class TrackPagination extends AhangifyPagination<AhangifyTrack> {
    }

}
