package com.artfara.apps.kipper;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

/**
 * Created by Van on 9/11/16.
 */
class PopUpAdapter implements GoogleMap.InfoWindowAdapter {
    LayoutInflater inflater=null;

    PopUpAdapter(LayoutInflater inflater) {
        this.inflater=inflater;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return(null);
    }

    @Override
    public View getInfoContents(Marker marker) {
        View popup=inflater.inflate(R.layout.popup, null);

        TextView tvTitle =(TextView) popup.findViewById(R.id.title);
        tvTitle .setText(marker.getTitle());

        TextView tvSnippet =(TextView)popup.findViewById(R.id.snippet);
        tvSnippet.setText(marker.getSnippet());

        TextView tvChatMsg =(TextView)popup.findViewById(R.id.chatmsg);
        tvChatMsg.setTypeface(null, Typeface.ITALIC);
        tvChatMsg.setText("Tap for chat");


        return(popup);
    }
}