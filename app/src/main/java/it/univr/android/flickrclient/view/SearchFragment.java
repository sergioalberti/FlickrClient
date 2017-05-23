package it.univr.android.flickrclient.view;

/**
 * Created by user on 5/16/17.
 */

import android.app.ListFragment;
import android.media.Image;
import android.os.Bundle;
import android.support.annotation.UiThread;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import it.univr.android.flickrclient.FlickrApplication;
import it.univr.android.flickrclient.MVC;
import it.univr.android.flickrclient.R;
import it.univr.android.flickrclient.model.Model;

import static java.util.Arrays.asList;

public class SearchFragment extends ListFragment implements AbstractFragment {
    private MVC mvc;
    private SearchAdapter sa = null;

    public SearchFragment(){
    }

    private class SearchAdapter extends ArrayAdapter<Model.FlickrImage> {
        private List<Model.FlickrImage> fi;

        private SearchAdapter(ArrayList<Model.FlickrImage> fi) {
            super(getActivity(), R.layout.fragment_search_item, fi);
            this.fi = fi;
        }

        public void updateSearchResults(){
            fi = Arrays.asList(mvc.model.getSearchResults());
        }

        @Override
        public android.view.View getView(int position, android.view.View convertView, ViewGroup parent) {
            android.view.View row = convertView;

            if (row == null) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                row = inflater.inflate(R.layout.fragment_search_item, parent, false);
            }

            Model.FlickrImage image = fi.get(position);
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
        final Model.FlickrImage[] fi = mvc.model.getSearchResults();

        if(fi != null){
            ArrayList<Model.FlickrImage> fiList = new ArrayList<>(asList(mvc.model.getSearchResults()));

            if(sa == null) {
                sa = new SearchAdapter(fiList);
                setListAdapter(sa);
            }
            else{
                sa.updateSearchResults();
                sa.notifyDataSetChanged();
            }
        }
        else{
            if(sa != null)
                sa.clear();
        }

    }

//    public void clearAdapter(){
//        sa.clear();
//        sa = null;
//    }
}