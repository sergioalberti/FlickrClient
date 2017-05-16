package it.univr.android.flickrclient.view;

/**
 * Created by user on 5/16/17.
 */

import android.app.ListFragment;
import android.os.Bundle;
import android.support.annotation.UiThread;
import android.util.Log;
import android.widget.ArrayAdapter;

import it.univr.android.flickrclient.FlickrApplication;
import it.univr.android.flickrclient.MVC;
import it.univr.android.flickrclient.model.Model;

public class SearchFragment extends ListFragment implements AbstractFragment {
    private MVC mvc;

    public SearchFragment(){

    }

    @Override @UiThread
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mvc = ((FlickrApplication) getActivity().getApplication()).getMvc();
        onModelChanged();
    }

    @Override @UiThread
    public void onModelChanged(){
        Model.FlickrImage[] searchResults = mvc.model.getSearchResults();

        if(searchResults != null) {
            ArrayAdapter<Model.FlickrImage> adapter = new ArrayAdapter<>(this.getActivity(), android.R.layout.simple_list_item_1, searchResults);

            setListAdapter(adapter);
        }
        else
            Log.d("ON MODEL CHANGED","ARRAY IMAGES NULL");
    }
}