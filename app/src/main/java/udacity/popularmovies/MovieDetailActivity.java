package udacity.popularmovies;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class MovieDetailActivity extends AppCompatActivity {

    private TextView mMovieTitleTextView;
    private ImageView mMoviePosterImageView;
    private TextView mMovieOverviewTextView;
    private TextView mMovieUserRatingTextView;
    private TextView mMovieReleaseDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);

        mMovieTitleTextView = (TextView) findViewById(R.id.tv_movie_title);
        mMoviePosterImageView = (ImageView) findViewById(R.id.iv_movie_poster);
        mMovieOverviewTextView = (TextView) findViewById(R.id.tv_movie_overview);
        mMovieUserRatingTextView = (TextView) findViewById(R.id.tv_user_rating);
        mMovieReleaseDate = (TextView) findViewById(R.id.tv_release_date);

        Intent intent = getIntent();

        if (intent != null) {
            if (intent.hasExtra("movieItem")) {
                Movie movie = intent.getExtras().getParcelable("movieItem");
                mMovieTitleTextView.setText(movie.title);
                mMovieOverviewTextView.setText(movie.overview);
                mMovieUserRatingTextView.setText("User Rating: " + Double.toString(movie.vote_average));
                mMovieReleaseDate.setText("Release Date: " + movie.release_date);

                Picasso.with(this)
                        .load(movie.poster_path)
                        .into(mMoviePosterImageView);
            }
        }
    }

}
