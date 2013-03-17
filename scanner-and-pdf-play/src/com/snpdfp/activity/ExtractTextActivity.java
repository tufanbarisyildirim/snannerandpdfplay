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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.FilteredTextRenderListener;
import com.itextpdf.text.pdf.parser.LocationTextExtractionStrategy;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import com.itextpdf.text.pdf.parser.RegionTextRenderFilter;
import com.itextpdf.text.pdf.parser.RenderFilter;
import com.itextpdf.text.pdf.parser.TextExtractionStrategy;
import com.snpdfp.layout.FolderLayout;
import com.snpdfp.layout.IFolderItemListener;
import com.snpdfp.utils.SAPDFCContstants;
import com.snpdfp.utils.SAPDFPathManager;
import com.snpdfp.utils.SAPDFUtils;

public class ExtractTextActivity extends SNPDFActivity implements
		IFolderItemListener {
	Logger logger = Logger.getLogger(ExtractTextActivity.class.getName());

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
		getAlertDialog().setTitle("Invalid selection")
				.setMessage("[" + file.getName() + "] folder can't be read!")
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				}).show();

	}

	// Your stuff here for file Click
	public void OnFileClicked(File file) {
		selectedFile = file;
		if (!file.getName().toLowerCase().endsWith(".pdf")) {
			getAlertDialog()
					.setTitle("Invalid selection")
					.setMessage(
							"Please select a valid .pdf file to extract text from!")
					.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss();
								}

							}).show();
		} else {
			extractText();
		}

	}

	private void extractText() {
		PdfReader pdfReader = null;
		try {
			pdfReader = new PdfReader(selectedFile.getAbsolutePath());
			if (pdfReader.isEncrypted()) {
				Intent pickPassword = new Intent(this,
						PickPasswordActivity.class);
				startActivityForResult(pickPassword,
						SAPDFCContstants.PICK_PASSWORD_REQUEST);

			} else {
				new TextExtractor().execute();
			}

		} catch (Exception e) {
			final Intent pickPassword = new Intent(this,
					PickPasswordActivity.class);
			getAlertDialog()
					.setTitle("Protected PDF")
					.setMessage(
							"Unable to read PDF, as it seems to be protected. You want to continue the EXTRACT action by filling the password?")
					.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss();
									startActivityForResult(
											pickPassword,
											SAPDFCContstants.PICK_PASSWORD_REQUEST);
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
		} finally {
			if (pdfReader != null)
				pdfReader.close();
		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == SAPDFCContstants.PICK_PASSWORD_REQUEST) {
				password = data.getStringExtra(SAPDFCContstants.TEXT);
				new TextExtractor().execute();
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
			mainFile = SAPDFPathManager.getTextFileForPDF(selectedFile);
			try {
				if (password != null) {
					pdfReader = new PdfReader(selectedFile.getAbsolutePath(),
							password.getBytes());
				} else {
					pdfReader = new PdfReader(selectedFile.getAbsolutePath());
				}

				out = new PrintWriter(new FileOutputStream(mainFile));
				Rectangle rect = new Rectangle(70, 80, 490, 580);
				RenderFilter filter = new RegionTextRenderFilter(rect);
				TextExtractionStrategy strategy;
				for (int i = 1; i <= pdfReader.getNumberOfPages(); i++) {
					strategy = new FilteredTextRenderListener(
							new LocationTextExtractionStrategy(), filter);
					out.println(PdfTextExtractor.getTextFromPage(pdfReader, i,
							strategy));
				}
				out.flush();
			} catch (Exception e) {
				logger.log(Level.SEVERE, "Unable to extract Text from PDF", e);
				error = true;
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
		setContentView(R.layout.activity_file_to_pdf);

		TextView textView = (TextView) findViewById(R.id.message);
		LinearLayout protect_pdf_layout = (LinearLayout) findViewById(R.id.protect_pdf_layout);
		protect_pdf_layout.setVisibility(View.GONE);

		if (error) {
			SAPDFUtils.setErrorText(
					textView,
					"Unable to extract text from file "
							+ selectedFile.getName());
			disableButtons();

		} else {
			SAPDFUtils.setSuccessText(textView,
					"TXT file successfully created.", mainFile);
		}
	}

}
