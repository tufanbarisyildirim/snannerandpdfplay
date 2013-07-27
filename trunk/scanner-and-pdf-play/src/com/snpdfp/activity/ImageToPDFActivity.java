package com.snpdfp.activity;

import java.io.File;
import java.io.FileOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import com.itextpdf.text.Document;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfWriter;
import com.snpdfp.utils.SNPDFCContstants;
import com.snpdfp.utils.SNPDFPathManager;
import com.snpdfp.utils.SNPDFUtils;

public class ImageToPDFActivity extends SNPDFActivity {
	Logger logger = Logger.getLogger(ImageToPDFActivity.class.getName());

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		String imagePath = getIntent().getStringExtra(
				SNPDFCContstants.IMAGE_URI);

		if (imagePath != null) {
			new ConvertImage(imagePath).execute();
		} else {
			final Context context = this;
			getAlertDialog()
					.setMessage(
							"Cannot identify the image, doesn't appear to exist on your phone!")
					.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss();
									Intent intent = new Intent(context,
											MainActivity.class);
									intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
									intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
									startActivity(intent);
								}

							}).show();
		}

	}

	private File convertToPDF(File input) throws Exception {
		String fileName = input.getName().substring(0,
				input.getName().lastIndexOf("."))
				+ ".pdf";
		File pdf = SNPDFPathManager.getSavePDFPath(fileName);

		logger.info("Intended PDF file path:" + pdf);

		Document document = new Document(SNPDFCContstants.PAGE_SIZE);
		document.setMargins(0, 0, 0, 0);
		PdfWriter writer = null;
		try {
			FileOutputStream fos = new FileOutputStream(pdf);
			writer = PdfWriter.getInstance(document, fos);
			writer.open();
			document.open();
			Image image = Image.getInstance(input.getAbsolutePath());

			float imageRatio = image.getHeight() / image.getWidth();
			float aspectRatio = SNPDFCContstants.PAGE_SIZE.getHeight()
					/ SNPDFCContstants.PAGE_SIZE.getWidth();

			if (imageRatio == aspectRatio) {
				image.scaleToFit(SNPDFCContstants.PAGE_SIZE.getWidth(),
						SNPDFCContstants.PAGE_SIZE.getHeight());

			} else if (imageRatio < aspectRatio) {
				image.scaleToFit(
						SNPDFCContstants.PAGE_SIZE.getWidth(),
						(SNPDFCContstants.PAGE_SIZE.getWidth() / image
								.getWidth()) * image.getHeight());
			} else {
				image.scaleToFit((SNPDFCContstants.PAGE_SIZE.getHeight()
						/ image.getHeight() * image.getWidth()),
						SNPDFCContstants.PAGE_SIZE.getHeight());
			}

			document.add(image);

		} catch (Exception e) {
			logger.log(Level.SEVERE, "unable to convert to pdf", e);
			if (pdf.exists()) {
				pdf.delete();
			}
			throw e;

		} finally {
			if (document != null) {
				document.close();
			}
			if (writer != null) {
				writer.close();
			}
		}

		return pdf;
	}

	private class ConvertImage extends AsyncTask<String, Void, Boolean> {

		private ProgressDialog progressDialog;

		private String imagePath;

		public ConvertImage(String imagePath) {
			this.imagePath = imagePath;
		}

		@Override
		protected void onPreExecute() {
			progressDialog = new ProgressDialog(ImageToPDFActivity.this);
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
			logger.info("****** starting to convert image to pdf **********");
			boolean error = false;

			try {
				File file = new File(imagePath);
				mainFile = convertToPDF(file);
				logger.info("Created PDF File: " + mainFile);

			} catch (Exception e) {
				logger.log(Level.SEVERE, "Unable to create PDF", e);
				error = true;
				errorMessage = e.getLocalizedMessage();
			} finally {
				SNPDFPathManager.getSNPDFPicFile().delete();
			}

			return error;
		}

	}

	public void displayResult(Boolean error) {
		setContentView(R.layout.activity_image_to_pdf);

		logger.info("****** starting to convert image to pdf **********");
		if (error) {
			SNPDFUtils.setErrorText(this, "Unable to create PDF ("
					+ errorMessage + ")");
			hideButtons();
		} else {
			SNPDFUtils.setSuccessText(this, "PDF successfully created.",
					mainFile);
		}
	}

}
