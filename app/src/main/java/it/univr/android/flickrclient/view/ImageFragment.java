package it.univr.android.flickrclient.view;


import android.animation.ObjectAnimator;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.UiThread;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

import it.univr.android.flickrclient.FlickrApplication;
import it.univr.android.flickrclient.MVC;
import it.univr.android.flickrclient.R;
import it.univr.android.flickrclient.controller.Controller;
import it.univr.android.flickrclient.model.Comment;
import it.univr.android.flickrclient.model.FlickrImage;
import it.univr.android.flickrclient.model.Model;

import static android.support.v4.content.FileProvider.getUriForFile;


/**
 * a fragment that shows fullsize image
 */
public class ImageFragment extends Fragment implements AbstractFragment {
    private MVC mvc;
    private FlickrImage image;
    private ImageView iv;
    private ListView lv;

    /**
     * says the interval in ms that an animation lasts
     */
    public static final int ANIMATION_DURATION = 150;

    /**
     * says the ImageFragment class' name
     */
    public final static String TAG = ImageFragment.class.getName();

    /**
     * initializes the ImageFragment instance
     */
    public ImageFragment(){
    }

    @Override @UiThread
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    // following two methods used for share item on action bar

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.fragment_image_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_share) {

            File imagePath = new File(getActivity().getFilesDir(), "images");

            if (!imagePath.exists())
                imagePath.mkdir();

            File newFile = new File(imagePath, "image_" + image.getTitle().hashCode() + ".jpg");

            image.share();
            image.setAbsoluteURL(newFile.getAbsolutePath());

            mvc.controller.callDownloadService(getActivity(), image, Model.UrlType.FULLSIZE);
            onModelChanged();

            return true;
        } else
            return super.onOptionsItemSelected(item);
    }

    @Override @UiThread
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_image, container, false);
        iv = (ImageView) view.findViewById(R.id.image_box);
        lv = (ListView) view.findViewById(R.id.list_view);

        return view;
    }

    @Override @UiThread
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
    }

    @Override @UiThread
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mvc = ((FlickrApplication) getActivity().getApplication()).getMvc();
        image = mvc.model.getEnabled();

        if (image != null) {
            // searching for comments

            mvc.controller.callSearchService(getActivity(), Controller.SEARCH_COMMENTS, image.getId());

            // downloading full size image

            mvc.controller.callDownloadService(getActivity(), image, Model.UrlType.FULLSIZE);
        }

        onModelChanged();
    }

    /**
     * called when the model changes
     */
    @Override
    public void onModelChanged() {
        // checking whenever call to this method was invoked to show share intent

        FlickrImage selectedImage = mvc.model.getShared();

        // getDrawable method on ImageView tells whenever the Bitmap is yet set or not. If it's set,
        // it's not set another time (since several calls to onModelChanged are permitted)

        if (image != null && image.getBitmap(Model.UrlType.FULLSIZE) != null && iv.getDrawable() == null) {
            iv.setImageBitmap(image.getBitmap(Model.UrlType.FULLSIZE));

            ObjectAnimator.ofFloat(iv, "alpha", 0f, 1f).setDuration(ANIMATION_DURATION).start();
            ObjectAnimator.ofFloat(iv, "scaleX", 0.9f, 1f).setDuration(ANIMATION_DURATION).start();
            ObjectAnimator.ofFloat(iv, "scaleY", 0.9f, 1f).setDuration(ANIMATION_DURATION).start();
        }

        // showing share intent

        else if (selectedImage != null && !selectedImage.getAbsoluteURL().equals("") && selectedImage.getBitmap(Model.UrlType.FULLSIZE) != null) {
            try {
                File newFile = new File(selectedImage.getAbsoluteURL());
                Uri contentUri = getUriForFile(getActivity(), getActivity().getApplicationContext().getPackageName(), newFile);

                FileOutputStream os = new FileOutputStream(newFile);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                selectedImage.getBitmap(Model.UrlType.FULLSIZE).compress(Bitmap.CompressFormat.JPEG, 100, stream);
                os.write(stream.toByteArray());
                os.close();

                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_SEND);
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_STREAM, contentUri);
                startActivity(Intent.createChooser(intent, getString(R.string.share)));

                selectedImage.unshare();

            } catch (Exception e) {
                Log.d("ShareBitmap", "error " + e.getMessage());
            }
        }

        // showing comments if available on specific image
        if (image != null && image.getComments() != null && lv.getCount() == 0) {
            ArrayList<Comment> comments = image.getComments();
            ArrayAdapter adapter = new ArrayAdapter(this.getActivity(), android.R.layout.simple_list_item_2, android.R.id.text1, comments) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);
                    TextView text1 = (TextView) view.findViewById(android.R.id.text1);
                    TextView text2 = (TextView) view.findViewById(android.R.id.text2);

                    text1.setText(comments.get(position).getAuthorName());
                    text2.setText(Html.fromHtml(comments.get(position).getComment()));
                    return view;
                }
            };

            lv.setAdapter(adapter);

            ObjectAnimator.ofFloat(lv, "alpha", 0f, 1f).setDuration(ANIMATION_DURATION * 3).start();
        }
    }
}