package com.polbins.themoviedb.app.detail;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.polbins.themoviedb.R;
import com.polbins.themoviedb.api.model.Genre;
import com.polbins.themoviedb.api.model.Images;
import com.polbins.themoviedb.api.model.Movie;
import com.polbins.themoviedb.api.model.SpokenLanguage;
import com.polbins.themoviedb.app.App;
import com.polbins.themoviedb.databinding.ActivityDetailBinding;

import javax.inject.Inject;



import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.content.ContextCompat;

public class DetailActivity extends AppCompatActivity implements DetailContract.View {
    private ActivityDetailBinding binding;

    public static final String MOVIE_ID = "movie_id";
    public static final String MOVIE_TITLE = "movie_title";

    @Inject
    DetailPresenter detailPresenter;

    private int movieId = -1;
    private Images images;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        DaggerDetailComponent.builder()
                .appComponent(App.getAppComponent(getApplication()))
                .detailModule(new DetailModule(this))
                .build()
                .inject(this);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            movieId = extras.getInt(MOVIE_ID);
            String movieTitle = extras.getString(MOVIE_TITLE);

            setTitle(movieTitle);
        }
        binding.bookButton.setOnClickListener(v -> {
            onBookButtonClick();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        detailPresenter.start(movieId);
    }

    @Override
    public void showLoading() {
        binding.progressBar.setVisibility(View.VISIBLE);
        showContent(false);
        binding.textView.setVisibility(View.GONE);
    }

    @Override
    public void showContent(Movie movie) {
        String fullImageUrl = getFullImageUrl(movie);

        if (!fullImageUrl.isEmpty()) {
            Glide.with(this)
                    .load(fullImageUrl)
                    .apply(RequestOptions.centerCropTransform())
                    .transition(withCrossFade())
                    .into(binding.imageView);
        }

        binding.overviewTextView.setText(getOverview(movie.overview));
        binding.genresTextView.setText(getGenres(movie));
        binding.durationTextView.setText(getDuration(movie));
        binding.languageTextView.setText(getLanguages(movie));

        binding.progressBar.setVisibility(View.GONE);
        showContent(true);
        binding.textView.setVisibility(View.GONE);
    }

    private String getDuration(Movie movie) {
        int runtime = movie.runtime;
        return runtime <= 0 ? "-" : getResources().getQuantityString(R.plurals.duration, runtime, runtime);
    }

    private String getOverview(String overview) {
        return TextUtils.isEmpty(overview) ? "-" : overview;
    }

    @NonNull
    private String getFullImageUrl(Movie movie) {
        String imagePath;

        if (movie.posterPath != null && !movie.posterPath.isEmpty()) {
            imagePath = movie.posterPath;
        } else {
            imagePath = movie.backdropPath;
        }

        if (images != null && images.baseUrl != null && !images.baseUrl.isEmpty()) {
            if (images.posterSizes != null) {
                if (images.posterSizes.size() > 4) {
                    // usually equal to 'w500'
                    return images.baseUrl + images.posterSizes.get(4) + imagePath;
                } else {
                    // back-off to hard-coded value
                    return images.baseUrl + "w500" + imagePath;
                }
            }
        }

        return "";
    }

    private String getGenres(Movie movie) {
        String genres = "";
        for (int i = 0; i < movie.genres.size(); i++) {
            Genre genre = movie.genres.get(i);
            genres += genre.name + ", ";
        }

        genres = removeTrailingComma(genres);

        return genres.isEmpty() ? "-" : genres;
    }

    private String getLanguages(Movie movie) {
        String languages = "";
        for (int i = 0; i < movie.spokenLanguages.size(); i++) {
            SpokenLanguage language = movie.spokenLanguages.get(i);
            languages += language.name + ", ";
        }

        languages = removeTrailingComma(languages);

        return languages.isEmpty() ? "-" : languages;
    }

    @NonNull
    private String removeTrailingComma(String text) {
        text = text.trim();
        if (text.endsWith(",")) {
            text = text.substring(0, text.length() - 1);
        }
        return text;
    }

    @Override
    public void showError() {
        binding.progressBar.setVisibility(View.GONE);
        showContent(false);
        binding.textView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onConfigurationSet(Images images) {
        this.images = images;
    }

    private void showContent(boolean show) {
        int visibility = show ? View.VISIBLE : View.INVISIBLE;

        binding.container.setVisibility(visibility);
        binding.overviewHeader.setVisibility(visibility);
        binding.overviewTextView.setVisibility(visibility);
        binding.bookButton.setVisibility(visibility);
    }


    void onBookButtonClick() {
        String url = getString(R.string.web_url) + movieId;

        if (Build.VERSION.SDK_INT >= 16) {
            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
            builder.setToolbarColor(ContextCompat.getColor(this, R.color.colorPrimary));
            CustomTabsIntent customTabsIntent = builder.build();
            customTabsIntent.launchUrl(this, Uri.parse(url));
        } else {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        }
    }

}
