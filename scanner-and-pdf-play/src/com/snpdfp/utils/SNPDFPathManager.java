package com.snpdfp.utils;

import java.io.File;

import android.os.Environment;

public class SNPDFPathManager {

  private static File getSNPDFRoot() {
    return new File(Environment.getExternalStorageDirectory(), "snpdf");
  }

  public static File getSavePDFPath(String fileName) {
    // Environment
    // .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
    File file = getSNPDFRoot();

    if (!file.exists()) {
      file.mkdirs();
    }

    file = getFile(file, fileName);

    return file;
  }

  private static File getFile(File file, String fileName) {

    File returnFile = new File(file, fileName.substring(0, fileName.lastIndexOf(".")) + "_SNPDF"
        + fileName.substring(fileName.lastIndexOf(".")));
    int i = 0;
    while (returnFile.exists()) {
      returnFile = new File(file, fileName.substring(0, fileName.lastIndexOf(".")) + "_SNPDF(" + ++i + ")"
          + fileName.substring(fileName.lastIndexOf(".")));
    }

    return returnFile;
  }

  public static File getRootDirectory() {
    File dir = getSNPDFRoot();
    if (!dir.exists()) {
      dir.mkdirs();
    }

    return dir;
  }

  public static File getTextFileForPDF(File pdffile) {
    File file = new File(Environment.getExternalStorageDirectory(), "snpdf");

    String pdfFileName = pdffile.getName();
    if (!file.exists()) {
      file.mkdirs();
    }

    file = getFile(file, getFileNameWithoutExtn(pdfFileName) + ".txt");

    return file;
  }

  public static String getFileNameWithoutExtn(String filename) {
    return filename.substring(0, filename.lastIndexOf("."));
  }

  public static boolean isSNPDFImage(File file) {
    if (file.getParentFile().equals(getSNPDFRoot()) && file.getName().endsWith(".jpg")) {
      return true;
    }

    return false;
  }

}
