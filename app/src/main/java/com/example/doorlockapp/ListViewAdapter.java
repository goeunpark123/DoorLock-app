package com.example.doorlockapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import static com.example.doorlockapp.ShowPicActivity.TAG;


public class ListViewAdapter extends BaseAdapter {

    private TextView titleView;
    private ArrayList<ListViewItem> ItemList = new ArrayList<ListViewItem>();

    @Override
    public int getCount() {
        return ItemList.size();
    }

    @Override
    public Object getItem(int position) {
        return ItemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final int pos = position;
        final Context context = parent.getContext();

        if (convertView==null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.listview_item, parent, false);
        }

        titleView = (TextView) convertView.findViewById(R.id.title);

        final ListViewItem ListItem = ItemList.get(position);

        titleView.setText(ListItem.getTitle());

        LinearLayout area = (LinearLayout)convertView.findViewById(R.id.p_item);
        area.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(v.getContext(), ListItem.getTitle(), Toast.LENGTH_LONG).show();

                Intent intent = new Intent(v.getContext(), ShowPicActivity.class);
                intent.putExtra("uri", ListItem.getTitle());
            }
        });

        return convertView;
    }

    public void addItem(String time, String title) {
        ListViewItem item = new ListViewItem();

        item.setTitle(title);

        ItemList.add(item);
    }
}
