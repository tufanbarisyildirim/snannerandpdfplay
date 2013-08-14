package com.snpdfp.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.snpdfp.utils.SNPDFArrayAdapter;
import com.snpdfp.utils.SNPDFCContstants;
import com.snpdfp.utils.SNPDFPathManager;
import com.snpdfp.utils.SNPDFUtils;

public class SplitOutput extends SNPDFActivity {

  private List<File> files = new ArrayList<File>();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.split_output);

    Intent callingIntent = getIntent();
    if (callingIntent.getBooleanExtra(SNPDFCContstants.SUCCESS, true)) {
      SNPDFUtils.setSuccessText(this, callingIntent.getStringExtra(SNPDFCContstants.MESSAGE));
    } else {
      SNPDFUtils.setErrorText(this, callingIntent.getStringExtra(SNPDFCContstants.MESSAGE));
    }

    List<CharSequence> fileNames = callingIntent.getCharSequenceArrayListExtra(SNPDFCContstants.FILES);

    for (CharSequence name : fileNames) {
      files.add(new File(SNPDFPathManager.getRootDirectory(), (String) name));
    }

    ListView lv = (ListView) findViewById(R.id.list_snpdf_files);

    lv.setAdapter(new SNPDFArrayAdapter(this, files));

    lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
        mainFile = new File(SNPDFPathManager.getRootDirectory(), (String) ((TextView) v.findViewById(R.id.rowtext))
            .getText());
        showIntentForPickedPDF();
      }
    });
  }

  private void showIntentForPickedPDF() {
    Intent filePick = new Intent(this, PickedPDFActivity.class);
    filePick.putExtra(SNPDFCContstants.FILE_URI, mainFile.getAbsolutePath());
    startActivity(filePick);
  }

  public void deleteAllPDF(View view) {
    final Context context = this;
    getAlertDialog().setTitle("Delete all prepared files?").setMessage("Are you sure to delete all prepared files?")
        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();

            for (File file : files) {
              file.delete();
            }

            Toast.makeText(context, "All extracted files from last operation deleted!", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(context, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            startActivity(intent);
          }

        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
          }
        }).show();

  }

  public void shareAllPDF(View view) {
    ArrayList<Uri> path = new ArrayList<Uri>();
    for (File file : files) {
      path.add(Uri.fromFile(file));
    }

    Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
    intent.setType("application/pdf");
    String shareBody = "Emailing extracted files";

    intent.putExtra(Intent.EXTRA_TEXT, shareBody);
    intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, path);

    try {
      startActivity(Intent.createChooser(intent, "Share via..."));
    } catch (ActivityNotFoundException e) {
      Toast.makeText(this, "No Application Available to share extracted PDFs", Toast.LENGTH_SHORT).show();
    }
  }

}
