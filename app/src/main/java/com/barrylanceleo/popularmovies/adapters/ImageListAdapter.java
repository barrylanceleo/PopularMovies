package com.barrylanceleo.popularmovies.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.barrylanceleo.popularmovies.R;
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

    /**
     * Cache of the children views for a member item.
     */
    public static class ViewHolder {
        public final CardView thumbnailCard;
        public final ImageView thumbnailImageView;

        public ViewHolder(View view) {
            thumbnailCard = (CardView) view.findViewById(R.id.image_thumbnail_card_view);
            thumbnailImageView = (ImageView) view.findViewById(R.id.image_thumbnail);
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
            mPicasso.load(imageLink)
                    .into(viewHolder.thumbnailImageView);
        }
        catch (JSONException e) {
            Log.i(LOG_TAG, "Unable to get author and content from the JSON");
        }
        return convertView;
    }
}
