package com.example.android.newsstage1;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;

public class Query {
    public static final String LOG_TAG = MainNewsActivity.class.getSimpleName();
    static Context mContext;

    public static ArrayList<News> extractNews(String NewsJSon) {
        ArrayList<News> NewsToday = new ArrayList<>();
        if (TextUtils.isEmpty(NewsJSon)) {
            Log.e(mContext.getString(R.string.Query), mContext.getString(R.string.emptyJson));
            return null;
        }
        try {
            JSONObject baseJsonResponse;
            JSONObject NewsResponse;
            JSONArray resultsArray;
            baseJsonResponse = new JSONObject(NewsJSon);
            if (baseJsonResponse.has(mContext.getString(R.string.response))) {
                NewsResponse = baseJsonResponse.getJSONObject(mContext.getResources().getString(R.string.response));

                if (NewsResponse.has(mContext.getString(R.string.results))) {
                    resultsArray = NewsResponse.getJSONArray(mContext.getString(R.string.results));

                    for (int i = 0; i < resultsArray.length(); i++) {
                        String webTitle = "";
                        String sectionName = "";
                        String DateTime = "";
                        String webUrl = "";
                        String thumbnail = "";
                        String contributor = "";
                        JSONObject currentNews = resultsArray.getJSONObject(i);
                        if (currentNews.has(mContext.getString(R.string.webTitle))) {
                            webTitle = currentNews.getString(mContext.getString(R.string.webTitle));
                        }
                        if (currentNews.has(mContext.getString(R.string.sectionName))) {
                            sectionName = currentNews.getString(mContext.getString(R.string.sectionName));
                        }
                        if (currentNews.has(mContext.getString(R.string.webUrl))) {
                            webUrl = currentNews.getString(mContext.getString(R.string.webUrl));
                        }
                        if (currentNews.has(mContext.getString(R.string.webPublicationDate))) {
                            DateTime = currentNews.getString(mContext.getString(R.string.webPublicationDate));
                        }

                        if (currentNews.has(mContext.getString(R.string.fields))) {
                            JSONObject fields = currentNews.getJSONObject(mContext.getString(R.string.fields));
                            if (fields.has(mContext.getString(R.string.thumbnail))) {
                                thumbnail = fields.getString(mContext.getString(R.string.thumbnail));
                            }
                        }
                        if (currentNews.has(mContext.getString(R.string.tags))) {
                            JSONArray tagsArray = currentNews.getJSONArray(mContext.getString(R.string.tags));
                            if (tagsArray.length() != 0) {
                                JSONObject tags = tagsArray.getJSONObject(0);
                                if (tags.has(mContext.getString(R.string.webTitle))) {
                                    contributor = tags.getString(mContext.getString(R.string.webTitle));
                                }
                            }
                        }
                        News NewNews = new News(webTitle, sectionName, webUrl, thumbnail, DateTime, contributor);
                        NewsToday.add(NewNews);
                    }
                }
            }
        } catch (JSONException e) {
            Log.e(mContext.getString(R.string.QueryUtils), mContext.getString(R.string.ProblemWithResults), e);
        }
        return NewsToday;
    }

    public static ArrayList<News> fetchNewsdata(String requestUrl, Context context) {
        mContext = context;
        URL url = createUrl(requestUrl);

        // Perform HTTP request to the URL and receive a JSON response back
        String jsonResponse = "";
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, mContext.getString(R.string.ProblemWithHTTP), e);
        }
        ArrayList<News> newsNow = Query.extractNews(jsonResponse);
        return newsNow;
    }

    private static URL createUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException exception) {
            Log.e(LOG_TAG, mContext.getString(R.string.ErrorWithUrl), exception);
            return null;
        }
        return url;
    }

    private static String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";

        if (url == null) {
            return jsonResponse;
        }
        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.connect();

            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, mContext.getString(R.string.ErrorCode) + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, mContext.getString(R.string.ProblemRetrievingJSON), e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }
}
