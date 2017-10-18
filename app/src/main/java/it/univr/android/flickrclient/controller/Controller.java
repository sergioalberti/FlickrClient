package it.univr.android.flickrclient.controller;

/**
 * Created by user on 5/16/17.
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.UiThread;
import android.support.annotation.WorkerThread;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import it.univr.android.flickrclient.MVC;
import it.univr.android.flickrclient.model.FlickrImage;
import it.univr.android.flickrclient.model.Model;
import it.univr.android.flickrclient.view.View;

public class Controller {
    private final String TAG = Controller.class.getName();
    private MVC mvc;
    private static ExecutorService executor = null;

    public static final String SEARCH_BY_KEY = "Key";
    public static final String SEARCH_MOST_POPULAR = "Most Popular";
    public static final String SEARCH_LAST_UPLOADS = "Last Uploads";
    public static final String SEARCH_BY_AUTHOR = "Author";
    public static final String SEARCH_COMMENTS = "Comments";


    @UiThread
    public void setMVC(MVC mvc){
        this.mvc = mvc;
    }

    @UiThread
    public void callSearchService(Context context, String searchType, String data){
        // if this method is invoked, a new search is required (a search from previous search happens
        // only if the user make a search by author from a current search). Thus the model has to be
        // hard reset by purgeModel invocation that clears even oldImageList

        if (searchType.equals(SEARCH_BY_AUTHOR))
            mvc.model.clearModel();
        else if (!searchType.equals(SEARCH_COMMENTS))
            mvc.model.purgeModel();

        SearchService.doFlickrSearch(context, searchType, data);
    }

    @UiThread
    public void callDownloadService(Context context, FlickrImage image, Model.UrlType ut){
        DownloadService.doDownload(context, image);
    }


    // some overloads to let the invocation working through different models with no additional
    // operations (i.e. ArrayList casting from single object)

    @WorkerThread
    public void callDownloadTask(String url) {
        callDownloadTask(mvc.model.getImage(url), Model.UrlType.FULLSIZE);
    }

    @WorkerThread
    public void callDownloadTask(FlickrImage image, Model.UrlType ut){
        callDownloadTask(new ArrayList<FlickrImage>(Collections.singletonList(image)), ut);
    }

    @WorkerThread
    public void callDownloadTask(ArrayList<FlickrImage> images, Model.UrlType ut) {
        executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        CompletionService<FlickrImage> completionService =
                new ExecutorCompletionService<>(executor);

        for(FlickrImage image : images){
            if ((image.getBitmap(Model.UrlType.THUMB) != null && ut== Model.UrlType.THUMB) ||
                (image.getBitmap(Model.UrlType.FULLSIZE) != null && ut== Model.UrlType.FULLSIZE)
            )
                return;

            completionService.submit(new DownloadTask(image, ut));
        }

        try{
            for(int i = 0; i < images.size(); i++) {
                Future<FlickrImage> f = completionService.take();
                mvc.model.updateImage(f.get());
            }
        }
        catch(InterruptedException e){
            Thread.currentThread().interrupt();
        }
        catch(ExecutionException e){
            Log.d("DownloadTask", e.getMessage());
            e.printStackTrace();
        }
    }



    @WorkerThread
    public void killWorkingTasks(){
        if(executor != null) {
            executor.shutdownNow();
            executor = null;
        }
    }

    @UiThread
    public void showSearchResults(){ mvc.forEachView(View::showSearchResults); }

    @UiThread
    public void clearPreviousSearch(){
        mvc.forEachView(View::clearPreviousSearch);
    }

    @UiThread
    public void showFullImage(){ mvc.forEachView(View::showFullImage); }

    // changed ThumbTask to DownloadTask due to conformity with both thumb size images and original
    // images that have to be downloaded

    private class DownloadTask implements Callable<FlickrImage> {
        private final FlickrImage image;
        private final Model.UrlType ut;

        public DownloadTask(FlickrImage image, Model.UrlType ut){
            this.image = image;
            this.ut = ut;
        }

        @Override
        public FlickrImage call() {
            if (ut == Model.UrlType.FULLSIZE)
                return image.setBitmap(downloadBitmapUtility(image.getImageURL()), Model.UrlType.FULLSIZE);
            else
                return image.setBitmap(downloadBitmapUtility(image.getThumbURL()), Model.UrlType.THUMB);
        }

        // utility used only by the download task to execute the download

        private Bitmap downloadBitmapUtility(String source) {
            try {
                if(!executor.isShutdown()) {
                    String CONNECTION_URL = source;
                    URL url = new URL(CONNECTION_URL);
                    URLConnection connection = url.openConnection();
                    connection.setDoInput(true);
                    connection.connect();

                    InputStream input = connection.getInputStream();
                    return BitmapFactory.decodeStream(input);
                }
            }
            catch (IOException e) {
                Log.d("DownloadBitmapUtility", "download interrupted");
            }
            return null;
        }
    }
}
