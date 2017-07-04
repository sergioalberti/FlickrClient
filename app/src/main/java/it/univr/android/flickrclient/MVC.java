package it.univr.android.flickrclient;

import android.os.Handler;
import android.os.Looper;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import it.univr.android.flickrclient.controller.Controller;
import it.univr.android.flickrclient.model.Model;
import it.univr.android.flickrclient.view.View;

/**
 * Created by user on 5/16/17.
 */

public class MVC {
    public final Model model;
    public final Controller controller;
    public final List<View> views = new CopyOnWriteArrayList<View>();

    public MVC(Model model, Controller controller){
        this.model = model;
        this.controller = controller;

        model.setMVC(this);
        controller.setMVC(this);
    }

    public void register(View v){
        views.add(v);
    }

    public void unregister(View v){
        views.remove(v);
    }

    public interface ViewTask{
        void process(View v);
    }

    public void forEachView(ViewTask vt){
        new Handler(Looper.getMainLooper()).post(() -> {
            for (View v : views)
                vt.process(v);

        });
    }

    //this is "wrong" in MVC pattern - FIX NEEDED
    public void forEachViewTMP(Model.FlickrImage image){
        for(View v : views)
            v.showFullImage(image);
    }

}
