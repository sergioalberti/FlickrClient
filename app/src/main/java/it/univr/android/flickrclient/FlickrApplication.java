package it.univr.android.flickrclient;

import android.app.Application;

import it.univr.android.flickrclient.controller.Controller;
import it.univr.android.flickrclient.model.Model;

/**
 * Created by user on 5/16/17.
 */

public class FlickrApplication extends Application {
    private MVC mvc;

    @Override
    public void onCreate(){
        super.onCreate();
        mvc = new MVC(new Model(), new Controller());
    }

    public MVC getMvc(){
        return mvc;
    }
}
