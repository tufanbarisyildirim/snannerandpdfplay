package com.shikhar.pdfutil.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import com.shikhar.pdfutil.R;

public class SimpleArrayAdapter extends ArrayAdapter<Pair<String, String>> {
  private final Context context;
  private final List<Pair<String, String>> items;
  private List<Pair<String, String>> filteredItems;

  public SimpleArrayAdapter(Context context, List<Pair<String, String>> items) {
    super(context, R.layout.searchable_row, items);
    this.context = context;
    this.items = items;
    this.filteredItems = this.items;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    View rowView = inflater.inflate(R.layout.searchable_row, parent, false);

    Pair<String, String> item = filteredItems.get(position);
    ((TextView) rowView.findViewById(R.id.rowtext)).setText(item.second);
    ((TextView) rowView.findViewById(R.id.rowPath)).setText(item.first);
    rowView.findViewById(R.id.rowPath).setVisibility(View.GONE);

    if (new File(item.first).isDirectory()) {
      ImageView imageView = (ImageView) rowView.findViewById(R.id.rowthumbnail);
      imageView.setBackgroundResource(R.drawable.folder);
    } else {
      rowView.findViewById(R.id.rowthumbnail).setVisibility(View.GONE);
    }

    return rowView;
  }

  @Override
  public Filter getFilter() {
    return new Filter() {

      @Override
      protected void publishResults(CharSequence constraint, FilterResults results) {
        filteredItems = (List<Pair<String, String>>) results.values;
        notifyDataSetChanged();
      }

      @Override
      protected FilterResults performFiltering(CharSequence constraint) {
        constraint = constraint.toString().toLowerCase();
        FilterResults result = new FilterResults();
        if (constraint != null && constraint.toString().length() > 0) {
          List<Pair<String, String>> names = new ArrayList<Pair<String, String>>();
          List<Pair<String, String>> lItems = new ArrayList<Pair<String, String>>();
          synchronized (this) {
            lItems.addAll(items);
          }

          for (Pair<String, String> name : lItems) {
            if (name.second.toLowerCase().contains(constraint))
              names.add(name);
          }

          result.count = names.size();
          result.values = names;

        } else {
          synchronized (this) {
            result.values = items;
            result.count = items.size();
          }
        }

        return result;
      }
    };
  }

  @Override
  public int getCount() {
    return filteredItems.size();
  }

}
