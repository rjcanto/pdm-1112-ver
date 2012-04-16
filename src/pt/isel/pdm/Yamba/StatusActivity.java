package pt.isel.pdm.Yamba;

import android.app.Activity;
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

public class StatusActivity extends Activity 
                 implements OnClickListener, OnPreferenceChangeListener, TextWatcher {
	private Button _submit;
	private EditText _text;
	private App _app;
	private TextView _availChars;
	private GeneralMenu _generalMenu;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(App.TAG, "StatusActivity.onCreate");
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
	}
	
	/** Called by submit button */
	public void onClick(View v) {
		Log.d(App.TAG, "StatusActivity.onClick");
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
	public boolean onCreateOptionsMenu(Menu menu) {
		_generalMenu = new GeneralMenu(this, menu);
		_generalMenu.timeline().setVisible(true);
		_generalMenu.status().setVisible(false);
		_generalMenu.refresh().setVisible(false);
		return true;
	}

	/** Process Item Menu selected */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {		
		return _generalMenu.processSelection(item) ? true : super.onOptionsItemSelected(item);
	}

	/** Invalidates the twitter when changing preferences */
	public void onPreferenceChanged(Preferences prefs, String key) {
		Log.d(App.TAG,"StatusActiviy.onPrefsChanged");
		if (key.equals("maxChars"))
			updateStatusMsgBox();
	}

	// UTILITIES 
	
	/** Task to update Status and enable submit button (in background) */
	private class UpdateStatusTask extends AsyncTask<String, Void, Void> {
		private static final long TOTAL_TM = 5000; // Elapsed time.
		private volatile Exception error=null;
		
		protected Void doInBackground(String... params) {
			Log.d(App.TAG, "UpdateStatusTask.doInBackground");
			try {
				long startTm = System.currentTimeMillis();
				_app.twitter().updateStatus(params[0]);
				long elapsedTm = System.currentTimeMillis() - startTm;
				if (elapsedTm < TOTAL_TM) // Provides a minimum duration
					Thread.sleep(TOTAL_TM - elapsedTm);
				Log.d(App.TAG, "Submited. Elapsed time=" + elapsedTm + ", text=" + params[0]);
			} catch (Exception ex) { error = ex; }
			return null;
		}
		protected void onPostExecute(Void res) {
			Log.d(App.TAG, "UpdateTask.onPostExecute");
			
			if (error!=null)
				Utils.showToast(_app.context(), getString(R.string.failMessage, error.getCause()));
			else {
				Utils.showToast(_app.context(), getString(R.string.successMessage));
				_text.setText("");
				_availChars.setText(String.valueOf(_app.prefs().maxChars())) ;
			}
			enableSubmit();
		}
	}

	/** Enable submit button of last activity */
	private void enableSubmit() {
		Button submit = _app.lastSubmit;
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