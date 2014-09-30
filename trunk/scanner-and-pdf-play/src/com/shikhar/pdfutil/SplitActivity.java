package com.shikhar.pdfutil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.lowagie.text.Document;
import com.lowagie.text.pdf.PdfCopy;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfReader;
import com.shikhar.pdfutil.layout.FolderLayout;
import com.shikhar.pdfutil.utils.SNPDFCContstants;
import com.shikhar.pdfutil.utils.SNPDFPathManager;
import com.shikhar.pdfutil.utils.SNPDFUtils;
import com.shikhar.pdfutil.R;

public class SplitActivity extends SNPDFActivity {

	Logger logger = Logger.getLogger(SplitActivity.class.getName());

	FolderLayout localFolders;
	File srcPDF;

	boolean password_req = false;
	String password;
	int numberOfPages;

	private List<Pair<Integer, Integer>> pages = new ArrayList<Pair<Integer, Integer>>();
	ArrayList<CharSequence> createdPDFs = new ArrayList<CharSequence>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_split);
		password_req = false;
		setInvisible(R.id.password);
	}

	public void pickFile(View view) {
		Intent filePick = new Intent(this, BrowsePDFActivity.class);
		startActivityForResult(filePick, SNPDFCContstants.PICK_FILE);
	}

	private boolean pdfDetailsComplete() {
		if (password_req) {
			password = ((EditText) findViewById(R.id.password)).getText().toString();
			if (password == null || password.equals("") || !SNPDFUtils.isPasswordCorrect(srcPDF, password)) {
				return false;
			}
		}

		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == SNPDFCContstants.PICK_FILE) {
				srcPDF = new File(data.getStringExtra(SNPDFCContstants.FILE_URI));
				setName();
				if (SNPDFUtils.isProtected(srcPDF)) {
					getAlertDialog().setTitle("PDF is encrypted!").setMessage("The selected PDF is protected, please enter it's password!")
						.setPositiveButton("OK", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
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
			showCancelledMsg();
		}
	}

	private void resetFields() {
		((EditText) findViewById(R.id.password)).setText("");
		((TextView) findViewById(R.id.message)).setText("");
		((EditText) findViewById(R.id.page_number)).setText("");
		pages = new ArrayList<Pair<Integer, Integer>>();
	}

	private void setName() {
		EditText editText = (EditText) findViewById(R.id.pdf_file);
		editText.setText(srcPDF.getName());
	}

	private boolean populatePageDetails(PdfReader inputPDF) {

		numberOfPages = inputPDF.getNumberOfPages();
		TextView textView = (TextView) findViewById(R.id.message);
		textView.setText("Maximum number of pages in selected PDF is " + numberOfPages + ".\nSo FROM cannot be less than 1 and TO cannot exceed "
			+ numberOfPages);

		if (numberOfPages <= 1) {
			return false;
		}

		return true;

	}

	private PdfReader getPDFReader(File file) throws IOException {
		return SNPDFUtils.getPdfReader(file.getAbsolutePath(), password_req, password);
	}

	public void split(View view) {
		if (srcPDF == null || !srcPDF.exists()) {
			getAlertDialog().setTitle("Please select a PDF first").setPositiveButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}

			}).show();
		} else if (!pdfDetailsComplete()) {
			getAlertDialog().setTitle("Incorrect password").setMessage("Please enter the correct PDF password")
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}

				}).show();
		} else {
			PdfReader inputPDF = null;

			try {
				inputPDF = getPDFReader(srcPDF);
				if (populatePageDetails(inputPDF)) {
					String pageString = ((EditText) findViewById(R.id.page_number)).getText().toString();

					StringBuffer errorMessage = new StringBuffer();
					if (pageString == null || pageString.trim().length() == 0) {
						errorMessage.append("Please enter page number!");
					} else {
						StringTokenizer tokenizer = new StringTokenizer(pageString, ",");
						while (tokenizer.hasMoreElements()) {
							String pageRange = tokenizer.nextToken().trim();
							if (pageRange.contains("-")) {
								String[] pageArray = pageRange.split("-");
								if (pageArray.length != 2) {
									errorMessage.append("\n" + pageRange + " is not in right format!");
								} else {
									try {
										int from = Integer.parseInt(pageArray[0].trim());
										int to = Integer.parseInt(pageArray[1].trim());
										if (invalidPageNumbers(to, from)) {
											errorMessage.append("\nPage numbers " + pageRange + " are out of range!");
										} else {
											pages.add(Pair.create(from, to));
										}
									} catch (Exception e) {
										errorMessage.append("\nInvalid page number format: " + pageRange);
									}
								}

							} else {
								try {
									int page = Integer.parseInt(pageRange);
									if (invalidPageNumbers(page, page)) {
										errorMessage.append("\nInvalid page number: " + page + " (out of range)");
									} else {
										pages.add(Pair.create(page, page));
									}
								} catch (Exception e) {
									errorMessage.append("\nInvalid page number: " + pageRange);
								}
							}
						}
					}
					if (errorMessage.length() > 0) {
						getAlertDialog().setTitle("Invalid page number!").setMessage(errorMessage).setPositiveButton("OK", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								pages = new ArrayList<Pair<Integer, Integer>>();
								dialog.dismiss();
							}
						}).show();
					} else {
						new SplitPDF(inputPDF, pages).execute();
					}

				} else {
					getAlertDialog().setTitle("Invalid PDF!").setMessage("The PDF just has one page - so cannot be further slipt!")
						.setPositiveButton("OK", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
							}
						}).show();
				}
			} catch (Exception e) {
				getAlertDialog().setTitle("ERROR").setMessage("Unable to process! Please re-enter all details")
					.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							resetFields();
						}
					}).show();
			}
		}
	}

	private boolean invalidPageNumbers(int toPageNumber, int fromPageNumber) {
		if ((toPageNumber == fromPageNumber && toPageNumber == numberOfPages && numberOfPages < 2)
			|| (toPageNumber > numberOfPages || fromPageNumber < 1 || toPageNumber < fromPageNumber)) {
			return true;
		}
		return false;
	}

	private class SplitPDF extends AsyncTask<String, Void, Boolean> {

		private ProgressDialog progressDialog;
		private PdfReader inputPDF;
		private List<Pair<Integer, Integer>> pages;

		public SplitPDF(PdfReader inputPDF, List<Pair<Integer, Integer>> pages) {
			this.inputPDF = inputPDF;
			this.pages = pages;
		}

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
			try {
				for (Pair<Integer, Integer> page : pages) {
					write(inputPDF, page.first, page.second);
				}
			} catch (Exception e) {
				error = true;
				errorMessage = e.getLocalizedMessage();
			} finally {
				if (inputPDF != null)
					inputPDF.close();
			}

			return error;
		}

		private void write(PdfReader inputPDF, int fromPageNumber, int toPageNumber) throws IOException {
			mainFile = SNPDFPathManager.getSavePDFPath("EXTRACTED_" + SNPDFPathManager.getFileNameWithoutExtn(srcPDF.getName()) + "_" + fromPageNumber
				+ "-" + toPageNumber + ".pdf");

			Document document = new Document();
			PdfCopy writer = null;
			try {
				writer = new PdfCopy(document, new FileOutputStream(mainFile));
				document.open();
				PdfImportedPage page = null;

				while (fromPageNumber <= toPageNumber) {
					document.newPage();
					page = writer.getImportedPage(inputPDF, fromPageNumber);
					writer.addPage(page);
					fromPageNumber++;
				}

				createdPDFs.add(mainFile.getName());
			} catch (Exception e) {
				throw new RuntimeException("Exception while splitting from " + fromPageNumber + " to " + toPageNumber + "[" + e.getLocalizedMessage() + "]");
			} finally {
				if (document.isOpen())
					document.close();
				if (writer != null)
					writer.close();

			}
		}
	}

	public void displayResult(Boolean error) {
		if (createdPDFs.size() == 1) {
			setContentView(R.layout.snpdf_output);

			if (error) {
				SNPDFUtils.setErrorText(this, "Unable to extract PDF " + srcPDF + " (" + errorMessage + ")");
				hideButtons();
			} else {
				SNPDFUtils.setSuccessText(this, "PDF " + srcPDF.getName() + " successfully extracted!!!", mainFile);
			}
		} else {
			Intent intent = new Intent(this, SplitOutput.class);
			intent.putCharSequenceArrayListExtra(SNPDFCContstants.FILES, createdPDFs);
			if (error) {
				intent.putExtra(SNPDFCContstants.MESSAGE, "Error while extracting files (" + errorMessage + ")");
				intent.putExtra(SNPDFCContstants.SUCCESS, false);
			} else {
				intent.putExtra(SNPDFCContstants.MESSAGE, createdPDFs.size() + " files successfully created after extraction");
				intent.putExtra(SNPDFCContstants.SUCCESS, true);
			}

			startActivity(intent);
		}

	}

}
