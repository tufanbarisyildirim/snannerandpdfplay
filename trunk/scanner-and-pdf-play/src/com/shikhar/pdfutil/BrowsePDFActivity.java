package com.shikhar.pdfutil;

import java.io.File;
import java.util.logging.Logger;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.shikhar.pdfutil.layout.FolderLayout;
import com.shikhar.pdfutil.layout.IFolderItemListener;
import com.shikhar.pdfutil.utils.SNPDFCContstants;
import com.shikhar.pdfutil.R;

public class BrowsePDFActivity extends SNPDFActivity implements IFolderItemListener {
	Logger logger = Logger.getLogger(BrowsePDFActivity.class.getName());

	FolderLayout localFolders;
	String requestType;
	String fileType;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getIntent().getStringExtra(SNPDFCContstants.FILE_TYPE) != null) {
			fileType = getIntent().getStringExtra(SNPDFCContstants.FILE_TYPE);
		}

		browseFile();
	}

	private void browseFile() {
		setContentView(R.layout.folders);

		localFolders = (FolderLayout) findViewById(R.id.localfolders);
		localFolders.setIFolderItemListener(this);

	}

	// Your stuff here for Cannot open Folder
	public void OnCannotFileRead(File file) {
		showCannotReadFileDialog(file);
	}

	/**
	 * Called on file click
	 */
	public void OnFileClicked(File file) {

		if (fileType == null && !file.getName().toLowerCase().endsWith(".pdf")) {
			getAlertDialog().setTitle("Invalid selection").setMessage("You can only select a .pdf file for this request!")
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}

				}).show();
		} else if (fileType != null && SNPDFCContstants.FILE_TYPE_TXT.equals(fileType) && !file.getName().toLowerCase().endsWith(".txt")
			&& !file.getName().toLowerCase().endsWith(".log") && !file.getName().toLowerCase().endsWith(".csv")) {
			getAlertDialog().setTitle("Invalid selection").setMessage("You can only select a .txt, .log or .csv file for this conversion!")
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}

				}).show();
		} else if (fileType != null && SNPDFCContstants.FILE_TYPE_HTML.equals(fileType) && !file.getName().toLowerCase().endsWith(".htm")
			&& !file.getName().toLowerCase().endsWith(".html")) {
			getAlertDialog().setTitle("Invalid selection").setMessage("You can only select a .htm OR .html file for this conversion!")
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}

				}).show();
		} else if (fileType != null && SNPDFCContstants.FILE_TYPE_DOC.equals(fileType) && !file.getName().toLowerCase().endsWith(".doc")) {
			getAlertDialog().setTitle("Invalid selection").setMessage("You can only select a .doc file for this conversion!")
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}

				}).show();
		} else {
			Intent result = new Intent();
			result.putExtra(SNPDFCContstants.FILE_URI, file.getAbsolutePath());
			setResult(Activity.RESULT_OK, result);
			finish();
		}

	}

	/**
	 * On back press, take to previous folder
	 */
	@Override
	public void onBackPressed() {
		File currDir = localFolders.getCurrDir();
		if (currDir != null && currDir.getParent() != null) {
			localFolders.setDir(currDir.getParent());
		} else {
			finish();
		}
	}
}
