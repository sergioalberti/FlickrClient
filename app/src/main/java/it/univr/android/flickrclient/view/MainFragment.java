package it.univr.android.flickrclient.view;

/**
 * Created by user on 5/16/17.
 */

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.UiThread;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import it.univr.android.flickrclient.FlickrApplication;
import it.univr.android.flickrclient.MVC;
import it.univr.android.flickrclient.R;

public class MainFragment extends Fragment implements AbstractFragment {
    private MVC mvc;
    private EditText searchKey;
    private Button searchButton;
    private Spinner searchSpinner;

    public MainFragment(){
    }

    @Override @UiThread
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override @UiThread
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        searchKey = (EditText) view.findViewById(R.id.editSearch);
        searchSpinner = (Spinner) view.findViewById(R.id.search_spinner);
        searchButton = (Button) view.findViewById(R.id.buttonSearch);

        searchKey.setVisibility(View.INVISIBLE);

        ArrayAdapter<CharSequence> spinnerAdapter =
                ArrayAdapter.createFromResource(view.getContext(), R.array.search_options, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        searchSpinner.setAdapter(spinnerAdapter);

        searchButton.setOnClickListener(__ -> {
            String selectedOption = searchSpinner.getSelectedItem().toString();

            mvc.controller.clearPreviousSearch();

            if(selectedOption.equals(mvc.controller.SEARCH_BY_KEY))
                mvc.controller.callSearchService(getActivity(), selectedOption, searchKey.getText().toString());
            else
                mvc.controller.callSearchService(getActivity(), selectedOption, null);

            mvc.controller.showSearchResults();
        });

        searchSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(parent.getItemAtPosition(position).toString().equals(mvc.controller.SEARCH_BY_KEY))
                    searchKey.setVisibility(View.VISIBLE);
                else
                    searchKey.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //nothing to do
            }
        });

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
        onModelChanged();
    }

    @Override
    public void onModelChanged() {
        //nothing to do
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.fragment_main_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_info) {

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            builder.setMessage("Sergio Alberti, Adam Seewald\n" +
                               "Giugno/Luglio 2017\n" +
                               "versione 1.0")
                    .setTitle("About")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) { }
                    })
                    .setIcon(R.drawable.vector_info_black);

            AlertDialog dialog = builder.create();
            dialog.show();

            return true;
        } else
            return super.onOptionsItemSelected(item);
    }
}