package com.simplecity.amp_library.http.ahangify;

import com.google.gson.annotations.SerializedName;
import com.simplecity.amp_library.model.Song;

/**
 * Created by peyman on 4/9/18.
 */
public class AhangifyTrack {

    @SerializedName("id")
    public long id;

    @SerializedName("uid")
    public String uid;

    @SerializedName("title")
    public String title;

    @SerializedName("artist")
    public AhangifyArtist artist;

    @SerializedName("album")
    public AhangifyAlbum album;

    @SerializedName("files")
    public AhangifyFile[] files;

    @SerializedName("duration")
    public long duration;

    public Song getSong() {
        Song song = new Song(null);

        song.id = id;
        song.name = title;
        song.duration = duration;

        song.artistName = artist != null? artist.title: "";
        song.artistId = artist != null? artist.id: 0;

        song.albumName = album != null? album.title: "";
        song.albumId = album != null? album.id: 0;

        song.albumArtistName = song.artistName;

        song.year = 0;
        song.track = 0;
        song.dateAdded = (int) (System.currentTimeMillis() / 1000);
        song.bookMark = 0;
        song.isPodcast = false;
        song.playCount = 10;

        song.path = "online";

        song.onlineTrack = this;

        return song;
    }


}
