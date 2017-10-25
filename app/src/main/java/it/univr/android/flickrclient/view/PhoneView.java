package it.univr.android.flickrclient.view;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import it.univr.android.flickrclient.FlickrApplication;
import it.univr.android.flickrclient.MVC;
import it.univr.android.flickrclient.R;

/**
 * defines layout to be shown on a smaller portable mobile device
 */
public class PhoneView extends FrameLayout implements View {
    private MVC mvc;

    /**
     * creates a PhoneView's instance
     * @param context the application's context
     */
    public PhoneView(Context context) {
        super(context);
    }

    /**
     * creates a PhoneView's instance
     * @param context the application's context
     * @param attrs the application's attributes
     */
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
                    .add(R.id.phone_view, new MainFragment(), MainFragment.TAG)
                    .commit();
        }
    }

    @Override
    protected void onDetachedFromWindow(){
        mvc.unregister(this);
        super.onDetachedFromWindow();
    }

    /**
     * called when the model changes
     */
    @Override
    public void onModelChanged() {
        getFragment().onModelChanged();
    }

    /**
     * adds a SearchFragment to the stack to perform a search operation
     */
    public void showSearchResults(){
        //transaction to SearchFragment to show results
        getFragmentManager()
                .beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .replace(R.id.phone_view, new SearchFragment(), SearchFragment.TAG)
                .addToBackStack(null)
                .commit();
    }

    /**
     * adds an ImageFragment to the stack to show an image when is requested from a SearchFragment
     */
    public void showFullImage(){
        getFragmentManager()
                .beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .replace(R.id.phone_view, new ImageFragment(), ImageFragment.TAG)
                .addToBackStack(null)
                .commit();
    }


}