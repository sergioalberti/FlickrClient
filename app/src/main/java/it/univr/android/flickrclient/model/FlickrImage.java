package it.univr.android.flickrclient.model;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by user on 7/11/17.
 */
public class FlickrImage implements Parcelable {
    private final String title;
    private final String author;
    private final String imageURL;
    private final String thumbURL;
    private final String authorName;
    private String absoluteURL;
    private Bitmap thumbBitmap = null;
    private Bitmap fullSizeBitmap = null;
    private boolean isEnabled = false;
    private boolean isShared = false;

    public FlickrImage(String title, String author, String imageURL, String thumbURL, String authorName) {
        this.title = title;
        this.author = author;
        this.imageURL = imageURL;
        this.thumbURL = thumbURL;
        this.authorName = authorName;
        this.absoluteURL = "";
    }

    protected FlickrImage(Parcel in) {
        title = in.readString();
        author = in.readString();
        imageURL = in.readString();
        thumbURL = in.readString();
        authorName = in.readString();
        absoluteURL = in.readString();
        thumbBitmap = in.readParcelable(Bitmap.class.getClassLoader());
        fullSizeBitmap = in.readParcelable(Bitmap.class.getClassLoader());
        isEnabled = in.readByte() != 0;
        isShared = in.readByte() != 0;
    }

    public boolean isEnbled() {
        return isEnabled;
    }

    public boolean isShared() {
        return isShared;
    }

    public void enable() {
        isEnabled = true;
    }

    public void share() {
        isShared = true;
    }

    public void disable() {
        isEnabled = false;
        isShared = false;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getAuthorName() {
        return authorName;
    }

    public String getAbsoluteURL() {
        return absoluteURL;
    }

    public String getImageURL() {
        return imageURL;
    }

    public String getThumbURL() {
        return thumbURL;
    }

    public String toString() {
        return imageURL;
    }

    // changed getThumbBitmap in getBitmap to ensure conformity with two size images model (one
    // used to thumbs, other to full size images)
    public Bitmap getBitmap(Model.UrlType ut) {
        if (ut == Model.UrlType.FULLSIZE)
            return fullSizeBitmap;
        else
            return thumbBitmap;
    }

    public FlickrImage setBitmap(Bitmap b, Model.UrlType ut) {
        if (ut == Model.UrlType.FULLSIZE)
            this.fullSizeBitmap = b;
        else
            this.thumbBitmap = b;
        return this;
    }

    public void setAbsoluteURL(String absoluteURL) {
        this.absoluteURL = absoluteURL;
    }

    public boolean equals(FlickrImage other) {
        return this.imageURL.equals(other.imageURL);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(author);
        dest.writeString(imageURL);
        dest.writeString(thumbURL);
        dest.writeString(authorName);
        dest.writeString(absoluteURL);
        dest.writeValue(thumbBitmap);
        dest.writeValue(fullSizeBitmap);
        dest.writeByte((byte) (isEnabled ? 1 : 0));
        dest.writeByte((byte) (isShared ? 1 : 0));
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