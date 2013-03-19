package com.snpdfp.layout;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.snpdfp.activity.R;

public class FolderLayout extends LinearLayout implements OnItemClickListener {
	Context context;
	IFolderItemListener folderListener;
	private List<String> item = null;
	private List<String> path = null;
	private String root = "/";
	private TextView myPath;
	private ListView lstView;

	public FolderLayout(Context context, AttributeSet attrs) {
		super(context, attrs);

		// TODO Auto-generated constructor stub
		this.context = context;

		LayoutInflater layoutInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		layoutInflater.inflate(R.layout.activity_file_select, this);

		myPath = (TextView) findViewById(R.id.path);
		lstView = (ListView) findViewById(R.id.list);

		Log.i("FolderView", "Constructed");
		getDir(root, lstView);

	}

	public void setIFolderItemListener(IFolderItemListener folderItemListener) {
		this.folderListener = folderItemListener;
	}

	// Set Directory for view at anytime
	public void setDir(String dirPath) {
		getDir(dirPath, lstView);
	}

	private void getDir(String dirPath, ListView v) {

		myPath.setText("Location: " + dirPath);
		item = new ArrayList<String>();
		path = new ArrayList<String>();
		File f = new File(dirPath);
		File[] files = f.listFiles();

		if (!dirPath.equals(root)) {

			item.add("<- goto root (/)");
			path.add(root);
			item.add("<- go back (../)");
			path.add(f.getParent());

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
				path.add(file.getPath());
				item.add(file.getName() + "/");
			}
		}

		// Display files now
		for (File file : fileList) {
			if (file.isFile()) {
				path.add(file.getPath());
				item.add(file.getName());
			}
		}

		Log.i("Folders", files.length + "");

		setItemList(item);

	}

	public void setItemList(List<String> item) {
		ArrayAdapter<String> fileList = new ArrayAdapter<String>(context,
				R.layout.row, item);

		lstView.setAdapter(fileList);
		lstView.setOnItemClickListener(this);
	}

	public void onListItemClick(ListView l, View v, int position, long id) {
		File file = new File(path.get(position));
		if (file.isDirectory()) {
			if (file.canRead())
				getDir(path.get(position), l);
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

	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		onListItemClick((ListView) arg0, arg0, arg2, arg3);
	}
}
