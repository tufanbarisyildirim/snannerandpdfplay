package com.snpdfp.activity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import com.itextpdf.text.pdf.PdfCopyFields;
import com.itextpdf.text.pdf.PdfReader;
import com.snpdfp.layout.FolderLayout;
import com.snpdfp.layout.IFolderItemListener;
import com.snpdfp.utils.SNPDFCContstants;
import com.snpdfp.utils.SNPDFPathManager;
import com.snpdfp.utils.SNPDFUtils;

public class CopyEncryptedActivity extends SNPDFActivity implements
		IFolderItemListener {
	Logger logger = Logger.getLogger(CopyEncryptedActivity.class.getName());

	FolderLayout localFolders;
	File selectedFile;

	String password;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		setContentView(R.layout.folders);
		localFolders = (FolderLayout) findViewById(R.id.localfolders);
		localFolders.setIFolderItemListener(this);
	}

	// Your stuff here for Cannot open Folder
	public void OnCannotFileRead(File file) {
		showCannotReadFileDialog(file);
	}

	// Your stuff here for file Click
	public void OnFileClicked(File file) {
		selectedFile = file;
		if (!file.getName().toLowerCase().endsWith(".pdf")) {
			getAlertDialog()
					.setTitle("Invalid selection")
					.setMessage("Please select a valid protected .pdf file!")
					.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss();
								}

							}).show();
		} else {
			copyPDF();
		}

	}

	private void copyPDF() {
		PdfReader pdfReader = null;
		try {
			pdfReader = new PdfReader(selectedFile.getAbsolutePath());
			if (pdfReader.isEncrypted()) {
				Intent pickPassword = new Intent(this,
						PickPasswordActivity.class);
				startActivityForResult(pickPassword,
						SNPDFCContstants.PICK_PASSWORD_REQUEST);

			} else {
				new CopyPDFExecutor().execute();
			}

		} catch (Exception e) {
			Intent pickPassword = new Intent(this, PickPasswordActivity.class);
			startActivityForResult(pickPassword,
					SNPDFCContstants.PICK_PASSWORD_REQUEST);

		} finally {
			if (pdfReader != null)
				pdfReader.close();
		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == SNPDFCContstants.PICK_PASSWORD_REQUEST) {
				password = data.getStringExtra(SNPDFCContstants.TEXT);
				new CopyPDFExecutor().execute();
			}

		} else {
			operationCancelled();
		}
	}

	private class CopyPDFExecutor extends AsyncTask<String, Void, Boolean> {

		private ProgressDialog progressDialog;

		@Override
		protected void onPreExecute() {
			progressDialog = new ProgressDialog(CopyEncryptedActivity.this);
			progressDialog
					.setMessage("Copying encrypted PDF to non-encrypted one...");
			progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			progressDialog.show();

		}

		@Override
		protected void onPostExecute(Boolean result) {
			if (progressDialog != null && progressDialog.isShowing())
				progressDialog.dismiss();

			displayResult(result);

		}

		@Override
		protected Boolean doInBackground(String... params) {
			logger.info("****** starting to copy encrypted PDF **********");
			boolean error = false;

			PrintWriter out = null;
			PdfReader pdfReader = null;
			PdfCopyFields copy = null;

			mainFile = SNPDFPathManager.getSavePDFPath("COPY_"
					+ selectedFile.getName());
			try {
				if (password != null) {
					pdfReader = new PdfReader(selectedFile.getAbsolutePath(),
							password.getBytes());
				} else {
					pdfReader = new PdfReader(selectedFile.getAbsolutePath());
				}

				copy = new PdfCopyFields(new FileOutputStream(mainFile));
				copy.addDocument(pdfReader);

			} catch (Exception e) {
				logger.log(Level.SEVERE, "Unable to extract Text from PDF", e);
				error = true;
				errorMessage = e.getLocalizedMessage();
			} finally {
				// close the writer
				if (pdfReader != null)
					pdfReader.close();

				if (copy != null) {
					copy.close();
				}

			}

			return error;

		}

	}

	public void displayResult(Boolean error) {
		setContentView(R.layout.activity_copy_encrypted);

		if (error) {
			SNPDFUtils.setErrorText(this, "Unable to extract text from file "
					+ selectedFile.getName() + " (" + errorMessage + ")");
			disableButtons();

		} else {
			SNPDFUtils.setSuccessText(this, "TXT file successfully created.",
					mainFile);
		}
	}

}
