package com.shikhar.pdfutil;

import java.io.File;

import android.os.Bundle;

import com.shikhar.pdfutil.utils.SNPDFCContstants;
import com.shikhar.pdfutil.utils.SNPDFUtils;
import com.shikhar.pdfutil.R;

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
