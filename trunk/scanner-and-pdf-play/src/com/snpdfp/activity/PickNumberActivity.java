package com.snpdfp.activity;

import java.util.logging.Logger;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.snpdfp.utils.SAPDFCContstants;

public class PickNumberActivity extends SNPDFActivity {

	Logger logger = Logger.getLogger(PickNumberActivity.class.getName());
	int maxNum;
	int fromPageNumber;
	int toPageNumber;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pick_number);
		TextView textView = (TextView) findViewById(R.id.message);
		Intent intent = getIntent();
		maxNum = intent.getIntExtra(SAPDFCContstants.NUMBER, 0);
		textView.setText("Maximum number of pages in selected PDF is " + maxNum
				+ ".\nSo FROM cannot be less than 1 and TO cannot exceed "
				+ (maxNum - 1));
	}

	public void splitPDF(View view) {
		String from = ((EditText) findViewById(R.id.from_number)).getText()
				.toString();
		String to = ((EditText) findViewById(R.id.to_number)).getText()
				.toString();

		boolean errorFill = false;
		if (from == null || "".equals(from) || to == null || "".equals(to)) {
			errorFill = true;
		} else {
			try {
				Integer.parseInt(to);
				Integer.parseInt(from);
				if (Integer.parseInt(to) > maxNum - 1
						|| Integer.parseInt(from) < 1) {
					errorFill = true;
				}

			} catch (Exception e) {
				errorFill = true;
			}

		}

		if (errorFill) {
			getAlertDialog()
					.setTitle("Invalid entry")
					.setMessage(
							"Invalid numbers entered! Please enter valid numbers as explained in the instructions.")
					.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss();
								}

							}).show();

			return;
		} else {
			// Create intent to deliver some kind of result data
			Intent result = new Intent();
			result.putExtra(SAPDFCContstants.FROM_NUMBER,
					Integer.parseInt(from));
			result.putExtra(SAPDFCContstants.TO_NUMBER, Integer.parseInt(to));
			setResult(Activity.RESULT_OK, result);
			finish();
		}

	}

	public void cancel(View view) {
		logger.info("*************** User cancelled the operation  **************");
		Intent result = new Intent();
		setResult(Activity.RESULT_CANCELED, result);
		finish();
	}

}
