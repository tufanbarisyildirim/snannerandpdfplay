package com.shikhar.pdfutil;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.shikhar.pdfutil.utils.SNPDFCContstants;
import com.shikhar.pdfutil.utils.SNPDFCheckedArrayAdapter;
import com.shikhar.pdfutil.utils.SNPDFPathManager;
import com.shikhar.pdfutil.R;

public class OpenSNPDFFolderActivity extends SNPDFActivity {

  CheckBoxListener checkBoxListener = new CheckBoxListener(new HashSet<String>());

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_open_snpdffolder);

    File root = SNPDFPathManager.getRootDirectory();
    File[] snpdfFiles = root.listFiles(new FilenameFilter() {
      @Override
      public boolean accept(File dir, String filename) {
        if (filename.toLowerCase().endsWith(".pdf") || filename.toLowerCase().endsWith(".txt"))
          return true;
        return false;
      }
    });

    if (snpdfFiles != null) {
      List<File> files = Arrays.asList(snpdfFiles);
      Collections.sort(files, new Comparator<File>() {
        @Override
        public int compare(File lhs, File rhs) {
          if (lhs.lastModified() > rhs.lastModified()) {
            return -1;
          } else if (lhs.lastModified() < rhs.lastModified()) {
            return 1;
          }

          return 0;
        }
      });

      ListView lv = (ListView) findViewById(R.id.list_snpdf_files);
      if (files.size() == 0) {
        List<String> none = new ArrayList<String>();
        none.add("NONE");
        lv.setAdapter(new ArrayAdapter<String>(this, R.layout.row, none));

        setInvisible(R.id.inputSearch, R.id.selectOption);
      } else {

        final ArrayAdapter<File> adapter = new SNPDFCheckedArrayAdapter(this, files, checkBoxListener,
            new View.OnClickListener() {
              @Override
              public void onClick(View v) {
                mainFile = new File(SNPDFPathManager.getRootDirectory(), ((CheckBox) v.findViewById(R.id.rowtext))
                    .getText().toString());
                showIntentForPickedPDF();

              }
            });

        lv.setAdapter(adapter);

        EditText inputSearch = (EditText) findViewById(R.id.inputSearch);
        // enable search
        inputSearch.addTextChangedListener(new TextWatcher() {

          @Override
          public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
            // When user changed the Text
            adapter.getFilter().filter(cs);
          }

          @Override
          public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {

          }

          @Override
          public void afterTextChanged(Editable arg0) {
          }
        });

      }

    }

  }

  private void showIntentForPickedPDF() {
    Intent filePick = new Intent(this, PickedPDFActivity.class);
    filePick.putExtra(SNPDFCContstants.FILE_URI, mainFile.getAbsolutePath());
    startActivity(filePick);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.snpdf_options_menu_folder, menu);
    return true;
  }

  public void deleteSelectedPDF(View view) {
    if (checkBoxListener.getSelectedFiles().size() > 0) {
      getAlertDialog().setTitle("Delete all selected files?").setMessage("Are you sure to delete all selected files?")
          .setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
              dialog.dismiss();
              deleteSelected();
            }

          }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
              dialog.dismiss();
            }
          }).show();
    } else {
      getAlertDialog().setTitle("None selected!").setMessage("Please select a file first.")
          .setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
              dialog.dismiss();
            }

          }).show();
    }

  }

  private void deleteSelected() {
    for (String fileName : checkBoxListener.getSelectedFiles()) {
      new File(SNPDFPathManager.getRootDirectory(), fileName).delete();
    }

    Toast.makeText(this, "All selected files have been deleted!", Toast.LENGTH_SHORT).show();

    Intent intent = new Intent(this, OpenSNPDFFolderActivity.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    startActivity(intent);

  }

  public void shareSelectedPDF(View view) {
    if (checkBoxListener.getSelectedFiles().size() > 0) {
      ArrayList<Uri> path = new ArrayList<Uri>();
      for (String fileName : checkBoxListener.getSelectedFiles()) {
        path.add(Uri.fromFile(new File(SNPDFPathManager.getRootDirectory(), fileName)));
      }

      Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
      intent.setType("application/pdf");
      String shareBody = "Emailing pdfs";

      intent.putExtra(Intent.EXTRA_TEXT, shareBody);
      intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, path);

      try {
        startActivity(Intent.createChooser(intent, "Share via..."));
      } catch (ActivityNotFoundException e) {
        Toast.makeText(this, "No Application Available to share selected PDFs", Toast.LENGTH_SHORT).show();
      }
    } else {
      getAlertDialog().setTitle("None selected!").setMessage("Please select a file first.")
          .setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
              dialog.dismiss();
            }

          }).show();
    }

  }

  public class CheckBoxListener implements CompoundButton.OnCheckedChangeListener {

    private Set<String> selectedFiles;

    public Set<String> getSelectedFiles() {
      return selectedFiles;
    }

    public CheckBoxListener(HashSet<String> selectedFiles) {
      this.selectedFiles = selectedFiles;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
      if (isChecked) {
        selectedFiles.add(buttonView.getText().toString());
      } else {
        selectedFiles.remove(buttonView.getText().toString());
      }
    }

  }

}
