package it.univr.android.flickrclient.view;

/**
 * Created by user on 5/16/17.
 */

import android.support.annotation.UiThread;

public interface View {

    @UiThread
    void onModelChanged();

    @UiThread
    void showSearchResults();

    @UiThread
    void clearPreviousSearch();
}