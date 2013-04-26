package com.snpdfp.activity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.usermodel.Range;
import org.apache.poi.hwpf.usermodel.Table;
import org.apache.poi.hwpf.usermodel.TableCell;
import org.apache.poi.hwpf.usermodel.TableRow;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

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
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.tool.xml.XMLWorkerHelper;
import com.snpdfp.utils.SNPDFCContstants;
import com.snpdfp.utils.SNPDFPathManager;
import com.snpdfp.utils.SNPDFUtils;

public class FileToPDFActivity extends SNPDFActivity {
	Logger logger = Logger.getLogger(FileToPDFActivity.class.getName());

	File srcFile = null;
	String fileType = SNPDFCContstants.FILE_TYPE_TXT;

	/** Called when the activity is first created. */

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		if (getIntent().getStringExtra(SNPDFCContstants.FILE_TYPE) != null) {
			fileType = getIntent().getStringExtra(SNPDFCContstants.FILE_TYPE);
		}

		setContentView(R.layout.activity_file_to_pdf);

		if (SNPDFCContstants.FILE_TYPE_HTML.equals(fileType)) {
			TextView textView = (TextView) findViewById(R.id.intro_ftp);
			textView.setText(textView.getText()
					+ "\nOnly strict HTML files get converted properly!");
		} else if (SNPDFCContstants.FILE_TYPE_DOC.equals(fileType)) {
			TextView textView = (TextView) findViewById(R.id.intro_ftp);
			textView.setText(textView.getText()
					+ "\nOnly text from DOC file gets converted to PDF. Images and formatting (including table structure) will be lost.");
		}

	}

	public void pickFile(View view) {
		Intent filePick = new Intent(this, BrowsePDFActivity.class);
		filePick.putExtra(SNPDFCContstants.FILE_TYPE, fileType);
		startActivityForResult(filePick, SNPDFCContstants.PICK_FILE);
	}

	public void convert(View view) {
		if (srcFile == null || !srcFile.exists()) {
			getAlertDialog()
					.setTitle("Incomplete details")
					.setMessage("Please select the file to convert to PDF!")
					.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss();
								}

							}).show();
		} else {
			new FileConverter().execute();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == SNPDFCContstants.PICK_FILE) {
				srcFile = new File(
						data.getStringExtra(SNPDFCContstants.FILE_URI));
				setName();
			}

		} else {
			operationCancelled();
		}
	}

	private void setName() {
		EditText editText = (EditText) findViewById(R.id.file);
		editText.setText(srcFile.getName());

	}

	private class FileConverter extends AsyncTask<String, Void, Boolean> {

		private ProgressDialog progressDialog;

		@Override
		protected void onPreExecute() {
			progressDialog = new ProgressDialog(FileToPDFActivity.this);
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
			logger.info("****** starting to convert file to pdf **********");
			boolean error = false;
			String fileName = srcFile.getName().substring(0,
					srcFile.getName().lastIndexOf("."))
					+ ".pdf";
			mainFile = SNPDFPathManager.getSavePDFPath(fileName);

			// create a new document
			Document document = new Document(SNPDFCContstants.PAGE_SIZE);

			PdfWriter pdfWriter = null;

			try {
				pdfWriter = PdfWriter.getInstance(document,
						new FileOutputStream(mainFile));
				// open document
				document.open();
				pdfWriter.open();

				if (srcFile.getName().toLowerCase().endsWith(".txt")
						|| srcFile.getName().toLowerCase().endsWith(".log")
						|| srcFile.getName().toLowerCase().endsWith(".csv")) {
					convertTXT(document);
				} else if (srcFile.getName().toLowerCase().endsWith(".htm")
						|| srcFile.getName().toLowerCase().endsWith(".html")) {
					convertHTML(document, pdfWriter);
				} else if (srcFile.getName().toLowerCase().endsWith(".doc")) {
					convertDoc(document);
				}

			} catch (Exception e) {
				logger.log(Level.SEVERE, "Unable to create PDF", e);
				error = true;
				errorMessage = e.getLocalizedMessage();
			} finally {
				// close the document
				if (document != null)
					document.close();
				// close the writer
				if (pdfWriter != null)
					pdfWriter.close();
			}

			return error;

		}

		private void convertTXT(Document document) throws IOException,
				DocumentException {
			BufferedReader in = null;
			try {
				in = new BufferedReader(new FileReader(srcFile));
				// Read the file and write it into the new file
				String fileText = in.readLine();

				while (fileText != null) {
					document.add(new Paragraph(fileText));
					fileText = in.readLine();
				}
			} finally {
				if (in != null)
					in.close();
			}

		}

		private void convertHTML(Document document, PdfWriter pdfWriter)
				throws IOException {
			// To convert a HTML file from the filesystem
			FileInputStream fis = null;
			XMLWorkerHelper worker = null;
			try {
				fis = new FileInputStream(srcFile);
				worker = XMLWorkerHelper.getInstance();
				// convert to PDF
				worker.parseXHtml(pdfWriter, document, fis);
			} finally {
				if (fis != null) {
					fis.close();
				}
			}

		}

		private void convertDoc(Document document) throws DocumentException,
				IOException {
			FileInputStream fis = null;
			POIFSFileSystem fs = null;
			try {
				fis = new FileInputStream(srcFile);
				fs = new POIFSFileSystem(fis);
				HWPFDocument doc = new HWPFDocument(fs);
				Range range = doc.getRange();
				TableRow row = null;
				PdfPTable pdftable = null;
				TableCell cell = null;
				PdfPCell pdfcell = null;
				Paragraph paragraph = null;
				for (int i = 0; i < range.numParagraphs(); i++) {
					org.apache.poi.hwpf.usermodel.Paragraph par = range
							.getParagraph(i);

					if (!par.isInTable()) {
						paragraph = new Paragraph(par.text().trim());
						document.add(paragraph);

					} else {
						document.add(new Paragraph("\n"));
						Table table = range.getTable(par);
						int paragraphCount = table.numParagraphs();
						for (int rowIdx = 0; rowIdx < table.numRows(); rowIdx++) {
							row = table.getRow(rowIdx);
							pdftable = new PdfPTable(row.numCells());
							for (int colIdx = 0; colIdx < row.numCells(); colIdx++) {
								cell = row.getCell(colIdx);
								pdfcell = new PdfPCell(new Phrase(cell.text()
										.trim()));
								pdftable.addCell(pdfcell);

							}

							// Set table width to complete pdf
							pdftable.setWidthPercentage(100f);
							document.add(pdftable);
						}

						i = i + paragraphCount;

					}

				}
			} finally {
				if (fis != null) {
					fis.close();
				}
			}

		}

	}

	public void displayResult(Boolean error) {
		setContentView(R.layout.snpdf_output);

		if (error) {
			SNPDFUtils.setErrorText(this,
					"Unable to convert file " + srcFile.getName() + " to PDF ("
							+ errorMessage + ")");
			disableButtons();
		} else {
			SNPDFUtils.setSuccessText(this, "PDF file successfully created.",
					mainFile);
		}
	}

}
