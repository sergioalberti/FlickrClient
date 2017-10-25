package it.univr.android.flickrclient.view;


import android.app.AlertDialog;
import android.app.ListFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.UiThread;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

import it.univr.android.flickrclient.FlickrApplication;
import it.univr.android.flickrclient.MVC;
import it.univr.android.flickrclient.R;
import it.univr.android.flickrclient.model.FlickrImage;
import it.univr.android.flickrclient.model.Model;

import static android.support.v4.content.FileProvider.getUriForFile;

/**
 * a fragment used to show search's results
 */
public class SearchFragment extends ListFragment implements AbstractFragment {
    private MVC mvc;
    private SearchAdapter searchAdapter = null;
    private boolean dialogIsShowing = false;

    /**
     * a String used in menu (local)
     */
    public static final String SEARCH_BY_AUTHOR = "Altre immagini di  ";;

    /**
     * a String used in menu (local)
     */
    public static final String SHARE = "Condividi";

    /**
     * says the SearchFragment class' name
     */
    public final static String TAG = SearchFragment.class.getName();

    /**
     * initializes the SearchFragment instance
     */
    public SearchFragment() { }

    private class SearchAdapter extends ArrayAdapter<FlickrImage> {
        private List<FlickrImage> imagesList;

        private SearchAdapter(List<FlickrImage> fi) {
            super(getActivity(), R.layout.fragment_search_item, fi);
            this.imagesList = fi;
        }

        public void updateSearchResults(){
            imagesList = mvc.model.getSearchResults();

        }

        @Override
        public int getCount () {
            return imagesList.size();
        }

        @Override
        public android.view.View getView(int position, android.view.View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                convertView = inflater.inflate(R.layout.fragment_search_item, parent, false);
            }

            FlickrImage image = imagesList.get(position);

            ImageView iv = (ImageView) convertView.findViewById(R.id.image_thumb);
            TextView tv = (TextView) convertView.findViewById(R.id.image_title);

            if (image != null && image.getBitmap(Model.UrlType.THUMB) != null)
                iv.setImageBitmap(image.getBitmap(Model.UrlType.THUMB));
            else
               iv.setImageResource(R.drawable.preview);

            tv.setText(position + 1 + ": " + image.getTitle());
            return convertView;
        }
    }

    @Override @UiThread
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mvc = ((FlickrApplication) getActivity().getApplication()).getMvc();

        onModelChanged();

        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                FlickrImage selectedImage = (FlickrImage)getListView().getItemAtPosition(position);

                // all images in the local store are disabled, only the selected one is enabled

                mvc.model.reset();
                selectedImage.enable();

                mvc.controller.showFullImage();
            }
        });

        registerForContextMenu(this.getListView());
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) menuInfo;
        FlickrImage selectedImage = (FlickrImage)getListView().getItemAtPosition(acmi.position);

        menu.setHeaderTitle(selectedImage.getTitle());
        menu.add(SHARE);

        // first we check whenever this search is a search by author (by checking if the AuthorName
        // is null - see the model's implementation), if so, another search by author is senseless
        // so we omit "search by author" menu voice

        if (selectedImage.getAuthorName() != null)
            menu.add(SEARCH_BY_AUTHOR + selectedImage.getAuthorName());
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();

        FlickrImage selectedImage = (FlickrImage)getListView().getItemAtPosition(acmi.position);

        if (item.getTitle().equals(SHARE)){
            File imagePath = new File(getActivity().getFilesDir(), "images");

            if (!imagePath.exists())
                imagePath.mkdir();

            File newFile = new File(imagePath, "image_" + selectedImage.getTitle().hashCode() + ".jpg");

            // downloading image
            selectedImage.share();
            selectedImage.setAbsoluteURL(newFile.getAbsolutePath());

            mvc.controller.callDownloadService(getActivity(), selectedImage, Model.UrlType.FULLSIZE);

            onModelChanged();

            // the model will save downloaded bitmap and show the share intent once the download is
            // finished. If the image was just downloaded, the method will not download it from
            // sketch
        } else {
            // saving previous search session
            mvc.model.backup();

            mvc.controller.callSearchService(getActivity(), mvc.controller.SEARCH_BY_AUTHOR, selectedImage.getAuthor());

            mvc.controller.showSearchResults();
        }
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();

        if(getView() == null){
            return;
        }

        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
        getView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK){
                    // back button clicked, resuming previous search

                    mvc.model.restore();

                    onModelChanged();
                }
                return false;
            }
        });
    }

    /**
     * called when the model changes
     */
    @Override @UiThread
    public void onModelChanged() {
        // checking whenever call to this method was invoked to show share intent

        FlickrImage selectedImage = mvc.model.getShared();

        if (selectedImage == null) {
            // the call was made after a search was required (we are sure of that because the model
            // is reset in a new search; thus image labeled as shared couldn't exist)

            // we are ready to perform actions to show the new search

            final List<FlickrImage> fiList = mvc.model.getSearchResults();

            if(fiList != null && fiList.size() > 0){
                if(searchAdapter == null) {
                    searchAdapter = new SearchAdapter(fiList);
                    setListAdapter(searchAdapter);
                    setListShown(true); //removes loading spinner and shows the list
                }
                else{
                    searchAdapter.updateSearchResults();
                    searchAdapter.notifyDataSetChanged();
                }
            }else{

                if(searchAdapter != null)
                    searchAdapter.clear();

                if (!dialogIsShowing && fiList != null && fiList.size() == 0) {
                    setListShown(true); //removes loading spinner and shows the list

                    dialogIsShowing = true;
                    AlertDialog.Builder builder;
                    builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("Invalid key")
                            .setMessage("No data found for searched key!")
                            .setIcon(R.drawable.info_dark)
                            .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // returning to the caller, no data found for the key

                                    getActivity().getFragmentManager().popBackStack();
                                }
                            })
                            .setCancelable(false)
                            .show();
                }
            }
        }
        // showing share intent

        else if (selectedImage != null && !selectedImage.getAbsoluteURL().equals("") && selectedImage.getBitmap(Model.UrlType.FULLSIZE) != null) {
            // since we are here, the onModelChanged was invoked when the DownloadTask has ended
            // to download the full size image after a shared intent invocation attempt

            try {
                File newFile = new File(selectedImage.getAbsoluteURL());
                Uri contentUri = getUriForFile(getActivity(), "it.univr.android.flickrclient", newFile);

                FileOutputStream os = new FileOutputStream(newFile);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                selectedImage.getBitmap(Model.UrlType.FULLSIZE).compress(Bitmap.CompressFormat.JPEG, 100, stream);
                os.write(stream.toByteArray());
                os.close();

                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_SEND);
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_STREAM, contentUri);
                startActivity(Intent.createChooser(intent, SHARE));

                selectedImage.unshare();

            } catch (Exception e) {
                Log.d("ShareBitmap", "error " + e.getMessage());
            }
        }
    }
}