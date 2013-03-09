package com.snpdfp.utils;

import android.widget.TextView;

public class SAPDFUtils {

	public static void setErrorText(TextView textView, String message) {
		//textView.setBackgroundColor(-65536);
		textView.setText(message);
		textView.setTextColor(-65536);
	}

	public static void setSuccessText(TextView textView, String message) {
		//textView.setBackgroundColor(-16711936);
		textView.setText(message);
		textView.setTextColor(-16711936);
	}

}
