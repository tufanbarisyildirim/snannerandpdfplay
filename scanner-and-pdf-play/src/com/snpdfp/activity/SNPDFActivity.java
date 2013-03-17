package com.snpdfp.activity;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.snpdfp.menu.About;
import com.snpdfp.menu.FAQActivity;
import com.snpdfp.utils.SAPDFCContstants;

public class SNPDFActivity extends Activity {

	protected File mainFile = null;

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		if (mainFile != null
				&& mainFile.getName().toLowerCase().endsWith(".txt")) {
			Button protect_button = (Button) findViewById(R.id.protectPDF);
			protect_button.setVisibility(View.GONE);
		}

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
			pdfintent.putExtra(SAPDFCContstants.FILE_URI,
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
		intent.putExtra(SAPDFCContstants.FILE_URI, mainFile.getAbsolutePath());
		startActivity(intent);
	}

	private void delete(File file) {
		file.delete();
		startActivity(new Intent(this, MainActivity.class));
	}

	public android.app.AlertDialog.Builder getAlertDialog() {
		return new AlertDialog.Builder(new ContextThemeWrapper(this,
				android.R.style.Theme_Dialog));
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
		getAlertDialog().setTitle("Operation cancelled!")
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						finish();
						return;
					}

				}).show();

	}

}
