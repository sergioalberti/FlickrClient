package it.univr.android.flickrclient.controller;

/**
 * Created by user on 5/16/17.
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.annotation.UiThread;
import android.support.annotation.WorkerThread;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import it.univr.android.flickrclient.MVC;
import it.univr.android.flickrclient.model.Model;
import it.univr.android.flickrclient.view.View;

import java.util.ArrayList;
import java.util.Date;


public class Controller {
    private final String TAG = Controller.class.getName();
    private MVC mvc;

    @UiThread
    public void setMVC(MVC mvc){
        this.mvc = mvc;
    }

    @UiThread
    public void callSearchService(Context context, String key){
        mvc.model.clearModel();
        SearchService.doFlickrSearch(context, key);

    }

    //@workerthread?
    public void callThumbTask(ArrayList<Model.FlickrImage> images) {
        for(Model.FlickrImage image : images)
            new ThumbTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, image);
    }

    public void showSearchResults(){
        mvc.forEachView(View::showSearchResults);
    }

    private class ThumbTask extends AsyncTask<Model.FlickrImage, Void, Model.FlickrImage> {

        @Override @WorkerThread
        protected Model.FlickrImage doInBackground(Model.FlickrImage... image) {
            try {
                String CONNECTION_URL = image[0].getThumbURL();
                URL url = new URL(CONNECTION_URL);
                URLConnection connection = url.openConnection();
                connection.setDoInput(true);
                connection.connect();

                InputStream input = connection.getInputStream();
                Bitmap thumb = BitmapFactory.decodeStream(input);

                return image[0].setThumbBitmap(thumb);
            }
            catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override @UiThread
        protected void onPostExecute(Model.FlickrImage im) {
            mvc.model.updateImage(im);
        }

    }
}
