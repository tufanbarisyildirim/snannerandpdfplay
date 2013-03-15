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

		TextView question3 = (TextView) findViewById(R.id.question3);
		question3.setText("What are Core and Non-Core Options?");

		TextView answer3 = (TextView) findViewById(R.id.answer3);
		answer3.setText("1. Core Options are the application capabilities which provide perfect results.\n2. Non-Core Options are the application capabilities which does not provide perfect, but more than workable results.");

		TextView question4 = (TextView) findViewById(R.id.question4);
		question4.setText("How can I concatenate encrypted PDFs?");

		TextView answer4 = (TextView) findViewById(R.id.answer4);
		answer4.setText("First copy the encrypted ones to non-encrypted PDFs and then concatenate.");

		TextView question5 = (TextView) findViewById(R.id.question5);
		question5.setText("How can I change a PDF password?");

		TextView answer5 = (TextView) findViewById(R.id.answer5);
		answer5.setText("First copy the encrypted PDF to non-encrypted one and then protect it with a new password.");
	}

}
