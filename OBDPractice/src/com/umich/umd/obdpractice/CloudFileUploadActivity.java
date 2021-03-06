package com.umich.umd.obdpractice;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.os.Bundle;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.support.v4.app.NavUtils;

import com.google.android.gms.*;
import com.google.android.gms.common.AccountPicker;

public class CloudFileUploadActivity extends Activity {

	// Name of file where filenames of downloaded files are stored
	private final static String DOWNLOADED_LOG_FILES = "downloaded_logs";
	// Debug tag for identifying from which activity debug message
	// originated
	private final static String DEBUG_TAG = "CloudUpload";
	// Key for specifying which arraylist in bundle is the file list
	private final static String LIST_KEY = "filesList";

	private final static int ACCOUNT_REQUEST_CODE = 1;
	
	private final static String NEW_UPLOAD = "Upload not intiated";

	// points to file title bar in view
	private static TextView file_title;
	private static TextView auth_account;
	private static TextView resp_code;
	private static TextView resp_content;

	private static String authentication_account;
	// The base name for the most currently selected log file
	private static String curr_log_file_base_name = null;
	// The base name for the previously selected log file
	private static String last_log_file_base_name = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_cloud_file_upload);
		// Show the Up button in the action bar.
		getActionBar().setDisplayHomeAsUpEnabled(true);

		// Assign view IDs to variables fileTitle and fileContent
		findViewsById();

		Intent intent = AccountPicker.newChooseAccountIntent(null, null,
				new String[] { "com.google" }, false, null, null, null, null);
		startActivityForResult(intent, ACCOUNT_REQUEST_CODE);

		generateFileSelectDialog();
	}

	private void findViewsById() {
		// assign file_name TextView to fileTitle
		file_title = (TextView) findViewById(R.id.cloud_file_name);
		auth_account = (TextView) findViewById(R.id.auth_account);
		resp_code = (TextView) findViewById(R.id.resp_code);
		resp_content = (TextView) findViewById(R.id.resp_content);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_cloud_file_upload, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private HashSet<String> getLogFileList() {

		// BufferedReader for reading from logfile list file
		BufferedReader logListReader = null;
		OutputStreamWriter osw = null;
		// Last line read from BufferedReader, should be one filename
		String readLine;
		// HashSet to store filenames
		HashSet<String> logFileNames = new HashSet<String>();

		try {
			// Create input stream from DOWNLOADED_LOG_FILES file
			logListReader = new BufferedReader(new InputStreamReader(
					openFileInput(DOWNLOADED_LOG_FILES)));

			while ((readLine = logListReader.readLine()) != null) {
				Log.d(DEBUG_TAG, "Added FileName " + readLine);
				// add each filename to HashSet, duplicates are discarded
				logFileNames.add(readLine);
			}

			osw = new OutputStreamWriter(openFileOutput(DOWNLOADED_LOG_FILES,
					Context.MODE_PRIVATE));

			for (String fileName : logFileNames) {
				osw.write(fileName + "\n");
			}
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {

			try {
				logListReader.close();
				osw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return logFileNames;

	}

	public void uploadFile(View view) {
		CloudManager fileUploader = new CloudManager(
				CloudFileUploadActivity.this, authentication_account);
		try {
			resp_code.setText("Uploading ...");
			resp_content.setText("Uploading ...");
			fileUploader.fileInsert(curr_log_file_base_name,
					getApplicationContext());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected void onActivityResult(final int requestCode,
			final int resultCode, final Intent data) {
		if (requestCode == ACCOUNT_REQUEST_CODE && resultCode == RESULT_OK) {
			setAccountName(data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME));
		}
	}

	public void show(final String message) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				resp_code.setText(message);
			}
		});
	}

	public void updateDisplay(final String respCon) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				resp_content.setText(respCon);
			}

		});
	}

	public void updateDisplay(final Map<String, List<String>> headers) {

		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				StringBuilder sb = new StringBuilder();
				List<String> currHeaderList;
				Set<String> keys = headers.keySet();
				for (String k : keys) {
					sb.append(k + ": ");
					currHeaderList = headers.get(k);
					for (String header : currHeaderList) {
						sb.append(header + " ");
					}
					sb.append("\n");
				}
				resp_content.setText(sb.toString());
			}
		});

	}

	private void setAccountName(String selectedAccountName) {
		authentication_account = selectedAccountName;
		auth_account.setText(authentication_account);
	}

	public static String getCurr_log_file_base_name() {
		return curr_log_file_base_name;
	}

	public static void setCurr_log_file_base_name(String curr_log_file_base_name) {
		CloudFileUploadActivity.curr_log_file_base_name = curr_log_file_base_name;
		file_title.setText(curr_log_file_base_name);
	}

	public static String getLast_log_file_base_name() {
		return last_log_file_base_name;
	}

	public static void setLast_log_file_base_name(String last_log_file_base_name) {
		CloudFileUploadActivity.last_log_file_base_name = last_log_file_base_name;
	}

	private void generateFileSelectDialog() {
		// Build SelectFiles fragment for selecting files
		SelectFilesFragment selectFiles = new SelectFilesFragment();
		// bundle to send to select file fragment storing file arraylist
		Bundle listBundle = new Bundle();
		// add arrayList to bundle
		listBundle.putStringArrayList(LIST_KEY, new ArrayList<String>(
				getLogFileList()));
		// set bundle to dialog fragment argument
		selectFiles.setArguments(listBundle);
		// show dialog fragment
		selectFiles.show(getFragmentManager(), "dialog");
	}

	public void selectFile(View v) {
		
		String oldRC = (String)resp_code.getText();
		String oldContent = (String)resp_content.getText(); 
		generateFileSelectDialog();
		resp_code.setText(NEW_UPLOAD);
		resp_content.setText(NEW_UPLOAD);
		
	}
	
	public void selectAccount(View v) {
		Intent intent = AccountPicker.newChooseAccountIntent(null, null,
				new String[] { "com.google" }, false, null, null, null, null);
		startActivityForResult(intent, ACCOUNT_REQUEST_CODE);
	}

}
