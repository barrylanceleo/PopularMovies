package com.barrylanceleo.popularmovies.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.barrylanceleo.popularmovies.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ImageListAdapter extends ArrayAdapter<JSONObject> {
    private static final String LOG_TAG = ImageListAdapter.class.getSimpleName();
    private Picasso mPicasso;
    private int mResourceId;
    public ImageListAdapter(Context context, int resourceId) {
        super(context, 0, new ArrayList<JSONObject>());
        mResourceId = resourceId;
        mPicasso = Picasso.with(context);
    }

    private class ImageLoadedCallback implements Callback {
        ProgressBar mProgressBar;
        ImageView thumbnailImageView;
        ImageLoadedCallback(ImageView thumbnailImageView, ProgressBar mProgressBar) {
            this.thumbnailImageView = thumbnailImageView;
            this.mProgressBar = mProgressBar;
        }

        @Override
        public void onSuccess() {
            if (mProgressBar != null) {
                mProgressBar.setVisibility(View.GONE);
                thumbnailImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            }

        }

        @Override
        public void onError() {
            if (mProgressBar != null) {
                mProgressBar.setVisibility(View.GONE);
                thumbnailImageView.setScaleType(ImageView.ScaleType.CENTER);
                thumbnailImageView.setImageResource(R.drawable.ic_error_outline_black_36dp);
            }
        }
    }
    /**
     * Cache of the children views for a member item.
     */
    private static class ViewHolder {
        private final ImageView thumbnailImageView;
        private final ProgressBar progressBar;

        private ViewHolder(View view) {
            thumbnailImageView = (ImageView) view.findViewById(R.id.image_thumbnail);
            progressBar = (ProgressBar) view.findViewById(R.id.image_thumbnail_progress);
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if(convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(mResourceId,
                    parent, false);

            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder)convertView.getTag();
        }
        final JSONObject imageJson = getItem(position);
        try {
            String imageLink = "http://image.tmdb.org/t/p/w780" +imageJson.getString("file_path");
            Log.i(LOG_TAG, "Loading: " +imageLink);
            viewHolder.progressBar.setVisibility(View.VISIBLE);
            mPicasso.load(imageLink)
                    .into(viewHolder.thumbnailImageView,
                            new ImageLoadedCallback(viewHolder.thumbnailImageView, viewHolder.progressBar));
        }
        catch (JSONException e) {
            Log.i(LOG_TAG, "Unable to get author and content from the JSON");
        }
        return convertView;
    }
}
