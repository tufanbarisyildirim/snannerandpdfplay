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
import android.widget.TextView;

import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.tool.xml.XMLWorkerHelper;
import com.snpdfp.layout.FolderLayout;
import com.snpdfp.layout.IFolderItemListener;
import com.snpdfp.utils.SNPDFCContstants;
import com.snpdfp.utils.SNPDFPathManager;
import com.snpdfp.utils.SNPDFUtils;

public class FileToPDFActivity extends SNPDFActivity implements
		IFolderItemListener {
	Logger logger = Logger.getLogger(FileToPDFActivity.class.getName());

	File srcFile = null;
	FolderLayout localFolders;
	String fileType = SNPDFCContstants.FILE_TYPE_TXT;

	/** Called when the activity is first created. */

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		if (getIntent().getStringExtra(SNPDFCContstants.FILE_TYPE) != null) {
			fileType = getIntent().getStringExtra(SNPDFCContstants.FILE_TYPE);
		}

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
		srcFile = file;
		if (SNPDFCContstants.FILE_TYPE_TXT.equals(fileType)) {
			if (!file.getName().toLowerCase().endsWith(".txt")
					&& !file.getName().toLowerCase().endsWith(".log")
					&& !file.getName().toLowerCase().endsWith(".csv")) {
				getAlertDialog()
						.setTitle("Invalid selection")
						.setMessage(
								"You can only select a .txt, .log or .csv file for this conversion!")
						.setPositiveButton("OK",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int which) {

									}

								}).show();
			} else {
				new FileConverter().execute();
			}

		} else if (SNPDFCContstants.FILE_TYPE_HTML.equals(fileType)) {
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
										dialog.dismiss();
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
			mainFile = SNPDFPathManager.getSavePDFPath(fileName);

			// create a new document
			Document document = new Document();

			PdfWriter pdfWriter = null;

			try {
				pdfWriter = PdfWriter.getInstance(document,
						new FileOutputStream(mainFile));
				// open document
				document.open();
				pdfWriter.open();

				if (srcFile.getName().toLowerCase().endsWith(".txt")
						|| srcFile.getName().toLowerCase().endsWith(".log")
						|| srcFile.getName().toLowerCase().endsWith(".csv")) {
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

				} else if (srcFile.getName().toLowerCase().endsWith(".htm")
						|| srcFile.getName().toLowerCase().endsWith(".html")) {
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

		if (error) {
			SNPDFUtils.setErrorText(textView, "Unable to convert file "
					+ srcFile.getName() + " to PDF!");
			disableButtons();
		} else {
			SNPDFUtils.setSuccessText(textView,
					"PDF file successfully created.", mainFile);
		}
	}

}
