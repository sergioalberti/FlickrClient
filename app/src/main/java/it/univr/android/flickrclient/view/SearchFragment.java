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

import it.univr.android.flickrclient.FlickrApplication;
import it.univr.android.flickrclient.MVC;
import it.univr.android.flickrclient.R;
import it.univr.android.flickrclient.model.Model;

public class SearchFragment extends ListFragment implements AbstractFragment {
    private MVC mvc;

    public SearchFragment(){
    }

    private class SearchAdapter extends ArrayAdapter<Model.FlickrImage> {
        private Model.FlickrImage[] fi = mvc.model.getSearchResults();

        private SearchAdapter(Model.FlickrImage[] fi) {
            super(getActivity(), R.layout.fragment_search_item, fi);
            this.fi = fi;
        }

        @Override
        public android.view.View getView(int position, android.view.View convertView, ViewGroup parent) {
            android.view.View row = convertView;

            if (row == null) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                row = inflater.inflate(R.layout.fragment_search_item, parent, false);
            }

            Model.FlickrImage image = fi[position];
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
        if(fi != null)
            setListAdapter(new SearchAdapter(fi));
    }
}