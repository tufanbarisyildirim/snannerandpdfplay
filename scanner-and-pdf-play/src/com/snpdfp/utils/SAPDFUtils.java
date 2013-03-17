package com.snpdfp.utils;

import android.widget.TextView;

public class SAPDFUtils {

	public static void setErrorText(TextView textView, String message) {
		// textView.setBackgroundColor(-65536);
		textView.setText(message);
		textView.setTextColor(-65536);
	}

	public static void setSuccessText(TextView textView, String message) {
		// textView.setBackgroundColor(-16711936);
		textView.setText(message);
		textView.setTextColor(-16711936);
	}

	public static String getSizeText(long length) {
		if (length < 1024)
			return length + " B";
		int exp = (int) (Math.log(length) / Math.log(1024));
		String pre = "KMGTPE".charAt(exp - 1) + "";
		return String.format("%.1f %sB", length / Math.pow(1024, exp), pre);
	}

}
