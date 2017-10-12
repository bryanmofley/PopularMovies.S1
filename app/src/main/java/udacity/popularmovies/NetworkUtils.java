package udacity.popularmovies;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.net.ssl.HttpsURLConnection;

/**
 * These utilities will be used to communicate with themoviedb.org servers
 * as well as parse the resulting JSON data.
 */

public final class NetworkUtils {

    static final String TMDB_SORT_BY_POPULARITY = "popular";
    static final String TMDB_SORT_BY_TOP_RATED = "top_rated";
    private static final String LOG_TAG = NetworkUtils.class.getSimpleName();
    /*
     * The following constants are specific to The MovieDB.  Note that
     * TMDB_API_KEY gets its value from gradle.properties
     */
    private static final String TMDB_API_KEY = BuildConfig.TMDB_API_KEY;
    private static final String TMDB_API_KEY_PARAM = "api_key";
    private static final String TMDB_DISCOVER_BASE_URL = "https://api.themoviedb.org/3/movie/";
    private static final String TMDB_POSTER_BASE_URL = "https://image.tmdb.org/t/p/";
    // poster sizes: w92, w154, w185, w342, w500, w780, original
    private static final String TMDB_POSTER_SIZE = "w342";
    private static final String TMDB_POSTER_URL = TMDB_POSTER_BASE_URL + TMDB_POSTER_SIZE;


    /**
     * This method builds a valid URL for querying The MovieDB
     *
     * @return the URL to use to query The MovieDB
     */
    public static URL buildUrl(String sortBy) {
        Uri builtUri = Uri.parse(TMDB_DISCOVER_BASE_URL + sortBy).buildUpon()
                .appendQueryParameter(TMDB_API_KEY_PARAM, TMDB_API_KEY)
                .build();

        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }

    /**
     * This method executes a url and returns the response
     *
     * @param url a URL to fetch HTTPS data from The MovieDB
     * @return the unparsed json data from The MovieDB
     * @throws IOException network and stream reading errors
     */
    public static String getResponseFromHttpsUrl(URL url) throws IOException {

        int responseCode = 0;
        InputStream in = null;

        HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();

        try {
            responseCode = urlConnection.getResponseCode();
            Log.i(LOG_TAG, "api.themoviedb.org HTTP Response Code: " + Integer.toString(responseCode));
            if (responseCode == 200) {
                in = urlConnection.getInputStream();
            } else {
                //likely a 401 or 404 error... go ahead and grab the contents to create an informed
                //error message later
                in = urlConnection.getErrorStream();
            }
            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                return scanner.next();
            } else {
                return null;
            }
        } finally {
            urlConnection.disconnect();
        }
    }

    /**
     * @param context The context this applies to
     * @param rawJson unaltered JSON data to be massaged
     * @return An array of Movies with all of their details
     * @throws JSONException error
     */
    public static List<Movie> getMovieDataFromJson(Context context, String rawJson) throws JSONException {
        // https://developers.themoviedb.org/3/discover/movie-discover for JSON details

        // The Movie Database response codes
        final String TMDB_STATUS_CODE = "status_code";
        final String TMDB_STATUS_MESSAGE = "status_message";
        final int TMDB_INVALID_API = 7;
        final int TMDB_RESOURCE_NOT_FOUND = 34;

        final String TMDB_RESULTS = "results";

        // The Movie Database Results object fields
        final String TMDB_ADULT = "adult";
        final String TMDB_BACKDROP_PATH = "backdrop_path";
        final String TMDB_GENRE_IDS = "genre_ids";
        final String TMDB_ID = "id";
        final String TMDB_ORIGINAL_LANGUAGE = "original_language";
        final String TMDB_ORIGINAL_TITLE = "original_title";
        final String TMDB_OVERVIEW = "overview";
        final String TMDB_POPULARITY = "popularity";
        final String TMDB_POSTER_PATH = "poster_path";
        final String TMDB_RELEASE_DATE = "release_date";
        final String TMDB_TITLE = "title";
        final String TMDB_VIDEO = "video";
        final String TMDB_VOTE_AVERAGE = "vote_average";
        final String TMDB_VOTE_COUNT = "vote_count";

        List<Movie> parsedMovieData = new ArrayList<Movie>();
        JSONObject movieJson = new JSONObject(rawJson);

        if (movieJson.has(TMDB_STATUS_CODE)) {
            int errorCode = movieJson.getInt(TMDB_STATUS_CODE);

            switch (errorCode) {
                case TMDB_INVALID_API:
                case TMDB_RESOURCE_NOT_FOUND:
                    Log.e(LOG_TAG, "api.themoviedb.org: " + movieJson.getString(TMDB_STATUS_MESSAGE));
                    break;
                default:
                    Log.e(LOG_TAG, "api.themoviedb.org: Unknown error");
            }
        }

        JSONArray movieDataArray = movieJson.getJSONArray(TMDB_RESULTS);

        for (int i = 0; i < movieDataArray.length(); i++) {
            JSONObject movie = movieDataArray.getJSONObject(i);

            /**
             * The genre data is one-to-many so we pull it out here and prep it for inclusion
             * in the Movie.Genre object
             */
            List<Movie.Genre> parsedGenreData = new ArrayList<Movie.Genre>();
            JSONArray genreDataArray = movie.optJSONArray(TMDB_GENRE_IDS);
            for (int j = 0; j < genreDataArray.length(); j++) {
                parsedGenreData.add(new Movie.Genre(genreDataArray.getInt(j)));
            }

            parsedMovieData.add(new Movie(
                    movie.getInt(TMDB_ID),
                    movie.getBoolean(TMDB_ADULT),
                            movie.getString(TMDB_BACKDROP_PATH),
                            parsedGenreData,
                    movie.getString(TMDB_ORIGINAL_LANGUAGE),
                    movie.getString(TMDB_ORIGINAL_TITLE),
                    movie.getString(TMDB_OVERVIEW),
                    movie.getDouble(TMDB_POPULARITY),
                            TMDB_POSTER_URL + movie.getString(TMDB_POSTER_PATH),
                            movie.getString(TMDB_RELEASE_DATE),
                            movie.getString(TMDB_TITLE),
                            movie.getBoolean(TMDB_VIDEO),
                            movie.getDouble(TMDB_VOTE_AVERAGE),
                            movie.getInt(TMDB_VOTE_COUNT)
                    )
            );
        }

        return parsedMovieData;
    }
}
