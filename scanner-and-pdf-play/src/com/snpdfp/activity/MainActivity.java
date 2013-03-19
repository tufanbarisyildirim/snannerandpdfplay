package com.snpdfp.activity;

import java.io.File;
import java.util.logging.Logger;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import com.snpdfp.utils.SNPDFCContstants;
import com.snpdfp.utils.SNPDFPathManager;

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
										finish();
										return;
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
			setContentView(R.layout.activity_main);
		}
	}

	public void lockPDF(View view) {
		logger.info("*************** starting to lock pdf **************");
		Intent filePick = new Intent(this, BrowsePDFActivity.class);
		filePick.putExtra(SNPDFCContstants.PDF_REQUEST_TYPE,
				SNPDFCContstants.PDF_REQUEST_LOCK);
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
		getAlertDialog()
				.setTitle("File select")
				.setMessage("Select the PDF to extract text from...")
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						pdfToText();
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

	private void pdfToText() {
		Intent filePick = new Intent(this, ExtractTextActivity.class);
		startActivity(filePick);

	}

	public void copyProtectedPDF(View view) {
		logger.info("*************** starting to copy encrypted PDF **************");
		getAlertDialog()
				.setTitle("File select")
				.setMessage("Select the protected PDF to copy...")
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						copyProtectedPDF();
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

	private void copyProtectedPDF() {
		Intent copyIntent = new Intent(this, CopyEncryptedActivity.class);
		startActivity(copyIntent);

	}

	public void addWatermark(View view) {
		logger.info("*************** starting to add watermark to PDF **************");
		getAlertDialog()
				.setTitle("File select")
				.setMessage("Select the PDF to add watermark to...")
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						addWatermark();
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

	private void addWatermark() {
		Intent waterMarkIntent = new Intent(this, WatermarkActivity.class);
		startActivity(waterMarkIntent);

	}

	public void convertTXTFile(View view) {
		logger.info("*************** starting converting TXT to pdf **************");
		getAlertDialog()
				.setTitle("File select")
				.setMessage("Select the TXT File to convert to PDF...")
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						convertTXTFile();
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

	private void convertTXTFile() {
		Intent filePick = new Intent(this, FileToPDFActivity.class);
		filePick.putExtra(SNPDFCContstants.FILE_TYPE,
				SNPDFCContstants.FILE_TYPE_TXT);
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

	private void convertHTMLFile() {
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
			// We are not calling operationCancelled as this activity should not
			// be finished
			Toast.makeText(this, "Operation cancelled", Toast.LENGTH_SHORT)
					.show();
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
