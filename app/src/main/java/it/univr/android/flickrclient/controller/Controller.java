package it.univr.android.flickrclient.controller;

/**
 * Created by user on 5/16/17.
 */

import android.support.annotation.UiThread;

import it.univr.android.flickrclient.MVC;


public class Controller {
    private final String TAG = Controller.class.getName();
    private MVC mvc;

    @UiThread
    public void setMVC(MVC mvc){
        this.mvc = mvc;
    }
}
