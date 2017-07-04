package it.univr.android.flickrclient.model;

/**
 * Created by user on 5/16/17.
 */

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.UiThread;
import android.util.Log;

import net.jcip.annotations.ThreadSafe;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import it.univr.android.flickrclient.MVC;
import it.univr.android.flickrclient.view.View;

@ThreadSafe
public class Model {
    private MVC mvc;
    private ArrayList<FlickrImage> imagesList = null;

    public static class FlickrImage implements Parcelable {
        private final String title;
        private final String imageURL;
        private final String thumbURL;
        private Bitmap thumbBitmap = null;

        public FlickrImage(String title, String imageURL, String thumbURL){
            this.title = title;
            this.imageURL = imageURL;
            this.thumbURL = thumbURL;
        }

        protected FlickrImage(Parcel in) {
            title = in.readString();
            imageURL = in.readString();
            thumbURL = in.readString();
            thumbBitmap = in.readParcelable(Bitmap.class.getClassLoader());
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
            return this.imageURL.equals(other.imageURL);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(title);
            dest.writeString(imageURL);
            dest.writeString(thumbURL);
            dest.writeValue(thumbBitmap);
        }

        public static final Creator<FlickrImage> CREATOR = new Creator<FlickrImage>() {
            @Override
            public FlickrImage createFromParcel(Parcel in) {
                return new FlickrImage(in);
            }

            @Override
            public FlickrImage[] newArray(int size) {
                return new FlickrImage[size];
            }
        };
    }

    public void setMVC(MVC mvc){
        this.mvc = mvc;
    }

    public void storeSearchResults(ArrayList<FlickrImage> result){
        imagesList = result;
        mvc.forEachView(View::onModelChanged);
    }

    public void updateImage(FlickrImage image){
        //controllo imageList!=null perchè se faccio una nuova ricerca
        //mentre sta scaricando i thumb di quella precedente imageList
        //vale null e mi dà errore quando richiama updateImage
        if(imagesList != null) {
            for (int i = 0; i < imagesList.size(); i++) {
                if (imagesList.get(i).equals(image)) {
                    imagesList.set(i, image);
                    mvc.forEachView(View::onModelChanged);
                    break;
                }
            }
        }
    }

    public List<FlickrImage> getSearchResults(){
        if(imagesList != null)
            return Collections.synchronizedList(imagesList);
        return null;
    }


    public void clearModel(){
        imagesList = null;
    }

}