package com.snpdfp.utils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.widget.TextView;

public class SAPDFUtils {

	public static void setErrorText(TextView textView, String message) {
		textView.setText(message);
		textView.setTextColor(-65536);
	}

	public static void setSuccessText(TextView textView, String message,
			File file) {
		String fileDetails = "Filename:"
				+ file.getName()
				+ "\nSize:"
				+ SAPDFUtils.getSizeText(file.length())
				+ "\nDate Modified:"
				+ new SimpleDateFormat(SAPDFCContstants.DATE_FORMAT,
						Locale.getDefault()).format(new Date(file
						.lastModified()));
		textView.setText(message + "\n" + fileDetails);
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
