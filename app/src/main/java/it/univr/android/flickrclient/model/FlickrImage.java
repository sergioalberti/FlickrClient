package it.univr.android.flickrclient.model;

import android.graphics.Bitmap;

import java.util.ArrayList;


/**
 * stores the data about an image
 */
public class FlickrImage {
    private final String title;
    private final String author;
    private final String id;
    private final String imageURL;
    private final String thumbURL;
    private final String authorName;
    private String absoluteURL;
    private Bitmap thumbBitmap = null;
    private Bitmap fullSizeBitmap = null;
    private boolean isEnabled = false;
    private boolean isShared = false;
    private ArrayList<Comment> comments = null;

    /**
     * creates an image
     * @param title the image's title
     * @param author the author's username
     * @param id the image's id
     * @param imageURL the fullsize image's URL
     * @param thumbURL the thumbnail's URL
     * @param authorName the author's name
     */
    public FlickrImage(String title, String author, String id, String imageURL, String thumbURL, String authorName) {
        this.title = title;
        this.author = author;
        this.id = id;
        this.imageURL = imageURL;
        this.thumbURL = thumbURL;
        this.authorName = authorName;
        this.absoluteURL = "";
    }

    /**
     * shows whenever the current image instance is enabled
     * i.e. whenever an image is selected by the user
     * @return a boolean showing if image is enabled
     */
    public boolean isEnbled() {
        return isEnabled;
    }

    /**
     * shows whenever the current image instance is shared
     * i.e. whenever an image is required to be shared by the user
     * @return a boolean showing if image is required to be shared
     */
    public boolean isShared() {
        return isShared;
    }

    /**
     * enables an image
     * i.e. whenever an image is selected by the user
     */
    public void enable() {
        isEnabled = true;
    }

    /**
     * flags an image as to be shared
     */
    public void share() {
        isShared = true;
    }

    /**
     * disables an image
     */
    public void disable() { isEnabled = false; }

    /**
     * unflags an image as shared
     */
    public void unshare() { isShared = false; }


    /**
     * @return the image's title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @return the author's username
     */
    public String getAuthor() {
        return author;
    }

    /**
     * @return the image's id
     */
    public String getId() {
        return id;
    }

    /**
     * @return the author's name
     */
    public String getAuthorName() {
        return authorName;
    }

    /**
     * if a user requires the current instance to be shared, this field is used
     * to store the image locally in the physical storage space on the device
     * @return the path to the physical storage on the device
     */
    public String getAbsoluteURL() {
        return absoluteURL;
    }

    /**
     * @return the fullsize image's URL
     */
    public String getImageURL() {
        return imageURL;
    }

    /**
     * @return the thumbnail's URL
     */
    public String getThumbURL() {
        return thumbURL;
    }

    /**
     * transform the current instance state to a String
     * @return a String representing the current instance state
     */
    public String toString() {
        return imageURL;
    }

    /**
     * adds a collection of comments to the current instance
     * @param comments a collection of comments
     */
    public void setComments(ArrayList<Comment> comments) { this.comments = comments; }

    /**
     * @return the collection of comments of the current instance
     */
    public ArrayList<Comment> getComments() { return comments; }


    /**
     * @param ut there are two types of downloads performable on an image in this model:
     *           # THUMB performs a download of the bitmap stream that represents the image's thumbnail
     *           # FULL_SIZE performs a download of the whole image as a bitmap stream
     * @return the required Bitmap
     */
    public Bitmap getBitmap(Model.UrlType ut) {
        if (ut == Model.UrlType.FULLSIZE)
            return fullSizeBitmap;
        else
            return thumbBitmap;
    }

    /**
     * @param b the Bitmap to be set
     * @param ut there are two types of downloads performable on an image in this model:
     *           # THUMB performs a download of the bitmap stream that represents the image's thumbnail
     *           # FULL_SIZE performs a download of the whole image as a bitmap stream
     * @return the modified instance
     */
    public FlickrImage setBitmap(Bitmap b, Model.UrlType ut) {
        if (ut == Model.UrlType.FULLSIZE)
            this.fullSizeBitmap = b;
        else
            this.thumbBitmap = b;
        return this;
    }

    /**
     * sets the instance's physical path on the device whenever an image is required to be shared
     * @param absoluteURL the physical path
     */
    public void setAbsoluteURL(String absoluteURL) {
        this.absoluteURL = absoluteURL;
    }

    /**
     * evaluates if two instance points to same data
     * @param other the other instance
     * @return says if two instance points to the same data
     */
    public boolean equals(FlickrImage other) {
        return this.id.equals(other.id);
    }
}
