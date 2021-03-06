package com.codepathgroup5.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.codepathgroup5.adapters.BusinessCardAdapter;
import com.codepathgroup5.utilities.Utility;
import com.codepathgroup5.wanttoknow.R;
import com.parse.ParseUser;
import com.yelp.clientlib.connection.YelpAPI;
import com.yelp.clientlib.connection.YelpAPIFactory;
import com.yelp.clientlib.entities.Business;
import com.yelp.clientlib.entities.SearchResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchActivity extends AppCompatActivity implements SearchView.OnQueryTextListener{

    private static final String SAVE_KEY_BUSINESS_LIST = ":MainActivity:businessList";

    YelpAPIFactory apiFactory = new YelpAPIFactory(Utility.CONSUMER_KEY, Utility.CONSUMER_SECRET, Utility.TOKEN, Utility.TOKEN_SECRET);
    YelpAPI yelpAPI = apiFactory.createAPI();

    SearchView searchView;

    RecyclerView recList;
    BusinessCardAdapter bca;
    ArrayList<Business> businesses;

    TextView welcomeText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        welcomeText = (TextView) findViewById(R.id.welcome_text);

        recList = (RecyclerView) findViewById(R.id.cardList);
        recList.setHasFixedSize(true);

        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);

        recList.setLayoutManager(llm);

//        Resources res = getResources();
//        Configuration conf = res.getConfiguration();
//        int flag = conf.orientation;
//        if (flag == Configuration.ORIENTATION_PORTRAIT) {
//            recList.setLayoutManager(new StaggeredGridLayoutManager(3, GridLayoutManager.HORIZONTAL ));
//        } else if (flag == Configuration.ORIENTATION_LANDSCAPE) {
//            recList.setLayoutManager(new StaggeredGridLayoutManager(2, GridLayoutManager.VERTICAL ));
//        }

        if(savedInstanceState == null){
            businesses = new ArrayList<>();
        }
        else{
            businesses = (ArrayList<Business>) savedInstanceState.getSerializable(SAVE_KEY_BUSINESS_LIST);
        }
        bca = new BusinessCardAdapter(businesses);
        recList.setAdapter(bca);

        if (businesses.size() == 0)
            welcomeText.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);

        searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setOnQueryTextListener(this);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_sort:
                Collections.sort(businesses, new Comparator<Business>() {
                    @Override
                    public int compare(Business business1, Business business2) {
                        return business1.name().compareTo(business2.name());
                    }
                });
                updateList();
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(SAVE_KEY_BUSINESS_LIST, businesses);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        businesses = (ArrayList<Business>) savedInstanceState.getSerializable(SAVE_KEY_BUSINESS_LIST);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        searchView.clearFocus();
        Toast.makeText(getApplicationContext(), "Searching", Toast.LENGTH_SHORT).show();
        Map<String, String> params = new HashMap<>();
        params.put("term", query);
        params.put("sort", "2");
        params.put("limit", "10");
        params.put("lang", "en");

        Call<SearchResponse> call = yelpAPI.search("Milpitas", params);
        Callback<SearchResponse> callback = new Callback<SearchResponse>() {
            @Override
            public void onResponse(Call<SearchResponse> call, Response<SearchResponse> response) {
                SearchResponse searchResponse = response.body();

                handleResponce(searchResponse);
            }

            @Override
            public void onFailure(Call<SearchResponse> call, Throwable t) {
                // HTTP error happened, do something to handle it.
                Toast.makeText(getApplicationContext(), "Failed, There might be a problem with server", Toast.LENGTH_SHORT).show();
            }
        };

        call.enqueue(callback);
        return true;
    }

    private void handleResponce(SearchResponse searchResponse) {
        businesses = searchResponse.businesses();
        int totalNumberOfResult = searchResponse.total();
        updateList();
    }

    @Override
    public boolean onQueryTextChange(String query) {
        return false;
    }

    private void updateList(){
        recList.setAdapter(new BusinessCardAdapter(businesses));
        recList.invalidate();
        if (businesses.size()!=0)
            welcomeText.setVisibility(View.INVISIBLE);
    }

    public void logOut(View view) {
        ParseUser.logOut();
        ParseUser currentUser = ParseUser.getCurrentUser();
        if(currentUser==null){
            Intent intent = new Intent(SearchActivity.this,StartActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    }

    public void contact(View view){
        Intent intent = new Intent(SearchActivity.this,ContactActivity.class);
        startActivity(intent);
    }
}
