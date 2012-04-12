package pt.isel.pdm.Yamba;

import java.io.Serializable;

import winterwell.jtwitter.Twitter;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;

public class StatusActivity extends Activity 
                 implements OnClickListener, OnSharedPreferenceChangeListener, TextWatcher {
	private static final String TAG = "PDM";
	private static final int MAX_CHARS = 140 ;
	private Button _submit;
	private EditText _text;
	private Twitter _twitter;
	private SharedPreferences _prefs;
	private TextView _availChars;
	private int _maxChars;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.status);
		
		_submit = (Button) findViewById(R.id.buttonUpdate);
		_submit.setOnClickListener(this);
		
		App app = (App) getApplication();
		if (app.lastSubmit != null && !app.lastSubmit.isEnabled())
			disableSubmit();
		app.lastSubmit = _submit;
		
		
		_prefs = PreferenceManager.getDefaultSharedPreferences(this);
		_prefs.registerOnSharedPreferenceChangeListener(this);
		getPreferenceMaxChar();
		
		_availChars = (TextView) findViewById(R.id.availChars);
		
		
		_text = (EditText) findViewById(R.id.editText);
		_text.addTextChangedListener(this);
		updateStatusMsgBox();
		
		
		/*// Test using Intents to pass _twitter to TimelineActivity
		Intent intent = new Intent(getBaseContext(), TimelineActivity.class);
		savedInstanceState.putSerializable("twitter", (Serializable)_twitter);
		intent.putExtra("bundle", savedInstanceState);
		startActivity(intent);*/
	}
	
	/** Called by submit button */
	public void onClick(View v) {
		Log.d(TAG, "onClick");
		disableSubmit();
		// Update status and enable submit in background  
		new UpdateStatusTask().execute(_text.getText().toString());
	}
	
	public void onTextChanged(CharSequence s, int start, int before, int count) { }
	
	public void beforeTextChanged(CharSequence s, int start, int count,	int after) { }
	
	public void afterTextChanged(Editable s) {
		_availChars.setText(String.valueOf(_maxChars - s.length()));
	}

	/** Initialize options menu */
	@Override
	public boolean onCreateOptionsMenu(Menu m) {
		getMenuInflater().inflate(R.menu.status, m);
		return true;
	}

	/** Process Item Menu selected */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.terminate:
			finish();
			return true;
		case R.id.prefs:
			startActivity( new Intent(this,PrefsActivity.class) );
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/** Invalidates the twitter when changing preferences */
	public void onSharedPreferenceChanged(SharedPreferences sp,	String key) {
		Log.d(TAG,"onPrefsChanged");
		if(key.equals("maxChars")) {
			getPreferenceMaxChar();
			updateStatusMsgBox();
		}
		//_twitter = null;
	}

	// UTILITIES 
	
	/** Task to update Status and enable submit button (in background) */
	private class UpdateStatusTask extends AsyncTask<String, Void, Void> {
		private static final long TOTAL_TM = 5000; // Elapsed time.
		private volatile Exception error=null;
		
		protected Void doInBackground(String... params) {
			try {
				long startTm = System.currentTimeMillis();
				getTwitter().updateStatus(params[0]);
				long elapsedTm = System.currentTimeMillis() - startTm;
				if (elapsedTm < TOTAL_TM) // Provides a minimum duration
					Thread.sleep(TOTAL_TM - elapsedTm);
				Log.d(TAG, "Submited. Elapsed time=" + elapsedTm + ", text=" + params[0]);
			} catch (Exception ex) { error = ex; }
			return null;
		}
		protected void onPostExecute(Void res) {
			Log.d(TAG, "onPostExecute");
			
			if (error!=null)
				showToast(getString(R.string.failMessage,error));
			else {
				showToast(getString(R.string.successMessage));
				_text.setText("");
				_availChars.setText(String.valueOf(_maxChars)) ;
			}
			enableSubmit();
		}
	}
	
	/** Displays a Toast with long length duration */ 
	private void showToast(String txt) {
		Toast.makeText(StatusActivity.this, txt, Toast.LENGTH_LONG).show();		
	}
	
	/** Return the twitter object using shared preferences */ 
	private Twitter getTwitter() {
		if (_twitter == null) {
			Log.d(TAG, "new Twitter");
			String user = _prefs.getString("user", "");
			String pass = _prefs.getString("pass", "");
			String url = _prefs.getString("url", "");
			_twitter = new Twitter(user, pass);
			_twitter.setAPIRootUrl(url);
		}
		return _twitter;
	}

	/** Enable submit button of last activity */
	private void enableSubmit() {
		Button submit = ((App) getApplication()).lastSubmit;
		submit.setEnabled(true);
		submit.setText(R.string.buttonUpdate);
	}

	/** Disable submit button of this activity */
	private void disableSubmit() {
		_submit.setEnabled(false);
		_submit.setText(R.string.buttonBusy);
	}
	
	private void updateStatusMsgBox() {
		_text.setHint(getString(R.string.hintText, _maxChars));
		_text.setFilters(new InputFilter[] { new InputFilter.LengthFilter(_maxChars) });
		if(_text.length() > _maxChars)
			_text.getText().delete(_maxChars, _text.length());
		_availChars.setText(String.valueOf(_maxChars-_text.length()));
	}
	
	private void getPreferenceMaxChar() {
		String sMaxChars = _prefs.getString("maxChars", Integer.toString(MAX_CHARS));
		if (sMaxChars=="")
			sMaxChars = Integer.toString(MAX_CHARS) ;
		_maxChars = Integer.parseInt(sMaxChars) ;
	}
}