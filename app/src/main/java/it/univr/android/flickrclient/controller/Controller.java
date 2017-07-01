package it.univr.android.flickrclient.controller;

/**
 * Created by user on 5/16/17.
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Interpolator;
import android.os.AsyncTask;
import android.renderscript.ScriptGroup;
import android.support.annotation.UiThread;
import android.support.annotation.WorkerThread;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.net.URL;
import java.net.URLConnection;

import it.univr.android.flickrclient.MVC;
import it.univr.android.flickrclient.model.Model;
import it.univr.android.flickrclient.view.View;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Controller {
    private final String TAG = Controller.class.getName();
    private MVC mvc;
    private ArrayList<Future<Model.FlickrImage>> thumbTaskList = new ArrayList<>();
    private static ExecutorService executor = null;


    @UiThread
    public void setMVC(MVC mvc){
        this.mvc = mvc;
    }

    @UiThread
    public void callSearchService(Context context, String key){
        mvc.model.clearModel();
        SearchService.doFlickrSearch(context, key);
    }

    @WorkerThread
    public void callThumbTask(ArrayList<Model.FlickrImage> images) {
        executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        CompletionService<Model.FlickrImage> completionService =
                new ExecutorCompletionService<>(executor);

        for(Model.FlickrImage image : images)
            completionService.submit(new ThumbTask(image));

        try{
            for(int i=0; i<images.size(); i++) {
                Future<Model.FlickrImage> f = completionService.take();
                thumbTaskList.add(f);
                mvc.model.updateImage(f.get());
            }
        }
        catch(InterruptedException e){
            Thread.currentThread().interrupt();
        }
        catch(ExecutionException e){
            Log.d("ThumbTask", "execution exception");
        }
    }

    @WorkerThread
    public void killWorkingTasks(){
        if(executor != null) {
            executor.shutdownNow();
            executor = null;
        }
    }

    public void showSearchResults(){
        mvc.forEachView(View::showSearchResults);
    }

    public void clearPreviousSearch(){
        mvc.forEachView(View::clearPreviousSearch);
    }


    private class ThumbTask implements Callable<Model.FlickrImage> {
        private final Model.FlickrImage image;

        public ThumbTask(Model.FlickrImage image){
            this.image = image;
        }

        @Override
        public Model.FlickrImage call() {
            try {
                if(!executor.isShutdown()) {
                    String CONNECTION_URL = image.getThumbURL();
                    URL url = new URL(CONNECTION_URL);
                    URLConnection connection = url.openConnection();
                    connection.setDoInput(true);
                    connection.connect();

                    InputStream input = connection.getInputStream();
                    Bitmap thumb = BitmapFactory.decodeStream(input);

                    image.setThumbBitmap(thumb);
                }
            }
            catch (IOException e) {
                Log.d("ThumbTask", "download interrupted");
            }

            return image;
        }

    }
}
