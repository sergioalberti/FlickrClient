package it.univr.android.flickrclient.view;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.UiThread;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import it.univr.android.flickrclient.R;

/**
 * the fragment used as holder in the TabletView
 */
public class TabletFragment extends Fragment implements AbstractFragment {
    /**
     * says the TabletFragment class' name
     */
    public final static String TAG = TabletFragment.class.getName();

    /**
     * initializes the TabletFragment instance
     */
    public TabletFragment(){ }

    @Override @UiThread
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override @UiThread
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        return inflater.inflate(R.layout.fragment_tablet, container, false);
    }

    @Override @UiThread
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        onModelChanged();
    }

    /**
     * called when the model changes
     */
    @Override
    public void onModelChanged() {
        //nothing to do
    }
}