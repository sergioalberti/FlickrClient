package it.univr.android.flickrclient.controller;

/**
 * Created by user on 5/16/17.
 */

import android.content.Context;
import android.support.annotation.UiThread;

import it.univr.android.flickrclient.MVC;
import it.univr.android.flickrclient.view.View;


public class Controller {
    private final String TAG = Controller.class.getName();
    private MVC mvc;

    @UiThread
    public void setMVC(MVC mvc){
        this.mvc = mvc;
    }

    @UiThread
    public void callSearchService(Context context, String key){
        SearchService.doFlickrSearch(context, key);
    }

    public void showSearchResults(){
        mvc.forEachView(View::showSearchResults);
    }
}
