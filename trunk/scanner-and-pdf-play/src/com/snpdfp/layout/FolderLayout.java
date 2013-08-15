package com.snpdfp.layout;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.content.Context;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.snpdfp.activity.R;
import com.snpdfp.utils.SimpleArrayAdapter;

public class FolderLayout extends LinearLayout {
  Context context;
  IFolderItemListener folderListener;
  private List<Pair<String, String>> item = null;
  private String root = "/";
  private TextView myPath;
  private ListView lstView;

  private ArrayAdapter<Pair<String, String>> fileListAdapter;

  protected File currDir = null;

  private EditText inputSearch;

  public FolderLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
    this.context = context;

    LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    layoutInflater.inflate(R.layout.activity_file_select, this);

    myPath = (TextView) findViewById(R.id.path);
    lstView = (ListView) findViewById(R.id.list);

    File file = new File("/sdcard");
    if (file.exists()) {
      getDir(file.getAbsolutePath(), lstView);
    } else {
      file = Environment.getExternalStorageDirectory();
      if (file.exists()) {
        getDir(file.getAbsolutePath(), lstView);
      } else {
        getDir(root, lstView);
      }
    }

    inputSearch = (EditText) findViewById(R.id.inputSearch);

    // enable search
    inputSearch.addTextChangedListener(new TextWatcher() {

      @Override
      public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
        // When user changed the Text
        if (fileListAdapter != null) {
          fileListAdapter.getFilter().filter(cs);
        }

      }

      @Override
      public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {

      }

      @Override
      public void afterTextChanged(Editable arg0) {
      }
    });
  }

  public void setIFolderItemListener(IFolderItemListener folderItemListener) {
    this.folderListener = folderItemListener;
  }

  // Set Directory for view at anytime
  public void setDir(String dirPath) {
    getDir(dirPath, lstView);
  }

  public File getCurrDir() {
    return currDir;
  }

  private void getDir(String dirPath, ListView v) {

    myPath.setText("location: " + dirPath);
    currDir = new File(dirPath);

    item = new ArrayList<Pair<String, String>>();
    File f = new File(dirPath);
    File[] files = f.listFiles();

    if (!dirPath.equals(root)) {

      item.add(new Pair<String, String>(root, "<- goto root (/)"));
      item.add(new Pair<String, String>(f.getParentFile().getAbsolutePath(), "<- go back (../)"));

    }

    // Sort the files
    List<File> fileList = Arrays.asList(files);
    Collections.sort(fileList, new Comparator<File>() {
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

    // Display all directories first
    for (File file : fileList) {
      if (file.isDirectory()) {
        item.add(new Pair<String, String>(file.getAbsolutePath(), file.getName() + "/"));
      }
    }

    // Display files now
    for (File file : fileList) {
      if (file.isFile()) {
        item.add(new Pair<String, String>(file.getAbsolutePath(), file.getName()));
      }
    }

    Log.i("Folders", files.length + "");

    setItemList(item);

  }

  public void setItemList(List<Pair<String, String>> item) {
    if (inputSearch != null) {
      inputSearch.setText("");
    }

    fileListAdapter = new SimpleArrayAdapter(context, item);
    lstView.setAdapter(fileListAdapter);
    lstView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
        String path = ((TextView) v.findViewById(R.id.rowPath)).getText().toString();
        File file = new File(path);
        if (file.isDirectory()) {
          if (file.canRead())
            getDir(path, (ListView) parent);
          else {
            // what to do when folder is unreadable
            if (folderListener != null) {
              folderListener.OnCannotFileRead(file);

            }
          }

        } else {
          // what to do when file is clicked
          // You can add more,like checking extension,and performing separate
          // actions
          if (folderListener != null) {
            folderListener.OnFileClicked(file);
          }

        }
      }
    });
  }

}
