package it.univr.android.flickrclient.view;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.annotation.UiThread;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;

import com.weiwangcn.betterspinner.library.material.MaterialBetterSpinner;

import java.util.Arrays;
import java.util.List;

import it.univr.android.flickrclient.FlickrApplication;
import it.univr.android.flickrclient.MVC;
import it.univr.android.flickrclient.R;
import it.univr.android.flickrclient.controller.Controller;

/**
 * the main fragment used as entry point
 */
public class MainFragment extends Fragment implements AbstractFragment {
    private MVC mvc;
    private EditText searchKey;
    private Button searchButton;
    private MaterialBetterSpinner searchSpinner;
    private String selectedOption = "";

    /**
     * says the MainFragment class' name
     */
    public final static String TAG = MainFragment.class.getName();

    /**
     * initializes the MainFragment instance
     */
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
        searchSpinner = (MaterialBetterSpinner) view.findViewById(R.id.search_spinner);
        searchButton = (Button) view.findViewById(R.id.buttonSearch);

        searchKey.setVisibility(View.INVISIBLE);

        ArrayAdapter<CharSequence> spinnerAdapter =
                ArrayAdapter.createFromResource(view.getContext(), R.array.search_options, R.layout.spinner_layout);
        searchSpinner.setAdapter(spinnerAdapter);

        searchButton.setOnClickListener(__ -> {
            // fragments from previous search are emptied

            FragmentManager fm = getFragmentManager();
            fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

            // a new search is made

            if(selectedOption.equals(Controller.SEARCH_BY_KEY))
                mvc.controller.callSearchService(getActivity(), selectedOption, searchKey.getText().toString());
            else
                mvc.controller.callSearchService(getActivity(), selectedOption, null);

            mvc.controller.showSearchResults();
        });

        searchSpinner.setOnItemClickListener((adapterView, view1, position, l) -> updateUI());

        searchKey.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) { }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateUI();
            }
        });

        return view;
    }

    @Override @UiThread
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mvc = ((FlickrApplication) getActivity().getApplication()).getMvc();
        onModelChanged();
        updateUI();
    }

    /**
     * called when the model changes
     */
    @Override
    public void onModelChanged() {  }

    private void updateUI(){
        if(!searchSpinner.getText().toString().equals("")) {
            List<String> selectedOptionsComparator = Arrays.asList(getResources().getStringArray(R.array.search_options));
            selectedOption = "" + selectedOptionsComparator.indexOf(searchSpinner.getText().toString());
            searchButton.setEnabled(true);
        }

        if(selectedOption.equals(Controller.SEARCH_BY_KEY))
            searchKey.setVisibility(View.VISIBLE);
        else
            searchKey.setVisibility(View.INVISIBLE);

        if(selectedOption.equals(Controller.SEARCH_BY_KEY) && searchKey.getText().toString().equals(""))
            searchButton.setEnabled(false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // the main menu with info has to be shown only on the first page. Thus we have to check
        // if there is another fragment shown (or if it lays in the back stack) different from the
        // MainFragment or TabletFragment.

        // if the SearchFragment is shown - it means that a new
        // search has been done. No info menu ha to be shown.

        // if the SearchFragment is in the back stack, ImageFragment is shown or another search is
        // done based on the author of one of the images of previous search. Thus no menu info
        // should be shown neither now.

        if (getFragmentManager().findFragmentByTag(SearchFragment.TAG) == null) {
            super.onCreateOptionsMenu(menu, inflater);
            menu.clear();
            inflater.inflate(R.menu.fragment_main_menu, menu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (getFragmentManager().findFragmentByTag(SearchFragment.TAG) == null && item.getItemId() == R.id.menu_info) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            builder.setMessage(getString(R.string.about_message))
                    .setTitle(getString(R.string.about))
                    .setPositiveButton(getString(R.string.ok), (dialog, id) -> { })
                    .setIcon(R.drawable.info_dark);

            AlertDialog dialog = builder.create();
            dialog.show();

            return true;
        } else
            return super.onOptionsItemSelected(item);
    }
}