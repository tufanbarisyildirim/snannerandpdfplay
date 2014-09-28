package com.snpdf.activity;

import java.io.File;

import android.os.Bundle;

import com.snpdf.utils.SNPDFCContstants;
import com.snpdf.utils.SNPDFUtils;
import com.snpdf.activity.R;

public class PickedPDFActivity extends SNPDFActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mainFile = new File(getIntent().getStringExtra(
				SNPDFCContstants.FILE_URI));

		setContentView(R.layout.snpdf_output);
		SNPDFUtils.setSuccessText(this, "File chosen:", mainFile);
	}

	@Override
	public void onBackPressed() {
		finish();
	}

}
