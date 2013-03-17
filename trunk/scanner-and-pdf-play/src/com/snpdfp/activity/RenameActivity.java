package com.snpdfp.activity;

import java.io.File;
import java.util.logging.Logger;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.snpdfp.utils.SAPDFCContstants;
import com.snpdfp.utils.SAPDFUtils;

public class RenameActivity extends SNPDFActivity {
	Logger logger = Logger.getLogger(RenameActivity.class.getName());

	File file = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();

		file = new File(intent.getStringExtra(SAPDFCContstants.FILE_URI));
		tryRename();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == SAPDFCContstants.PICK_NAME_REQUEST) {
				renameFile(data.getStringExtra(SAPDFCContstants.TEXT));
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
					.setNegativeButton("cancel",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss();
									finish();
									return;
								}

							}).show();
		} else {
			boolean success = file.renameTo(newFile);
			mainFile = newFile;

			setContentView(R.layout.activity_rename);

			TextView textView = (TextView) findViewById(R.id.message);

			if (!success) {
				SAPDFUtils.setErrorText(textView, "Unable to rename file: "
						+ file.getName());
				disableButtons();

			} else {
				SAPDFUtils.setSuccessText(textView,
						"File successfully renamed.", mainFile);
			}
		}

	}

	private void tryRename() {
		Intent pickName = new Intent(this, PickStringActivity.class);
		pickName.putExtra(SAPDFCContstants.TEXT,
				file.getName().substring(0, file.getName().lastIndexOf(".")));
		startActivityForResult(pickName, SAPDFCContstants.PICK_NAME_REQUEST);

	}

}
