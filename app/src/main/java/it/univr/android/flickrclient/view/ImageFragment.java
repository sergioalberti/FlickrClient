package it.univr.android.flickrclient.view;


import android.animation.ObjectAnimator;
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.UiThread;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import it.univr.android.flickrclient.FlickrApplication;
import it.univr.android.flickrclient.MVC;
import it.univr.android.flickrclient.R;
import it.univr.android.flickrclient.model.FlickrImage;
import it.univr.android.flickrclient.model.Model;

public class ImageFragment extends Fragment implements AbstractFragment {
    private MVC mvc;
    private FlickrImage image;
    private ImageView iv;

    public static final int ANIMATION_DURATION = 150;
    public static final String ENABLED_IMAGE_URL = "enabled_image_url";

    public ImageFragment(){
    }

    @Override @UiThread
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mvc = ((FlickrApplication) getActivity().getApplication()).getMvc();
        image = mvc.model.getEnabled();
        if(image != null && savedInstanceState != null && !savedInstanceState.isEmpty()){
            savedInstanceState.putString(ENABLED_IMAGE_URL, image.getImageURL());
        }
    }

    // following two methods used for share item on action bar

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_image_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_share) {
            // to preserve a saving approach, the intent that was implemented in SearchFragment is
            // not repeated, it is simply recalled by the Fragment TAG

            item.setTitle(SearchFragment.SHARE);
            SearchFragment sf = (SearchFragment) getFragmentManager().findFragmentByTag(SearchFragment.TAG);
            sf.onContextItemSelected(item);

            return true;
        } else
            return super.onOptionsItemSelected(item);
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

        //onModelChanged();
    }

    @Override
    public void onModelChanged() {
        // checking whenever call to this method was invoked to show share intent

        FlickrImage selectedImage = mvc.model.getShared();

        // getDrawable method on ImageView tells whenever the Bitmap is yet set or not. If it's set,
        // it's not set another time (since several calls to onModelChanged are permitted

        if (image != null && image.getBitmap(Model.UrlType.FULLSIZE) != null && iv.getDrawable() == null) {
            iv.setImageBitmap(image.getBitmap(Model.UrlType.FULLSIZE));

            // some animations are used when image appears for the fist time

            ObjectAnimator.ofFloat(iv, "alpha", 0f, 1f).setDuration(ANIMATION_DURATION).start();
            ObjectAnimator.ofFloat(iv, "scaleX", 0.9f, 1f).setDuration(ANIMATION_DURATION).start();
            ObjectAnimator.ofFloat(iv, "scaleY", 0.9f, 1f).setDuration(ANIMATION_DURATION).start();
        }

        // showing share intent

        else if (selectedImage != null && !selectedImage.getAbsoluteURL().equals("") && selectedImage.getBitmap(Model.UrlType.FULLSIZE) != null) {
            // as previously even here, SearchFragment's onModelChanged invocation is recalled after
            // we are sure that the call to this model was made after the DownloadTask has downloaded
            // the full size bitmap that we are interested in

            SearchFragment sf = (SearchFragment) getFragmentManager().findFragmentByTag(SearchFragment.TAG);
            sf.onModelChanged();
        }
    }
}