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
import android.widget.Button;
import android.widget.TextView;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfWriter;
import com.snpdfp.utils.SAPDFCContstants;
import com.snpdfp.utils.SAPDFPathManager;
import com.snpdfp.utils.SAPDFUtils;

public class ProtectPDFActivity extends SNPDFActivity {
	Logger logger = Logger.getLogger(ProtectPDFActivity.class.getName());

	File srcFile = null;
	String password = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent intent = getIntent();
		srcFile = new File(intent.getStringExtra(SAPDFCContstants.FILE_URI));

		// Check if already encrypted
		PdfReader pdfReader = null;
		try {
			pdfReader = new PdfReader(srcFile.getAbsolutePath());
			if (pdfReader.isEncrypted()) {
				getAlertDialog()
						.setTitle("Invalid selection")
						.setMessage(
								"The PDF is already protected, cannot process further!")
						.setPositiveButton("OK",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int which) {
										dialog.dismiss();
										finish();
										return;
									}

								}).show();
			} else {
				pickPassword();
			}

		} catch (IOException e) {
			logger.log(
					Level.SEVERE,
					"It seems the PDF is already protected with a password, cannot process further!",
					e);
			getAlertDialog()
					.setTitle("ERROR")
					.setMessage(
							"It seems the PDF is already protected with a password, cannot process further!")
					.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss();
									finish();
									return;
								}

							}).show();

		} finally {
			if (pdfReader != null) {
				pdfReader.close();
			}
		}
	}

	private void pickPassword() {
		logger.info("Taking the password...");
		Intent pickPassword = new Intent(this, PickPasswordActivity.class);
		startActivityForResult(pickPassword,
				SAPDFCContstants.PICK_PASSWORD_REQUEST);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == SAPDFCContstants.PICK_PASSWORD_REQUEST) {
				password = data.getStringExtra(SAPDFCContstants.TEXT);
				new PDFProtect().execute();
			}

		} else {
			getAlertDialog()
					.setTitle("Operation Cancelled!")
					.setPositiveButton("OK",
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

			String fileName = "PROTECTED_"
					+ srcFile.getName().substring(0,
							srcFile.getName().lastIndexOf(".")) + ".pdf";
			mainFile = SAPDFPathManager.getSavePDFPath(fileName);

			PdfReader reader = null;
			PdfStamper stamper = null;
			try {
				reader = new PdfReader(srcFile.getAbsolutePath());
				stamper = new PdfStamper(reader, new FileOutputStream(mainFile));
				stamper.setEncryption(password.getBytes(), password.getBytes(),
						PdfWriter.ALLOW_PRINTING, PdfWriter.ENCRYPTION_AES_128
								| PdfWriter.DO_NOT_ENCRYPT_METADATA);
			} catch (Exception e) {
				logger.log(Level.SEVERE, "Unable to lock PDF", e);
				error = true;

			} finally {
				try {
					stamper.close();
				} catch (DocumentException e) {

				} catch (IOException e) {

				}
				reader.close();
			}

			return error;

		}

	}

	public void displayResult(Boolean error) {
		setContentView(R.layout.activity_protect_pdf);

		TextView textView = (TextView) findViewById(R.id.message);
		Button protect_button = (Button) findViewById(R.id.protectPDF);
		// Disable the already protected pdf
		protect_button.setVisibility(View.GONE);

		if (error) {
			SAPDFUtils.setErrorText(textView,
					"Unable to lock file: " + srcFile.getName());
			disableButtons();
		} else {
			SAPDFUtils.setSuccessText(textView, "PDF successfully protected: "
					+ mainFile.getName());
		}
	}

}
