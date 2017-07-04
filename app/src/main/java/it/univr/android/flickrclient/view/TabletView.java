package it.univr.android.flickrclient.view;

/**
 * Created by user on 5/16/17.
 */

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.text.Layout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import it.univr.android.flickrclient.FlickrApplication;
import it.univr.android.flickrclient.MVC;
import it.univr.android.flickrclient.R;
import it.univr.android.flickrclient.model.Model;

public class TabletView extends LinearLayout implements View {
    private MVC mvc;
    private SearchFragment searchFragment;
    private boolean imageFragmentActive = false;

    public TabletView(Context context) {
        super(context);
        getFragmentManager().beginTransaction().add(R.id.main_fragment, new MainFragment()).commit();
        getFragmentManager().beginTransaction().add(R.id.search_fragment, new SearchFragment()).commit();
    }

    public TabletView(Context context, AttributeSet attrs) {
        super(context, attrs);
        getFragmentManager().beginTransaction().add(R.id.main_fragment, new MainFragment()).commit();
        getFragmentManager().beginTransaction().add(R.id.search_fragment, new SearchFragment()).commit();
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
        getMainFragment().onModelChanged();

        //chiamo onModelChanged sul SearchFragment solo se
        //non ho attivo l'ImageFragment
        if(!imageFragmentActive)
            getSearchFragment().onModelChanged();
    }

    public void showSearchResults(){
        //se è attivo l'ImageFragment per mostrare i risultati
        //della ricerca devo sostituirlo con un SearchFragment
        if(imageFragmentActive){
            getFragmentManager()
                    .beginTransaction()
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .replace(R.id.search_fragment, new SearchFragment())
                    .commit();

            imageFragmentActive = false;
        }
    }

    public void showFullImage(Model.FlickrImage image){
        imageFragmentActive = true;

        getFragmentManager()
                .beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .replace(R.id.search_fragment, ImageFragment.newInstance(image))
                .addToBackStack(null)
                .commit();
    }

    public void clearPreviousSearch(){
        if(!imageFragmentActive)
            searchFragment.setListShown(false);

        searchFragment.clearAdapter();
    }
}
