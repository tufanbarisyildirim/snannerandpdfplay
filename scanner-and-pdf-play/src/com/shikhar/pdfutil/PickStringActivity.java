package com.shikhar.pdfutil;

import java.util.logging.Logger;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.shikhar.pdfutil.utils.SNPDFCContstants;
import com.shikhar.pdfutil.R;

public class PickStringActivity extends SNPDFActivity {

	Logger logger = Logger.getLogger(PickStringActivity.class.getName());

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pick_string);

		String name = getIntent().getStringExtra(SNPDFCContstants.TEXT);
		EditText editText = (EditText) findViewById(R.id.name);
		editText.setText(name);
	}

	public void passName(View view) {
		String name = ((EditText) findViewById(R.id.name)).getText().toString();

		if (name == null || "".equals(name.trim())) {
			getAlertDialog()
					.setTitle("Invalid entry")
					.setMessage("Please enter a valid name!")
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
		result.putExtra(SNPDFCContstants.TEXT, name);
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
