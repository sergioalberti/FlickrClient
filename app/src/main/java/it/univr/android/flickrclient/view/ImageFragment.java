package it.univr.android.flickrclient.view;


import android.os.Bundle;
import android.support.annotation.UiThread;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;

import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import it.univr.android.flickrclient.FlickrApplication;
import it.univr.android.flickrclient.MVC;
import it.univr.android.flickrclient.R;
import it.univr.android.flickrclient.model.Model;

public class ImageFragment extends Fragment implements AbstractFragment {
    private MVC mvc;
    private Model.FlickrImage image;

    public ImageFragment(){
    }

    public static ImageFragment newInstance(Model.FlickrImage image){
        ImageFragment newFragment = new ImageFragment();

        Bundle args = new Bundle();
        args.putParcelable("image", image);
        newFragment.setArguments(args);
        return newFragment;
    }

    @Override @UiThread
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        image = getArguments().getParcelable("image");
        if (image == null)
            Log.d("ImageFragment", "image null");
        else{
            Log.d("title", image.getTitle());
            Log.d("image url", image.getImageURL());
            Log.d("thumb url", image.getThumbURL());
        }
    }

    @Override @UiThread
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_image, container, false);

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