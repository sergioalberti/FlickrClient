package it.univr.android.flickrclient.controller;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.WorkerThread;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import it.univr.android.flickrclient.FlickrApplication;
import it.univr.android.flickrclient.MVC;
import it.univr.android.flickrclient.model.Comment;
import it.univr.android.flickrclient.model.FlickrImage;
import it.univr.android.flickrclient.model.Model;
import it.univr.android.flickrclient.view.View;

/**
 * permits to call an intent, when a new search is required by the user
 */
public class SearchService extends IntentService {
    private static final String ACTION_FLICKR_SEARCH = "search";
    private static final String SAVED_TYPE = "search_type";
    private static final String SAVED_ADDITIONAL_DATA = "additional_data";
    private static final String API_KEY = "be4922ffb4ded82d452af0477842bdba";


    /**
     * initialize the current search service instance
     */
    public SearchService(){
        super("search service");
    }

    /**
     * starts a SearchService's intent
     * @param context the current application's context
     * @param searchType the model permits many types of search. Thus a type has to be specified:
     *                   # SEARCH_BY_KEY stores to model images given a specific string
     *                   # SEARCH_MOST_POPULAR stores t.m. a collection of the most popular images
     *                   # SEARCH_LAST_UPLOADS s.t.m. a collection of the last uploaded images
     *                   # SEARCH_BY_AUTHOR s.t.m. a collection of the last uploaded images given an
     *                     author
     *                   # SEARCH_COMMENTS s.t.m. a collection of comments (instances of class Comment)
     *                     to be shown when an ImageFragment is shown to the used
     * @param data depending on the searchType specified ahead, few addintional data may be required.
     *             Those can be specified with respect to the required format through the current field
     */
    static void doFlickrSearch(Context context, String searchType, String data){
        Intent intent = new Intent(context, SearchService.class);
        intent.setAction(ACTION_FLICKR_SEARCH);
        intent.putExtra(SAVED_TYPE, searchType);
        intent.putExtra(SAVED_ADDITIONAL_DATA, data);

        context.startService(intent);
    }

    @WorkerThread
    protected void onHandleIntent(Intent intent) {
        MVC mvc = ((FlickrApplication) getApplication()).getMvc();

        switch(intent.getAction()){
            case ACTION_FLICKR_SEARCH:
                String searchType = (String) intent.getSerializableExtra(SAVED_TYPE);
                String data = (String) intent.getSerializableExtra(SAVED_ADDITIONAL_DATA);

                if (!searchType.equals(Controller.SEARCH_COMMENTS)) {
                    ArrayList<FlickrImage> result = (ArrayList<FlickrImage>) flickrSearch(searchType, data);
                    mvc.model.store(result);
                    mvc.controller.killWorkingTasks();

                    // a new intent is not invoked since here we're in another intent, thus is senseless

                    if (result != null && result.size() > 0)
                        mvc.controller.callDownloadTask(result, Model.UrlType.THUMB);
                    else
                        mvc.forEachView(View::onModelChanged);
                } else {
                    ArrayList<Comment> result = (ArrayList<Comment>) flickrSearch(searchType, data);

                    // to update ImageFragment with latest comments, a call to onModelChanged must
                    // by performed (the call is invoked from Model.updateImage)

                    if (result != null && result.size() > 0) {
                        FlickrImage image = mvc.model.getImageFromId(data);

                        if (image != null) {
                            image.setComments(result);
                            mvc.model.updateImage(image);
                        }
                    }
                }

                break;
        }
    }

    @WorkerThread
    private ArrayList flickrSearch(String searchType, String data){
        try {
            String key;
            String CONNECTION_URL;

            switch (searchType) {
                case Controller.SEARCH_BY_KEY:
                    key = URLEncoder.encode(data, "UTF-8");
                    CONNECTION_URL = "https://api.flickr.com/services/rest?method=flickr." +
                            "photos.search&api_key=" + API_KEY + "&text=" + key +
                            "&extras=url_z,url_s,owner_name,&per_page=50";
                    break;
                case Controller.SEARCH_LAST_UPLOADS:
                    CONNECTION_URL = "https://api.flickr.com/services/rest?method=flickr." +
                            "photos.getRecent&api_key=" + API_KEY + "&extras=url_z,url_s,owner_name," +
                            "&per_page=50";
                    break;
                case Controller.SEARCH_MOST_POPULAR:
                    CONNECTION_URL = "https://api.flickr.com/services/rest?method=flickr." +
                            "interestingness.getList&api_key=" + API_KEY + "&extras=url_z,url_s," +
                            "owner_name,&per_page=50";
                    break;
                case Controller.SEARCH_BY_AUTHOR:
                    CONNECTION_URL = "https://api.flickr.com/services/rest?method=flickr." +
                            "people.getPublicPhotos&api_key=" + API_KEY + "&user_id=" + data +
                            "&extras=url_z,url_s,&per_page=50";
                    break;
                default:
                    CONNECTION_URL = "https://api.flickr.com/services/rest?method=flickr." +
                            "photos.comments.getList&api_key=" + API_KEY + "&photo_id=" + data;
                    break;
            }

            URL url = new URL(CONNECTION_URL);
            URLConnection conn = url.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            String line, xml = "";

            while ((line = in.readLine()) != null)
                xml += line;

            in.close();

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(xml));
            Document document = builder.parse(is);
            NodeList photos = document.getElementsByTagName("photo");
            NodeList comments = document.getElementsByTagName("comment");

            if (photos.getLength() != 0) {
                ArrayList<FlickrImage> response = new ArrayList<>();
                NamedNodeMap nnm;
                Node title, url_z, url_s, owner, owner_name, id;

                for (int i = 0; i < photos.getLength(); i++){
                    nnm = photos.item(i).getAttributes();
                    title = nnm.getNamedItem("title");
                    url_z = nnm.getNamedItem("url_z");
                    url_s = nnm.getNamedItem("url_s");
                    owner = nnm.getNamedItem("owner");
                    owner_name = nnm.getNamedItem("ownername");
                    id = nnm.getNamedItem("id");

                    if (title != null && url_z != null && url_s != null && owner != null && owner_name != null && id != null)
                        response.add(new FlickrImage(
                                title.getTextContent(),
                                owner.getTextContent(),
                                id.getTextContent(),
                                url_z.getTextContent(),
                                url_s.getTextContent(),
                                owner_name.getTextContent()
                        ));
                    else if (title != null && url_z != null && url_s != null && owner != null && id != null)
                        response.add(new FlickrImage(
                                title.getTextContent(),
                                owner.getTextContent(),
                                id.getTextContent(),
                                url_z.getTextContent(),
                                url_s.getTextContent(),
                                null
                        ));
                }

                return response;

            } else if (comments.getLength() != 0) {
                ArrayList<Comment> response = new ArrayList<>();
                NamedNodeMap nnm;
                Node author_name, datecreate;
                String comment;
                Date date;

                for (int i = 0; i < comments.getLength(); i++){
                    nnm = comments.item(i).getAttributes();
                    comment = comments.item(i).getTextContent();
                    author_name = nnm.getNamedItem("authorname");
                    datecreate = nnm.getNamedItem("datecreate");

                    if (author_name != null && datecreate != null) {
                        date = new java.util.Date(Long.parseLong(datecreate.getTextContent()) * 1000);

                        response.add(new Comment(author_name.getTextContent(), comment, date));
                    }
                }

                return response;
            }
        }
        catch(IOException | ParserConfigurationException | SAXException e){
            e.printStackTrace();
        }

        // returns an empty list. This may happens when the search has not produced any results

        return new ArrayList<>();
    }
}
