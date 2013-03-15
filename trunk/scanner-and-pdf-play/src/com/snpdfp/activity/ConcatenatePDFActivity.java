package com.snpdfp.activity;

import java.io.File;
import java.io.FileOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.TextView;

import com.itextpdf.text.pdf.PdfCopyFields;
import com.itextpdf.text.pdf.PdfReader;
import com.snpdfp.layout.FolderLayout;
import com.snpdfp.layout.IFolderItemListener;
import com.snpdfp.utils.SAPDFPathManager;
import com.snpdfp.utils.SAPDFUtils;

public class ConcatenatePDFActivity extends SNPDFActivity implements
		IFolderItemListener {

	Logger logger = Logger.getLogger(ConcatenatePDFActivity.class.getName());

	FolderLayout localFolders;
	File firstFile;
	File secondFile;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getAlertDialog()
				.setTitle("PDF select")
				.setMessage("Select the first PDF...")
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						selectFile();
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

	private void selectFile() {
		setContentView(R.layout.folders);

		localFolders = (FolderLayout) findViewById(R.id.localfolders);
		localFolders.setIFolderItemListener(this);

	}

	@Override
	public void OnCannotFileRead(File file) {
		getAlertDialog().setTitle("Invalid selection")
				.setMessage("[" + file.getName() + "] folder can't be read!")
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				}).show();

	}

	@Override
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
			if (firstFile == null) {
				firstFile = file;
				getAlertDialog()
						.setTitle("PDF select")
						.setMessage(
								"First PDF selected! Now select the second PDF to concatenate...")
						.setPositiveButton("OK",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int which) {
										dialog.dismiss();
										selectFile();
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

			} else {
				secondFile = file;
				new Concatenate().execute();
			}
		}

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

			mainFile = SAPDFPathManager.getSavePDFPath(firstFile.getName()
					+ "_" + secondFile.getName() + ".pdf");

			try {
				reader1 = new PdfReader(firstFile.getAbsolutePath());
				reader2 = new PdfReader(secondFile.getAbsolutePath());
				copy = new PdfCopyFields(new FileOutputStream(mainFile));
				copy.addDocument(reader1);
				copy.addDocument(reader2);

			} catch (Exception e) {
				logger.log(Level.SEVERE, "Unable to concatenate PDFs", e);
				error = true;
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

		setContentView(R.layout.activity_concatenate_pdf);
		TextView textView = (TextView) findViewById(R.id.message);

		if (error) {
			SAPDFUtils.setErrorText(textView, "Unable to concatenate PDFs "
					+ firstFile.getName() + " and " + secondFile.getName());
			disableButtons();
		} else {
			SAPDFUtils.setSuccessText(textView, "PDFs " + firstFile.getName()
					+ " and " + secondFile.getName()
					+ " successfully concatenated to: " + mainFile.getName());
		}

	}

}
