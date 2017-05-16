package it.univr.android.flickrclient.controller;

/**
 * Created by user on 5/16/17.
 */

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.WorkerThread;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import it.univr.android.flickrclient.FlickrApplication;
import it.univr.android.flickrclient.MVC;
import it.univr.android.flickrclient.model.Model;

public class SearchService extends IntentService {
    private static final String ACTION_FLICKR_SEARCH = "search";
    private static final String SEARCH_KEY = "search_key";

    public SearchService(){
        super("search service");
    }

    static void doFlickrSearch(Context context, String key){
        Intent intent = new Intent(context, SearchService.class);
        intent.setAction(ACTION_FLICKR_SEARCH);
        intent.putExtra(SEARCH_KEY, key);

        context.startService(intent);
    }

    @WorkerThread
    protected void onHandleIntent(Intent intent) {
        switch(intent.getAction()){
            case ACTION_FLICKR_SEARCH:
                String searchKey = (String) intent.getSerializableExtra(SEARCH_KEY);
                Model.FlickrImage[] result = flickrSearch(searchKey);
                MVC mvc = ((FlickrApplication) getApplication()).getMvc();
                mvc.model.storeSearchResults(result);
                break;
        }
    }

    @WorkerThread
    private Model.FlickrImage[] flickrSearch(String searchKey){
        ArrayList<Model.FlickrImage> response = new ArrayList<>();
        int counter = 0;

        try {
            String key = URLEncoder.encode(searchKey, "UTF-8");
            String CONNECTION_URL = "https://api.flickr.com/services/rest?method=flickr.\n" +
                    "photos.search&api_key=be4922ffb4ded82d452af0477842bdba&text=" + key + "&extras=url_z,\n" +
                    "&per_page=50";

            URL url = new URL(CONNECTION_URL);
            URLConnection conn = url.openConnection();

            //necessario aprire e chiudere il buffer
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            String line;
            Pattern titlePattern = Pattern.compile("title=\"([^\"]*)\"", Pattern.DOTALL);
            Pattern URLPattern = Pattern.compile("url_z=\"([^\"]*)\"", Pattern.DOTALL);
            Matcher titleMatcher = null, URLMatcher = null;

            while ((line = in.readLine()) != null){
                if(line.contains("<photo id")){
                    titleMatcher = titlePattern.matcher(line);
                    URLMatcher =  URLPattern.matcher(line);

                    if(titleMatcher.find() && URLMatcher.find()) {
                        response.add(new Model.FlickrImage(titleMatcher.group(1), URLMatcher.group(1)));
                        counter++;
                    }
                }
            }

            if (in != null)
                in.close();
        }
        catch(IOException e){
            e.printStackTrace();
        }

        return response.toArray(new Model.FlickrImage[counter]);
    }
}
