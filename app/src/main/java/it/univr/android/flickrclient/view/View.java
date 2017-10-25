package it.univr.android.flickrclient.view;

import android.support.annotation.UiThread;

/**
 * interface used to define a new View in FlickrClient
 */
public interface View {

    /**
     * called when the model changes
     */
    @UiThread
    void onModelChanged();

    /**
     * adds a SearchFragment to the stack to perform a search operation when it's required
     */
    @UiThread
    void showSearchResults();

    /**
     * adds an ImageFragment to the stack to show an image when is requested from a SearchFragment
     */
    @UiThread
    void showFullImage();

}