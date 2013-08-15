package com.snpdfp.activity;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import android.app.ProgressDialog;
import android.content.DialogInterface;
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

    List<String> imagePath = getIntent().getStringArrayListExtra(SNPDFCContstants.IMAGE_URI);

    if (imagePath != null && imagePath.size() > 0) {
      new ConvertImage(imagePath).execute();
    } else {
      getAlertDialog().setMessage("Seems no valid image was selected for conversion!")
          .setPositiveButton("OK", new DialogInterface.OnClickListener() {
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

    Document document = new Document(SNPDFCContstants.PAGE_SIZE);
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

    float imageRatio = image.getHeight() / image.getWidth();
    float aspectRatio = SNPDFCContstants.PAGE_SIZE.getHeight() / SNPDFCContstants.PAGE_SIZE.getWidth();

    if (imageRatio == aspectRatio) {
      image.scaleToFit(SNPDFCContstants.PAGE_SIZE.getWidth(), SNPDFCContstants.PAGE_SIZE.getHeight());

    } else if (imageRatio < aspectRatio) {
      image.scaleToFit(SNPDFCContstants.PAGE_SIZE.getWidth(),
          (SNPDFCContstants.PAGE_SIZE.getWidth() / image.getWidth()) * image.getHeight());
    } else {
      image.scaleToFit((SNPDFCContstants.PAGE_SIZE.getHeight() / image.getHeight() * image.getWidth()),
          SNPDFCContstants.PAGE_SIZE.getHeight());
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
    setContentView(R.layout.activity_image_to_pdf);

    logger.info("****** starting to convert image to pdf **********");
    if (error) {
      SNPDFUtils.setErrorText(this, "Unable to create PDF (" + errorMessage + ")");
      hideButtons();
    } else {
      SNPDFUtils.setSuccessText(this, "PDF successfully created.", mainFile);
    }
  }

}
