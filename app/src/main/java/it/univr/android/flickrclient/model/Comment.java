package it.univr.android.flickrclient.model;

import android.text.format.DateFormat;

import java.util.Date;


/**
 * stores the data about a user's comment
 */
public class Comment {
    private final String authorName;
    private final String comment;
    private final Date date;

    /**
     * creates a new comment
     * @param authorName author's name
     * @param comment comment's content
     * @param date comment's date
     */
    public Comment(String authorName, String comment, Date date) {
        this.authorName = authorName;
        this.comment = comment;
        this.date = date;
    }

    /**
     * @return the author's name
     */
    public String getAuthorName() { return authorName; }

    /**
     * @return the comment's content
     */
    public String getComment() { return comment; }


    /**
     * transform the current instance state to a String
     * @return a String representing the current instance state
     */
    public String toString() {
        return DateFormat.format("dd/MM/yyyy hh:mm", date) + ", " + getAuthorName() + ", " + getComment();
    }
}
