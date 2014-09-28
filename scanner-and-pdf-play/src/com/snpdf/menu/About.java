package com.snpdf.menu;

import android.os.Bundle;
import android.text.util.Linkify;
import android.widget.TextView;

import com.snpdf.activity.SNPDFActivity;
import com.snpdf.activity.R;

public class About extends SNPDFActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);

		Linkify.addLinks((TextView) findViewById(R.id.app_url),
				Linkify.WEB_URLS);

		TextView textView = (TextView) findViewById(R.id.app_detail);
		textView.setText(R.string.what_it_does);
	}
}
