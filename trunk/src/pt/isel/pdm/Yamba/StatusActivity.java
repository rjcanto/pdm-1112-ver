package pt.isel.pdm.Yamba;

import winterwell.jtwitter.Twitter.Status;
import android.app.Activity;
import android.content.Intent;
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
	private Intent _serviceIntent;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(App.TAG, "StatusActivity.onCreate");
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.status);
		
		_app = (App) getApplication();
		_app.prefs().registerOnPreferenceChangeListener(this);
		
		_submit = (Button) findViewById(R.id.buttonUpdate);
		_submit.setOnClickListener(this);				
		
		_availChars = (TextView) findViewById(R.id.availChars);
				
		_text = (EditText) findViewById(R.id.editText);
		_text.addTextChangedListener(this);
		
		updateStatusMsgBox();
	}
	
	@Override
	protected void onDestroy() {
		_app.prefs().unregisterOnPreferenceChangeListener(this);
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		Log.d(App.TAG, "StatusActivity.onResume");
		super.onResume();
		_app.statusAct = this;

		if (_app.sendingStatus)
			disableSubmit();
		else
			enableSubmit();
	}

	@Override
	protected void onPause() {
		Log.d(App.TAG, "StatusActivity.onPause");
		super.onPause();
		_app.statusAct = null;
	}


	/** Called by submit button */
	public void onClick(View v) {
		Log.d(App.TAG, "StatusActivity.onClick");
		disableSubmit();
		if (_serviceIntent == null)
			_serviceIntent = new Intent(this, StatusUploadService.class);
		_serviceIntent.putExtra("statusText", _text.getText().toString());
		_app.sendingStatus = true;
		startService(_serviceIntent);
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
		_generalMenu.userInfo().setVisible(true);
		return true;
	}

	/** Process Item Menu selected */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {		
		return _generalMenu.processSelection(item) ? true : super.onOptionsItemSelected(item);
	}

	/** Invalidates the twitter when changing preferences */
	public void onPreferenceChanged(Preferences prefs, String key, boolean sessionInvalidated) {
		Log.d(App.TAG,"StatusActiviy.onPrefsChanged");
		if (key.equals("maxChars"))
			updateStatusMsgBox();
	}

	public void onStatusSent(Status status) {
		Log.d(App.TAG, "StatusActivity.onStatusSent");
		//if (status != null) {
			// Reset text and available chars
			_text.setText("");
			_availChars.setText(String.valueOf(_app.prefs().maxChars()));
			//_app.statusAdapter.add(status);
			
		//}
		enableSubmit();
	}
	
	
	/** Enable submit button of last activity */
	private void enableSubmit() {
		Log.d(App.TAG, "StatusActivity.enableSubmit");
		_submit.setEnabled(true);
		_submit.setText(R.string.buttonUpdate);
		_text.setFocusable(true);
		_text.setFocusableInTouchMode(true);
	}

	/** Disable submit button of this activity */
	private void disableSubmit() {
		Log.d(App.TAG, "StatusActivity.disableSubmit");
		_submit.setEnabled(false);
		_submit.setText(R.string.buttonBusy);
		_text.setFocusable(false);
		_text.setFocusableInTouchMode(false);
	}
	
	private void updateStatusMsgBox() {
		int maxChars = _app.prefs().maxChars();
		_text.setHint(getString(R.string.hintText, maxChars));
		_text.setFilters(new InputFilter[] { new InputFilter.LengthFilter(maxChars) });
		if(_text.length() > maxChars)
			_text.getText().delete(maxChars, _text.length());
		_availChars.setText(String.valueOf(maxChars - _text.length()));
	}
}