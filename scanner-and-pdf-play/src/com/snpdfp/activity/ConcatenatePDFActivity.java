package com.snpdfp.activity;

import java.io.File;
import java.io.FileOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.itextpdf.text.pdf.PdfCopyFields;
import com.itextpdf.text.pdf.PdfReader;
import com.snpdfp.layout.FolderLayout;
import com.snpdfp.utils.SNPDFCContstants;
import com.snpdfp.utils.SNPDFPathManager;
import com.snpdfp.utils.SNPDFUtils;

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

		EditText password1 = (EditText) findViewById(R.id.password1);
		password1.setVisibility(View.GONE);
		password1_req = false;

		EditText password2 = (EditText) findViewById(R.id.password2);
		password2.setVisibility(View.GONE);
		password2_req = false;

		findViewById(R.id.section3).setVisibility(View.GONE);
		EditText password3 = (EditText) findViewById(R.id.password3);
		password3.setVisibility(View.GONE);
		password3_req = false;

		findViewById(R.id.section4).setVisibility(View.GONE);
		EditText password4 = (EditText) findViewById(R.id.password4);
		password4.setVisibility(View.GONE);
		password4_req = false;

		findViewById(R.id.section5).setVisibility(View.GONE);
		EditText password5 = (EditText) findViewById(R.id.password5);
		password5.setVisibility(View.GONE);
		password4_req = false;

		findViewById(R.id.required_fields).setVisibility(View.GONE);
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
			findViewById(R.id.section3).setVisibility(View.VISIBLE);
		} else if (findViewById(R.id.section4).getVisibility() == View.GONE) {
			findViewById(R.id.section4).setVisibility(View.VISIBLE);
		} else if (findViewById(R.id.section5).getVisibility() == View.GONE) {
			findViewById(R.id.section5).setVisibility(View.VISIBLE);
			findViewById(R.id.add_more).setVisibility(View.GONE);
		}

		findViewById(R.id.required_fields).setVisibility(View.VISIBLE);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == SNPDFCContstants.PICK_FILE1) {
				firstFile = new File(
						data.getStringExtra(SNPDFCContstants.FILE_URI));
				((EditText) findViewById(R.id.pdf_file1)).setText(firstFile
						.getName());

				if (SNPDFUtils.isProtected(firstFile)) {
					getAlertDialog()
							.setTitle("PDF is encrypted!")
							.setMessage(
									"The selected PDF is protected, please enter it's password!")
							.setPositiveButton("OK",
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog,
												int which) {
											dialog.dismiss();
											EditText password1 = (EditText) findViewById(R.id.password1);
											password1
													.setVisibility(View.VISIBLE);
											password1_req = true;
											password1.setText("");

										}

									}).show();

				} else {
					EditText password1 = (EditText) findViewById(R.id.password1);
					password1.setVisibility(View.GONE);
					password1_req = false;
					password1.setText("");
				}

			} else if (requestCode == SNPDFCContstants.PICK_FILE2) {
				secondFile = new File(
						data.getStringExtra(SNPDFCContstants.FILE_URI));
				((EditText) findViewById(R.id.pdf_file2)).setText(secondFile
						.getName());
				if (SNPDFUtils.isProtected(secondFile)) {
					getAlertDialog()
							.setTitle("PDF is encrypted!")
							.setMessage(
									"The selected PDF is protected, please enter it's password!")
							.setPositiveButton("OK",
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog,
												int which) {
											dialog.dismiss();
											EditText password2 = (EditText) findViewById(R.id.password2);
											password2
													.setVisibility(View.VISIBLE);
											password2_req = true;
											password2.setText("");

										}

									}).show();
				} else {
					EditText password2 = (EditText) findViewById(R.id.password2);
					password2.setVisibility(View.GONE);
					password2_req = false;
					password2.setText("");
				}
			} else if (requestCode == SNPDFCContstants.PICK_FILE3) {
				thirdFile = new File(
						data.getStringExtra(SNPDFCContstants.FILE_URI));
				((EditText) findViewById(R.id.pdf_file3)).setText(thirdFile
						.getName());
				if (SNPDFUtils.isProtected(thirdFile)) {
					getAlertDialog()
							.setTitle("PDF is encrypted!")
							.setMessage(
									"The selected PDF is protected, please enter it's password!")
							.setPositiveButton("OK",
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog,
												int which) {
											dialog.dismiss();
											EditText password3 = (EditText) findViewById(R.id.password3);
											password3
													.setVisibility(View.VISIBLE);
											password3_req = true;
											password3.setText("");

										}

									}).show();
				} else {
					EditText password3 = (EditText) findViewById(R.id.password3);
					password3.setVisibility(View.GONE);
					password3_req = false;
					password3.setText("");
				}
			} else if (requestCode == SNPDFCContstants.PICK_FILE4) {
				fourthFile = new File(
						data.getStringExtra(SNPDFCContstants.FILE_URI));
				((EditText) findViewById(R.id.pdf_file4)).setText(fourthFile
						.getName());
				if (SNPDFUtils.isProtected(fourthFile)) {
					getAlertDialog()
							.setTitle("PDF is encrypted!")
							.setMessage(
									"The selected PDF is protected, please enter it's password!")
							.setPositiveButton("OK",
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog,
												int which) {
											dialog.dismiss();
											EditText password4 = (EditText) findViewById(R.id.password4);
											password4
													.setVisibility(View.VISIBLE);
											password4_req = true;
											password4.setText("");

										}

									}).show();
				} else {
					EditText password4 = (EditText) findViewById(R.id.password4);
					password4.setVisibility(View.GONE);
					password4_req = false;
					password4.setText("");
				}
			} else if (requestCode == SNPDFCContstants.PICK_FILE5) {
				fifthFile = new File(
						data.getStringExtra(SNPDFCContstants.FILE_URI));
				((EditText) findViewById(R.id.pdf_file5)).setText(fifthFile
						.getName());
				if (SNPDFUtils.isProtected(fifthFile)) {
					getAlertDialog()
							.setTitle("PDF is encrypted!")
							.setMessage(
									"The selected PDF is protected, please enter it's password!")
							.setPositiveButton("OK",
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog,
												int which) {
											dialog.dismiss();
											EditText password5 = (EditText) findViewById(R.id.password5);
											password5
													.setVisibility(View.VISIBLE);
											password5_req = true;
											password5.setText("");

										}

									}).show();
				} else {
					EditText password5 = (EditText) findViewById(R.id.password5);
					password5.setVisibility(View.GONE);
					password5_req = false;
					password5.setText("");
				}
			}

		} else {
			operationCancelled();
		}
	}

	public void concatenate(View view) {
		if (firstFile == null || !firstFile.exists() || secondFile == null
				|| !secondFile.exists()) {
			getAlertDialog()
					.setTitle("Incomplete details")
					.setMessage("Please provide atleast first two PDF files!")
					.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss();
								}

							}).show();
		} else {
			if (!firstPDFDetailsComplete()) {
				getAlertDialog()
						.setTitle("Incorrect password")
						.setMessage("Please enter valid password for first PDF")
						.setPositiveButton("OK",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int which) {
										dialog.dismiss();
									}

								}).show();
			} else if (!secondPDFDetailsComplete()) {
				getAlertDialog()
						.setTitle("Incorrect password")
						.setMessage(
								"Please enter valid password for second PDF")
						.setPositiveButton("OK",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int which) {
										dialog.dismiss();
									}

								}).show();
			} else if (!thirdPDFDetailsComplete()) {
				getAlertDialog()
						.setTitle("Incorrect password")
						.setMessage("Please enter valid password for third PDF")
						.setPositiveButton("OK",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int which) {
										dialog.dismiss();
									}

								}).show();
			} else if (!fourthPDFDetailsComplete()) {
				getAlertDialog()
						.setTitle("Incorrect password")
						.setMessage(
								"Please enter valid password for fourth PDF")
						.setPositiveButton("OK",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int which) {
										dialog.dismiss();
									}

								}).show();
			} else if (!fifthPDFDetailsComplete()) {
				getAlertDialog()
						.setTitle("Incorrect password")
						.setMessage("Please enter valid password for fifth PDF")
						.setPositiveButton("OK",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int which) {
										dialog.dismiss();
									}

								}).show();
			} else {
				new Concatenate().execute();
			}

		}

	}

	private boolean secondPDFDetailsComplete() {
		if (password2_req) {
			password2 = ((EditText) findViewById(R.id.password2)).getText()
					.toString();

			if (password2 == null || password2.equals("")
					|| !SNPDFUtils.isPasswordCorrect(secondFile, password2)) {
				return false;
			}
		}

		return true;
	}

	private boolean firstPDFDetailsComplete() {
		if (password1_req) {
			password1 = ((EditText) findViewById(R.id.password1)).getText()
					.toString();

			if (password1 == null || password1.equals("")
					|| !SNPDFUtils.isPasswordCorrect(firstFile, password1)) {
				return false;
			}
		}

		return true;
	}

	private boolean thirdPDFDetailsComplete() {
		if (thirdFile != null) {
			if (password3_req) {
				password3 = ((EditText) findViewById(R.id.password3)).getText()
						.toString();

				if (password3 == null || password3.equals("")
						|| !SNPDFUtils.isPasswordCorrect(thirdFile, password3)) {
					return false;
				}
			}
		}

		return true;
	}

	private boolean fourthPDFDetailsComplete() {
		if (fourthFile != null) {
			if (password4_req) {
				password4 = ((EditText) findViewById(R.id.password4)).getText()
						.toString();

				if (password4 == null || password4.equals("")
						|| !SNPDFUtils.isPasswordCorrect(fourthFile, password4)) {
					return false;
				}
			}
		}

		return true;
	}

	private boolean fifthPDFDetailsComplete() {
		if (fifthFile != null) {
			if (password5_req) {
				password5 = ((EditText) findViewById(R.id.password5)).getText()
						.toString();

				if (password5 == null || password5.equals("")
						|| !SNPDFUtils.isPasswordCorrect(fifthFile, password5)) {
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

			mainFile = SNPDFPathManager.getSavePDFPath(SNPDFPathManager
					.getFileNameWithoutExtn(firstFile.getName())
					+ "_"
					+ SNPDFPathManager.getFileNameWithoutExtn(secondFile
							.getName()) + ".pdf");

			try {
				if (password1_req) {
					reader1 = new PdfReader(firstFile.getAbsolutePath(),
							password1.getBytes());
				} else {
					reader1 = new PdfReader(firstFile.getAbsolutePath());
				}

				if (password2_req) {
					reader2 = new PdfReader(secondFile.getAbsolutePath(),
							password2.getBytes());
				} else {
					reader2 = new PdfReader(secondFile.getAbsolutePath());
				}

				copy = new PdfCopyFields(new FileOutputStream(mainFile));
				copy.addDocument(reader1);
				copy.addDocument(reader2);

				if (thirdFile != null) {
					if (password3_req) {
						reader3 = new PdfReader(thirdFile.getAbsolutePath(),
								password3.getBytes());
					} else {
						reader3 = new PdfReader(thirdFile.getAbsolutePath());
					}

					copy.addDocument(reader3);
				}

				if (fourthFile != null) {
					if (password4_req) {
						reader4 = new PdfReader(fourthFile.getAbsolutePath(),
								password4.getBytes());
					} else {
						reader4 = new PdfReader(fourthFile.getAbsolutePath());
					}

					copy.addDocument(reader4);
				}

				if (fifthFile != null) {
					if (password5_req) {
						reader5 = new PdfReader(fifthFile.getAbsolutePath(),
								password5.getBytes());
					} else {
						reader5 = new PdfReader(fifthFile.getAbsolutePath());
					}

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
			SNPDFUtils.setErrorText(this,
					"Unable to concatenate selected PDFs (" + errorMessage
							+ ")");
			hideButtons();
		} else {
			SNPDFUtils.setSuccessText(this,
					"Selected PDFs successfully concatenated.", mainFile);
		}

	}

}
