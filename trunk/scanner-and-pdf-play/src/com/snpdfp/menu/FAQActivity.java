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

		TextView question2 = (TextView) findViewById(R.id.question2);
		question2.setText("How can I add text to existing PDF?");

		TextView answer2 = (TextView) findViewById(R.id.answer2);
		answer2.setText("1. Prepare a .txt file with new text and convert this file to PDF using this tool.\n2. Finally concatenate this created PDF to the intended PDF.");

	}

}
