package it.univr.android.flickrclient.view;

/**
 * Created by user on 5/16/17.
 */

import android.app.ListFragment;
import android.os.Bundle;
import android.support.annotation.UiThread;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import it.univr.android.flickrclient.FlickrApplication;
import it.univr.android.flickrclient.MVC;
import it.univr.android.flickrclient.R;
import it.univr.android.flickrclient.model.Model;

import static java.util.Arrays.asList;

public class SearchFragment extends ListFragment implements AbstractFragment {
    private MVC mvc;
    private SearchAdapter searchAdapter = null;

    public SearchFragment(){
    }

    private class SearchAdapter extends ArrayAdapter<Model.FlickrImage> {
        private List<Model.FlickrImage> imagesList;

        private SearchAdapter(List<Model.FlickrImage> fi) {
            super(getActivity(), R.layout.fragment_search_item, fi);
            this.imagesList = fi;
        }

        public void updateSearchResults(){
            imagesList = mvc.model.getSearchResults();
        }

        @Override
        public android.view.View getView(int position, android.view.View convertView, ViewGroup parent) {
            android.view.View row = convertView;

            if (row == null) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                row = inflater.inflate(R.layout.fragment_search_item, parent, false);
            }

            Model.FlickrImage image = imagesList.get(position);
            if (image.getThumbBitmap() != null)
                ((ImageView) row.findViewById(R.id.image_thumb)).setImageBitmap(image.getThumbBitmap());

            ((TextView) row.findViewById(R.id.image_title)).setText(image.getTitle());

            return row;
        }
    }

    @Override @UiThread
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mvc = ((FlickrApplication) getActivity().getApplication()).getMvc();
        onModelChanged();
    }

    @Override @UiThread
    public void onModelChanged() {
        final List<Model.FlickrImage> fiList = mvc.model.getSearchResults();

        if(fiList != null){
            if(searchAdapter == null) {
                searchAdapter = new SearchAdapter(fiList);
                setListAdapter(searchAdapter);
            }
            else{
                searchAdapter.updateSearchResults();
                searchAdapter.notifyDataSetChanged();
            }
        }
        else{
            if(searchAdapter != null)
                searchAdapter.clear();
        }
    }

//    public void clearAdapter(){
//        searchAdapter.clear();
//        searchAdapter = null;
//    }
}