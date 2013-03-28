package com.snpdfp.activity;

import java.io.File;
import java.util.logging.Logger;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.snpdfp.utils.SNPDFCContstants;
import com.snpdfp.utils.SNPDFUtils;

public class RenameActivity extends SNPDFActivity {
	Logger logger = Logger.getLogger(RenameActivity.class.getName());

	File file = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();

		file = new File(intent.getStringExtra(SNPDFCContstants.FILE_URI));
		tryRename();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == SNPDFCContstants.PICK_NAME_REQUEST) {
				renameFile(data.getStringExtra(SNPDFCContstants.TEXT));
			}
		} else {
			operationCancelled();
		}

	}

	private void renameFile(String name) {
		String newFileName = name
				+ file.getName().substring(file.getName().lastIndexOf("."));
		File newFile = new File(file.getParentFile(), newFileName);
		if (newFile.exists()) {
			getAlertDialog()
					.setTitle("Name already exists!")
					.setMessage("Want to re-enter new name?")
					.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss();
									tryRename();
									return;
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
		} else {
			boolean success = file.renameTo(newFile);
			mainFile = newFile;

			setContentView(R.layout.activity_rename);

			if (!success) {
				SNPDFUtils.setErrorText(this,
						"Unable to rename file: " + file.getName());
				disableButtons();

			} else {

				Toast.makeText(
						this,
						"File successfully renamed from " + file.getName()
								+ " to " + mainFile.getName(),
						Toast.LENGTH_SHORT).show();
				SNPDFUtils.setSuccessText(this, "File successfully renamed.",
						mainFile);
			}
		}

	}

	private void tryRename() {
		Intent pickName = new Intent(this, PickStringActivity.class);
		pickName.putExtra(SNPDFCContstants.TEXT,
				file.getName().substring(0, file.getName().lastIndexOf(".")));
		startActivityForResult(pickName, SNPDFCContstants.PICK_NAME_REQUEST);

	}

}
