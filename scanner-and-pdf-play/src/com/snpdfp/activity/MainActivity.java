package com.snpdfp.activity;

import java.io.File;
import java.util.logging.Logger;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import com.snpdfp.utils.SNPDFCContstants;
import com.snpdfp.utils.SNPDFPathManager;

public class MainActivity extends SNPDFActivity {

	Logger logger = Logger.getLogger(MainActivity.class.getName());
	private static boolean welcomeShown = false;

	Uri output = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final Intent intent = getIntent();
		String action = intent.getAction();
		String type = intent.getType();

		// setup page size
		setUpPageSize();

		logger.info(action + "  " + type);
		if (Intent.ACTION_SEND.equals(action) && type != null) {
			if (type.startsWith("image/")) {
				getAlertDialog()
						.setTitle(SNPDFCContstants.APP_TITLE)
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
										operationCancelled();
									}

								}).show();

			} else if (type.equals("application/pdf")) {
				getAlertDialog()
						.setTitle(SNPDFCContstants.APP_TITLE)
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
										operationCancelled();
									}

								}).show();
			}

		} else {
			if (!welcomeShown) {
				showWelcomeMessage();
				welcomeShown = true;
			}

			setContentView(R.layout.activity_main);
		}
	}

	private void showWelcomeMessage() {
		mPrefs = PreferenceManager.getDefaultSharedPreferences(this);

		if (!mPrefs.getBoolean(snpdfSkipIntro, false)) {
			AlertDialog.Builder imageDialog = new AlertDialog.Builder(this);
			LayoutInflater inflater = (LayoutInflater) this
					.getSystemService(LAYOUT_INFLATER_SERVICE);
			final View layout = inflater.inflate(
					R.layout.custom_fullimage_dialog,
					(ViewGroup) findViewById(R.id.welcome_snpdf));
			imageDialog.setView(layout);
			imageDialog.setPositiveButton("OK",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							CheckBox dontShowAgain = (CheckBox) layout
									.findViewById(R.id.skip);
							if (dontShowAgain.isChecked()) {
								SharedPreferences.Editor editor = mPrefs.edit();
								editor.putBoolean(snpdfSkipIntro, true);
								editor.commit();
							}
						}

					});

			imageDialog.create();
			imageDialog.show();
		}

	}

	public void lockPDF(View view) {
		logger.info("*************** starting to lock pdf **************");
		Intent intent = new Intent(this, ProtectPDFActivity.class);
		startActivity(intent);
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

	public void copyProtectedPDF(View view) {
		Intent copyIntent = new Intent(this, CopyEncryptedActivity.class);
		startActivity(copyIntent);
	}

	public void addWatermark(View view) {
		logger.info("*************** starting to add watermark to PDF **************");
		Intent waterMarkIntent = new Intent(this, WatermarkActivity.class);
		startActivity(waterMarkIntent);

	}

	public void convertTXTFile(View view) {
		Intent filePick = new Intent(this, FileToPDFActivity.class);
		filePick.putExtra(SNPDFCContstants.FILE_TYPE,
				SNPDFCContstants.FILE_TYPE_TXT);
		startActivity(filePick);

	}

	public void convertDocFile(View view) {
		Intent filePick = new Intent(this, FileToPDFActivity.class);
		filePick.putExtra(SNPDFCContstants.FILE_TYPE,
				SNPDFCContstants.FILE_TYPE_DOC);
		startActivity(filePick);

	}

	public void convertHTMLFile(View view) {
		Intent filePick = new Intent(this, FileToPDFActivity.class);
		filePick.putExtra(SNPDFCContstants.FILE_TYPE,
				SNPDFCContstants.FILE_TYPE_HTML);
		startActivity(filePick);
	}

	/** Called when the user clicks the SCAN button */
	public void scan(View view) {
		logger.info("*************** starting scanner **************");
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

		File file = SNPDFPathManager.getSNPDFPicFile();

		output = Uri.fromFile(file);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, output);
		startActivityForResult(intent,
				SNPDFCContstants.PICK_CAMERA_IMAGE_REQUEST);
	}

	/** Called when the user clicks the ConvertImage button */
	public void convertImage(View view) {
		logger.info("*************** starting convert image **************");
		Intent imagePick = new Intent(Intent.ACTION_PICK,
				MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		startActivityForResult(imagePick,
				SNPDFCContstants.RESULT_LOAD_IMAGE_REQUEST);
	}

	private void convertImage(Intent intent) {
		logger.info("*************** starting image to pdf **************");
		Intent pdfintent = new Intent(this, ImageToPDFActivity.class);
		pdfintent.putExtra(SNPDFCContstants.IMAGE_URI,
				getImagePathFromURI((Uri) intent
						.getParcelableExtra(Intent.EXTRA_STREAM)));
		startActivity(pdfintent);

	}

	private void lockPDF(Intent intent) {
		logger.info("*************** starting protect pdf **************");
		Intent pdfintent = new Intent(this, ProtectPDFActivity.class);
		pdfintent.putExtra(SNPDFCContstants.FILE_URI,
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
			if (requestCode == SNPDFCContstants.PICK_CAMERA_IMAGE_REQUEST) {
				logger.info("Image picked:" + output);
				Intent intent = new Intent(this, ImageToPDFActivity.class);
				intent.putExtra(SNPDFCContstants.IMAGE_URI, output.getPath());
				startActivity(intent);

			} else if (requestCode == SNPDFCContstants.RESULT_LOAD_IMAGE_REQUEST) {
				logger.info("image loaded: " + data.getData());
				Intent intent = new Intent(this, ImageToPDFActivity.class);
				intent.putExtra(SNPDFCContstants.IMAGE_URI,
						getImagePathFromURI(data.getData()));
				startActivity(intent);

			}

		} else {
			showCancelledMsg();
		}

	}

	/**
	 * It is absolutely necessary here
	 */
	@Override
	public void onBackPressed() {
		finish();
	}

}
