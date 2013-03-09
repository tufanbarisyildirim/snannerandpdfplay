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
		Linkify.addLinks((TextView) findViewById(R.id.source_code),
				Linkify.WEB_URLS);
	}
}
