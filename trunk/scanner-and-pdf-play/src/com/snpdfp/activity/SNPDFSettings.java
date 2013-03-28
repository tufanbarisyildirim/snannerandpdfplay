package com.snpdfp.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

public class SNPDFSettings extends SNPDFActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_snpdfsettings);

		// Select page size
		mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		String pageSize = mPrefs.getString(snpdfPageSize, "A4");

		RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radioPageSize);
		int length = radioGroup.getChildCount();
		for (int i = 0; i < length; i++) {
			RadioButton radioButton = (RadioButton) radioGroup.getChildAt(i);
			if (pageSize.equalsIgnoreCase(radioButton.getText().toString())) {
				radioButton.setChecked(true);
				break;
			}
		}

	}

	public void save(View view) {
		mPrefs = PreferenceManager.getDefaultSharedPreferences(this);

		RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radioPageSize);

		int selectedId = radioGroup.getCheckedRadioButtonId();
		RadioButton radioButton = (RadioButton) findViewById(selectedId);

		String size = radioButton.getText().toString();

		setPageSize(size);

		SharedPreferences.Editor editor = mPrefs.edit();
		editor.putString(snpdfPageSize, size);
		editor.commit(); // Very important to save the preference

		Toast.makeText(this, "Settings saved!", Toast.LENGTH_SHORT).show();

		onBackPressed();
	}

}
