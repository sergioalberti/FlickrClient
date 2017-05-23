package it.univr.android.flickrclient.model;

/**
 * Created by user on 5/16/17.
 */

import android.graphics.Bitmap;
import android.support.annotation.UiThread;
import android.util.Log;

import it.univr.android.flickrclient.MVC;
import it.univr.android.flickrclient.view.View;

public class Model {
    private MVC mvc;
    private FlickrImage[] imageArray = null;

    public static class FlickrImage {
        private final String title;
        private final String imageURL;
        private final String thumbURL;
        private Bitmap thumbBitmap = null;
        static int COUNT = 0;

        public FlickrImage(String title, String imageURL, String thumbURL){
            this.title = title;
            this.imageURL = imageURL;
            this.thumbURL = thumbURL;
        }

        public String getTitle(){
            return title;
        }

        public String getImageURL(){
            return imageURL;
        }

        public String getThumbURL(){
            return thumbURL;
        }

        public String toString(){ return imageURL; }

        public FlickrImage setThumbBitmap(Bitmap b){
            this.thumbBitmap = b;
            return this;
        }

        public Bitmap getThumbBitmap(){
            return thumbBitmap;
        }

        public boolean equals(FlickrImage other){
            return this.imageURL == other.imageURL;
        }
    }

    public void setMVC(MVC mvc){
        this.mvc = mvc;
    }

    public void storeSearchResults(FlickrImage[] result){
        imageArray = result;
        mvc.forEachView(View::onModelChanged);
    }

    public void updateImage(FlickrImage image){
        for (int i = 0; i < imageArray.length; i++)
            if (imageArray[i].equals(image)) {
                imageArray[i] = image;
                mvc.forEachView(View::onModelChanged);
                break;
            }
    }

    @UiThread
    public FlickrImage[] getSearchResults(){
        if (imageArray != null)
            return imageArray.clone();
        return null;
    }
}