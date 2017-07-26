package it.univr.android.flickrclient.view;

/**
 * Created by user on 5/16/17.
 */

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import it.univr.android.flickrclient.FlickrApplication;
import it.univr.android.flickrclient.MVC;
import it.univr.android.flickrclient.R;

public class TabletView extends LinearLayout implements View {
    private MVC mvc;

    public TabletView(Context context) { super(context); }

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

    @Override
    public void onModelChanged() {
        getMainFragment().onModelChanged();
        getSearchFragment().onModelChanged();
    }

    public void showSearchResults(){
        getFragmentManager()
                .beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .replace(R.id.search_fragment, new SearchFragment(), SearchFragment.TAG)
                .addToBackStack(null)
                .commit();
    }

    public void showFullImage(){
        getFragmentManager()
                .beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .replace(R.id.search_fragment, new ImageFragment(), ImageFragment.TAG)
                .addToBackStack(null)
                .commit();
    }

    public void clearPreviousSearch() { }
}
