package com.snpdfp.activity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfWriter;
import com.snpdfp.layout.FolderLayout;
import com.snpdfp.utils.SNPDFCContstants;
import com.snpdfp.utils.SNPDFPathManager;
import com.snpdfp.utils.SNPDFUtils;

public class SplitActivity extends SNPDFActivity {

  Logger logger = Logger.getLogger(SplitActivity.class.getName());

  FolderLayout localFolders;
  File srcPDF;

  boolean password_req = false;
  String password;
  int numberOfPages;

  int fromPageNumber1;
  int toPageNumber1;

  int fromPageNumber2;
  int toPageNumber2;
  boolean enterSecond = false;

  int fromPageNumber3;
  int toPageNumber3;
  boolean enterThird = false;

  int fromPageNumber4;
  int toPageNumber4;
  boolean enterFourth = false;

  int fromPageNumber5;
  int toPageNumber5;
  boolean enterFifth = false;

  List<String> createdPDFs = new ArrayList<String>();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_split);

    EditText password = (EditText) findViewById(R.id.password);
    password.setVisibility(View.GONE);
    password_req = false;

    findViewById(R.id.section2).setVisibility(View.GONE);
    findViewById(R.id.section3).setVisibility(View.GONE);
    findViewById(R.id.section4).setVisibility(View.GONE);
    findViewById(R.id.section5).setVisibility(View.GONE);
  }

  public void addMore(View view) {
    if (findViewById(R.id.section2).getVisibility() == View.GONE) {
      findViewById(R.id.section2).setVisibility(View.VISIBLE);
      enterSecond = true;
    } else if (findViewById(R.id.section3).getVisibility() == View.GONE) {
      findViewById(R.id.section3).setVisibility(View.VISIBLE);
      enterThird = true;
    } else if (findViewById(R.id.section4).getVisibility() == View.GONE) {
      findViewById(R.id.section4).setVisibility(View.VISIBLE);
      enterFourth = true;
    } else if (findViewById(R.id.section5).getVisibility() == View.GONE) {
      findViewById(R.id.section5).setVisibility(View.VISIBLE);
      findViewById(R.id.add_more).setVisibility(View.GONE);
      enterFifth = true;
    }

  }

  public void pickFile(View view) {
    Intent filePick = new Intent(this, BrowsePDFActivity.class);
    startActivityForResult(filePick, SNPDFCContstants.PICK_FILE);
  }

  public void fillPageDetails(View view) {
    if (srcPDF == null || !srcPDF.exists()) {
      getAlertDialog().setTitle("Please select a PDF first")
          .setPositiveButton("OK", new DialogInterface.OnClickListener() {
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
      populatePageDetails(true);
    }

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
          getAlertDialog().setTitle("PDF is encrypted!")
              .setMessage("The selected PDF is protected, please enter it's password!")
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
      operationCancelled();
    }
  }

  private void resetFields() {
    ((EditText) findViewById(R.id.password)).setText("");
    ((TextView) findViewById(R.id.message)).setText("");
    ((EditText) findViewById(R.id.from_number1)).setText("");
    ((EditText) findViewById(R.id.to_number1)).setText("");
    ((EditText) findViewById(R.id.from_number2)).setText("");
    ((EditText) findViewById(R.id.to_number2)).setText("");
    ((EditText) findViewById(R.id.from_number3)).setText("");
    ((EditText) findViewById(R.id.to_number3)).setText("");
    ((EditText) findViewById(R.id.from_number4)).setText("");
    ((EditText) findViewById(R.id.to_number4)).setText("");
    ((EditText) findViewById(R.id.from_number5)).setText("");
    ((EditText) findViewById(R.id.to_number5)).setText("");
    findViewById(R.id.section2).setVisibility(View.GONE);
    findViewById(R.id.section3).setVisibility(View.GONE);
    findViewById(R.id.section4).setVisibility(View.GONE);
    findViewById(R.id.section5).setVisibility(View.GONE);
  }

  private void setName() {
    EditText editText = (EditText) findViewById(R.id.pdf_file);
    editText.setText(srcPDF.getName());

  }

  private void populatePageDetails(boolean showMessage) {
    PdfReader pdfReader = null;
    try {
      pdfReader = getPDFReader(srcPDF);

      numberOfPages = pdfReader.getNumberOfPages();
      if (numberOfPages <= 1) {
        getAlertDialog().setTitle("Invalid selection")
            .setMessage("The selected PDF just has " + numberOfPages + " page, so cannot be split further!!!")
            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
              }

            }).show();
      } else if (showMessage) {
        TextView textView = (TextView) findViewById(R.id.message);
        textView.setText("Maximum number of pages in selected PDF is " + numberOfPages
            + ".\nSo FROM cannot be less than 1 and TO cannot exceed " + (numberOfPages - 1));

      }

    } catch (Exception e) {
      getAlertDialog().setTitle("ERROR").setMessage("Unable to process! Please re-enter all details")
          .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
              dialog.dismiss();
            }

          }).show();
    } finally {
      if (pdfReader != null) {
        pdfReader.close();
      }
    }

  }

  private PdfReader getPDFReader(File file) throws IOException {
    if (!password_req) {
      return new PdfReader(file.getAbsolutePath());
    } else {
      return new PdfReader(file.getAbsolutePath(), password.getBytes());
    }
  }

  public void split(View view) {
    if (!checkFirst()) {
      getAlertDialog().setTitle("Incorrect page numbers").setMessage("Please enter valid page numbers in first block!")
          .setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
              dialog.dismiss();
            }

          }).show();
    } else if (!checkSecond()) {
      getAlertDialog().setTitle("Incorrect page numbers")
          .setMessage("Please enter valid page numbers in second block!")
          .setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
              dialog.dismiss();
            }

          }).show();
    } else if (!checkThird()) {
      getAlertDialog().setTitle("Incorrect page numbers").setMessage("Please enter valid page numbers in third block!")
          .setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
              dialog.dismiss();
            }

          }).show();
    } else if (!checkFourth()) {
      getAlertDialog().setTitle("Incorrect page numbers")
          .setMessage("Please enter valid page numbers in fourth block!")
          .setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
              dialog.dismiss();
            }

          }).show();
    } else if (!checkFifth()) {
      getAlertDialog().setTitle("Incorrect page numbers").setMessage("Please enter valid page numbers in fifth block!")
          .setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
              dialog.dismiss();
            }

          }).show();
    } else if (srcPDF == null || !srcPDF.exists()) {
      getAlertDialog().setTitle("Please select a PDF first")
          .setPositiveButton("OK", new DialogInterface.OnClickListener() {
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
      // populate page details
      populatePageDetails(true);
      boolean errorFill = false;
      try {
        toPageNumber1 = Integer.parseInt(((EditText) findViewById(R.id.to_number1)).getText().toString());
        fromPageNumber1 = Integer.parseInt(((EditText) findViewById(R.id.from_number1)).getText().toString());
        if (toPageNumber1 > numberOfPages - 1 || fromPageNumber1 < 1) {
          errorFill = true;
        }

        if (enterSecond) {
          toPageNumber2 = Integer.parseInt(((EditText) findViewById(R.id.to_number2)).getText().toString());
          fromPageNumber2 = Integer.parseInt(((EditText) findViewById(R.id.from_number2)).getText().toString());
          if (toPageNumber2 > numberOfPages - 1 || fromPageNumber2 < 1) {
            errorFill = true;
          }
        }

        if (enterThird) {
          toPageNumber3 = Integer.parseInt(((EditText) findViewById(R.id.to_number3)).getText().toString());
          fromPageNumber3 = Integer.parseInt(((EditText) findViewById(R.id.from_number3)).getText().toString());
          if (toPageNumber3 > numberOfPages - 1 || fromPageNumber3 < 1) {
            errorFill = true;
          }
        }

        if (enterFourth) {
          toPageNumber4 = Integer.parseInt(((EditText) findViewById(R.id.to_number4)).getText().toString());
          fromPageNumber4 = Integer.parseInt(((EditText) findViewById(R.id.from_number4)).getText().toString());
          if (toPageNumber4 > numberOfPages - 1 || fromPageNumber4 < 1) {
            errorFill = true;
          }
        }

        if (enterFifth) {
          toPageNumber5 = Integer.parseInt(((EditText) findViewById(R.id.to_number5)).getText().toString());
          fromPageNumber5 = Integer.parseInt(((EditText) findViewById(R.id.from_number5)).getText().toString());
          if (toPageNumber5 > numberOfPages - 1 || fromPageNumber5 < 1) {
            errorFill = true;
          }
        }

      } catch (Exception e) {
        errorFill = true;
      }

      if (errorFill) {
        getAlertDialog().setTitle("Incorrect page numbers")
            .setMessage("Invalid numbers entered! Please enter valid numbers as explained in the instructions.")
            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
              }

            }).show();
      } else {
        new SplitPDF().execute();
      }

    }

  }

  private boolean checkFirst() {
    String from = ((EditText) findViewById(R.id.from_number1)).getText().toString();
    String to = ((EditText) findViewById(R.id.to_number1)).getText().toString();

    if (from == null || "".equals(from) || to == null || "".equals(to)) {
      return false;
    }

    return true;
  }

  private boolean checkSecond() {
    if (enterSecond) {
      String from = ((EditText) findViewById(R.id.from_number2)).getText().toString();
      String to = ((EditText) findViewById(R.id.to_number2)).getText().toString();

      if (from == null || "".equals(from) || to == null || "".equals(to)) {
        return false;
      }
    }
    return true;

  }

  private boolean checkThird() {
    if (enterThird) {
      String from = ((EditText) findViewById(R.id.from_number3)).getText().toString();
      String to = ((EditText) findViewById(R.id.to_number3)).getText().toString();

      if (from == null || "".equals(from) || to == null || "".equals(to)) {
        return false;
      }
    }

    return true;

  }

  private boolean checkFourth() {
    if (enterFourth) {
      String from = ((EditText) findViewById(R.id.from_number4)).getText().toString();
      String to = ((EditText) findViewById(R.id.to_number4)).getText().toString();

      if (from == null || "".equals(from) || to == null || "".equals(to)) {
        return false;
      }
    }

    return true;

  }

  private boolean checkFifth() {
    if (enterFifth) {
      String from = ((EditText) findViewById(R.id.from_number5)).getText().toString();
      String to = ((EditText) findViewById(R.id.to_number5)).getText().toString();

      if (from == null || "".equals(from) || to == null || "".equals(to)) {
        return false;
      }
    }

    return true;

  }

  private class SplitPDF extends AsyncTask<String, Void, Boolean> {

    private ProgressDialog progressDialog;

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
      PdfReader inputPDF = null;
      try {
        inputPDF = getPDFReader(srcPDF);
        if (write(inputPDF, fromPageNumber1, toPageNumber1, true)) {
          return true;
        }
        if (write(inputPDF, fromPageNumber2, toPageNumber2, enterSecond)) {
          return true;
        }
        if (write(inputPDF, fromPageNumber3, toPageNumber3, enterThird)) {
          return true;
        }
        if (write(inputPDF, fromPageNumber4, toPageNumber4, enterFourth)) {
          return true;
        }
        if (write(inputPDF, fromPageNumber5, toPageNumber5, enterFifth)) {
          return true;
        }
      } catch (Exception e) {
        error = true;
        errorMessage = e.getLocalizedMessage();
      } finally {
        if (inputPDF != null) {
          inputPDF.close();
        }

      }

      return error;
    }

    private boolean write(PdfReader inputPDF, int fromPageNumber, int toPageNumber, boolean detailsComplete) {
      boolean error = false;
      if (detailsComplete) {
        mainFile = SNPDFPathManager.getSavePDFPath("EXTRACTED_"
            + SNPDFPathManager.getFileNameWithoutExtn(srcPDF.getName()) + "_" + fromPageNumber + "-" + toPageNumber
            + ".pdf");

        Document document = new Document();
        PdfWriter writer = null;
        try {
          // Create a writer for the outputstream
          writer = PdfWriter.getInstance(document, new FileOutputStream(mainFile));

          document.open();
          PdfContentByte cb = writer.getDirectContent(); // Holds the PDF data
          PdfImportedPage page = null;

          while (fromPageNumber <= toPageNumber) {
            document.newPage();
            page = writer.getImportedPage(inputPDF, fromPageNumber);
            cb.addTemplate(page, 0, 0);
            fromPageNumber++;
          }

          createdPDFs.add(mainFile.getName());

        } catch (Exception e) {
          error = true;
          errorMessage = "Exception while splitting from " + fromPageNumber + " to " + toPageNumber + "["
              + e.getLocalizedMessage() + "]";
        } finally {
          if (document.isOpen())
            document.close();
          if (writer != null) {
            writer.close();
          }
        }
      }

      return error;
    }
  }

  public void displayResult(Boolean error) {
    if (createdPDFs.size() == 1) {
      setContentView(R.layout.snpdf_output);

      if (error) {
        SNPDFUtils.setErrorText(this, "Unable to extract PDF " + srcPDF + " from page " + fromPageNumber1 + " to page "
            + toPageNumber1 + " (" + errorMessage + ")");
        hideButtons();

      } else {
        SNPDFUtils.setSuccessText(this, "PDF " + srcPDF.getName() + " successfully extracted  from page "
            + fromPageNumber1 + " to page " + toPageNumber1, mainFile);
      }
    } else {
      Intent intent = new Intent(this, OpenSNPDFFolderActivity.class);
      StringBuffer successText = new StringBuffer();
      if (error) {
        successText.append("Unable to extract PDF " + srcPDF + "(" + errorMessage + ")");
        intent.putExtra(SNPDFCContstants.SUCCESS, false);
      } else {
        successText.append("PDF " + srcPDF.getName() + " successfully extracted into " + createdPDFs.size() + " pdfs:");
        for (String fileName : createdPDFs) {
          successText.append("\n" + fileName);
        }
        intent.putExtra(SNPDFCContstants.SUCCESS, true);
      }

      intent.putExtra(SNPDFCContstants.TEXT, successText.toString());
      startActivity(intent);
    }

  }

}
