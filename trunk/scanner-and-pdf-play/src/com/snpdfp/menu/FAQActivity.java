package com.snpdfp.menu;

import android.os.Bundle;
import android.widget.TextView;

import com.snpdfp.activity.R;
import com.snpdfp.activity.SNPDFActivity;
import com.snpdfp.utils.SNPDFPathManager;

public class FAQActivity extends SNPDFActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_faq);

    TextView question1 = (TextView) findViewById(R.id.question1);
    question1.setText("Where can I find all the prepared PDFs?");

    TextView answer1 = (TextView) findViewById(R.id.answer1);
    answer1.setText(SNPDFPathManager.getRootDirectory().getAbsolutePath());

    TextView question2 = (TextView) findViewById(R.id.question2);
    question2.setText("How can I add text to existing PDF?");

    TextView answer2 = (TextView) findViewById(R.id.answer2);
    answer2
        .setText("1. Prepare a .txt file with new text and convert this file to PDF using this tool.\n2. Finally concatenate this created PDF to the intended PDF.");

    TextView question3 = (TextView) findViewById(R.id.question3);
    question3.setText("How can I change a PDF password?");

    TextView answer3 = (TextView) findViewById(R.id.answer3);
    answer3.setText("First copy the encrypted PDF to non-encrypted one and then protect it with a new password.");

    TextView question4 = (TextView) findViewById(R.id.question4);
    question4.setText("Does any option change the existing source file?");

    TextView answer4 = (TextView) findViewById(R.id.answer4);
    answer4.setText("Nopes, all options work on a new file. So your source files remain safe and untouched!");

    TextView question5 = (TextView) findViewById(R.id.question5);
    question5.setText("Why does my scanned/converted images overflow in the created PDF?");

    TextView answer5 = (TextView) findViewById(R.id.answer5);
    answer5
        .setText("It is probably because you selected a big image, and the Auto-Fit option in settings is set to false. Change it to true.");
  }

}
