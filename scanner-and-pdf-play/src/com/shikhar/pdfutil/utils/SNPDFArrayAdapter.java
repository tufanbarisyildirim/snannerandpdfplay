package com.shikhar.pdfutil.utils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import com.shikhar.pdfutil.R;

public class SNPDFArrayAdapter extends ArrayAdapter<File> {
  private final Context context;
  private final List<File> files;
  private List<File> filteredFiles;

  public SNPDFArrayAdapter(Context context, List<File> files) {
    super(context, R.layout.row_snpdf_view, files);
    this.context = context;
    this.files = files;
    this.filteredFiles = this.files;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    View rowView = inflater.inflate(R.layout.row_snpdf_view, parent, false);

    TextView fileNameView = (TextView) rowView.findViewById(R.id.rowtext);
    TextView fileDetailView = (TextView) rowView.findViewById(R.id.rowdetail);

    File file = filteredFiles.get(position);
    fileNameView.setText(file.getName());
    fileDetailView
        .setText(SNPDFUtils.getSizeText(file.length())
            + " | "
            + new SimpleDateFormat(SNPDFCContstants.DATE_FORMAT, Locale.getDefault()).format(new Date(file
                .lastModified())));
    ImageView imageView = (ImageView) rowView.findViewById(R.id.rowthumbnail);
    imageView.setBackgroundResource(R.drawable.pdf);
    if (!file.getName().toLowerCase().endsWith(".pdf")) {
      imageView.setBackgroundResource(R.drawable.text);
    }
    return rowView;
  }

  @Override
  public Filter getFilter() {
    return new Filter() {

      @Override
      protected void publishResults(CharSequence constraint, FilterResults results) {
        filteredFiles = (List<File>) results.values;
        notifyDataSetChanged();
      }

      @Override
      protected FilterResults performFiltering(CharSequence constraint) {
        constraint = constraint.toString().toLowerCase();
        FilterResults result = new FilterResults();
        if (constraint != null && constraint.toString().length() > 0) {
          List<File> filt = new ArrayList<File>();
          List<File> lItems = new ArrayList<File>();
          synchronized (this) {
            lItems.addAll(files);
          }

          for (File file : lItems) {
            if (file.getName().toLowerCase().contains(constraint))
              filt.add(file);
          }

          result.count = filt.size();
          result.values = filt;

        } else {
          synchronized (this) {
            result.values = files;
            result.count = files.size();
          }
        }

        return result;
      }
    };
  }

  @Override
  public int getCount() {
    return filteredFiles.size();
  }
}
