package it.univr.android.flickrclient.model;

/**
 * Created by user on 5/16/17.
 */

import android.graphics.Bitmap;
import android.support.annotation.UiThread;

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

        public String toString(){
            return title + "\n" + imageURL;
        }

        public void setThumbBitmap(Bitmap b){
            this.thumbBitmap = b;
        }

        public Bitmap getThumbBitmap(){
            return thumbBitmap;
        }
    }

    public void setMVC(MVC mvc){
        this.mvc = mvc;
    }

    public void storeSearchResults(FlickrImage[] result){
        imageArray = result;
        mvc.forEachView(View::onModelChanged);
    }

    @UiThread
    public FlickrImage[] getSearchResults(){
        return imageArray;
        //return imageArray.clone();
    }
}