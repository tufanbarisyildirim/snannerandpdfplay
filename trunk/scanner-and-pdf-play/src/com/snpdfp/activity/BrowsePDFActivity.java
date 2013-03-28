package com.snpdfp.activity;

import java.io.File;
import java.util.logging.Logger;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.snpdfp.layout.FolderLayout;
import com.snpdfp.layout.IFolderItemListener;
import com.snpdfp.utils.SNPDFCContstants;

public class BrowsePDFActivity extends SNPDFActivity implements
		IFolderItemListener {
	Logger logger = Logger.getLogger(BrowsePDFActivity.class.getName());

	FolderLayout localFolders;
	String requestType;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
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
		if (!file.getName().endsWith(".pdf")) {
			getAlertDialog()
					.setTitle("Invalid selection")
					.setMessage(
							"You can only select a .pdf file for this request!")
					.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
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

}
