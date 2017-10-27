package it.univr.android.flickrclient.controller;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.WorkerThread;

import it.univr.android.flickrclient.FlickrApplication;
import it.univr.android.flickrclient.MVC;
import it.univr.android.flickrclient.model.FlickrImage;

/**
 * permits to call an intent when a new download is required by the user
 */
public class DownloadService extends IntentService {
    private static final String ACTION_FLICKR_DOWNLOAD = "download";
    private static final String SAVED_IMAGE_URL = "image_url";

    /**
     * initialize the current DownloadService instance
     */
    public DownloadService(){
        super("download service");
    }

    /**
     * starts a DownloadService's intent
     * @param context points to the application's context
     * @param image the image on which a download operation has to be performed
     */
    static void doDownload(Context context, FlickrImage image){
        Intent intent = new Intent(context, DownloadService.class);
        intent.setAction(ACTION_FLICKR_DOWNLOAD);
        intent.putExtra(SAVED_IMAGE_URL, image.getImageURL());

        context.startService(intent);
    }

    @WorkerThread
    protected void onHandleIntent(Intent intent) {
        MVC mvc = ((FlickrApplication) getApplication()).getMvc();

        switch(intent.getAction()){
            case ACTION_FLICKR_DOWNLOAD:
                String imageUrl = (String) intent.getSerializableExtra(SAVED_IMAGE_URL);

                mvc.controller.callDownloadTask(imageUrl);
                break;
        }
    }
}
