package com.shikhar.pdfutil;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;

import com.lowagie.text.Document;
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfWriter;
import com.shikhar.pdfutil.utils.SNPDFCContstants;
import com.shikhar.pdfutil.utils.SNPDFPathManager;
import com.shikhar.pdfutil.utils.SNPDFUtils;
import com.shikhar.pdfutil.R;

public class ImageToPDFActivity extends SNPDFActivity {
	Logger logger = Logger.getLogger(ImageToPDFActivity.class.getName());

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		List<String> imagePath = getIntent().getStringArrayListExtra(SNPDFCContstants.IMAGE_URI);

		if (imagePath != null && imagePath.size() > 0) {
			new ConvertImage(imagePath).execute();
		} else {
			getAlertDialog().setMessage("Seems no valid image was selected for conversion!").setPositiveButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					showMainPage();
				}

			}).show();
		}

	}

	private File convertToPDF(List<String> imagePath) throws Exception {
		String fileName = "PREPARED.pdf";
		if (imagePath.size() == 1) {
			String temp = new File(imagePath.get(0)).getName();
			fileName = temp.substring(0, temp.lastIndexOf(".")) + ".pdf";
		}

		File pdf = SNPDFPathManager.getSavePDFPath(fileName);

		logger.info("Intended PDF file path:" + pdf);

		Document document = null;
		if ("LANDSCAPE".equalsIgnoreCase(SNPDFCContstants.PAGE_LAYOUT)) {
			document = new Document(SNPDFCContstants.PAGE_SIZE.rotate());
		} else {
			document = new Document(SNPDFCContstants.PAGE_SIZE);
		}

		document.setMargins(0, 0, 0, 0);
		PdfWriter writer = null;
		try {
			FileOutputStream fos = new FileOutputStream(pdf);
			writer = PdfWriter.getInstance(document, fos);
			writer.open();
			document.open();

			for (String path : imagePath) {
				document.add(getImage(path));
			}

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

			// Clean up files
			for (String path : imagePath) {
				// Clean up snpdf images
				File file = new File(path);
				if (SNPDFPathManager.isSNPDFImage(file)) {
					file.delete();
				}
			}
		}

		return pdf;
	}

	private Image getImage(String path) throws Exception {
		Image image = Image.getInstance(path);
		Rectangle rectangle = null;
		if ("LANDSCAPE".equalsIgnoreCase(SNPDFCContstants.PAGE_LAYOUT)) {
			rectangle = SNPDFCContstants.PAGE_SIZE.rotate();
		} else {
			rectangle = SNPDFCContstants.PAGE_SIZE;
		}

		if (SNPDFCContstants.AUTOFILL) {
			float imageRatio = image.getHeight() / image.getWidth();
			float aspectRatio = rectangle.getHeight() / rectangle.getWidth();

			if (imageRatio == aspectRatio) {
				image.scaleToFit(rectangle.getWidth(), rectangle.getHeight());

			} else if (imageRatio < aspectRatio) {
				image.scaleToFit(rectangle.getWidth(), (rectangle.getWidth() / image.getWidth()) * image.getHeight());
			} else {
				image.scaleToFit((rectangle.getHeight() / image.getHeight() * image.getWidth()), rectangle.getHeight());
			}
		}

		return image;
	}

	private class ConvertImage extends AsyncTask<String, Void, Boolean> {

		private ProgressDialog progressDialog;

		private List<String> imagePath;

		public ConvertImage(List<String> imagePath) {
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
				mainFile = convertToPDF(imagePath);
				logger.info("Created PDF File: " + mainFile);

			} catch (Exception e) {
				logger.log(Level.SEVERE, "Unable to create PDF", e);
				error = true;
				errorMessage = e.getLocalizedMessage();
			}

			return error;
		}

	}

	public void displayResult(Boolean error) {
		setContentView(R.layout.snpdf_output);

		logger.info("****** starting to convert image to pdf **********");
		if (error) {
			SNPDFUtils.setErrorText(this, "Unable to create PDF (" + errorMessage + ")");
			hideButtons();
		} else {
			SNPDFUtils.setSuccessText(this, "PDF successfully created.", mainFile);
			SNPDFUtils.showPageSizeInfo(this);
		}
	}

}
