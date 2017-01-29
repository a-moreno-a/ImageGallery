package com.ariel.imagegallery;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;

import com.downloadtool.arielmoreno.downloadtool.DistributedContentDownloader;

public class GalleryActivity extends AppCompatActivity {
    private static Bundle mBundleRecyclerViewState;
    private final String KEY_RECYCLER_STATE = "recycler_state";
    StaggeredGridLayoutManager layoutManager;
    RecyclerView mRecyclerView;
    GalleryAdapter adapter;
    int count = 0;
    int total = 0;
    int imgcount;
    /**
     * this is used as to flag when the refresher is active
     */
    Integer pendingCount = new Integer(0);
    DistributedContentDownloader downloadManager;
    private SwipeRefreshLayout swipeContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        downloadManager = new DistributedContentDownloader(40 * 1024 * 1024);
        total = downloadManager.SubscribeDistributedContent("http://pastebin.com/raw/wgkJgazE", new String[]{"urls", "raw"});

        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.content_gallery);

        swipeContainer.setColorSchemeResources(R.color.colorAccent);

        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Your code to refresh the list here.
                // Make sure you call swipeContainer.setRefreshing(false)
                // once the network request has completed successfully.
                addData();
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopulateGrid();
            }
        });

        layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView = (RecyclerView) findViewById(R.id.gallery_grid);
        mRecyclerView.setLayoutManager(layoutManager);

        adapter = new GalleryAdapter(this, downloadManager);

        mRecyclerView.setAdapter(adapter);
        mRecyclerView.getRecycledViewPool().setMaxRecycledViews(0, 16);

        ItemSpaceCosmetic decoration = new ItemSpaceCosmetic(16);
        mRecyclerView.addItemDecoration(decoration);
    }

    private void addData() {
        PopulateGrid();
        PopulateGrid();
        PopulateGrid();
        PopulateGrid();
    }

    private void PopulateGrid() {
        adapter.insert(count % 10);
        mRecyclerView.getLayoutManager().scrollToPosition(0);
        count++;
    }

    public void IncPendingCount() {
        synchronized (pendingCount) {
            pendingCount++;
            if (pendingCount > 0) {
                swipeContainer.setRefreshing(true);
            }
        }
    }

    public void DecPendingCount() {
        synchronized (pendingCount) {
            pendingCount--;
            if (pendingCount == 0) {
                swipeContainer.setRefreshing(false);
            }
        }
    }

}
