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
import android.widget.Button;
import android.widget.TextView;

import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfWriter;
import com.snpdfp.layout.FolderLayout;
import com.snpdfp.layout.IFolderItemListener;
import com.snpdfp.utils.SAPDFCContstants;
import com.snpdfp.utils.SAPDFPathManager;
import com.snpdfp.utils.SAPDFUtils;

public class SplitActivity extends SNPDFActivity implements IFolderItemListener {

	Logger logger = Logger.getLogger(SplitActivity.class.getName());

	FolderLayout localFolders;
	File srcPDF;
	String password;
	int numberOfPages;
	int fromPageNumber;
	int toPageNumber;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getAlertDialog()
				.setTitle("PDF select")
				.setMessage("Select the PDF to Extract Pages from!")
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
			srcPDF = file;
			pickPageNumberToSplit();
		}

	}

	private void pickPageNumberToSplit() {
		PdfReader pdfReader = null;
		try {
			pdfReader = new PdfReader(srcPDF.getAbsolutePath());
			if (pdfReader.isEncrypted()) {
				Intent pickPassword = new Intent(this,
						PickPasswordActivity.class);
				startActivityForResult(pickPassword,
						SAPDFCContstants.PICK_PASSWORD_REQUEST);
			} else {
				startPickPageNumberActivity();
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
			if (pdfReader != null) {
				pdfReader.close();
			}
		}

	}

	private void startPickPageNumberActivity() {
		PdfReader pdfReader = null;
		try {
			pdfReader = getPDFReader(srcPDF);

			numberOfPages = pdfReader.getNumberOfPages();
			if (numberOfPages <= 1) {
				getAlertDialog()
						.setTitle("Invalid selection")
						.setMessage(
								"The selected PDF just has just "
										+ numberOfPages
										+ " pages, so cannot be split further!!!")
						.setPositiveButton("Yes",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int which) {
										dialog.dismiss();
										finish();
										return;
									}

								}).show();
			} else {
				Intent pickPage = new Intent(this, PickNumberActivity.class);
				pickPage.putExtra(SAPDFCContstants.NUMBER, numberOfPages);
				startActivityForResult(pickPage,
						SAPDFCContstants.PICK_NUMBER_REQUEST);

			}

		} catch (Exception e) {
			String message = "Exception while reading the PDF!";
			if (password != null) {
				message = message + " (you might have entered wrong password)!";
			}

			getAlertDialog()
					.setTitle("ERROR")
					.setMessage(message)
					.setPositiveButton("Ok",
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

	private PdfReader getPDFReader(File file) throws IOException {
		if (password == null) {
			return new PdfReader(file.getAbsolutePath());
		} else {
			return new PdfReader(file.getAbsolutePath(), password.getBytes());
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == SAPDFCContstants.PICK_NUMBER_REQUEST) {
				fromPageNumber = data.getIntExtra(SAPDFCContstants.FROM_NUMBER,
						0);
				toPageNumber = data.getIntExtra(SAPDFCContstants.TO_NUMBER,
						numberOfPages);

				new SplitPDF().execute();
			} else {
				if (requestCode == SAPDFCContstants.PICK_PASSWORD_REQUEST) {
					password = data.getStringExtra(SAPDFCContstants.TEXT);
					startPickPageNumberActivity();
				}
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

			pdffile = SAPDFPathManager.getSavePDFPath("EXTRACTED_"
					+ srcPDF.getName() + ".pdf");

			Document document = new Document();
			PdfReader inputPDF = null;
			PdfWriter writer = null;
			try {
				inputPDF = getPDFReader(srcPDF);
				// Create a writer for the outputstream
				writer = PdfWriter.getInstance(document, new FileOutputStream(
						pdffile));

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
		setContentView(R.layout.activity_split);
		TextView textView = (TextView) findViewById(R.id.message);
		Button open_button = (Button) findViewById(R.id.openPDF);
		Button share_button = (Button) findViewById(R.id.sharePDF);
		Button protect_button = (Button) findViewById(R.id.protectPDF);

		if (error) {
			SAPDFUtils.setErrorText(textView, "Unable to extract PDF " + srcPDF
					+ " from page " + fromPageNumber + " to page "
					+ toPageNumber);
			open_button.setEnabled(false);
			share_button.setEnabled(false);
			protect_button.setEnabled(false);

		} else {
			SAPDFUtils.setSuccessText(textView, "PDF " + srcPDF.getName()
					+ " successfully extracted  from page " + fromPageNumber
					+ " to page " + toPageNumber + " into PDF file:" + pdffile);
		}
	}

}
