package udacity.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.net.URL;
import java.util.List;

public class MainActivity extends AppCompatActivity implements MovieAdapter.MovieAdapterOnClickHandler {

    private final int SPAN_COUNT = 2;
    private TextView mErrorMessageTextView;
    private ProgressBar mProgressBar;
    private RecyclerView mRecyclerView;
    private MovieAdapter mMovieAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecyclerView = (RecyclerView) findViewById(R.id.rv_movies);
        mErrorMessageTextView = (TextView) findViewById(R.id.tv_error_message);
        mProgressBar = (ProgressBar) findViewById(R.id.pb_loading_indicator);

        GridLayoutManager layoutManager =
                new GridLayoutManager(this, SPAN_COUNT, LinearLayoutManager.VERTICAL, false);

        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(false);
        mMovieAdapter = new MovieAdapter(this);
        mRecyclerView.setAdapter(mMovieAdapter);

        loadMovieData(NetworkUtils.TMDB_SORT_BY_POPULARITY);
    }

    private void loadMovieData(String sortBy) {
        showMovieDataView();

        new TheMovieDatabaseQueryTask().execute(sortBy);
    }

    private void showMovieDataView() {
        mErrorMessageTextView.setVisibility(View.INVISIBLE);
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    private void showErrorMessage() {
        mRecyclerView.setVisibility(View.INVISIBLE);
        mErrorMessageTextView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(Movie movieItem) {
        Class movieDetailActivity = MovieDetailActivity.class;
        Intent intent = new Intent(this, movieDetailActivity);
        intent.putExtra("movieItem", movieItem);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.movies, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_sort_popular:
                mMovieAdapter.setMovieData(null);
                loadMovieData(NetworkUtils.TMDB_SORT_BY_POPULARITY);
                break;
            case R.id.action_sort_top_rated:
                mMovieAdapter.setMovieData(null);
                loadMovieData(NetworkUtils.TMDB_SORT_BY_TOP_RATED);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Background thread to initiating fetching data from the internet and returning it to the activity
     */
    private class TheMovieDatabaseQueryTask extends AsyncTask<String, Void, List<Movie>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected List<Movie> doInBackground(String... params) {
            String sortBy;
            if (params.length == 0) {
                sortBy = NetworkUtils.TMDB_SORT_BY_POPULARITY;
            } else {
                sortBy = params[0];
            }

            URL tmdbRequestUrl = NetworkUtils.buildUrl(sortBy);

            try {
                String jsonMovieResponse = NetworkUtils
                        .getResponseFromHttpsUrl(tmdbRequestUrl);

                List<Movie> simpleJsonMovieData = NetworkUtils
                        .getMovieDataFromJson(MainActivity.this, jsonMovieResponse);
                return simpleJsonMovieData;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<Movie> movieData) {
            mProgressBar.setVisibility(View.INVISIBLE);
            if (movieData != null) {
                showMovieDataView();

                mMovieAdapter.setMovieData(movieData);
            } else {
                showErrorMessage();
            }
        }
    }
}
