package com.snpdfp.activity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Logger;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfWriter;
import com.snpdfp.layout.FolderLayout;
import com.snpdfp.utils.SNPDFCContstants;
import com.snpdfp.utils.SNPDFPathManager;
import com.snpdfp.utils.SNPDFUtils;

public class SplitActivity extends SNPDFActivity {

	Logger logger = Logger.getLogger(SplitActivity.class.getName());

	FolderLayout localFolders;
	File srcPDF;

	boolean password_req = false;
	String password;
	int numberOfPages;
	int fromPageNumber;
	int toPageNumber;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_split);

		EditText password = (EditText) findViewById(R.id.password);
		password.setVisibility(View.GONE);
		password_req = false;
	}

	public void pickFile(View view) {
		Intent filePick = new Intent(this, BrowsePDFActivity.class);
		startActivityForResult(filePick, SNPDFCContstants.PICK_FILE);
	}

	public void fillPageDetails(View view) {
		if (srcPDF == null || !srcPDF.exists()) {
			getAlertDialog()
					.setTitle("Please select a PDF first")
					.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss();
								}

							}).show();
		} else if (!pdfDetailsComplete()) {
			getAlertDialog()
					.setTitle("Incorrect password")
					.setMessage("Please enter the correct PDF password")
					.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss();
								}

							}).show();
		} else {
			populatePageDetails(true);
		}

	}

	private boolean pdfDetailsComplete() {
		if (password_req) {
			password = ((EditText) findViewById(R.id.password)).getText()
					.toString();

			if (password == null || password.equals("")
					|| !SNPDFUtils.isPasswordCorrect(srcPDF, password)) {
				return false;
			}
		}

		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == SNPDFCContstants.PICK_FILE) {
				srcPDF = new File(
						data.getStringExtra(SNPDFCContstants.FILE_URI));
				setName();
				if (SNPDFUtils.isProtected(srcPDF)) {
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
											resetFields();
										}

									}).show();

				} else {
					EditText password = (EditText) findViewById(R.id.password);
					password.setVisibility(View.GONE);
					password_req = false;
					resetFields();
				}
			}

		} else {
			operationCancelled();
		}
	}

	private void resetFields() {
		((EditText) findViewById(R.id.password)).setText("");
		((TextView) findViewById(R.id.message)).setText("");
		((EditText) findViewById(R.id.from_number)).setText("");
		((EditText) findViewById(R.id.to_number)).setText("");

	}

	private void setName() {
		EditText editText = (EditText) findViewById(R.id.pdf_file);
		editText.setText(srcPDF.getName());

	}

	private void populatePageDetails(boolean showMessage) {
		PdfReader pdfReader = null;
		try {
			pdfReader = getPDFReader(srcPDF);

			numberOfPages = pdfReader.getNumberOfPages();
			if (numberOfPages <= 1) {
				getAlertDialog()
						.setTitle("Invalid selection")
						.setMessage(
								"The selected PDF just has "
										+ numberOfPages
										+ " page, so cannot be split further!!!")
						.setPositiveButton("OK",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int which) {
										dialog.dismiss();
									}

								}).show();
			} else if (showMessage) {
				TextView textView = (TextView) findViewById(R.id.message);
				textView.setText("Maximum number of pages in selected PDF is "
						+ numberOfPages
						+ ".\nSo FROM cannot be less than 1 and TO cannot exceed "
						+ (numberOfPages - 1));

			}

		} catch (Exception e) {
			getAlertDialog()
					.setTitle("ERROR")
					.setMessage(
							"Unable to process! Please re-enter all details")
					.setPositiveButton("Ok",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss();
								}

							}).show();
		} finally {
			if (pdfReader != null) {
				pdfReader.close();
			}
		}

	}

	private PdfReader getPDFReader(File file) throws IOException {
		if (!password_req) {
			return new PdfReader(file.getAbsolutePath());
		} else {
			return new PdfReader(file.getAbsolutePath(), password.getBytes());
		}
	}

	public void split(View view) {
		String from = ((EditText) findViewById(R.id.from_number)).getText()
				.toString();
		String to = ((EditText) findViewById(R.id.to_number)).getText()
				.toString();

		if (from == null || "".equals(from) || to == null || "".equals(to)) {
			getAlertDialog()
					.setTitle("Incorrect page numbers")
					.setMessage("Please enter valid page numbers!")
					.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss();
								}

							}).show();
		} else if (srcPDF == null || !srcPDF.exists()) {
			getAlertDialog()
					.setTitle("Please select a PDF first")
					.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss();
								}

							}).show();
		} else if (!pdfDetailsComplete()) {
			getAlertDialog()
					.setTitle("Incorrect password")
					.setMessage("Please enter the correct PDF password")
					.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss();
								}

							}).show();
		} else {
			// populate page details
			populatePageDetails(true);
			boolean errorFill = false;
			try {
				toPageNumber = Integer.parseInt(to);
				fromPageNumber = Integer.parseInt(from);
				if (toPageNumber > numberOfPages - 1 || fromPageNumber < 1) {
					errorFill = true;
				}

			} catch (Exception e) {
				errorFill = true;
			}

			if (errorFill) {
				getAlertDialog()
						.setTitle("Incorrect page numbers")
						.setMessage(
								"Invalid numbers entered! Please enter valid numbers as explained in the instructions.")
						.setPositiveButton("OK",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int which) {
										dialog.dismiss();
									}

								}).show();
			} else {
				new SplitPDF().execute();
			}

		}

	}

	private class SplitPDF extends AsyncTask<String, Void, Boolean> {

		private ProgressDialog progressDialog;

		@Override
		protected void onPreExecute() {
			progressDialog = new ProgressDialog(SplitActivity.this);
			progressDialog.setMessage("Extracting requested pages from PDF...");
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

			mainFile = SNPDFPathManager.getSavePDFPath("EXTRACTED_"
					+ SNPDFPathManager.getFileNameWithoutExtn(srcPDF.getName())
					+ "_" + fromPageNumber + "-" + toPageNumber + ".pdf");

			Document document = new Document();
			PdfReader inputPDF = null;
			PdfWriter writer = null;
			try {
				inputPDF = getPDFReader(srcPDF);
				// Create a writer for the outputstream
				writer = PdfWriter.getInstance(document, new FileOutputStream(
						mainFile));

				document.open();
				PdfContentByte cb = writer.getDirectContent(); // Holds the PDF
																// data
				PdfImportedPage page = null;

				while (fromPageNumber <= toPageNumber) {
					document.newPage();
					page = writer.getImportedPage(inputPDF, fromPageNumber);
					cb.addTemplate(page, 0, 0);
					fromPageNumber++;
				}

			} catch (Exception e) {
				error = true;
				errorMessage = e.getLocalizedMessage();
			} finally {
				if (document.isOpen())
					document.close();
				if (inputPDF != null) {
					inputPDF.close();
				}
				if (writer != null) {
					writer.close();
				}
			}

			return error;
		}

	}

	public void displayResult(Boolean error) {
		setContentView(R.layout.snpdf_output);

		if (error) {
			SNPDFUtils.setErrorText(this, "Unable to extract PDF " + srcPDF
					+ " from page " + fromPageNumber + " to page "
					+ toPageNumber + " (" + errorMessage + ")");
			hideButtons();

		} else {
			SNPDFUtils.setSuccessText(this, "PDF " + srcPDF.getName()
					+ " successfully extracted  from page " + fromPageNumber
					+ " to page " + toPageNumber, mainFile);
		}
	}

}
