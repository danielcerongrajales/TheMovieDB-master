package com.polbins.themoviedb.app.main;

import android.content.Intent;
import android.os.Handler;
import android.os.Bundle;
import android.view.View;

import com.polbins.themoviedb.R;
import com.polbins.themoviedb.api.model.Images;
import com.polbins.themoviedb.api.model.Movie;
import com.polbins.themoviedb.app.App;
import com.polbins.themoviedb.app.detail.DetailActivity;
import com.polbins.themoviedb.databinding.ActivityDetailBinding;
import com.polbins.themoviedb.databinding.ActivityMainBinding;

import java.util.List;

import javax.inject.Inject;

import static com.polbins.themoviedb.app.detail.DetailActivity.MOVIE_ID;
import static com.polbins.themoviedb.app.detail.DetailActivity.MOVIE_TITLE;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class MainActivity extends AppCompatActivity implements
        MainContract.View,
        SwipeRefreshLayout.OnRefreshListener, EndlessScrollListener.ScrollToBottomListener, MoviesAdapter.ItemClickListener {
    private static final String TAG = "Main";
    private ActivityMainBinding binding;
    @Inject
    MainPresenter presenter;

//    @BindView(R.id.swipeRefreshLayout)
//    SwipeRefreshLayout swipeRefreshLayout;
//    @BindView(R.id.recyclerView)
//    RecyclerView contentView;
//    @BindView(R.id.textView)
//    View errorView;
//    @BindView(R.id.progressBar)
//    View loadingView;

    private MoviesAdapter moviesAdapter;
    private EndlessScrollListener endlessScrollListener;
    private Images images;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupContentView();
        DaggerMainComponent.builder()
                .appComponent(App.getAppComponent(getApplication()))
                .mainModule(new MainModule(this))
                .build()
                .inject(this);
        binding.textView.setOnClickListener(v -> {
            onClickErrorView();
        });

    }

    private void setupContentView() {
        binding.swipeRefreshLayout.setOnRefreshListener(this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        endlessScrollListener = new EndlessScrollListener(linearLayoutManager, this);
        binding.recyclerView.setLayoutManager(linearLayoutManager);
        binding.recyclerView.addOnScrollListener(endlessScrollListener);
    }

    @Override
    public void onRefresh() {
        endlessScrollListener.onRefresh();
        presenter.onPullToRefresh();
    }

    @Override
    public void onScrollToBottom() {
        presenter.onScrollToBottom();
    }

    @Override
    protected void onStart() {
        super.onStart();
        presenter.start();
    }

    @Override
    public void showLoading(boolean isRefresh) {
        if (isRefresh) {
            if (!binding.swipeRefreshLayout.isRefreshing()) {
                binding.swipeRefreshLayout.setRefreshing(true);
            }
        } else {
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.recyclerView.setVisibility(View.GONE);
            binding.textView.setVisibility(View.GONE);
        }
    }

    @Override
    public void showContent(List<Movie> movies, boolean isRefresh) {
        if (moviesAdapter == null) {
            moviesAdapter = new MoviesAdapter(movies, this, images, this);
            binding.recyclerView.setAdapter(moviesAdapter);
        } else {
            if (isRefresh) {
                moviesAdapter.clear();
            }
            moviesAdapter.addAll(movies);
            moviesAdapter.notifyDataSetChanged();
        }

        // Delay SwipeRefreshLayout animation by 1.5 seconds
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                binding.swipeRefreshLayout.setRefreshing(false);
            }
        }, 1500);

        binding.progressBar.setVisibility(View.GONE);
        binding.recyclerView.setVisibility(View.VISIBLE);
        binding.textView.setVisibility(View.GONE);
    }

    @Override
    public void showError() {
        binding.swipeRefreshLayout.setRefreshing(false);
        binding.progressBar.setVisibility(View.GONE);
        binding.recyclerView.setVisibility(View.GONE);
        binding.textView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onConfigurationSet(Images images) {
        this.images = images;

        if (moviesAdapter != null) {
            moviesAdapter.setImages(images);
        }
    }

    @Override
    public void onItemClick(int movieId, String movieTitle) {
        Intent i = new Intent(this, DetailActivity.class);
        i.putExtra(MOVIE_ID, movieId);
        i.putExtra(MOVIE_TITLE, movieTitle);
        startActivity(i);
    }


    void onClickErrorView() {
        presenter.start();
    }
}
