package com.snpdf.activity;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;

import com.lowagie.text.Image;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;
import com.snpdf.utils.SNPDFCContstants;
import com.snpdf.utils.SNPDFPathManager;
import com.snpdf.utils.SNPDFUtils;
import com.snpdf.activity.R;

public class WatermarkActivity extends SNPDFActivity {
	Logger logger = Logger.getLogger(WatermarkActivity.class.getName());

	File selectedFile;
	File image;

	private Uri currentUri;

	private List<Uri> cleanUpUris = new ArrayList<Uri>();

	boolean password_req = false;
	String password;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_watermark);

		EditText password = (EditText) findViewById(R.id.password);
		password.setVisibility(View.GONE);
		password_req = false;

	}

	public void pickFile(View view) {
		Intent filePick = new Intent(this, BrowsePDFActivity.class);
		startActivityForResult(filePick, SNPDFCContstants.PICK_FILE);
	}

	public void pickImage(View view) {
		getAlertDialog().setTitle("Select image from?").setMessage("Do you want to select photo from Gallery or take a new photo?")
			.setPositiveButton("From Gallery", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();

					Intent imagePick = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
					startActivityForResult(imagePick, SNPDFCContstants.RESULT_LOAD_IMAGE_REQUEST);
				}

			}).setNegativeButton("Take Photo", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();

					currentUri = Uri.fromFile(SNPDFPathManager.getSavePDFPath("PIC.jpg"));
					cleanUpUris.add(currentUri);
					Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
					intent.putExtra(MediaStore.EXTRA_OUTPUT, currentUri);
					startActivityForResult(intent, SNPDFCContstants.PICK_CAMERA_IMAGE_REQUEST);
				}

			}).show();

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == SNPDFCContstants.PICK_FILE) {
				selectedFile = new File(data.getStringExtra(SNPDFCContstants.FILE_URI));
				((EditText) findViewById(R.id.file)).setText(selectedFile.getName());
				if (SNPDFUtils.isProtected(selectedFile)) {
					getAlertDialog().setTitle("PDF is encrypted!").setMessage("The selected PDF is protected, please enter it's password!")
						.setPositiveButton("OK", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
								EditText password = (EditText) findViewById(R.id.password);
								password.setVisibility(View.VISIBLE);
								password_req = true;
								password.setText("");
							}

						}).show();

				} else {
					EditText password = (EditText) findViewById(R.id.password);
					password.setVisibility(View.GONE);
					password_req = false;
					password.setText("");
				}
			} else if (requestCode == SNPDFCContstants.RESULT_LOAD_IMAGE_REQUEST) {
				image = new File(getImagePathFromURI(data.getData()));
				EditText imageFile = (EditText) findViewById(R.id.image);
				imageFile.setText(image.getName());

			} else if (requestCode == SNPDFCContstants.PICK_CAMERA_IMAGE_REQUEST) {
				image = new File(currentUri.getPath());
				EditText imageFile = (EditText) findViewById(R.id.image);
				imageFile.setText(image.getName() + " - New Camera Pic");
			}

		} else {
			showCancelledMsg();
		}
	}

	public void addWatermark(View view) {
		if (selectedFile == null || !selectedFile.exists()) {
			getAlertDialog().setTitle("Incomplete details").setMessage("Please select the PDF file to add watermark to!")
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}

				}).show();
		} else if (image == null || !image.exists()) {
			getAlertDialog().setTitle("Incomplete details").setMessage("Please select a valid image to add as watermark!")
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				}).show();
		} else if (!arePDFDetailsComplete()) {
			getAlertDialog().setTitle("PDF password required").setMessage("Please enter a valid password for the selected PDF")
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}

				}).show();
		} else {
			new WatermarkExecutor().execute();
		}

	}

	private boolean arePDFDetailsComplete() {
		if (password_req) {
			password = ((EditText) findViewById(R.id.password)).getText().toString();
			if (password == null || password.equals("") || !SNPDFUtils.isPasswordCorrect(selectedFile, password)) {
				return false;
			}
		}

		return true;
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
				pdfReader = SNPDFUtils.getPdfReader(selectedFile.getAbsolutePath(), password != null, password);

				int number_of_pages = pdfReader.getNumberOfPages();
				stamp = new PdfStamper(pdfReader, new FileOutputStream(mainFile));
				int i = 0;
				Image watermark_image = Image.getInstance(image.getAbsolutePath());
				watermark_image.scaleToFit(pdfReader.getPageSize(1).getWidth(), pdfReader.getPageSize(1).getHeight());
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
				errorMessage = e.getLocalizedMessage();
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
		setContentView(R.layout.snpdf_output);

		if (error) {
			SNPDFUtils.setErrorText(this, "Unable to add watermark to PDF: " + selectedFile.getName() + " (" + errorMessage + ")");
			hideButtons();

		} else {
			SNPDFUtils.setSuccessText(this, "Watermark successfully added.", mainFile);
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// clean up imgaes
		for (Uri uri : cleanUpUris) {
			new File(uri.getPath()).delete();
		}
	}

}
