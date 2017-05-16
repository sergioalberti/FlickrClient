package it.univr.android.flickrclient.model;

/**
 * Created by user on 5/16/17.
 */

import it.univr.android.flickrclient.MVC;

public class Model {
    private MVC mvc;
    private FlickrImage[] imageList = null;

    public static class FlickrImage {
        private final String name;
        private final String imageURL;

        public FlickrImage(String n, String u){
            this.name = n;
            this.imageURL = u;
        }

        public String getName(){
            return name;
        }

        public String getImageURL(){
            return imageURL;
        }

        public String toString(){
            return name + "\n" + imageURL;
        }
    }

    public void setMVC(MVC mvc){
        this.mvc = mvc;
    }
}