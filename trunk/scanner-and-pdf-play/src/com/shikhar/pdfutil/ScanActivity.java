package com.shikhar.pdfutil;

import java.util.ArrayList;
import java.util.logging.Logger;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;

import com.shikhar.pdfutil.utils.SNPDFCContstants;
import com.shikhar.pdfutil.utils.SNPDFPathManager;

public class ScanActivity extends SNPDFActivity {

  Logger logger = Logger.getLogger(ScanActivity.class.getName());

  private ArrayList<String> uris = new ArrayList<String>();

  private Uri currentUri;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    String request = getIntent().getStringExtra(SNPDFCContstants.REQUEST_TYPE);
    if ("s".equalsIgnoreCase(request)) {
      scanPhoto();
    } else if ("p".equalsIgnoreCase(request)) {
      pickImage();
    } else {
      logger.warning("Invalid request");
      finish();
    }

  }

  private void pickImage() {
    Intent imagePick = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
    startActivityForResult(imagePick, SNPDFCContstants.RESULT_LOAD_IMAGE_REQUEST);
  }

  private void scanPhoto() {
    currentUri = Uri.fromFile(SNPDFPathManager.getSavePDFPath("PIC.jpg"));

    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    intent.putExtra(MediaStore.EXTRA_OUTPUT, currentUri);
    startActivityForResult(intent, SNPDFCContstants.PICK_CAMERA_IMAGE_REQUEST);

  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
    if (resultCode == Activity.RESULT_OK) {
      if (requestCode == SNPDFCContstants.PICK_CAMERA_IMAGE_REQUEST) {
        uris.add(currentUri.getPath());
        getAlertDialog().setTitle("Multiple scans?")
            .setMessage("Do you want to scan more images? All the scanned images will go into the same PDF.")
            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                scanPhoto();
              }

            }).setNegativeButton("No", new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                startConversion();
              }

            }).show();

      } else if (requestCode == SNPDFCContstants.RESULT_LOAD_IMAGE_REQUEST) {
        String path = getImagePathFromURI(data.getData());
        if (path == null) {
          getAlertDialog()
              .setTitle("Invalid image?")
              .setMessage(
                  "Cannot process the selected image as it doesn't appear to be in your phone. Continue with another?")
              .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                  dialog.dismiss();
                  pickImage();
                }

              }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                  dialog.dismiss();
                  cancel();
                }

              }).show();

        } else {
          uris.add(path);
          getAlertDialog().setTitle("Multiple images?")
              .setMessage("Do you want to convert more images? All the selected images will go into the same PDF.")
              .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                  dialog.dismiss();
                  pickImage();
                }

              }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                  dialog.dismiss();
                  startConversion();
                }

              }).show();
        }

      }

    } else {
      cancel();
    }

  }

  private void cancel() {
    if (uris.size() == 0) {
      operationCancelled();
    } else {
      getAlertDialog().setTitle("Cancel?")
          .setMessage("Do you want to cancel the complete operation, OR prepare PDF from already selected?")
          .setPositiveButton("Prepare PDF", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
              dialog.dismiss();
              startConversion();
            }

          }).setNegativeButton("Cancel All", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
              dialog.dismiss();
              operationCancelled();
            }

          }).show();
    }

  }

  private void startConversion() {
    Intent intent = new Intent(this, ImageToPDFActivity.class);
    intent.putStringArrayListExtra(SNPDFCContstants.IMAGE_URI, uris);
    startActivity(intent);

  }
}
