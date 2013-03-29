package com.snpdfp.activity;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.itextpdf.text.PageSize;
import com.snpdfp.menu.About;
import com.snpdfp.menu.FAQActivity;
import com.snpdfp.utils.SNPDFCContstants;

public class SNPDFActivity extends Activity {

	SharedPreferences mPrefs;
	final String snpdfPageSize = "SNPDF_PAGE_SIZE";

	protected File mainFile = null;

	protected String errorMessage = "";

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		if (mainFile != null
				&& mainFile.getName().toLowerCase().endsWith(".txt")) {
			LinearLayout protect_pdf_layout = (LinearLayout) findViewById(R.id.protect_pdf_layout);
			protect_pdf_layout.setVisibility(View.GONE);
		}

	}

	public void cancel(View view) {
		showCancelledMsg();
		Intent result = new Intent();
		setResult(Activity.RESULT_CANCELED, result);
		finish();
	}

	/** Called when the user clicks the SCAN button */
	public void openPDF(View view) {
		Uri path = Uri.fromFile(mainFile);
		Intent intent = new Intent(Intent.ACTION_VIEW);
		if (mainFile.getName().toLowerCase().endsWith(".txt")) {
			intent.setDataAndType(path, "text/plain");
		} else {
			intent.setDataAndType(path, "application/pdf");
		}
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

		try {
			startActivity(Intent.createChooser(intent, "Open file with..."));
		} catch (ActivityNotFoundException e) {
			Toast.makeText(this, "No Application Available to View file",
					Toast.LENGTH_SHORT).show();
		}
	}

	public void sharePDF(View view) {
		Uri path = Uri.fromFile(mainFile);
		Intent intent = new Intent(Intent.ACTION_SEND);
		if (mainFile.getName().toLowerCase().endsWith(".txt")) {
			intent.setDataAndType(path, "text/plain");
		} else {
			intent.setDataAndType(path, "application/pdf");
		}
		String shareBody = "Emailing " + mainFile.getName();
		intent.putExtra(Intent.EXTRA_TEXT, shareBody);
		intent.putExtra(Intent.EXTRA_STREAM, path);

		try {
			startActivity(Intent.createChooser(intent, "Share via..."));
		} catch (ActivityNotFoundException e) {
			Toast.makeText(this, "No Application Available to share PDF",
					Toast.LENGTH_SHORT).show();
		}
	}

	public void protectPDF(View view) {
		if (!mainFile.getName().toLowerCase().endsWith(".pdf")) {
			getAlertDialog()
					.setTitle("Invalid option")
					.setMessage(
							"This option is not valid for non-pdf files: "
									+ mainFile.getName())
					.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss();
								}

							}).show();
		} else {
			Intent pdfintent = new Intent(this, ProtectPDFActivity.class);
			pdfintent.putExtra(SNPDFCContstants.FILE_URI,
					mainFile.getAbsolutePath());
			startActivity(pdfintent);
		}

	}

	public void deletePDF(View view) {
		getAlertDialog()
				.setTitle("Delete file?")
				.setMessage(
						"Are you sure to delete file: " + mainFile.getName())
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						delete(mainFile);
					}

				})
				.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
							}
						}).show();

	}

	public void renamePDF(View view) {
		getAlertDialog()
				.setTitle("New name")
				.setMessage(
						"Enter the new name. File type will not be changed!")
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						renameFile(mainFile);
					}

				})
				.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
							}
						}).show();

	}

	protected void disableButtons() {
		Button open_button = (Button) findViewById(R.id.openPDF);
		Button share_button = (Button) findViewById(R.id.sharePDF);
		Button protect_button = (Button) findViewById(R.id.protectPDF);
		Button delete_button = (Button) findViewById(R.id.deletePDF);
		Button rename_button = (Button) findViewById(R.id.renamePDF);
		open_button.setEnabled(false);
		share_button.setEnabled(false);
		delete_button.setEnabled(false);
		protect_button.setEnabled(false);
		rename_button.setEnabled(false);
	}

	private void renameFile(File mainFile) {
		Intent intent = new Intent(this, RenameActivity.class);
		intent.putExtra(SNPDFCContstants.FILE_URI, mainFile.getAbsolutePath());
		startActivity(intent);
	}

	private void delete(File file) {
		boolean success = file.delete();
		String message = "Unable to delete file " + file.getName();
		if (success)
			message = "File " + file.getName() + " successfully deleted!";
		Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

		startActivity(new Intent(this, MainActivity.class));
	}

	public android.app.AlertDialog.Builder getAlertDialog() {
		return new AlertDialog.Builder(this);
	}

	protected String getImagePathFromURI(Uri contentUri) {
		String[] proj = { MediaStore.Images.Media.DATA };
		CursorLoader loader = new CursorLoader(this, contentUri, proj, null,
				null, null);
		Cursor cursor = loader.loadInBackground();
		int column_index = cursor
				.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();
		return cursor.getString(column_index);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.snpdf_options_menu, menu);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.about:
			startActivity(new Intent(this, About.class));
			return true;
		case R.id.sndpf_location:
			startActivity(new Intent(this, OpenSNPDFFolderActivity.class));
			return true;
		case R.id.faq:
			startActivity(new Intent(this, FAQActivity.class));
			return true;
		case R.id.home:
			startActivity(new Intent(this, MainActivity.class));
			return true;
		case R.id.facebook:
			startActivity(new Intent(Intent.ACTION_VIEW,
					Uri.parse(SNPDFCContstants.FACEBOOK_URL)));
			return true;
		case R.id.settings:
			startActivity(new Intent(this, SNPDFSettings.class));
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onBackPressed() {
		finish();
		Intent intent = new Intent(this, MainActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}

	protected void operationCancelled() {
		showCancelledMsg();
		finish();

	}

	protected void showCancelledMsg() {
		Toast.makeText(this, "Operation cancelled", Toast.LENGTH_SHORT).show();
	}

	protected void showCannotReadFileDialog(File file) {
		getAlertDialog().setTitle("Invalid selection")
				.setMessage("[" + file.getName() + "] folder can't be read!")
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				}).show();

	}

	protected void setUpPageSize() {
		mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		String pageSize = mPrefs.getString(snpdfPageSize, "A4");
		setPageSize(pageSize);

	}

	protected void setPageSize(String size) {
		if ("A4".equalsIgnoreCase(size)) {
			SNPDFCContstants.PAGE_SIZE = PageSize.A4;

		} else if ("A3".equalsIgnoreCase(size)) {
			SNPDFCContstants.PAGE_SIZE = PageSize.A3;

		} else if ("A2".equalsIgnoreCase(size)) {
			SNPDFCContstants.PAGE_SIZE = PageSize.A2;

		} else if ("A1".equalsIgnoreCase(size)) {
			SNPDFCContstants.PAGE_SIZE = PageSize.A1;

		} else if ("A0".equalsIgnoreCase(size)) {
			SNPDFCContstants.PAGE_SIZE = PageSize.A0;

		} else if ("B4".equalsIgnoreCase(size)) {
			SNPDFCContstants.PAGE_SIZE = PageSize.B4;

		} else if ("B3".equalsIgnoreCase(size)) {
			SNPDFCContstants.PAGE_SIZE = PageSize.B3;

		} else if ("B2".equalsIgnoreCase(size)) {
			SNPDFCContstants.PAGE_SIZE = PageSize.B2;

		} else if ("B1".equalsIgnoreCase(size)) {
			SNPDFCContstants.PAGE_SIZE = PageSize.B1;

		} else if ("B0".equalsIgnoreCase(size)) {
			SNPDFCContstants.PAGE_SIZE = PageSize.B0;

		}

	}

}
