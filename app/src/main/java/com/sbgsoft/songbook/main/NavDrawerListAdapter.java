package com.sbgsoft.songbook.main;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.sbgsoft.songbook.R;
import com.sbgsoft.songbook.views.SongBookThemeTextView;

import java.util.ArrayList;

public class NavDrawerListAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<NavDrawerItem> navDrawerItems;
    private SongBookTheme mTheme;

    public NavDrawerListAdapter(Context context, ArrayList<NavDrawerItem> navDrawerItems){
        this.context = context;
        this.navDrawerItems = navDrawerItems;
        resetTheme();
    }

    @Override
    public int getCount() {
        return navDrawerItems.size();
    }

    @Override
    public Object getItem(int position) {
        return navDrawerItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater)
                    context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.nav_drawer_item, null);
        }

        // Get the views
        ImageView imgIcon = (ImageView) convertView.findViewById(R.id.icon);
        SongBookThemeTextView txtTitle = (SongBookThemeTextView) convertView.findViewById(R.id.title);

        // Set the view info
        imgIcon.setImageResource(navDrawerItems.get(position).getIcon());
        txtTitle.setText(navDrawerItems.get(position).getTitle());

        // Set the text color to match the theme
        txtTitle.setCustomText(mTheme.getTitleFontColor(), true, mTheme.getTitleFontShadowColor());

        return convertView;
    }

    public void resetTheme() {
        mTheme = MainActivity.dbAdapter.getCurrentSettings().getSongBookTheme();
    }
}
