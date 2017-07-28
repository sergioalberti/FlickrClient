package it.univr.android.flickrclient.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.format.DateFormat;

import java.util.Date;

/**
 * Created by user on 7/27/17.
 */

public class Comment implements Parcelable {
    private final String author;
    private final String authorName;
    private final String comment;
    private final Date date;

    public Comment(String author, String authorName, String comment, Date date) {
        this.author = author;
        this.authorName = authorName;
        this.comment = comment;
        this.date = date;
    }

    public String getAuthorName() { return authorName; }

    public String getComment() { return comment; }

    public Date getDate() { return date; }

    public String toString() {
        return DateFormat.format("dd/MM/yyyy hh:mm", getDate()) + ", " + getAuthorName() + ", " + getComment();
    }

    protected Comment(Parcel in) {
        author = in.readString();
        authorName = in.readString();
        comment = in.readString();
        date = in.readParcelable(Date.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(author);
        dest.writeString(authorName);
        dest.writeString(comment);
        dest.writeValue(date);
    }

    @Override
    public int describeContents() { return 0; }

    public static final Creator<Comment> CREATOR = new Creator<Comment>() {
        @Override
        public Comment createFromParcel(Parcel in) { return new Comment(in); }

        @Override
        public Comment[] newArray(int size) { return new Comment[size]; }
    };
}
