package com.snpdfp.activity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.logging.Level;
import java.util.logging.Logger;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.tool.xml.XMLWorkerHelper;
import com.snpdfp.layout.FolderLayout;
import com.snpdfp.layout.IFolderItemListener;
import com.snpdfp.utils.SAPDFCContstants;
import com.snpdfp.utils.SAPDFPathManager;
import com.snpdfp.utils.SAPDFUtils;

public class FileToPDFActivity extends SNPDFActivity implements
		IFolderItemListener {
	Logger logger = Logger.getLogger(FileToPDFActivity.class.getName());

	File srcFile = null;
	FolderLayout localFolders;
	String fileType = SAPDFCContstants.FILE_TYPE_TXT;

	/** Called when the activity is first created. */

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		if (getIntent().getStringExtra(SAPDFCContstants.FILE_TYPE) != null) {
			fileType = getIntent().getStringExtra(SAPDFCContstants.FILE_TYPE);
		}

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
		srcFile = file;
		if (SAPDFCContstants.FILE_TYPE_TXT.equals(fileType)) {
			if (!file.getName().toLowerCase().endsWith(".txt")) {
				getAlertDialog()
						.setTitle("Invalid selection")
						.setMessage(
								"You can only select a .txt file for this conversion!")
						.setPositiveButton("OK",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int which) {

									}

								}).show();
			} else {
				new FileConverter().execute();
			}

		} else if (SAPDFCContstants.FILE_TYPE_HTML.equals(fileType)) {
			if (!file.getName().toLowerCase().endsWith(".htm")
					&& !file.getName().toLowerCase().endsWith(".html")) {
				getAlertDialog()
						.setTitle("Invalid selection")
						.setMessage(
								"You can only select a .htm OR .html file for this conversion!")
						.setPositiveButton("OK",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int which) {

									}

								}).show();
			} else {
				new FileConverter().execute();
			}
		}

	}

	private class FileConverter extends AsyncTask<String, Void, Boolean> {

		private ProgressDialog progressDialog;

		@Override
		protected void onPreExecute() {
			progressDialog = new ProgressDialog(FileToPDFActivity.this);
			progressDialog.setMessage("Converting to PDF...");
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
			logger.info("****** starting to convert file to pdf **********");
			boolean error = false;
			String fileName = srcFile.getName().substring(0,
					srcFile.getName().lastIndexOf("."))
					+ ".pdf";
			mainFile = SAPDFPathManager.getSavePDFPath(fileName);

			// create a new document
			Document document = new Document();

			PdfWriter pdfWriter = null;

			try {
				pdfWriter = PdfWriter.getInstance(document,
						new FileOutputStream(mainFile));
				// open document
				document.open();
				pdfWriter.open();

				if (srcFile.getName().endsWith(".txt")) {
					BufferedReader in = null;
					try {
						in = new BufferedReader(new FileReader(srcFile));
						// Read the file and write it into the new file
						String fileText = in.readLine();

						while (fileText != null) {
							document.add(new Paragraph(fileText));
							fileText = in.readLine();
						}
					} finally {
						if (in != null)
							in.close();
					}

				} else if (srcFile.getName().endsWith(".htm")
						|| srcFile.getName().endsWith(".html")) {
					// To convert a HTML file from the filesystem
					FileInputStream fis = null;
					XMLWorkerHelper worker = null;
					try {
						fis = new FileInputStream(srcFile);
						worker = XMLWorkerHelper.getInstance();
						// convert to PDF
						worker.parseXHtml(pdfWriter, document, fis);

					} finally {
						if (fis != null) {
							fis.close();
						}
					}
				}

			} catch (Exception e) {
				logger.log(Level.SEVERE, "Unable to create PDF", e);
				error = true;

			} finally {
				// close the document
				if (document != null)
					document.close();
				// close the writer
				if (pdfWriter != null)
					pdfWriter.close();
			}

			return error;

		}

	}

	public void displayResult(Boolean error) {
		setContentView(R.layout.activity_file_to_pdf);

		TextView textView = (TextView) findViewById(R.id.message);
		Button open_button = (Button) findViewById(R.id.openPDF);
		Button share_button = (Button) findViewById(R.id.sharePDF);
		Button protect_button = (Button) findViewById(R.id.protectPDF);
		Button delete_button = (Button) findViewById(R.id.deletePDF);

		if (error) {
			SAPDFUtils.setErrorText(textView, "Unable to convert file "
					+ srcFile.getName() + " to PDF!");
			open_button.setEnabled(false);
			share_button.setEnabled(false);
			protect_button.setEnabled(false);
			delete_button.setEnabled(false);
		} else {
			SAPDFUtils.setSuccessText(textView,
					"PDF file successfully created: " + mainFile.getName());
		}
	}

}
