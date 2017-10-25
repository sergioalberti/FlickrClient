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

/**
 * Used to store data in the current dataset
 */
@ThreadSafe
public class Model implements Iterable<FlickrImage> {
    private MVC mvc;
    private @GuardedBy("this") ArrayList<FlickrImage> imagesList = null;
    private @GuardedBy("this") ArrayList<FlickrImage> oldImagesList = null;

    /**
     * used to iterate items through dataset collection
     * @return current dataset iterator
     */
    @Override
    public Iterator<FlickrImage> iterator() {
        return imagesList.iterator();
    }

    /**
     * enumeration is used to know which type of image is required
     */
    public enum UrlType {
        THUMB,
        FULLSIZE
    }


    /**
     * the method is used to set the current model's MVC instance
     * @param mvc the mvc instance to be set in the current model
     */
    public void setMVC(MVC mvc){
        this.mvc = mvc;
    }

    /**
     * used to store data in the current dataset
     * @param result the collection of data to be set in the current dataset
     */
    public void store(ArrayList<FlickrImage> result){
        synchronized (this) {
            imagesList = result;
        }

        mvc.forEachView(View::onModelChanged);
    }

    /**
     * provides a way to store old dataset data when necessary due to a new search invocation
     */
    public void backup() {
        synchronized (this) {
            oldImagesList = imagesList;
        }
    }

    /**
     * used to restore previous dataset data
     */
    public void restore() {
        synchronized (this) {
            imagesList = oldImagesList;
        }
    }

    /**
     * updates the current dataset's item's pointer when a fresh image has to be saved
     * @param image the image to be saved
     */
    public void updateImage(FlickrImage image){ get(OperationType.UPDATE, null, image); }

    /**
     * returns a FlickrImage pointer to the object through the store from a String
     * @param imageURL the image's URL
     * @return the required FlickrImage pointer
     */
    public FlickrImage getImage(String imageURL){ return get(OperationType.GET_FROM_URL, imageURL, null);  }

    /**
     * returns a FlickrImage pointer to the object thorugh the store from an Id
     * @param id the image's id
     * @return the required FlickrImage pointer
     */
    public FlickrImage getImageFromId(String id){ return get(OperationType.GET_FROM_ID, id, null);  }

    /**
     * used to get the selected image (i.e. when the image has to be enlarged),
     * when it's selected in the fragment
     * @return the required FlickrImage pointer
     */
    public FlickrImage getEnabled(){ return get(OperationType.GET_ENABLED, null, null); }

    /**
     * used to get the sharable image (i.e. when the image is selected to be shared), when the image
     * has to be sent thorugh fragments invocations
     * @return the required FlickrImage pointer
     */
    public FlickrImage getShared(){ return get(OperationType.GET_SHARED, null, null); }

    /**
     * used to disable all images from the list, once the ImageFragment is closed
     */
    public void reset(){ get(OperationType.RESET, null, null); }

    /**
     * enumeration is used to know which type of operation is required
     */
    private enum OperationType {
        GET_ENABLED,
        GET_SHARED,
        GET_FROM_URL,
        GET_FROM_ID,
        UPDATE,
        RESET
    }

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

    /**
     * returns a collection of current items in the model, to be used when followed by search op
     * @return a collection of current items in the model
     */
    public List<FlickrImage> getSearchResults(){
        if (imagesList != null)
            return Collections.synchronizedList(imagesList);
        return null;
    }

    /**
     * clears current imageList allowing the entry of fresh items
     */
    public void clearModel() {
        synchronized (this){
            imagesList = null;
        }
    }

    /**
     * clears the whole model by purging both list used to store data
     */
    public void purgeModel() {
        clearModel();
        synchronized (this) {
            oldImagesList = null;
        }
    }
}