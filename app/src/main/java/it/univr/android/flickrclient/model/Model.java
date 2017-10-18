package it.univr.android.flickrclient.model;

/**
 * Created by user on 5/16/17.
 */

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import it.univr.android.flickrclient.MVC;
import it.univr.android.flickrclient.view.View;

@ThreadSafe
public class Model implements Iterable<FlickrImage> {
    private MVC mvc;
    private @GuardedBy("this") ArrayList<FlickrImage> imagesList = null;
    private @GuardedBy("this") ArrayList<FlickrImage> oldImagesList = null;

    @Override
    public Iterator<FlickrImage> iterator() {
        return imagesList.iterator();
    }

    // enumeration is used to know which type of image is required
    public enum UrlType {
        THUMB,
        FULLSIZE
    }

    public void setMVC(MVC mvc){
        this.mvc = mvc;
    }

    public void store(ArrayList<FlickrImage> result){
        synchronized (this) {
            imagesList = result;
        }

        mvc.forEachView(View::onModelChanged);
    }

    public void backup() {
        synchronized (this) {
            oldImagesList = imagesList;
        }
    }

    public void restore() {
        synchronized (this) {
            imagesList = oldImagesList;
        }
    }

    public void updateImage(FlickrImage image){ get(OperationType.UPDATE, null, image); }

    // added a method that returns the FlickrImage object from local store. The method is used
    // whenever storing the whole FlickrImage object is too wasteful. Thus only a String is used
    // to point to a FlickrImage in the store.

    public FlickrImage getImage(String imageURL){ return get(OperationType.GET_FROM_URL, imageURL, null);  }

    public FlickrImage getImageFromId(String id){ return get(OperationType.GET_FROM_ID, id, null);  }

    // used to get the selected image (i.e. when the image has to be enlarged)
    // the selected image is disabled when calling this method

    public FlickrImage getEnabled(){ return get(OperationType.GET_ENABLED, null, null); }

    public FlickrImage getShared(){ return get(OperationType.GET_SHARED, null, null); }

    // used to disable all images from the list, once the ImageFragment is closed

    public void reset(){ get(OperationType.RESET, null, null); }

    // enumeration is used to know which type of operation is required

    private enum OperationType {
        GET_ENABLED,
        GET_SHARED,
        GET_FROM_URL,
        GET_FROM_ID,
        UPDATE,
        RESET
    }

    // utility method used to iterate model

    private synchronized FlickrImage get(OperationType ot, String data, FlickrImage newimage){
        if (imagesList != null) {
            for (FlickrImage image : imagesList) {
                if (ot == OperationType.GET_ENABLED && image.isEnbled() || ot == OperationType.GET_SHARED && image.isShared())
                    return image;
                else if (ot == OperationType.GET_FROM_URL && image.getImageURL().equals(data))
                    return image;
                else if (ot == OperationType.GET_FROM_ID && image.getId().equals(data))
                    return image;
                else if (ot == OperationType.UPDATE && image.equals(newimage)) {
                    imagesList.set(imagesList.indexOf(image), newimage);
                    mvc.forEachView(View::onModelChanged);
                    break;
                } else if (ot == OperationType.RESET) {
                    image.disable();
                    image.unshare();
                }
            }

        }
        return null;
    }

    public List<FlickrImage> getSearchResults(){
        if (imagesList != null)
            return Collections.synchronizedList(imagesList);
        return null;
    }

    public void clearModel() {
        synchronized (this){
            imagesList = null;
        }
    }

    public void purgeModel() {
        clearModel();
        synchronized (this) {
            oldImagesList = null;
        }
    }
}