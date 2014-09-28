package com.snpdf.activity;

import java.io.File;
import java.io.FileOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.lowagie.text.pdf.PdfCopyFields;
import com.lowagie.text.pdf.PdfReader;
import com.snpdf.utils.SNPDFCContstants;
import com.snpdf.utils.SNPDFPathManager;
import com.snpdf.utils.SNPDFUtils;
import com.snpdf.activity.R;

public class CopyEncryptedActivity extends SNPDFActivity {
	Logger logger = Logger.getLogger(CopyEncryptedActivity.class.getName());

	File selectedFile;

	String password;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_copy_encrypted);
	}

	public void pickFile(View view) {
		Intent filePick = new Intent(this, BrowsePDFActivity.class);
		startActivityForResult(filePick, SNPDFCContstants.PICK_FILE);
	}

	public void copyPDF(View view) {
		if (selectedFile == null || !selectedFile.exists()) {
			getAlertDialog().setTitle("Incomplete details").setMessage("Please select a protected PDF file!")
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}

				}).show();
		} else {
			password = ((EditText) findViewById(R.id.password)).getText().toString();

			if (password == null || "".equals(password)) {
				getAlertDialog().setTitle("Incomplete details").setMessage("Please enter the password of the encrypted file!")
					.setPositiveButton("OK", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}

					}).show();

			} else if (!SNPDFUtils.isPasswordCorrect(selectedFile, password)) {
				getAlertDialog().setTitle("Incorrect password!").setMessage("Please enter the correct password for selected PDF!")
					.setPositiveButton("OK", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}

					}).show();

			} else {
				new CopyPDFExecutor().execute();
			}
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == SNPDFCContstants.PICK_FILE) {
				selectedFile = new File(data.getStringExtra(SNPDFCContstants.FILE_URI));
				setName();
				((EditText) findViewById(R.id.password)).setText("");
			}

		} else {
			showCancelledMsg();
		}
	}

	private void setName() {
		EditText editText = (EditText) findViewById(R.id.pdf_file);
		editText.setText(selectedFile.getName());
	}

	private class CopyPDFExecutor extends AsyncTask<String, Void, Boolean> {

		private ProgressDialog progressDialog;

		@Override
		protected void onPreExecute() {
			progressDialog = new ProgressDialog(CopyEncryptedActivity.this);
			progressDialog.setMessage("Copying encrypted PDF to non-encrypted one...");
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

			PdfReader pdfReader = null;
			PdfCopyFields copy = null;

			mainFile = SNPDFPathManager.getSavePDFPath("COPY_" + selectedFile.getName());
			try {
				pdfReader = SNPDFUtils.getPdfReader(selectedFile.getAbsolutePath(), password != null, password);
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
		setContentView(R.layout.snpdf_output);

		if (error) {
			SNPDFUtils.setErrorText(this, "Unable to extract text from file " + selectedFile.getName() + " (" + errorMessage + ")");
			hideButtons();

		} else {
			SNPDFUtils.setSuccessText(this, "Unprotected PDF successfully created: ", mainFile);
		}
	}

}
