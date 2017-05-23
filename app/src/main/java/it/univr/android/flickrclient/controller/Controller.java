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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import it.univr.android.flickrclient.MVC;
import it.univr.android.flickrclient.model.Model;
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

    //@workerthread?
    public void callThumbTask(Model.FlickrImage[] images) {
        //for(Model.FlickrImage image : images)
        new ThumbTask().execute(images[1]);
    }

    public void showSearchResults(){
        mvc.forEachView(View::showSearchResults);
    }

    private class ThumbTask extends AsyncTask<Model.FlickrImage, Void, Void> {

        @Override @WorkerThread
        protected Void doInBackground(Model.FlickrImage... image) {
            try {
                String CONNECTION_URL = image[0].getThumbURL();
                URL url = new URL(CONNECTION_URL);
                URLConnection connection = url.openConnection();
                connection.setDoInput(true);
                connection.connect();

                InputStream input = connection.getInputStream();
                Bitmap thumb = BitmapFactory.decodeStream(input);
                mvc.model.updateImage(image[0].setThumbBitmap(thumb));
            }
            catch (IOException e) {
                e.printStackTrace();
                return null;
            }

            return null;
        }

        //TODO
        //idea: mantenere il .clone() nel model
        //e fare nel model un metodo che permette di salvare cambiamenti ad una sola immagine
        //ogni volta che un asynctask finsice di scaricare la thumb, modifica quella singola
        //immagine tramite il metodo nel model. questo chiama onModelChanged che aggiorna
        //l'adapter nella view.
//        @Override @UiThread
//        protected void onPostExecute(BigInteger[] factors) {
//            mvc.model.storeFactorization(n, factors);
//            Log.d(TAG, "computed " + Arrays.toString(factors));
//        }

    }
}
