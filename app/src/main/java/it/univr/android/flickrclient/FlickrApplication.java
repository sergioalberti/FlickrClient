package it.univr.android.flickrclient;

import android.app.Application;

import it.univr.android.flickrclient.controller.Controller;
import it.univr.android.flickrclient.model.Model;


/**
 * instantiate a new FlickrClient's instance
 */
public class FlickrApplication extends Application {
    private MVC mvc;

    @Override
    public void onCreate(){
        super.onCreate();
        mvc = new MVC(new Model(), new Controller());
    }

    /**
     * @return the current MVC pattern's instance
     */
    public MVC getMvc(){
        return mvc;
    }
}
