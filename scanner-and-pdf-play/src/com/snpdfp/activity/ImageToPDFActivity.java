package com.snpdfp.activity;

import java.io.File;
import java.io.FileOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import com.itextpdf.text.Document;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfWriter;
import com.snpdfp.utils.SNPDFCContstants;
import com.snpdfp.utils.SNPDFPathManager;
import com.snpdfp.utils.SNPDFUtils;

public class ImageToPDFActivity extends SNPDFActivity {
	Logger logger = Logger.getLogger(ImageToPDFActivity.class.getName());

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		new ConvertImage().execute();

	}

	private File convertToPDF(File input) throws Exception {
		String fileName = input.getName().substring(0,
				input.getName().lastIndexOf("."))
				+ ".pdf";
		File pdf = SNPDFPathManager.getSavePDFPath(fileName);

		logger.info("Intended PDF file path:" + pdf);

		Document document = new Document(PageSize.A4);
		document.setMargins(0, 0, 0, 0);
		PdfWriter writer = null;
		try {
			FileOutputStream fos = new FileOutputStream(pdf);
			writer = PdfWriter.getInstance(document, fos);
			writer.open();
			document.open();
			Image image = Image.getInstance(input.getAbsolutePath());

			float imageRatio = image.getHeight() / image.getWidth();
			float a4Ratio = PageSize.A4.getHeight() / PageSize.A4.getWidth();

			if (imageRatio == a4Ratio) {
				image.scaleToFit(PageSize.A4.getWidth(),
						PageSize.A4.getHeight());

			} else if (imageRatio < a4Ratio) {
				image.scaleToFit(
						PageSize.A4.getWidth(),
						(PageSize.A4.getWidth() / image.getWidth())
								* image.getHeight());
			} else {
				image.scaleToFit(
						(PageSize.A4.getHeight() / image.getHeight() * image
								.getWidth()), PageSize.A4.getHeight());
			}

			document.add(image);

		} catch (Exception e) {
			logger.log(Level.SEVERE, "unable to convert to pdf", e);
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
			Intent intent = getIntent();
			String imagePath = intent
					.getStringExtra(SNPDFCContstants.IMAGE_URI);

			try {
				File file = new File(imagePath);
				mainFile = convertToPDF(file);
				logger.info("Created PDF File: " + mainFile);

			} catch (Exception e) {
				logger.log(Level.SEVERE, "Unable to create PDF", e);
				error = true;
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
			SNPDFUtils.setErrorText(this, "Unable to create PDF");
			disableButtons();
		} else {
			SNPDFUtils.setSuccessText(this, "PDF successfully created.",
					mainFile);
		}
	}

}
