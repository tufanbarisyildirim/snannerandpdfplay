package com.snpdfp.activity;

import java.util.logging.Logger;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.snpdfp.utils.SAPDFCContstants;

public class PickPasswordActivity extends SNPDFActivity {

	Logger logger = Logger.getLogger(PickPasswordActivity.class.getName());

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pick_password);
	}

	public void sendPassword(View view) {
		String password = ((EditText) findViewById(R.id.password)).getText()
				.toString();
		String confirmPassword = ((EditText) findViewById(R.id.confirm_password))
				.getText().toString();

		if (password == null || confirmPassword == null || "".equals(password)
				|| "".equals(confirmPassword)
				|| !password.equals(confirmPassword)) {
			getAlertDialog()
					.setTitle("Invalid entry")
					.setMessage(
							"Password and Confirm Password fields required and must match!")
					.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss();
								}

							}).show();

			return;
		}

		// Create intent to deliver some kind of result data
		Intent result = new Intent();
		result.putExtra(SAPDFCContstants.TEXT, password);
		setResult(Activity.RESULT_OK, result);
		finish();
	}

	public void cancel(View view) {
		logger.info("*************** User cancelled the operation  **************");
		Intent result = new Intent();
		setResult(Activity.RESULT_CANCELED, result);
		finish();
	}

}
