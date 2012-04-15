package pt.isel.pdm.Yamba;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class StatusActivity extends Activity 
                 implements OnClickListener, OnPreferenceChangeListener, TextWatcher {
	private static final String TAG = "PDM";
	private Button _submit;
	private EditText _text;
	private App _app;
	private TextView _availChars;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.status);
		
		_submit = (Button) findViewById(R.id.buttonUpdate);
		_submit.setOnClickListener(this);
		
		_app = (App) getApplication();
		if (_app.lastSubmit != null && !_app.lastSubmit.isEnabled())
			disableSubmit();
		_app.lastSubmit = _submit;
		
		_app.prefs().registerOnPreferenceChangeListener(this);		
		
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
		_availChars.setText(String.valueOf(_app.prefs().maxChars() - s.length()));
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
			startActivity( new Intent(this, PrefsActivity.class) );
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/** Invalidates the twitter when changing preferences */
	public void onPreferenceChanged(Preferences prefs, String key) {
		Log.d(TAG,"onPrefsChanged");
		if (key.equals("maxChars"))
			updateStatusMsgBox();
	}

	// UTILITIES 
	
	/** Task to update Status and enable submit button (in background) */
	private class UpdateStatusTask extends AsyncTask<String, Void, Void> {
		private static final long TOTAL_TM = 5000; // Elapsed time.
		private volatile Exception error=null;
		
		protected Void doInBackground(String... params) {
			try {
				long startTm = System.currentTimeMillis();
				_app.twitter().updateStatus(params[0]);
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
				_availChars.setText(String.valueOf(_app.prefs().maxChars())) ;
			}
			enableSubmit();
		}
	}
	
	/** Displays a Toast with long length duration */ 
	private void showToast(String txt) {
		Toast.makeText(StatusActivity.this, txt, Toast.LENGTH_LONG).show();		
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
		final int maxChars = _app.prefs().maxChars();
		_text.setHint(getString(R.string.hintText, maxChars));
		_text.setFilters(new InputFilter[] { new InputFilter.LengthFilter(maxChars) });
		if(_text.length() > maxChars)
			_text.getText().delete(maxChars, _text.length());
		_availChars.setText(String.valueOf(maxChars - _text.length()));
	}
}