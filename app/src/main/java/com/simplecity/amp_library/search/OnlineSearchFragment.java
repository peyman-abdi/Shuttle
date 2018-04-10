package com.simplecity.amp_library.search;

import android.content.Context;
import android.os.Bundle;

import com.afollestad.materialdialogs.MaterialDialog;
import com.simplecity.amp_library.R;
import com.simplecity.amp_library.utils.SettingsManager;

import java.util.Arrays;
import java.util.List;

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

    public static MaterialDialog getSearchLimitDialog(Context context, boolean quick_search) {
        int defaultSearch;
        if (quick_search) {
            defaultSearch = SettingsManager.getInstance().getQuickSearchLimit();
        } else {
            defaultSearch = SettingsManager.getInstance().getFullSearchLimit();
        }
        List<String> items = Arrays.asList(context.getResources().getStringArray(R.array.pref_search_limit));

        return new MaterialDialog.Builder(context)
                .title(R.string.pref_online_search_limit_dialog_title)
                .items(items)
                .itemsCallbackSingleChoice(items.indexOf(String.valueOf(defaultSearch)), (dialog, itemView, which, text) -> {
                    if (quick_search) {
                        SettingsManager.getInstance().setQuickSearchLimit(Integer.parseInt(items.get(which)));
                    } else {
                        SettingsManager.getInstance().setFullSearchLimit(Integer.parseInt(items.get(which)));
                    }
                    return false;
                })
                .build();
    }
}
