package it.univr.android.flickrclient.controller;

/**
 * Created by user on 5/16/17.
 */

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.WorkerThread;
import android.util.Log;

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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import it.univr.android.flickrclient.FlickrApplication;
import it.univr.android.flickrclient.MVC;
import it.univr.android.flickrclient.model.Model;

public class SearchService extends IntentService {
    private static final String ACTION_FLICKR_SEARCH = "search";
    private static final String SAVED_KEY = "search_key";
    private static final String SAVED_TYPE = "search_type";
    private static final String API_KEY = "be4922ffb4ded82d452af0477842bdba";
    private MVC mvc;


    public SearchService(){
        super("search service");
    }

    static void doFlickrSearch(Context context, String searchType, String key){
        Intent intent = new Intent(context, SearchService.class);
        intent.setAction(ACTION_FLICKR_SEARCH);
        intent.putExtra(SAVED_TYPE, searchType);
        intent.putExtra(SAVED_KEY, key);

        context.startService(intent);
    }

    @WorkerThread
    protected void onHandleIntent(Intent intent) {
        mvc = ((FlickrApplication) getApplication()).getMvc();

        switch(intent.getAction()){
            case ACTION_FLICKR_SEARCH:
                String searchKey = (String) intent.getSerializableExtra(SAVED_KEY);
                String searchType = (String) intent.getSerializableExtra(SAVED_TYPE);

                ArrayList<Model.FlickrImage> result = flickrSearch(searchType, searchKey);
                mvc.model.storeSearchResults(result);
                mvc.controller.killWorkingTasks();
                mvc.controller.callThumbTask(result);
                break;
        }
    }

    @WorkerThread
    private ArrayList<Model.FlickrImage> flickrSearch(String searchType, String searchKey){
        ArrayList<Model.FlickrImage> response = new ArrayList<>();

        try {
            String key = null;
            String CONNECTION_URL = null;

            if(searchType.equals(mvc.controller.SEARCH_BY_KEY)) {
                key = URLEncoder.encode(searchKey, "UTF-8");
                CONNECTION_URL = "https://api.flickr.com/services/rest?method=flickr.\n" +
                        "photos.search&api_key=" + API_KEY + "&text=" + key + "&extras=url_z,url_s,\n" +
                        "&per_page=50";
            }
            else if(searchType.equals(mvc.controller.SEARCH_LAST_UPLOADS))
                CONNECTION_URL = "https://api.flickr.com/services/rest?method=flickr.\n" +
                        "photos.getRecent&api_key=" + API_KEY + "&extras=url_z,url_s,\n" +
                        "&per_page=50";
            else
                CONNECTION_URL = "https://api.flickr.com/services/rest?method=flickr.\n" +
                        "interestingness.getList&api_key=" + API_KEY + "&extras=url_z,url_s,\n" +
                        "&per_page=50";

            URL url = new URL(CONNECTION_URL);
            URLConnection conn = url.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            String line = null, xml = "";

            while ((line = in.readLine()) != null)
                xml += line;

            if (in != null)
                in.close();

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(xml));
            Document document = builder.parse(is);
            NodeList photos = document.getElementsByTagName("photo");

            NamedNodeMap nnm;
            Node title, url_z, url_s;

            for (int i = 0; i < photos.getLength(); i++){
                nnm = photos.item(i).getAttributes();
                title = nnm.getNamedItem("title");
                url_z = nnm.getNamedItem("url_z");
                url_s = nnm.getNamedItem("url_s");
                if (title != null && url_z != null && url_s != null)
                    response.add(new Model.FlickrImage(title.getTextContent(), url_z.getTextContent(), url_s.getTextContent()));
            }
        }
        catch(IOException | ParserConfigurationException | SAXException e){
            e.printStackTrace();
        }

        return response;
    }
}
