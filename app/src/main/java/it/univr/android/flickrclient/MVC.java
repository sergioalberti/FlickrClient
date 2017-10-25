package it.univr.android.flickrclient;

import android.os.Handler;
import android.os.Looper;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import it.univr.android.flickrclient.controller.Controller;
import it.univr.android.flickrclient.model.Model;
import it.univr.android.flickrclient.view.View;

/**
 * defines the application's MVC pattern's data
 */
public class MVC {
    /**
     * points to the application's data
     */
    public final Model model;


    /**
     * point to the application's control blocks to manipulate the data to be shown on views
     */
    public final Controller controller;


    /**
     * defines a list of views
     */
    public final List<View> views = new CopyOnWriteArrayList<View>();

    /**
     * builds a new application context
     * @param model the model to be used
     * @param controller the controller to be used
     */
    public MVC(Model model, Controller controller){
        this.model = model;
        this.controller = controller;

        model.setMVC(this);
        controller.setMVC(this);
    }

    /**
     * adds a View to the current view's list
     * @param v the View that has to be added
     */
    public void register(View v){
        views.add(v);
    }

    /**
     * removes a View from the current view's list
     * @param v the View that has to be removed
     */
    public void unregister(View v){
        views.remove(v);
    }

    /**
     * defines a Task that can be executed on a View
     */
    public interface ViewTask{
        void process(View v);
    }

    /**
     * given a ViewTask, the method executes it on all the Views
     * @param vt the ViewTask to be performed
     */
    public void forEachView(ViewTask vt){
        new Handler(Looper.getMainLooper()).post(() -> {
            for (View v : views)
                vt.process(v);

        });
    }

}
