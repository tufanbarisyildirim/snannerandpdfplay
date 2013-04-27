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
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import com.snpdfp.utils.SNPDFCContstants;
import com.snpdfp.utils.SNPDFPathManager;
import com.snpdfp.utils.SNPDFUtils;

public class ExtractTextActivity extends SNPDFActivity {
	Logger logger = Logger.getLogger(ExtractTextActivity.class.getName());

	File selectedFile;

	boolean password_req = false;
	String password;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pdf_to_text);

		EditText password = (EditText) findViewById(R.id.password);
		password.setVisibility(View.GONE);
		password_req = false;
	}

	public void pickFile(View view) {
		Intent filePick = new Intent(this, BrowsePDFActivity.class);
		startActivityForResult(filePick, SNPDFCContstants.PICK_FILE);
	}

	public void extractText(View view) {
		if (selectedFile == null || !selectedFile.exists()) {
			getAlertDialog()
					.setTitle("Incomplete details")
					.setMessage(
							"Please select the PDF file to extract text from!")
					.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss();
								}

							}).show();
		} else {
			if (!arePDFDetailsComplete()) {
				getAlertDialog()
						.setTitle("PDF password required")
						.setMessage(
								"Please enter a valid password for the selected PDF")
						.setPositiveButton("OK",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int which) {
										dialog.dismiss();
									}

								}).show();
			} else {
				new TextExtractor().execute();
			}

		}

	}

	private boolean arePDFDetailsComplete() {
		if (password_req) {
			password = ((EditText) findViewById(R.id.password)).getText()
					.toString();

			if (password == null || password.equals("")
					|| !SNPDFUtils.isPasswordCorrect(selectedFile, password)) {
				return false;
			}
		}

		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == SNPDFCContstants.PICK_FILE) {
				selectedFile = new File(
						data.getStringExtra(SNPDFCContstants.FILE_URI));
				((EditText) findViewById(R.id.file)).setText(selectedFile
						.getName());
				if (SNPDFUtils.isProtected(selectedFile)) {
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
											EditText password = (EditText) findViewById(R.id.password);
											password.setVisibility(View.VISIBLE);
											password_req = true;
											password.setText("");
										}

									}).show();

				} else {
					EditText password = (EditText) findViewById(R.id.password);
					password.setVisibility(View.GONE);
					password_req = false;
					password.setText("");
				}
			}

		} else {
			operationCancelled();
		}
	}

	private class TextExtractor extends AsyncTask<String, Void, Boolean> {

		private ProgressDialog progressDialog;

		@Override
		protected void onPreExecute() {
			progressDialog = new ProgressDialog(ExtractTextActivity.this);
			progressDialog.setMessage("Extracting text from PDF...");
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
			logger.info("****** starting to extract text from pdf **********");
			boolean error = false;

			PrintWriter out = null;
			PdfReader pdfReader = null;
			mainFile = SNPDFPathManager.getTextFileForPDF(selectedFile);
			try {
				if (password_req) {
					pdfReader = new PdfReader(selectedFile.getAbsolutePath(),
							password.getBytes());
				} else {
					pdfReader = new PdfReader(selectedFile.getAbsolutePath());
				}

				out = new PrintWriter(new FileOutputStream(mainFile));
				for (int i = 1; i <= pdfReader.getNumberOfPages(); i++) {
					out.println(PdfTextExtractor.getTextFromPage(pdfReader, i));
				}
				out.flush();
			} catch (Exception e) {
				logger.log(Level.SEVERE, "Unable to extract Text from PDF", e);
				error = true;
				errorMessage = e.getLocalizedMessage();
			} finally {
				// close the document
				if (out != null)
					out.close();
				// close the writer
				if (pdfReader != null)
					pdfReader.close();
			}

			return error;

		}

	}

	public void displayResult(Boolean error) {
		setContentView(R.layout.snpdf_output);

		LinearLayout protect_pdf_layout = (LinearLayout) findViewById(R.id.protect_pdf_layout);
		protect_pdf_layout.setVisibility(View.GONE);

		if (error) {
			SNPDFUtils.setErrorText(this, "Unable to extract text from file "
					+ selectedFile.getName() + " (" + errorMessage + ")");
			hideButtons();

		} else {
			SNPDFUtils.setSuccessText(this, "TXT file successfully created.",
					mainFile);
		}
	}

}
