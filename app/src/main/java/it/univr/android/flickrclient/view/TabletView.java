package it.univr.android.flickrclient.view;


import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import it.univr.android.flickrclient.FlickrApplication;
import it.univr.android.flickrclient.MVC;
import it.univr.android.flickrclient.R;

/**
 * defines layout to be shown on a bigger portable mobile device
 */
public class TabletView extends LinearLayout implements View {
    private MVC mvc;

    /**
     * creates a TabletView's instance
     * @param context the application's context
     */
    public TabletView(Context context) { super(context); }

    /**
     * creates a PhoneView's instance
     * @param context the application's context
     * @param attrs the application's attributes
     */
    public TabletView(Context context, AttributeSet attrs) { super(context, attrs);  }

    private FragmentManager getFragmentManager(){
        return ((Activity) getContext()).getFragmentManager();
    }

    private AbstractFragment getMainFragment(){
        return (AbstractFragment) getFragmentManager().findFragmentById(R.id.main_fragment);
    }

    private AbstractFragment getSearchFragment(){
        return (AbstractFragment) getFragmentManager().findFragmentById(R.id.search_fragment);
    }

    @Override
    protected void onAttachedToWindow(){
        super.onAttachedToWindow();
        mvc = ((FlickrApplication) getContext().getApplicationContext()).getMvc();
        mvc.register(this);

        if (getSearchFragment() == null) {
            getFragmentManager()
                    .beginTransaction()
                    .add(R.id.main_fragment, new MainFragment(), MainFragment.TAG)
                    .commit();
            getFragmentManager()
                    .beginTransaction()
                    .add(R.id.search_fragment, new TabletFragment(), TabletFragment.TAG)
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
        getMainFragment().onModelChanged();
        getSearchFragment().onModelChanged();
    }

    /**
     * adds a SearchFragment to the stack to perform a search operation
     */
    public void showSearchResults(){
        getFragmentManager()
                .beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .replace(R.id.search_fragment, new SearchFragment(), SearchFragment.TAG)
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
                .replace(R.id.search_fragment, new ImageFragment(), ImageFragment.TAG)
                .addToBackStack(null)
                .commit();
    }


}
