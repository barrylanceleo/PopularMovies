package com.barrylanceleo.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

public class MovieDetails extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        ActionBar actionBar = getSupportActionBar();

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);


        // get the extras from Intent
        Intent mIntent = getIntent();
        Bundle movieDetailsBundle = mIntent.getExtras();

        // load the backdrop image along with its progress bar
        final ProgressBar backDropProgressBar = (ProgressBar) findViewById(R.id.backdrop_image_details_progress);
        backDropProgressBar.setVisibility(View.VISIBLE);
        ImageView backdropImageView = (ImageView) findViewById(R.id.backdropImageViewDetails);
        Picasso.with(this).load(movieDetailsBundle.getString("backdropUrl"))
                .into(backdropImageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        if (backDropProgressBar != null) {
                            backDropProgressBar.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onError() {
                        // NO-OP
                    }
                });

        // set the movie title
        TextView titleTextView = (TextView) findViewById(R.id.movie_title_details);
        titleTextView.setText(movieDetailsBundle.getString("title"));

        // set the overview
        TextView overviewTextView = (TextView) findViewById(R.id.movie_overview_details);
        overviewTextView.setText("\t\t\t");
        overviewTextView.append(movieDetailsBundle.getString("overview"));

        // load poster image
        final ProgressBar posterProgressBar = (ProgressBar) findViewById(R.id.poster_details_progress);
        posterProgressBar.setVisibility(View.VISIBLE);
        ImageView posterImageView = (ImageView) findViewById(R.id.poster_details_image_view);
        Picasso.with(this).load(movieDetailsBundle.getString("posterUrl"))
                .into(posterImageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        if (posterProgressBar != null) {
                            posterProgressBar.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onError() {
                        // NO-OP
                    }
                });

        // set the release date
        TextView releaseDateTextView = (TextView) findViewById(R.id.release_date_details);
        releaseDateTextView.append("\n" + movieDetailsBundle.getString("release_date"));

        // set the ratings
        TextView ratingsTextView = (TextView) findViewById(R.id.ratings_details);
        ratingsTextView.append("\n" + movieDetailsBundle.getDouble("vote_average") + "/10");


    }

}
