package com.snpdfp.activity;

import java.io.File;
import java.io.FileOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.TextView;

import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.snpdfp.layout.FolderLayout;
import com.snpdfp.layout.IFolderItemListener;
import com.snpdfp.utils.SNPDFCContstants;
import com.snpdfp.utils.SNPDFPathManager;
import com.snpdfp.utils.SNPDFUtils;

public class WatermarkActivity extends SNPDFActivity implements
		IFolderItemListener {
	Logger logger = Logger.getLogger(WatermarkActivity.class.getName());

	FolderLayout localFolders;
	File selectedFile;
	File image;

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
							"Please select a valid .pdf file to add watermark to!")
					.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss();
								}

							}).show();
		} else {
			addWatermark();
		}

	}

	private void addWatermark() {
		PdfReader pdfReader = null;
		try {
			pdfReader = new PdfReader(selectedFile.getAbsolutePath());
			if (pdfReader.isEncrypted()) {
				Intent pickPassword = new Intent(this,
						PickPasswordActivity.class);
				startActivityForResult(pickPassword,
						SNPDFCContstants.PICK_PASSWORD_REQUEST);

			} else {
				pickImage();
			}

		} catch (Exception e) {
			final Intent pickPassword = new Intent(this,
					PickPasswordActivity.class);
			getAlertDialog()
					.setTitle("Protected PDF")
					.setMessage(
							"Unable to read PDF, as it seems to be protected. You want to continue the add-watermark action by filling the password?")
					.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss();
									startActivityForResult(
											pickPassword,
											SNPDFCContstants.PICK_PASSWORD_REQUEST);
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

	private void pickImage() {
		getAlertDialog().setTitle("Pick Image")
				.setMessage("Pick the image to add as watermark!")
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						Intent imagePick = new Intent(Intent.ACTION_PICK,
								MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
						startActivityForResult(imagePick,
								SNPDFCContstants.RESULT_LOAD_IMAGE_REQUEST);
					}
				}).show();

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == SNPDFCContstants.PICK_PASSWORD_REQUEST) {
				password = data.getStringExtra(SNPDFCContstants.TEXT);
				pickImage();
			} else if (requestCode == SNPDFCContstants.RESULT_LOAD_IMAGE_REQUEST) {
				image = new File(getImagePathFromURI(data.getData()));
				new WatermarkExecutor().execute();
			}

		} else {
			operationCancelled();
		}
	}

	private class WatermarkExecutor extends AsyncTask<String, Void, Boolean> {

		private ProgressDialog progressDialog;

		@Override
		protected void onPreExecute() {
			progressDialog = new ProgressDialog(WatermarkActivity.this);
			progressDialog.setMessage("Adding watermark to the PDF...");
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
			logger.info("****** starting to add watermark to pdf **********");
			boolean error = false;

			PdfReader pdfReader = null;
			PdfStamper stamp = null;
			mainFile = SNPDFPathManager.getSavePDFPath(selectedFile.getName());
			try {
				if (password != null) {
					pdfReader = new PdfReader(selectedFile.getAbsolutePath(),
							password.getBytes());
				} else {
					pdfReader = new PdfReader(selectedFile.getAbsolutePath());
				}

				int number_of_pages = pdfReader.getNumberOfPages();
				stamp = new PdfStamper(pdfReader,
						new FileOutputStream(mainFile));
				int i = 0;
				Image watermark_image = Image.getInstance(image
						.getAbsolutePath());
				watermark_image.scaleToFit(PageSize.A4.getWidth(),
						PageSize.A4.getHeight());
				watermark_image.setAbsolutePosition(0, 0);
				PdfContentByte add_watermark = null;
				while (i < number_of_pages) {
					i++;
					add_watermark = stamp.getUnderContent(i);
					add_watermark.addImage(watermark_image);
				}

			} catch (Exception e) {
				logger.log(Level.SEVERE, "Unable to add watermark to PDF", e);
				error = true;
			} finally {
				// close the document
				if (stamp != null) {
					try {
						stamp.close();
					} catch (Exception e) {

					}
				}
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
		if (error) {
			SNPDFUtils
					.setErrorText(textView, "Unable to add watermark to PDF: "
							+ selectedFile.getName());
			disableButtons();

		} else {
			SNPDFUtils.setSuccessText(textView,
					"Watermark successfully added.", mainFile);
		}
	}

}
