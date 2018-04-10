package com.simplecity.amp_library.search;

import android.os.Bundle;

/**
 * Created by peyman on 4/9/18.
 * */
public class OnlineSearchFragment extends SearchFragment {
    public static OnlineSearchFragment newOnlineInstance(String query) {
        Bundle args = new Bundle();
        args.putString(ARG_QUERY, query);
        OnlineSearchFragment fragment = new OnlineSearchFragment();
        fragment.setArguments(args);
        fragment.onlineMode = true;
        return fragment;
    }
}
