package com.example.android.newsstage1;

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

    public static ArrayList<News> extractNews(String NewsJSon) {
        ArrayList<News> NewsToday = new ArrayList<>();
        if (TextUtils.isEmpty(NewsJSon)) {
            Log.e("Query", "NewsJSON is empty");
            return null;
        }
        try {
            JSONObject baseJsonResponse;
            JSONObject NewsResponse;
            JSONArray resultsArray;
            baseJsonResponse = new JSONObject(NewsJSon);
            if (baseJsonResponse.has("response")) {
                NewsResponse = baseJsonResponse.getJSONObject("response");

                if (NewsResponse.has("results")) {
                    resultsArray = NewsResponse.getJSONArray("results");

                    for (int i = 0; i < resultsArray.length(); i++) {
                        String webTitle = "";
                        String sectionName = "";
                        String DateTime = "";
                        String webUrl = "";
                        String thumbnail = "";
                        String contributor = "";
                        JSONObject currentNews = resultsArray.getJSONObject(i);
                        if (currentNews.has("webTitle")) {
                            webTitle = currentNews.getString("webTitle");
                        }
                        if (currentNews.has("sectionName")) {
                            sectionName = currentNews.getString("sectionName");
                        }
                        if (currentNews.has("webUrl")) {
                            webUrl = currentNews.getString("webUrl");
                        }
                        if (currentNews.has("webPublicationDate")) {
                            DateTime = currentNews.getString("webPublicationDate");
                        }

                        if (currentNews.has("fields")) {
                            JSONObject fields = currentNews.getJSONObject("fields");
                            if (fields.has("thumbnail")) {
                                thumbnail = fields.getString("thumbnail");
                            }
                        }
                        if (currentNews.has("tags")) {
                            JSONArray tagsArray = currentNews.getJSONArray("tags");
                            if (tagsArray.length() != 0) {
                                JSONObject tags = tagsArray.getJSONObject(0);
                                if (tags.has("webTitle")) {
                                    contributor = tags.getString("webTitle");
                                }
                            }
                        }
                        News NewNews = new News(webTitle, sectionName, webUrl, thumbnail, DateTime, contributor);
                        NewsToday.add(NewNews);
                    }
                }
            }
        } catch (JSONException e) {
            Log.e("QueryUtils", "Problem parsing the News JSON results", e);
        }
        return NewsToday;
    }

    public static ArrayList<News> fetchNewsdata(String requestUrl) {
        URL url = createUrl(requestUrl);

        // Perform HTTP request to the URL and receive a JSON response back
        String jsonResponse = "";
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem making the HTTP request", e);
        }
        ArrayList<News> newsNow = Query.extractNews(jsonResponse);
        return newsNow;
    }

    private static URL createUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException exception) {
            Log.e(LOG_TAG, "Error with creating URL", exception);
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
                Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem retrieving the News JSON results", e);
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
