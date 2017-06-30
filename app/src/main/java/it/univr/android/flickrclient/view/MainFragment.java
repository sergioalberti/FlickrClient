package it.univr.android.flickrclient.view;

/**
 * Created by user on 5/16/17.
 */

import android.os.Bundle;
import android.support.annotation.UiThread;
import android.app.Fragment;
import android.view.LayoutInflater;

import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import it.univr.android.flickrclient.FlickrApplication;
import it.univr.android.flickrclient.MVC;
import it.univr.android.flickrclient.R;

public class MainFragment extends Fragment implements AbstractFragment {
    private MVC mvc;
    private EditText searchKey;
    private Button searchButton;

    public MainFragment(){
    }

    @Override @UiThread
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override @UiThread
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        searchKey = (EditText) view.findViewById(R.id.editSearch);
        searchButton = (Button) view.findViewById(R.id.buttonSearch);

        searchButton.setOnClickListener(__ -> {
            mvc.controller.clearPreviousSearch();
            mvc.controller.callSearchService(getActivity(), searchKey.getText().toString());
            mvc.controller.showSearchResults();
        });

        return view;
    }

    @Override @UiThread
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
    }

    @Override @UiThread
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mvc = ((FlickrApplication) getActivity().getApplication()).getMvc();
        onModelChanged();
    }

    @Override
    public void onModelChanged() {
        //nothing to do
    }
}