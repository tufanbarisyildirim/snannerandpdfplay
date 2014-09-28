package com.snpdfp.activity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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

import com.lowagie.text.pdf.PdfEncryptor;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfWriter;
import com.snpdfp.utils.SNPDFCContstants;
import com.snpdfp.utils.SNPDFPathManager;
import com.snpdfp.utils.SNPDFUtils;

public class ProtectPDFActivity extends SNPDFActivity {
	Logger logger = Logger.getLogger(ProtectPDFActivity.class.getName());

	File srcFile = null;
	String password = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_protect_pdf);

		Intent intent = getIntent();
		if (intent.getStringExtra(SNPDFCContstants.FILE_URI) != null) {
			srcFile = new File(intent.getStringExtra(SNPDFCContstants.FILE_URI));
			setName();
		}

	}

	public void pickFile(View view) {
		Intent filePick = new Intent(this, BrowsePDFActivity.class);
		startActivityForResult(filePick, SNPDFCContstants.PICK_FILE);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == SNPDFCContstants.PICK_FILE) {
				srcFile = new File(data.getStringExtra(SNPDFCContstants.FILE_URI));
				setName();
			}

		} else {
			showCancelledMsg();
		}
	}

	private void setName() {
		EditText editText = (EditText) findViewById(R.id.pdf_file);
		editText.setText(srcFile.getName());

	}

	public void protect(View view) {
		if (srcFile == null || !srcFile.exists()) {
			getAlertDialog().setTitle("Incomplete details").setMessage("Please select a PDF file!")
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}

				}).show();
		} else {
			password = ((EditText) findViewById(R.id.password)).getText().toString();
			String confirmPassword = ((EditText) findViewById(R.id.confirm_password)).getText().toString();

			if (password == null || confirmPassword == null || "".equals(password) || "".equals(confirmPassword) || !password.equals(confirmPassword)) {
				getAlertDialog().setTitle("Incomplete details").setMessage("Password and Confirm Password fields are required, and they must match!")
					.setPositiveButton("OK", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}

					}).show();

			} else {
				if (SNPDFUtils.isProtected(srcFile)) {
					getAlertDialog().setTitle("Invalid selection").setMessage("The PDF is already protected, cannot process further!")
						.setPositiveButton("OK", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
							}

						}).show();
				} else {
					new PDFProtect().execute();
				}
			}
		}
	}

	private class PDFProtect extends AsyncTask<String, Void, Boolean> {

		private ProgressDialog progressDialog;

		@Override
		protected void onPreExecute() {
			progressDialog = new ProgressDialog(ProtectPDFActivity.this);
			progressDialog.setMessage("Protecting PDF...");
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
			logger.info("****** starting to lock pdf **********");
			boolean error = false;

			String fileName = "PROTECTED_" + srcFile.getName().substring(0, srcFile.getName().lastIndexOf(".")) + ".pdf";
			mainFile = SNPDFPathManager.getSavePDFPath(fileName);

			PdfReader reader = null;
			FileOutputStream fileOutputStream = null;
			try {
				reader = SNPDFUtils.getPdfReader(srcFile.getAbsolutePath(), false, null);
				fileOutputStream = new FileOutputStream(mainFile);
				PdfEncryptor.encrypt(reader, fileOutputStream, password.getBytes(), password.getBytes(), PdfWriter.ALLOW_ASSEMBLY | PdfWriter.ALLOW_COPY
					| PdfWriter.ALLOW_DEGRADED_PRINTING | PdfWriter.ALLOW_FILL_IN | PdfWriter.ALLOW_MODIFY_ANNOTATIONS | PdfWriter.ALLOW_MODIFY_CONTENTS
					| PdfWriter.ALLOW_PRINTING | PdfWriter.ALLOW_SCREENREADERS, false);

			} catch (Exception e) {
				logger.log(Level.SEVERE, "Unable to lock PDF", e);
				error = true;
				errorMessage = e.getLocalizedMessage();
			} finally {
				if (fileOutputStream != null) {
					try {
						fileOutputStream.close();
					} catch (IOException e) {
						errorMessage = e.getLocalizedMessage();
					}
				}
				reader.close();
			}

			return error;

		}

	}

	public void displayResult(Boolean error) {
		setContentView(R.layout.snpdf_output);

		LinearLayout protect_pdf_layout = (LinearLayout) findViewById(R.id.protect_pdf_layout);
		// Disable the already protected pdf
		protect_pdf_layout.setVisibility(View.GONE);

		if (error) {
			SNPDFUtils.setErrorText(this, "Unable to lock file: " + srcFile.getName() + " (" + errorMessage + ")");
			hideButtons();
		} else {
			SNPDFUtils.setSuccessText(this, "PDF successfully protected.", mainFile);
		}
	}

}
