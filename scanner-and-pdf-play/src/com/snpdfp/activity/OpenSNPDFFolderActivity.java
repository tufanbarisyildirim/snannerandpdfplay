package com.snpdfp.activity;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.snpdfp.activity.R;
import com.snpdfp.utils.SAPDFCContstants;
import com.snpdfp.utils.SAPDFPathManager;

public class OpenSNPDFFolderActivity extends SNPDFActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_open_snpdffolder);

		File root = SAPDFPathManager.getRootDirectory();
		File[] snpdfFiles = root.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String filename) {
				if (filename.toLowerCase().endsWith(".pdf"))
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

			// Prepare name list
			List<String> names = new ArrayList<String>();
			for (File file : files) {
				names.add(file.getName());
			}

			ListView lv = null;
			lv = (ListView) findViewById(R.id.list_snpdf_files);
			lv.setAdapter(new ArrayAdapter<String>(this, R.layout.row, names));

			lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				public void onItemClick(AdapterView<?> parent, View v,
						int position, long id) {
					pdffile = new File(SAPDFPathManager.getRootDirectory(),
							(String) parent.getAdapter().getItem(position));

					showIntentForPickedPDF();

				}

			});
		}

	}

	private void showIntentForPickedPDF() {
		Intent filePick = new Intent(this, PickedPDFActivity.class);
		filePick.putExtra(SAPDFCContstants.FILE_URI, pdffile.getAbsolutePath());
		startActivity(filePick);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}

}
