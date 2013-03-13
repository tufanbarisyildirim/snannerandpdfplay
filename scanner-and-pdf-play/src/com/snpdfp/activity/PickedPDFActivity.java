package com.snpdfp.activity;

import java.io.File;

import android.os.Bundle;
import android.widget.TextView;

import com.snpdfp.utils.SAPDFCContstants;
import com.snpdfp.utils.SAPDFUtils;

public class PickedPDFActivity extends SNPDFActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mainFile = new File(getIntent()
				.getStringExtra(SAPDFCContstants.FILE_URI));

		setContentView(R.layout.snpdf_output);
		TextView textView = (TextView) findViewById(R.id.message);
		SAPDFUtils
				.setSuccessText(textView, "Chosen file: " + mainFile.getName());
	}

}
