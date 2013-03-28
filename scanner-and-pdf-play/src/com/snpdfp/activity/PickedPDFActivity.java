package com.snpdfp.activity;

import java.io.File;

import android.os.Bundle;

import com.snpdfp.utils.SNPDFCContstants;
import com.snpdfp.utils.SNPDFUtils;

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
		super.onBackPressed();
	}

}
