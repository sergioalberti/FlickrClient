package it.univr.android.flickrclient.view;

/**
 * Created by user on 5/16/17.
 */

import android.app.ListFragment;
import android.os.Bundle;
import android.support.annotation.UiThread;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import it.univr.android.flickrclient.FlickrApplication;
import it.univr.android.flickrclient.MVC;
import it.univr.android.flickrclient.R;
import it.univr.android.flickrclient.model.Model;

public class SearchFragment extends ListFragment implements AbstractFragment {
    private MVC mvc;
    private SearchAdapter searchAdapter = null;

    public static final int ANIMATION_DURATION = 150;

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
        public int getCount () {
            return imagesList.size();
        }

        @Override
        public android.view.View getView(int position, android.view.View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                convertView = inflater.inflate(R.layout.fragment_search_item, parent, false);
            }

            Model.FlickrImage image = imagesList.get(position);

            ImageView iv = (ImageView) convertView.findViewById(R.id.image_thumb);
            TextView tv = (TextView) convertView.findViewById(R.id.image_title);

            if (image != null && image.getBitmap(Model.UrlType.THUMB) != null)
                iv.setImageBitmap(image.getBitmap(Model.UrlType.THUMB));
            else
               iv.setImageResource(R.drawable.preview);

            tv.setText(position + 1 + ": " + image.getTitle());
            return convertView;
        }
    }

    @Override @UiThread
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mvc = ((FlickrApplication) getActivity().getApplication()).getMvc();
        onModelChanged();

        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Model.FlickrImage selectedImage = (Model.FlickrImage)getListView().getItemAtPosition(position);

                // all images in the local store are disabled, only the selected one is enabled

                mvc.model.reset();
                selectedImage.enable();

                mvc.controller.callDownloadService(getActivity(), selectedImage, Model.UrlType.FULLSIZE);
                mvc.controller.showFullImage();
            }
        });

        getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getActivity(), "Item " + position + " was longclicked", Toast.LENGTH_SHORT).show();
                return true;
            }
        });
    }

    @Override @UiThread
    public void onModelChanged() {
        final List<Model.FlickrImage> fiList = mvc.model.getSearchResults();

        if(fiList != null){
            if(searchAdapter == null) {
                searchAdapter = new SearchAdapter(fiList);
                setListAdapter(searchAdapter);
                setListShown(true); //removes loading spinner and shows the list
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

    @UiThread
    public void clearAdapter(){
        if(searchAdapter != null) {
            searchAdapter.clear();
            searchAdapter = null;
        }
    }
}