package com.barrylanceleo.popularmovies.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.barrylanceleo.popularmovies.R;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class VideoListAdapter extends ArrayAdapter<JSONObject> {
    private static final String LOG_TAG = VideoListAdapter.class.getSimpleName();
    private Picasso mPicasso;
    private int mResourceId;
    public VideoListAdapter(Context context, int resourceId) {
        super(context, 0, new ArrayList<JSONObject>());
        mResourceId = resourceId;
        mPicasso = Picasso.with(context);
    }

    /**
     * Cache of the children views for a member item.
     */
    private static class ViewHolder {
        private final CardView thumbnailCard;
        private final ImageView thumbnailImageView;
        private final TextView nameTextView;
        private final TextView typeTextView;
        private final TextView resolutionTextView;
        private final TextView siteTextView;


        private ViewHolder(View view) {
            thumbnailCard = (CardView) view.findViewById(R.id.video_thumbnail_card_view);
            thumbnailImageView = (ImageView) view.findViewById(R.id.video_thumbnail);
            nameTextView = (TextView) view.findViewById(R.id.video_name);
            typeTextView = (TextView) view.findViewById(R.id.video_type);
            resolutionTextView = (TextView) view.findViewById(R.id.video_resolution);
            siteTextView = (TextView) view.findViewById(R.id.video_site);
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;
        if(convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(mResourceId,
                    parent, false);

            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder)convertView.getTag();
        }
        final JSONObject videoJson = getItem(position);
        try {
            viewHolder.nameTextView.setText(videoJson.getString("name"));
            viewHolder.typeTextView.setText(videoJson.getString("type"));
            viewHolder.resolutionTextView.setText(videoJson.getString("size") +"p");
            viewHolder.siteTextView.setText(videoJson.getString("site"));
            String imageLink = "http://img.youtube.com/vi/" +videoJson.getString("key") +"/sddefault.jpg";
            Log.i(LOG_TAG, "Loading: " +imageLink);
            mPicasso.load(imageLink)
                    .into(viewHolder.thumbnailImageView);

            viewHolder.thumbnailCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        String site = videoJson.getString("site");
                        String key = videoJson.getString("key");
                        // open an intent to watch the video
                        if(site.equalsIgnoreCase("youtube")) {
                            String youtubeLink = "http://www.youtube.com/watch?v=" +key;
                            Log.i(LOG_TAG, "Opening: " +youtubeLink);

                            // Create an intent
                            Intent openVideoIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(youtubeLink));
                            // check if the implicit intent will resolve to an application
                            if (openVideoIntent.resolveActivity(getContext().getPackageManager()) != null) {
                                getContext().startActivity(openVideoIntent);
                            }
                            else {
                                Snackbar.make(viewHolder.thumbnailImageView,
                                        "No app found to play the video.",
                                        Snackbar.LENGTH_LONG).setAction("Action", null).show();
                            }
                        }
                        else {
                            Snackbar.make(viewHolder.thumbnailImageView,
                                    "Unsupported video site.",
                                    Snackbar.LENGTH_LONG).setAction("Action", null).show();
                        }
                    }
                    catch (JSONException e) {
                        Log.e(LOG_TAG, "Unable to get site and key from Json", e);
                    }
                }
            });


        }
        catch (JSONException e) {
            Log.i(LOG_TAG, "Unable to get author and content from the JSON");
        }
        return convertView;
    }
}
