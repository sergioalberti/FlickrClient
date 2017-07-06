package it.univr.android.flickrclient.view;


import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.UiThread;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import it.univr.android.flickrclient.FlickrApplication;
import it.univr.android.flickrclient.MVC;
import it.univr.android.flickrclient.R;
import it.univr.android.flickrclient.model.Model;

public class ImageFragment extends Fragment implements AbstractFragment {
    private MVC mvc;
    private Model.FlickrImage image;
    private ImageView iv;

    public static final String ENABLED_IMAGE_URL = "enabled_image_url";

    public ImageFragment(){
    }

    @Override @UiThread
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mvc = ((FlickrApplication) getActivity().getApplication()).getMvc();
        image = mvc.model.getEnabled();
        if(image != null && savedInstanceState != null && !savedInstanceState.isEmpty()){
            savedInstanceState.putString(ENABLED_IMAGE_URL, image.getImageURL());
        }
    }

    @Override @UiThread
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_image, container, false);
        iv = (ImageView) view.findViewById(R.id.image_box);

        return view;
    }

    @Override @UiThread
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
    }

    @Override @UiThread
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if(savedInstanceState != null && !savedInstanceState.isEmpty())
            image = mvc.model.getImage(savedInstanceState.getString(ENABLED_IMAGE_URL));

        // maybe the savedIstanceState is still empty, so a nullity check is made. The image could
        // have been downloaded by another ImageFragment call previously, in such case a new download
        // is wasteful (since full image data are stored)

        if (image != null && image.getBitmap(Model.UrlType.FULLSIZE) == null)
            mvc.controller.callDownloadService(getActivity(), image, Model.UrlType.FULLSIZE);

        onModelChanged();
    }

    @Override
    public void onModelChanged() {

        if (image != null && image.getBitmap(Model.UrlType.FULLSIZE) != null)
            iv.setImageBitmap(image.getBitmap(Model.UrlType.FULLSIZE));
    }
}