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

/**
 * Use to manipulate data through the model
 */
public class Controller {
    private final String TAG = Controller.class.getName();
    private MVC mvc;
    private static ExecutorService executor = null;

    public static final String SEARCH_BY_KEY = "Key";
    public static final String SEARCH_MOST_POPULAR = "Most Popular";
    public static final String SEARCH_LAST_UPLOADS = "Last Uploads";
    public static final String SEARCH_BY_AUTHOR = "Author";
    public static final String SEARCH_COMMENTS = "Comments";

    /**
     * the method is used to set the current model's MVC instance
     * @param mvc the mvc instance to be set in the current controller
     */
    @UiThread
    public void setMVC(MVC mvc){
        this.mvc = mvc;
    }

    /**
     * invoked when a new search is required (a search from previous search happens
     * only if the user make a search by author from a current search)
     * @param context points to the application's context
     * @param searchType the model permits many types of search. Thus a type has to be specified:
     *                   # SEARCH_BY_KEY stores to model images given a specific string
     *                   # SEARCH_MOST_POPULAR stores t.m. a collection of the most popular images
     *                   # SEARCH_LAST_UPLOADS s.t.m. a collection of the last uploaded images
     *                   # SEARCH_BY_AUTHOR s.t.m. a collection of the last uploaded images given an
     *                     author
     *                   # SEARCH_COMMENTS s.t.m. a collection of comments (instances of class Comment)
     *                     to be shown when an ImageFragment is shown to the used
     * @param data depending on the searchType specified ahead, few addintional data may be required.
     *             Those can be specified with respect to the required format through the current field
     */
    @UiThread
    public void callSearchService(Context context, String searchType, String data){
        // a new search is performed, thus the model has to be
        // hard reset by purgeModel invocation that clears even oldImageList

        if (searchType.equals(SEARCH_BY_AUTHOR))
            mvc.model.clearModel();
        else if (!searchType.equals(SEARCH_COMMENTS))
            mvc.model.purgeModel();

        SearchService.doFlickrSearch(context, searchType, data);
    }

    /**
     * invoked when a download to the data in the model is required (i.e. when a bitmap download has
     * to be performed).
     * To be called from the UIThread.
     * @param context points to the application's context
     * @param image the image on which a download operation has to be performed
     * @param ut there are two types of downloads performable on an image in this model:
     *           # THUMB performs a download of the bitmap stream that represents the image's thumbnail
     *           # FULL_SIZE performs a download of the whole image as a bitmap stream
     */
    @UiThread
    public void callDownloadService(Context context, FlickrImage image, Model.UrlType ut){
        DownloadService.doDownload(context, image, ut);
    }

    /**
     * an overload to let the invocation callDownloadTask(String) working
     * through different models with no additional operations (i.e. ArrayList casting from single object).
     * to be called from the WorkerThread
     * @param url the image in this overload is get through its URL address
     */
    @WorkerThread
    public void callDownloadTask(String url) {
        callDownloadTask(mvc.model.getImage(url), Model.UrlType.FULLSIZE);
    }

    /**
     * an overload to let the invocation callDownloadTask(FlickrImage, UrlType) working
     * through different models with no additional operations (i.e. ArrayList casting from single object).
     * to be called from the WorkerThread
     * @param image the image which requires to download a bitmap
     * @param ut there are two types of downloads performable on an image in this model:
     *           # THUMB performs a download of the bitmap stream that represents the image's thumbnail
     *           # FULL_SIZE performs a download of the whole image as a bitmap stream
     */
    @WorkerThread
    public void callDownloadTask(FlickrImage image, Model.UrlType ut){
        callDownloadTask(new ArrayList<FlickrImage>(Collections.singletonList(image)), ut);
    }

    /**
     * an overload to let the invocation callDownloadTask(ArrayList<FlickrImage>, UrlType) working
     * through different models with no additional operations (i.e. ArrayList casting from single object).
     * to be called from the WorkerThread
     * @param images the images' collection which requires to download a bitmap
     * @param ut there are two types of downloads performable on an image in this model:
     *           # THUMB performs a download of the bitmap stream that represents the image's thumbnail
     *           # FULL_SIZE performs a download of the whole image as a bitmap stream
     */
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


    /**
     * ends all working tasks, disposes the executor
     */
    @WorkerThread
    public void killWorkingTasks(){
        if(executor != null) {
            executor.shutdownNow();
            executor = null;
        }
    }

    /**
     * wakes all the views allowing to show search results
     */
    @UiThread
    public void showSearchResults(){ mvc.forEachView(View::showSearchResults); }

    /**
     * wakes all the views allowing to show the full size image
     */
    @UiThread
    public void showFullImage(){ mvc.forEachView(View::showFullImage); }

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
