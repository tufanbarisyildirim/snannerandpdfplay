package com.snpdfp.menu;

import android.os.Bundle;
import android.text.util.Linkify;
import android.widget.TextView;

import com.snpdfp.activity.R;
import com.snpdfp.activity.SNPDFActivity;

public class About extends SNPDFActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);

		Linkify.addLinks((TextView) findViewById(R.id.app_home),
				Linkify.WEB_URLS);

		TextView textView = (TextView) findViewById(R.id.app_detail);
		textView.setText("It is a PDF utility which can be used to:\n1. Scan/Convert any image to PDF\n2. Extract pages from PDF\n3. Concatenate two PDFs\n4. Protect a PDF with password\n5. Convert a TXT/HTML file to PDF\n6. Extract text from PDF\n7. Add watermark to PDF\n8. Copy an encrypted PDF to a non-encrypted one\n9. Change PDF password\netc...");
	}
}
