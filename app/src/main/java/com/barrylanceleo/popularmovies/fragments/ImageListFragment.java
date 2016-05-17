package com.barrylanceleo.popularmovies.fragments;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.barrylanceleo.popularmovies.MovieDbApiHelper;
import com.barrylanceleo.popularmovies.R;
import com.barrylanceleo.popularmovies.Utility;
import com.barrylanceleo.popularmovies.adapters.ImageListAdapter;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class ImageListFragment extends Fragment {
    private static final String LOG_TAG = ImageListFragment.class.getSimpleName();

    private static final String MOVIE_ID = "movie_id";
    private int mMovieId;
    private ListView mImageListView;
    private ImageListAdapter mImageListAdapter;
    private SwipeRefreshLayout mImageSwipeRefreshLayout;
    private TextView mNoImageTextView;
    private MovieDbApiHelper mMovieDbHelper;

    // image download queue, only one item needs to be queue
    boolean mIsDownloadPending = false;
    String mImageUrlToDownload;
    String mImageNameToDownload;


    public ImageListFragment() {
        // Required empty public constructor
    }

    public static ImageListFragment newInstance(int movieId) {
        ImageListFragment fragment = new ImageListFragment();
        Bundle args = new Bundle();
        args.putInt(MOVIE_ID, movieId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mMovieId = getArguments().getInt(MOVIE_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_image_list, container, false);
        mImageSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.image_swipreRefresh);
        mImageSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshImages(mMovieId);
            }
        });
        mImageListView = (ListView) rootView.findViewById(R.id.image_list);
        mNoImageTextView = (TextView) rootView.findViewById(R.id.image_empty_textView);
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mImageListAdapter = new ImageListAdapter(getContext(), R.layout.list_item_image);
        mImageListView.setAdapter(mImageListAdapter);
        mImageListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final JSONObject imageJson = mImageListAdapter.getItem(position);
                try {
                    String imageLink = "http://image.tmdb.org/t/p/w780" +imageJson.getString("file_path");
                    double imageAspectRatio = Double.parseDouble(imageJson.getString("aspect_ratio"));

                    // display it in a dialog box with download button
                    AlertDialog.Builder imageAlertBuilder = new AlertDialog.Builder(getContext());
                    imageAlertBuilder.setView(R.layout.list_item_image);
                    LayoutInflater inflater = LayoutInflater.from(getContext());
                    RelativeLayout dialogLayout = (RelativeLayout)inflater.inflate(R.layout.dialog_image, null);
                    ImageView dialogImageView = (ImageView) dialogLayout.findViewById(R.id.diaglog_image);

                    // set up the layout params of the image view based on the aspect ratio
                    DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
                    ViewGroup.LayoutParams layoutParams = dialogImageView.getLayoutParams();
                    layoutParams.width = (int)Math.ceil(displayMetrics.widthPixels);
                    layoutParams.height = (int)(displayMetrics.widthPixels/imageAspectRatio);
                    dialogImageView.setLayoutParams(layoutParams);
                    Log.i(LOG_TAG, "Loading: " +imageLink);
                    Picasso picasso = Picasso.with(getContext());
                    picasso.load(imageLink)
                            .into(dialogImageView);

                    imageAlertBuilder.setView(dialogLayout);
                    imageAlertBuilder.setNeutralButton("Save to phone", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                String imageLink = "http://image.tmdb.org/t/p/original" +imageJson.getString("file_path");
                                String fileName = imageJson.getString("file_path").substring(1);
                                OnImageDownloadStart(imageLink, fileName);
                            }
                            catch (JSONException e) {
                                Log.e(LOG_TAG, "Unable to image link from JSON");
                            }

                        }
                    });
                    imageAlertBuilder.show();
                   // getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(imageLink)));
                }
                catch (JSONException e) {
                    Log.e(LOG_TAG, "Unable to image link from JSON");
                }
            }
        });
        mMovieDbHelper = MovieDbApiHelper.getInstance(getContext().getResources().getString(R.string.api_key));
        refreshImages(mMovieId);
    }

    void refreshImages(final int movieId) {
        mImageSwipeRefreshLayout.setRefreshing(true);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final List<JSONObject> imageList = mMovieDbHelper.getImages(movieId);
                    ImageListFragment.this.getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mImageListAdapter.clear();
                            mImageListAdapter.addAll(imageList);
                        }
                    });
                    onRefreshCompleted(imageList.size());
                }
                catch (MovieDbApiHelper.UnableToFetchDataException e) {
                    onRefreshCompleted(-1);
                }
            }
        }).start();
    }

    void onRefreshCompleted(final int imageCount) {
        ImageListFragment.this.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(imageCount <= 0) {
                    // no reviews
                    mNoImageTextView.setVisibility(View.VISIBLE);
                    mImageListView.setVisibility(View.GONE);

                    if(imageCount < 0) {
                        // no internet
                        Snackbar.make(mNoImageTextView,
                                getString(R.string.no_internet) +" " +getString(R.string.refresh_direction),
                                Snackbar.LENGTH_LONG).setAction("Action", null).show();
                    }
                }
                else {
                    mNoImageTextView.setVisibility(View.GONE);
                    mImageListView.setVisibility(View.VISIBLE);
                }
                mImageSwipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    void OnImageDownloadStart(final String imageUrl, final String imageName) {

        // check if we have permission to write to external storage
        // if we do not have permission, queue the download and request permission
        if(!(ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {

            //  queue the download
            if(mIsDownloadPending) {
                Log.wtf(LOG_TAG, "I messed up. " +
                        "Second file needs to be downloaded while the first one is still pending.");
            }
            mIsDownloadPending = true;
            mImageUrlToDownload = imageUrl;
            mImageNameToDownload = imageName;

            // show a snack bar about the permission required
            Snackbar.make(mImageListView,
                    "Storage access is required to save image to phone.",
                    Snackbar.LENGTH_LONG).setAction("Action", null).show();

            // request permission
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    0);

            // leave
            return;
        }

        // get the default pictures directory, create it if it doesn't exist
        File picturesDirectory = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        final File destinationDirectory;
        if(!picturesDirectory.exists()) {
            if(!picturesDirectory.mkdir()) {
                return;
            }
        }

        // create the app specific directory
        destinationDirectory = new File(picturesDirectory, Utility.IMAGE_DIRECTORY);
        if(!destinationDirectory.isDirectory()) {
            if(destinationDirectory.isFile()) {
                if(!destinationDirectory.delete()) {
                    // return if we are unable to delete
                    return;
                }
            }
            if(!destinationDirectory.mkdir()) {
                return;
            }
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    File downloadedFile = Utility.downloadFile(imageUrl,
                            new File(destinationDirectory, imageName).getAbsolutePath());
                    onDownloadComplete(downloadedFile);
                }
                catch (MovieDbApiHelper.UnableToFetchDataException e) {
                    Log.e(LOG_TAG, "No able to create file");
                }
            }
        }).start();
    }

    void onDownloadComplete(File downloadedFile) {
        if(downloadedFile == null) {
            Snackbar.make(mImageListView,
                    "Download Failed. No Internet.",
                    Snackbar.LENGTH_LONG).setAction("Action", null).show();
        }
        else {
            // create a bitmap of the image file
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap bitmap = BitmapFactory.decodeFile(downloadedFile.getAbsolutePath(), options);

            // create pending intent for the notification
            Intent openImageIntent =new Intent(Intent.ACTION_VIEW);
            openImageIntent.setDataAndType(Uri.fromFile(downloadedFile), "image/*");
            PendingIntent pendingIntent = PendingIntent.getActivity(getContext(), 0, openImageIntent, 0);
            // build the notification
            Notification.Builder notificationBuilder = new Notification.Builder(getContext());
            notificationBuilder.setSmallIcon(R.drawable.ic_done_black_36dp)
                    .setContentTitle("Download Complete")
                    .setContentText("Touch to open")
                    .setStyle(new Notification.BigPictureStyle().bigPicture(bitmap))
                    .setContentIntent(pendingIntent);
            NotificationManager mNotificationManager =
                    (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(0, notificationBuilder.build());

            // scan so that the mediaManager picks up the new file
            MediaScannerConnection.scanFile(getContext(),
                    new String[] { downloadedFile.getAbsolutePath() },
                    null,
                    null);

            Snackbar.make(mImageListView,
                    "Download Complete. Please check your notification bar.",
                    Snackbar.LENGTH_LONG).setAction("Action", null).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 0: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    if(mIsDownloadPending) {
                        mIsDownloadPending = false;
                        OnImageDownloadStart(mImageUrlToDownload, mImageNameToDownload);
                    }

                } else {
                    // permission denied
                }
                return;
            }
        }
    }

}
