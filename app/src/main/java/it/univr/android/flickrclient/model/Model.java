package it.univr.android.flickrclient.model;

/**
 * Created by user on 5/16/17.
 */

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import net.jcip.annotations.ThreadSafe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import it.univr.android.flickrclient.MVC;
import it.univr.android.flickrclient.view.View;

@ThreadSafe
public class Model {
    private MVC mvc;
    private ArrayList<FlickrImage> imagesList = null;

    // enumeration is used to know which type of image is required
    public enum UrlType {
        THUMB,
        FULLSIZE
    }

    public static class FlickrImage implements Parcelable {
        private final String title;
        private final String imageURL;
        private final String thumbURL;
        private String absoluteURL;
        private Bitmap thumbBitmap = null;
        private Bitmap fullSizeBitmap = null;
        private boolean isEnabled = false;
        private boolean isShared = false;

        public FlickrImage(String title, String imageURL, String thumbURL){
            this.title = title;
            this.imageURL = imageURL;
            this.thumbURL = thumbURL;
            this.absoluteURL = "";
        }

        protected FlickrImage(Parcel in) {
            title = in.readString();
            imageURL = in.readString();
            thumbURL = in.readString();
            thumbBitmap = in.readParcelable(Bitmap.class.getClassLoader());
        }

        public boolean isEnbled(){
            return isEnabled;
        }

        public boolean isShared(){
            return isShared;
        }

        public void enable(){
            isEnabled = true;
        }

        public void share() { isShared = true; }

        public void disable(){
            isEnabled = false;
            isShared = false;
        }

        public String getTitle(){
            return title;
        }

        public String getAbsoluteURL(){
            return absoluteURL;
        }

        public String getImageURL(){
            return imageURL;
        }

        public String getThumbURL(){
            return thumbURL;
        }

        public String toString(){ return imageURL; }

        // changed getThumbBitmap in getBitmap to ensure conformity with two size images model (one
        // used to thumbs, other to full size images)
        public Bitmap getBitmap(UrlType ut) {
            if (ut == UrlType.FULLSIZE)
                return fullSizeBitmap;
            else
                return thumbBitmap;
        }

        public FlickrImage setBitmap(Bitmap b, UrlType ut){
            if (ut == UrlType.FULLSIZE)
                this.fullSizeBitmap = b;
            else
                this.thumbBitmap = b;
            return this;
        }

        public void setAbsoluteURL(String absoluteURL){
            this.absoluteURL = absoluteURL;
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

    // added a method that returns the FlickrImage object from local store. The method is used
    // whenever storing the whole FlickrImage object is too wasteful. Thus only a String is used
    // to point to a FlickrImage in the store.

    public FlickrImage getImage(String imageURL){
        if(imagesList != null) {
            for (int i = 0; i < imagesList.size(); i++) {
                if (imagesList.get(i).getImageURL().equals(imageURL)) {
                    return imagesList.get(i);
                }
            }
        }
        return null;
    }

    // used to get the selected image (i.e. when the image has to be enlarged)
    // the selected image is disabled when calling this method

    public FlickrImage getEnabled(){
        if(imagesList != null) {
            for (int i = 0; i < imagesList.size(); i++) {
                if (imagesList.get(i).isEnbled()) {
                    return imagesList.get(i);
                }
            }
        }
        return null;
    }

    public FlickrImage getShared(){
        if(imagesList != null) {
            for (int i = 0; i < imagesList.size(); i++) {
                if (imagesList.get(i).isShared()) {
                    return imagesList.get(i);
                }
            }
        }
        return null;
    }

    // used to disable all images from the list, once the ImageFragment is closed

    public void reset(){
        if(imagesList != null) {
            for (int i = 0; i < imagesList.size(); i++) {
                imagesList.get(i).disable();
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