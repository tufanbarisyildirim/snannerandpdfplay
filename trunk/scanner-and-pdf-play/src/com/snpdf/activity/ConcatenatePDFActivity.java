package com.snpdf.activity;

import static com.snpdf.utils.SNPDFUtils.getPdfReader;

import java.io.File;
import java.io.FileOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.lowagie.text.pdf.PdfCopyFields;
import com.lowagie.text.pdf.PdfReader;
import com.snpdf.layout.FolderLayout;
import com.snpdf.utils.SNPDFCContstants;
import com.snpdf.utils.SNPDFPathManager;
import com.snpdf.utils.SNPDFUtils;
import com.snpdf.activity.R;

public class ConcatenatePDFActivity extends SNPDFActivity {

	Logger logger = Logger.getLogger(ConcatenatePDFActivity.class.getName());

	FolderLayout localFolders;
	File firstFile;
	File secondFile;
	File thirdFile;
	File fourthFile;
	File fifthFile;

	boolean password1_req = false;
	boolean password2_req = false;
	boolean password3_req = false;
	boolean password4_req = false;
	boolean password5_req = false;

	String password1 = null;
	String password2 = null;
	String password3 = null;
	String password4 = null;
	String password5 = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_concatenate_pdf);

		setInvisible(R.id.password1, R.id.password2, R.id.password3, R.id.password4, R.id.password5, R.id.section3, R.id.section4, R.id.section5);
		password1_req = false;
		password2_req = false;
		password3_req = false;
		password4_req = false;
		password4_req = false;

		setInvisible(R.id.required_fields, R.id.remove_last);
		setVisible(R.id.add_more);
	}

	public void pickFile1(View view) {
		Intent filePick = new Intent(this, BrowsePDFActivity.class);
		startActivityForResult(filePick, SNPDFCContstants.PICK_FILE1);
	}

	public void pickFile2(View view) {
		Intent filePick = new Intent(this, BrowsePDFActivity.class);
		startActivityForResult(filePick, SNPDFCContstants.PICK_FILE2);
	}

	public void pickFile3(View view) {
		Intent filePick = new Intent(this, BrowsePDFActivity.class);
		startActivityForResult(filePick, SNPDFCContstants.PICK_FILE3);
	}

	public void pickFile4(View view) {
		Intent filePick = new Intent(this, BrowsePDFActivity.class);
		startActivityForResult(filePick, SNPDFCContstants.PICK_FILE4);
	}

	public void pickFile5(View view) {
		Intent filePick = new Intent(this, BrowsePDFActivity.class);
		startActivityForResult(filePick, SNPDFCContstants.PICK_FILE5);
	}

	public void addMore(View view) {
		if (findViewById(R.id.section3).getVisibility() == View.GONE) {
			setVisible(R.id.section3);
		} else if (findViewById(R.id.section4).getVisibility() == View.GONE) {
			setVisible(R.id.section4);
		} else if (findViewById(R.id.section5).getVisibility() == View.GONE) {
			setVisible(R.id.section5);
			setInvisible(R.id.add_more);
		}

		setVisible(R.id.remove_last, R.id.required_fields);
	}

	public void removeLast(View view) {
		if (findViewById(R.id.section5).getVisibility() == View.VISIBLE) {
			setInvisible(R.id.section5);
			password5_req = false;
			password5 = null;
			fifthFile = null;
		} else if (findViewById(R.id.section4).getVisibility() == View.VISIBLE) {
			setInvisible(R.id.section4);
			password4_req = false;
			password4 = null;
			fourthFile = null;
		} else if (findViewById(R.id.section3).getVisibility() == View.VISIBLE) {
			setInvisible(R.id.section3);
			password3_req = false;
			password3 = null;
			thirdFile = null;
			setInvisible(R.id.remove_last, R.id.required_fields);
		}

		setVisible(R.id.add_more);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == SNPDFCContstants.PICK_FILE1) {
				firstFile = new File(data.getStringExtra(SNPDFCContstants.FILE_URI));
				((EditText) findViewById(R.id.pdf_file1)).setText(firstFile.getName());

				if (SNPDFUtils.isProtected(firstFile)) {
					password1_req = true;
					getAlertDialogForPasswordRequired(R.id.password1).show();
				} else {
					password1_req = false;
					setPwdInvisible(R.id.password1);
				}

			} else if (requestCode == SNPDFCContstants.PICK_FILE2) {
				secondFile = new File(data.getStringExtra(SNPDFCContstants.FILE_URI));
				((EditText) findViewById(R.id.pdf_file2)).setText(secondFile.getName());
				if (SNPDFUtils.isProtected(secondFile)) {
					password2_req = true;
					getAlertDialogForPasswordRequired(R.id.password2).show();
				} else {
					password2_req = false;
					setPwdInvisible(R.id.password2);
				}

			} else if (requestCode == SNPDFCContstants.PICK_FILE3) {
				thirdFile = new File(data.getStringExtra(SNPDFCContstants.FILE_URI));
				((EditText) findViewById(R.id.pdf_file3)).setText(thirdFile.getName());
				if (SNPDFUtils.isProtected(thirdFile)) {
					password3_req = true;
					getAlertDialogForPasswordRequired(R.id.password3).show();
				} else {
					setPwdInvisible(R.id.password3);
					password3_req = false;
				}

			} else if (requestCode == SNPDFCContstants.PICK_FILE4) {
				fourthFile = new File(data.getStringExtra(SNPDFCContstants.FILE_URI));
				((EditText) findViewById(R.id.pdf_file4)).setText(fourthFile.getName());
				if (SNPDFUtils.isProtected(fourthFile)) {
					password4_req = true;
					getAlertDialogForPasswordRequired(R.id.password4).show();
				} else {
					setPwdInvisible(R.id.password4);
					password4_req = false;
				}
			} else if (requestCode == SNPDFCContstants.PICK_FILE5) {
				fifthFile = new File(data.getStringExtra(SNPDFCContstants.FILE_URI));
				((EditText) findViewById(R.id.pdf_file5)).setText(fifthFile.getName());
				if (SNPDFUtils.isProtected(fifthFile)) {
					password5_req = true;
					getAlertDialogForPasswordRequired(R.id.password5).show();
				} else {
					setPwdInvisible(R.id.password5);
					password5_req = false;
				}
			}

		} else {
			showCancelledMsg();
		}
	}

	private void setPwdInvisible(int password) {
		EditText passwordView = (EditText) findViewById(password);
		passwordView.setVisibility(View.GONE);
		passwordView.setText("");

	}

	private Builder getAlertDialogForPasswordRequired(final int password) {
		return getAlertDialog().setTitle("PDF is encrypted!").setMessage("The selected PDF is protected, please enter it's password!")
			.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					EditText passwordView = (EditText) findViewById(password);
					passwordView.setVisibility(View.VISIBLE);
					passwordView.setText("");
				}

			});

	}

	public void concatenate(View view) {
		if (firstFile == null || !firstFile.exists() || secondFile == null || !secondFile.exists()) {
			getSimpleAlertDialog("Incomplete details", "Please provide atleast first two PDF files!").show();

		} else {
			if (!firstPDFDetailsComplete()) {
				getSimpleAlertDialog("Incorrect password", "Please enter valid password for first PDF").show();

			} else if (!secondPDFDetailsComplete()) {
				getSimpleAlertDialog("Incorrect password", "Please enter valid password for second PDF").show();

			} else if (!thirdPDFDetailsComplete()) {
				getSimpleAlertDialog("Incorrect password", "Please enter valid password for third PDF").show();

			} else if (!fourthPDFDetailsComplete()) {
				getSimpleAlertDialog("Incorrect password", "Please enter valid password for fourth PDF").show();

			} else if (!fifthPDFDetailsComplete()) {
				getSimpleAlertDialog("Incorrect password", "Please enter valid password for fifth PDF").show();

			} else {
				new Concatenate().execute();
			}

		}

	}

	private boolean secondPDFDetailsComplete() {
		if (password2_req) {
			password2 = ((EditText) findViewById(R.id.password2)).getText().toString();

			if (password2 == null || password2.equals("") || !SNPDFUtils.isPasswordCorrect(secondFile, password2)) {
				return false;
			}
		}

		return true;
	}

	private boolean firstPDFDetailsComplete() {
		if (password1_req) {
			password1 = ((EditText) findViewById(R.id.password1)).getText().toString();

			if (password1 == null || password1.equals("") || !SNPDFUtils.isPasswordCorrect(firstFile, password1)) {
				return false;
			}
		}

		return true;
	}

	private boolean thirdPDFDetailsComplete() {
		if (thirdFile != null) {
			if (password3_req) {
				password3 = ((EditText) findViewById(R.id.password3)).getText().toString();

				if (password3 == null || password3.equals("") || !SNPDFUtils.isPasswordCorrect(thirdFile, password3)) {
					return false;
				}
			}
		}

		return true;
	}

	private boolean fourthPDFDetailsComplete() {
		if (fourthFile != null) {
			if (password4_req) {
				password4 = ((EditText) findViewById(R.id.password4)).getText().toString();

				if (password4 == null || password4.equals("") || !SNPDFUtils.isPasswordCorrect(fourthFile, password4)) {
					return false;
				}
			}
		}

		return true;
	}

	private boolean fifthPDFDetailsComplete() {
		if (fifthFile != null) {
			if (password5_req) {
				password5 = ((EditText) findViewById(R.id.password5)).getText().toString();

				if (password5 == null || password5.equals("") || !SNPDFUtils.isPasswordCorrect(fifthFile, password5)) {
					return false;
				}
			}
		}

		return true;
	}

	private class Concatenate extends AsyncTask<String, Void, Boolean> {

		private ProgressDialog progressDialog;

		@Override
		protected void onPreExecute() {
			progressDialog = new ProgressDialog(ConcatenatePDFActivity.this);
			progressDialog.setMessage("Concatinating...");
			progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			progressDialog.show();

		}

		@Override
		protected void onPostExecute(Boolean result) {
			if (progressDialog != null && progressDialog.isShowing())
				progressDialog.dismiss();

			displayResult(result);
		}

		@Override
		protected Boolean doInBackground(String... params) {

			boolean error = false;
			PdfReader reader1 = null;
			PdfReader reader2 = null;
			PdfReader reader3 = null;
			PdfReader reader4 = null;
			PdfReader reader5 = null;
			PdfCopyFields copy = null;

			mainFile = SNPDFPathManager.getSavePDFPath(SNPDFPathManager.getFileNameWithoutExtn(firstFile.getName()) + "_"
				+ SNPDFPathManager.getFileNameWithoutExtn(secondFile.getName()) + ".pdf");

			try {
				reader1 = getPdfReader(firstFile.getAbsolutePath(), password1_req, password1);
				reader2 = getPdfReader(secondFile.getAbsolutePath(), password2_req, password2);

				copy = new PdfCopyFields(new FileOutputStream(mainFile));
				copy.addDocument(reader1);
				copy.addDocument(reader2);

				if (thirdFile != null) {
					reader3 = getPdfReader(thirdFile.getAbsolutePath(), password3_req, password3);
					copy.addDocument(reader3);
				}

				if (fourthFile != null) {
					reader4 = getPdfReader(fourthFile.getAbsolutePath(), password4_req, password4);
					copy.addDocument(reader4);
				}

				if (fifthFile != null) {
					reader5 = getPdfReader(fifthFile.getAbsolutePath(), password5_req, password5);
					copy.addDocument(reader5);
				}

			} catch (Exception e) {
				logger.log(Level.SEVERE, "Unable to concatenate PDFs", e);
				error = true;
				errorMessage = e.getLocalizedMessage();

			} finally {
				if (reader1 != null) {
					reader1.close();
				}
				if (reader2 != null) {
					reader2.close();
				}
				if (reader3 != null) {
					reader3.close();
				}
				if (reader4 != null) {
					reader4.close();
				}
				if (reader5 != null) {
					reader5.close();
				}
				if (copy != null) {
					copy.close();
				}
			}

			return error;
		}
	}

	public void displayResult(Boolean error) {

		setContentView(R.layout.snpdf_output);

		if (error) {
			SNPDFUtils.setErrorText(this, "Unable to concatenate selected PDFs (" + errorMessage + ")");
			hideButtons();
		} else {
			SNPDFUtils.setSuccessText(this, "Selected PDFs successfully concatenated.", mainFile);
		}

	}

}
