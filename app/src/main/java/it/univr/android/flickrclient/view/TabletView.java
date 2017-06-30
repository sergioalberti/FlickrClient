package it.univr.android.flickrclient.view;

/**
 * Created by user on 5/16/17.
 */

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.LinearLayout;

import it.univr.android.flickrclient.FlickrApplication;
import it.univr.android.flickrclient.MVC;
import it.univr.android.flickrclient.R;

public class TabletView extends LinearLayout implements View {
    private MVC mvc;
    private SearchFragment searchFragment;

    public TabletView(Context context) {
        super(context);
    }

    public TabletView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

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

        //nascondo il loading spinner al primo avvio (su tablet)
        searchFragment = ((SearchFragment)getSearchFragment());
        searchFragment.setEmptyText("Nothing to show");
        searchFragment.setListShown(true);
    }

    @Override
    protected void onDetachedFromWindow(){
        mvc.unregister(this);
        super.onDetachedFromWindow();
    }

    @Override
    public void onModelChanged() {
        //delegate to both fragments
        getMainFragment().onModelChanged();
        getSearchFragment().onModelChanged();
    }

    public void showSearchResults(){
        //nothing to do
        //tablet always shows search results
    }

    public void clearPreviousSearch(){
        searchFragment.setListShown(false);
        searchFragment.clearAdapter();
    }
}
