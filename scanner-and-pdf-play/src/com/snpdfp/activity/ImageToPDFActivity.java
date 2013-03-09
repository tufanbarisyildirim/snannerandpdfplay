package com.snpdfp.activity;

import java.io.File;
import java.io.FileOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.itextpdf.text.Document;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfWriter;
import com.snpdfp.utils.SAPDFCContstants;
import com.snpdfp.utils.SAPDFPathManager;
import com.snpdfp.utils.SAPDFUtils;

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
		File pdf = SAPDFPathManager.getSavePDFPath(fileName);

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

			if (image.getHeight() / image.getWidth() == PageSize.A4.getHeight()
					/ PageSize.A4.getWidth()) {
				image.scaleToFit(PageSize.A4.getWidth(),
						PageSize.A4.getHeight());

			} else if (image.getHeight() / image.getWidth() < PageSize.A4
					.getHeight() / PageSize.A4.getWidth()) {
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
					.getStringExtra(SAPDFCContstants.IMAGE_URI);

			try {
				File file = new File(imagePath);
				pdffile = convertToPDF(file);
				logger.info("Created PDF File: " + pdffile);

			} catch (Exception e) {
				logger.log(Level.SEVERE, "Unable to create PDF", e);
				error = true;
			}

			return error;
		}

	}

	public void displayResult(Boolean error) {
		setContentView(R.layout.activity_image_to_pdf);

		logger.info("****** starting to convert image to pdf **********");
		TextView textView = (TextView) findViewById(R.id.message);
		Button open_button = (Button) findViewById(R.id.openPDF);
		Button share_button = (Button) findViewById(R.id.sharePDF);
		Button protect_button = (Button) findViewById(R.id.protectPDF);

		if (error) {
			SAPDFUtils.setErrorText(textView, "Unable to create PDF");
			open_button.setEnabled(false);
			share_button.setEnabled(false);
			protect_button.setEnabled(false);
		} else {
			SAPDFUtils.setSuccessText(textView, "Created PDF File: " + pdffile);
		}
	}

}
