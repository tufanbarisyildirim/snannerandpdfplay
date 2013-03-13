package com.snpdfp.activity;

import java.io.File;
import java.util.logging.Logger;

import android.app.Activity;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;

import com.snpdfp.utils.SAPDFCContstants;
import com.snpdfp.utils.SAPDFPathManager;

public class MainActivity extends SNPDFActivity {

	Logger logger = Logger.getLogger(MainActivity.class.getName());

	Uri output = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final Intent intent = getIntent();
		String action = intent.getAction();
		String type = intent.getType();

		logger.info(action + "  " + type);
		if (Intent.ACTION_SEND.equals(action) && type != null) {
			if (type.startsWith("image/")) {
				getAlertDialog()
						.setTitle(SAPDFCContstants.APP_TITLE)
						.setMessage(
								"Are you sure you want to convert the selected image into PDF?")
						.setPositiveButton("Yes",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int which) {
										dialog.dismiss();
										convertImage(intent);
									}

								})
						.setNegativeButton("Cancel",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int which) {
										dialog.dismiss();
										finish();
										return;
									}

								}).show();

			} else if (type.equals("application/pdf")) {
				getAlertDialog()
						.setTitle(SAPDFCContstants.APP_TITLE)
						.setMessage(
								"Are you sure you want to protect the selected PDF with a password?")
						.setPositiveButton("Yes",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int which) {
										dialog.dismiss();
										lockPDF(intent);
									}

								})
						.setNegativeButton("Cancel",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int which) {
										dialog.dismiss();
										finish();
										return;
									}

								}).show();
			}

		} else {
			setContentView(R.layout.activity_main);
		}
	}

	public void lockPDF(View view) {
		logger.info("*************** starting to lock pdf **************");
		Intent filePick = new Intent(this, BrowsePDFActivity.class);
		filePick.putExtra(SAPDFCContstants.PDF_REQUEST_TYPE,
				SAPDFCContstants.PDF_REQUEST_LOCK);
		startActivity(filePick);
	}

	public void concatenatePDF(View view) {
		logger.info("*************** concatenating two pdfs **************");
		Intent filePick = new Intent(this, ConcatenatePDFActivity.class);
		startActivity(filePick);
	}

	public void extractPDF(View view) {
		logger.info("*************** starting to split the PDF **************");
		Intent filePick = new Intent(this, SplitActivity.class);
		startActivity(filePick);
	}
	
	public void pdfToText(View view) {
		logger.info("*************** starting to extract text from PDF **************");
		Intent filePick = new Intent(this, ExtractTextActivity.class);
		startActivity(filePick);
	}

	public void convertTXTFile(View view) {
		logger.info("*************** starting converting TXT to pdf **************");
		getAlertDialog().setTitle("File select")
				.setMessage("Select the TXT File to convert to PDF...")
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						convertTXTFile();
					}

				}).show();

	}

	private void convertTXTFile() {
		Intent filePick = new Intent(this, FileToPDFActivity.class);
		filePick.putExtra(SAPDFCContstants.FILE_TYPE,
				SAPDFCContstants.FILE_TYPE_TXT);
		startActivity(filePick);

	}

	public void convertHTMLFile(View view) {
		logger.info("*************** starting converting HTML to pdf **************");
		getAlertDialog()
				.setTitle("NOTE")
				.setMessage(
						"Only strict HTML files are capable for conversion. The conversion may not work on distorted HTMLs!")
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						convertHTMLFile();
					}

				}).show();

	}

	private void convertHTMLFile() {
		Intent filePick = new Intent(this, FileToPDFActivity.class);
		filePick.putExtra(SAPDFCContstants.FILE_TYPE,
				SAPDFCContstants.FILE_TYPE_HTML);
		startActivity(filePick);
	}

	/** Called when the user clicks the SCAN button */
	public void scan(View view) {
		logger.info("*************** starting scanner **************");
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

		File file = SAPDFPathManager.getSNPDFPicFile();

		output = Uri.fromFile(file);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, output);
		startActivityForResult(intent,
				SAPDFCContstants.PICK_CAMERA_IMAGE_REQUEST);
	}

	/** Called when the user clicks the ConvertImage button */
	public void convertImage(View view) {
		logger.info("*************** starting convert image **************");
		Intent imagePick = new Intent(Intent.ACTION_PICK,
				MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		startActivityForResult(imagePick,
				SAPDFCContstants.RESULT_LOAD_IMAGE_REQUEST);
	}

	private void convertImage(Intent intent) {
		logger.info("*************** starting image to pdf **************");
		Intent pdfintent = new Intent(this, ImageToPDFActivity.class);
		pdfintent.putExtra(SAPDFCContstants.IMAGE_URI,
				getImagePathFromURI((Uri) intent
						.getParcelableExtra(Intent.EXTRA_STREAM)));
		startActivity(pdfintent);

	}

	private void lockPDF(Intent intent) {
		logger.info("*************** starting protect pdf **************");
		Intent pdfintent = new Intent(this, ProtectPDFActivity.class);
		pdfintent.putExtra(SAPDFCContstants.FILE_URI,
				getFilePathFromURI((Uri) intent
						.getParcelableExtra(Intent.EXTRA_STREAM)));
		startActivity(pdfintent);

	}

	private String getFilePathFromURI(Uri contentUri) {
		return new File(contentUri.getPath()).getAbsolutePath();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == SAPDFCContstants.PICK_CAMERA_IMAGE_REQUEST) {
				logger.info("Image picked:" + output);
				Intent intent = new Intent(this, ImageToPDFActivity.class);
				intent.putExtra(SAPDFCContstants.IMAGE_URI, output.getPath());
				startActivity(intent);

			} else if (requestCode == SAPDFCContstants.RESULT_LOAD_IMAGE_REQUEST) {
				logger.info("image loaded: " + data.getData());
				Intent intent = new Intent(this, ImageToPDFActivity.class);
				intent.putExtra(SAPDFCContstants.IMAGE_URI,
						getImagePathFromURI(data.getData()));
				startActivity(intent);

			}

		} else {
			getAlertDialog()
					.setTitle("Operation Cancelled!")
					.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss();
									return;
								}

							}).show();
		}

	}

	private String getImagePathFromURI(Uri contentUri) {
		String[] proj = { MediaStore.Images.Media.DATA };
		CursorLoader loader = new CursorLoader(this, contentUri, proj, null,
				null, null);
		Cursor cursor = loader.loadInBackground();
		int column_index = cursor
				.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();
		return cursor.getString(column_index);
	}

	/**
	 * It is absolutely necessary here
	 */
	@Override
	public void onBackPressed() {
		finish();
	}

}
