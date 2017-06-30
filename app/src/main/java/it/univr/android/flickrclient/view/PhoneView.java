package it.univr.android.flickrclient.view;

/**
 * Created by user on 5/16/17.
 */

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import it.univr.android.flickrclient.FlickrApplication;
import it.univr.android.flickrclient.MVC;
import it.univr.android.flickrclient.R;

public class PhoneView extends FrameLayout implements View {
    private MVC mvc;

    public PhoneView(Context context) {
        super(context);
    }

    public PhoneView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private FragmentManager getFragmentManager(){
        return ((Activity) getContext()).getFragmentManager();
    }

    private AbstractFragment getFragment(){
        return (AbstractFragment) getFragmentManager().findFragmentById(R.id.phone_view);
    }

    @Override
    protected void onAttachedToWindow(){
        super.onAttachedToWindow();
        mvc = ((FlickrApplication) getContext().getApplicationContext()).getMvc();
        mvc.register(this);

        if(getFragment() == null){
            getFragmentManager()
                    .beginTransaction()
                    .add(R.id.phone_view, new MainFragment())
                    .commit();
        }
    }

    @Override
    protected void onDetachedFromWindow(){
        mvc.unregister(this);
        super.onDetachedFromWindow();
    }

    @Override
    public void onModelChanged() {
        getFragment().onModelChanged();
    }

    public void showSearchResults(){
        //transaction to SearchFragment to show results
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.phone_view, new SearchFragment())
                .addToBackStack(null)
                .commit();
    }

    public void clearPreviousSearch(){
        //nothing to do
        //previous search is not shown when this
        //method is called
    }
}