package com.snpdfp.menu;

import android.os.Bundle;
import android.widget.TextView;

import com.snpdfp.activity.R;
import com.snpdfp.activity.SNPDFActivity;
import com.snpdfp.utils.SAPDFPathManager;

public class FAQActivity extends SNPDFActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_faq);

		TextView question1 = (TextView) findViewById(R.id.question1);
		question1.setText("Where can I find all the prepared PDFs?");

		TextView answer1 = (TextView) findViewById(R.id.answer1);
		answer1.setText(SAPDFPathManager.getRootDirectory().getAbsolutePath());
	}

}
