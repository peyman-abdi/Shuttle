package com.simplecity.amp_library.http.ahangify;

import io.reactivex.Single;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Created by peyman on 4/8/18.
 */
public interface AhangifyService {

    @POST("/api/search")
    Single<AhangifySearchResult> getSearchResult(@Query("page") int page, @Query("limit") int limit, @Body AhangifySearchQuery query);

}
