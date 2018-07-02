package com.example.android.newsstage1;

import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import java.util.ArrayList;

public class MainNewsActivity extends AppCompatActivity implements LoaderCallbacks<ArrayList<News>> {

    private static final int NEWS_LOADER_ID = 1;
    private static final String NEWS_REQUEST_URL = "https://content.guardianapis.com/search?q=NOT%20Sport&api-key=*ENTERKEY**&show-fields=thumbnail&show-tags=contributor";
    private ImageView mEmptyStateTextView;
    private NewsAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.news_activity_main);

        ListView NewsListView = (ListView) findViewById(R.id.list);

        mAdapter = new NewsAdapter(this, new ArrayList<News>());
        NewsListView.setAdapter(mAdapter);

        mEmptyStateTextView = findViewById(R.id.empty_view);
        NewsListView.setEmptyView(mEmptyStateTextView);

        NewsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                News currentNews = mAdapter.getItem(i);
                Uri NewsUri = Uri.parse(currentNews.getmUrl());
                Intent websiteIntent = new Intent(Intent.ACTION_VIEW, NewsUri);
                startActivity(websiteIntent);
            }
        });
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {

            LoaderManager loaderManager = getLoaderManager();
            loaderManager.initLoader(NEWS_LOADER_ID, null, this);
        } else {
            View loadingIndicator = findViewById(R.id.loading_indicator);
            loadingIndicator.setVisibility(View.GONE);
            mEmptyStateTextView.setImageResource(R.drawable.photo);
        }
    }

    @Override
    public Loader<ArrayList<News>> onCreateLoader(int i, Bundle bundle) {
        return new NewsLoader(this, NEWS_REQUEST_URL);
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<News>> loader, ArrayList<News> news) {
        mAdapter.clear();
        if (news != null && !news.isEmpty()) {
            mAdapter.addAll(news);
        }
        mEmptyStateTextView.setImageResource(R.drawable.wotnonews);

        View loadingIndicator = findViewById(R.id.loading_indicator);
        loadingIndicator.setVisibility(View.GONE);
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<News>> loader) {
        mAdapter.clear();
    }
}
