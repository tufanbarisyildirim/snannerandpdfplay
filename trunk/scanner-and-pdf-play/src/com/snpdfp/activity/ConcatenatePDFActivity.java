package com.snpdfp.activity;

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

import com.itextpdf.text.pdf.PdfCopyFields;
import com.itextpdf.text.pdf.PdfReader;
import com.snpdfp.layout.FolderLayout;
import com.snpdfp.utils.SNPDFCContstants;
import com.snpdfp.utils.SNPDFPathManager;
import com.snpdfp.utils.SNPDFUtils;

public class ConcatenatePDFActivity extends SNPDFActivity {

	Logger logger = Logger.getLogger(ConcatenatePDFActivity.class.getName());

	FolderLayout localFolders;
	File firstFile;
	File secondFile;

	boolean password1_req = false;
	boolean password2_req = false;

	String password1 = null;
	String password2 = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_concatenate_pdf);

		EditText password1 = (EditText) findViewById(R.id.password1);
		password1.setVisibility(View.GONE);
		password1_req = false;

		EditText password2 = (EditText) findViewById(R.id.password2);
		password2.setVisibility(View.GONE);
		password2_req = false;
	}

	public void pickFile1(View view) {
		Intent filePick = new Intent(this, BrowsePDFActivity.class);
		startActivityForResult(filePick, SNPDFCContstants.PICK_FILE1);
	}

	public void pickFile2(View view) {
		Intent filePick = new Intent(this, BrowsePDFActivity.class);
		startActivityForResult(filePick, SNPDFCContstants.PICK_FILE2);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == SNPDFCContstants.PICK_FILE1) {
				firstFile = new File(
						data.getStringExtra(SNPDFCContstants.FILE_URI));
				((EditText) findViewById(R.id.pdf_file1)).setText(firstFile
						.getName());

				if (SNPDFUtils.isProtected(firstFile)) {
					getAlertDialog()
							.setTitle("PDF is encrypted!")
							.setMessage(
									"The selected PDF is protected, please enter it's password!")
							.setPositiveButton("OK",
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog,
												int which) {
											dialog.dismiss();
											EditText password1 = (EditText) findViewById(R.id.password1);
											password1
													.setVisibility(View.VISIBLE);
											password1_req = true;
											password1.setText("");

										}

									}).show();

				} else {
					EditText password1 = (EditText) findViewById(R.id.password1);
					password1.setVisibility(View.GONE);
					password1_req = false;
					password1.setText("");
				}

			} else if (requestCode == SNPDFCContstants.PICK_FILE2) {
				secondFile = new File(
						data.getStringExtra(SNPDFCContstants.FILE_URI));
				((EditText) findViewById(R.id.pdf_file2)).setText(secondFile
						.getName());
				if (SNPDFUtils.isProtected(secondFile)) {
					getAlertDialog()
							.setTitle("PDF is encrypted!")
							.setMessage(
									"The selected PDF is protected, please enter it's password!")
							.setPositiveButton("OK",
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog,
												int which) {
											dialog.dismiss();
											EditText password2 = (EditText) findViewById(R.id.password2);
											password2
													.setVisibility(View.VISIBLE);
											password2_req = true;
											password2.setText("");

										}

									}).show();
				} else {
					EditText password2 = (EditText) findViewById(R.id.password2);
					password2.setVisibility(View.GONE);
					password2_req = false;
					password2.setText("");
				}
			}

		} else {
			operationCancelled();
		}
	}

	public void concatenate(View view) {
		if (firstFile == null || !firstFile.exists() || secondFile == null
				|| !secondFile.exists()) {
			getAlertDialog()
					.setTitle("Incomplete details")
					.setMessage("Please provide both the PDF files!")
					.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss();
								}

							}).show();
		} else {
			if (!firstPDFDetailsComplete()) {
				getAlertDialog()
						.setTitle("First PDF password")
						.setMessage("Please enter valid password for first PDF")
						.setPositiveButton("OK",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int which) {
										dialog.dismiss();
									}

								}).show();
			} else if (!secondPDFDetailsComplete()) {
				getAlertDialog()
						.setTitle("Second PDF password")
						.setMessage(
								"Please enter valid password for second PDF")
						.setPositiveButton("OK",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int which) {
										dialog.dismiss();
									}

								}).show();
			} else {
				new Concatenate().execute();
			}

		}

	}

	private boolean secondPDFDetailsComplete() {
		if (password2_req) {
			password2 = ((EditText) findViewById(R.id.password2)).getText()
					.toString();

			if (password2 == null || password2.equals("")
					|| !SNPDFUtils.isPasswordCorrect(secondFile, password2)) {
				return false;
			}
		}

		return true;
	}

	private boolean firstPDFDetailsComplete() {
		if (password1_req) {
			password1 = ((EditText) findViewById(R.id.password1)).getText()
					.toString();

			if (password1 == null || password1.equals("")
					|| !SNPDFUtils.isPasswordCorrect(firstFile, password1)) {
				return false;
			}
		}

		return true;
	}

	private class Concatenate extends AsyncTask<String, Void, Boolean> {

		private ProgressDialog progressDialog;

		@Override
		protected void onPreExecute() {
			progressDialog = new ProgressDialog(ConcatenatePDFActivity.this);
			progressDialog.setMessage("Concatinating...");
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

			boolean error = false;
			PdfReader reader1 = null;
			PdfReader reader2 = null;
			PdfCopyFields copy = null;

			mainFile = SNPDFPathManager.getSavePDFPath(SNPDFPathManager
					.getFileNameWithoutExtn(firstFile.getName())
					+ "_"
					+ SNPDFPathManager.getFileNameWithoutExtn(secondFile
							.getName()) + ".pdf");

			try {
				if (password1_req) {
					reader1 = new PdfReader(firstFile.getAbsolutePath(),
							password1.getBytes());
				} else {
					reader1 = new PdfReader(firstFile.getAbsolutePath());
				}

				if (password2_req) {
					reader2 = new PdfReader(secondFile.getAbsolutePath(),
							password2.getBytes());
				} else {
					reader2 = new PdfReader(secondFile.getAbsolutePath());
				}

				copy = new PdfCopyFields(new FileOutputStream(mainFile));
				copy.addDocument(reader1);
				copy.addDocument(reader2);

			} catch (Exception e) {
				logger.log(Level.SEVERE, "Unable to concatenate PDFs", e);
				error = true;
				errorMessage = e.getLocalizedMessage();

			} finally {
				if (reader1 != null) {
					reader1.close();
				}
				if (reader2 != null) {
					reader2.close();
				}
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
			SNPDFUtils.setErrorText(this, "Unable to concatenate PDFs "
					+ firstFile.getName() + " and " + secondFile.getName()
					+ " (" + errorMessage + ")");
			disableButtons();
		} else {
			SNPDFUtils.setSuccessText(this, "PDFs " + firstFile.getName()
					+ " and " + secondFile.getName()
					+ " successfully concatenated.", mainFile);
		}

	}

}
