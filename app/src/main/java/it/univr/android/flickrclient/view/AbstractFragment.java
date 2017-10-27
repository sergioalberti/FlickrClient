package it.univr.android.flickrclient.view;

import android.support.annotation.UiThread;

/**
 * an abstract representation of a fragment
 */
interface AbstractFragment {

    /**
     * called when the model is changed.
     * has to be called from the UIThread.
     */
    @UiThread
    void onModelChanged();
}