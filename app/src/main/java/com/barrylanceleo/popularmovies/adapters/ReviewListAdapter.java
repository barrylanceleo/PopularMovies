package com.barrylanceleo.popularmovies.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.barrylanceleo.popularmovies.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ReviewListAdapter extends ArrayAdapter<JSONObject> {
    private static final String LOG_TAG = ReviewListAdapter.class.getSimpleName();

    private int mResourceId;
    public ReviewListAdapter(Context context, int resourceId) {
        super(context, 0, new ArrayList<JSONObject>());
        mResourceId = resourceId;
    }

    /**
     * Cache of the children views for a member item.
     */
    private static class ViewHolder {
        private final TextView authorTextView;
        private final TextView contentTextView;

        private ViewHolder(View view) {
            authorTextView = (TextView) view.findViewById(R.id.review_author);
            contentTextView = (TextView) view.findViewById(R.id.review_content);
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
        JSONObject review = getItem(position);
        try {
            viewHolder.authorTextView.setText(review.getString("author"));
            viewHolder.contentTextView.setText(review.getString("content"));
        }
        catch (JSONException e) {
            Log.i(LOG_TAG, "Unable to get author and content from the JSON");
        }
        return convertView;
    }
}
