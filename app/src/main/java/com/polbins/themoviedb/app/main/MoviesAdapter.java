package com.polbins.themoviedb.app.main;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.polbins.themoviedb.R;
import com.polbins.themoviedb.api.model.Images;
import com.polbins.themoviedb.api.model.Movie;
import com.polbins.themoviedb.databinding.ItemBinding;

import java.util.List;


import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by polbins on 25/02/2017.
 */

public class MoviesAdapter extends RecyclerView.Adapter<MoviesAdapter.ViewHolder> {
     ItemBinding binding;
    private List<Movie> movies;
    private Activity activity;
    private Images images;
    private ItemClickListener itemClickListener;

    public MoviesAdapter(List<Movie> movies, Activity activity, Images images, ItemClickListener itemClickListener) {
        this.movies = movies;
        this.activity = activity;
        this.images = images;
        this.itemClickListener = itemClickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        binding = ItemBinding
                .inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);


    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Movie movie = movies.get(position);

        String fullImageUrl = getFullImageUrl(movie);
        if (!fullImageUrl.isEmpty()) {
            Glide.with(activity)
                    .load(fullImageUrl)
                    .apply(RequestOptions.centerCropTransform())
                    .transition(withCrossFade())
                    .into(binding.imageView);
        }

        String popularity = getPopularityString(movie.popularity);
        binding.popularityTextView.setText(popularity);
        binding.titleTextView.setText(movie.title);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                itemClickListener.onItemClick(movie.id, movie.title);
            }
        });
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

    @Override
    public int getItemCount() {
        return movies.size();
    }

    public void clear() {
        movies.clear();
    }

    public void addAll(List<Movie> movies) {
        this.movies.addAll(movies);
    }

    public void setImages(Images images) {
        this.images = images;
    }


    class ViewHolder extends RecyclerView.ViewHolder {
//        View itemView;
//        @BindView(R.id.imageView)
//        ImageView imageView;
//        @BindView(R.id.popularityTextView)
//        TextView popularityTextView;
//        @BindView(R.id.titleTextView)
//        TextView titleTextView;

        ViewHolder (ItemBinding binding) {
            super(binding.getRoot());
//            this.itemView = itemView;

        }

    }

    private String getPopularityString(float popularity) {
        java.text.DecimalFormat decimalFormat = new java.text.DecimalFormat("#.#");
        return decimalFormat.format(popularity);
    }

    public interface ItemClickListener {

        void onItemClick(int movieId, String title);

    }

}
