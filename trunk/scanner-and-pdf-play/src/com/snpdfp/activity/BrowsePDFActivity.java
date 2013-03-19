package com.snpdfp.activity;

import java.io.File;
import java.util.logging.Logger;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.snpdfp.layout.FolderLayout;
import com.snpdfp.layout.IFolderItemListener;
import com.snpdfp.utils.SNPDFCContstants;
import com.snpdfp.utils.SNPDFUtils;

public class BrowsePDFActivity extends SNPDFActivity implements
		IFolderItemListener {
	Logger logger = Logger.getLogger(BrowsePDFActivity.class.getName());

	FolderLayout localFolders;
	String requestType;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestType = getIntent().getStringExtra(
				SNPDFCContstants.PDF_REQUEST_TYPE);

		if (requestType == null) {
			TextView textView = new TextView(this);
			SNPDFUtils.setErrorText(textView, "Invalid request!!!");
			setContentView(textView);

		} else {
			getAlertDialog()
					.setTitle("PDF select")
					.setMessage("Select the PDF to protect...")
					.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss();
									browseFile();
								}

							})
					.setNegativeButton("Cancel",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss();
									finish();
									return;
								}

							}).show();

		}

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
			if (SNPDFCContstants.PDF_REQUEST_LOCK.equals(requestType)) {
				Intent pdfintent = new Intent(this, ProtectPDFActivity.class);
				pdfintent.putExtra(SNPDFCContstants.FILE_URI,
						file.getAbsolutePath());
				startActivity(pdfintent);
			}
		}

	}

}
