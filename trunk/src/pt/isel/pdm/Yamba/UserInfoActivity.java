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

public class UserInfoActivity extends Activity 
                 implements OnPreferenceChangeListener {
	
	private App _app;
	private TextView _tvName, _tvStatusCount, _tvNSubscriptions, _tvNSubscribers;
	private GeneralMenu _generalMenu;
	private Intent _serviceIntent;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(App.TAG, "UserInfoActivity.onCreate");
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.userinfo);
		
		_app = (App) getApplication();
		_app.prefs().registerOnPreferenceChangeListener(this);
		
		_tvName = (TextView) findViewById(R.id.userInfo_name);
		_tvStatusCount = (TextView) findViewById(R.id.userInfo_statusCount);
		_tvNSubscriptions = (TextView) findViewById(R.id.userInfo_nSubscriptions);
		_tvNSubscribers = (TextView) findViewById(R.id.userInfo_nSubscribers);

		refresh(); // to onResume?
	}
	
	@Override
	protected void onDestroy() {
		_app.prefs().unregisterOnPreferenceChangeListener(this);
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		Log.d(App.TAG, "UserInfoActivity.onResume");
		super.onResume();
		//_app.statusAct = this;

		
	}

	@Override
	protected void onPause() {
		Log.d(App.TAG, "UserInfoActivity.onPause");
		super.onPause();
		//_app.statusAct = null;
	}

	/** Initialize options menu */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		_generalMenu = new GeneralMenu(this, menu);
		_generalMenu.timeline().setVisible(true);
		_generalMenu.status().setVisible(true);
		_generalMenu.refresh().setVisible(false);
		_generalMenu.userInfo().setVisible(false);
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
		if (sessionInvalidated)
			refresh();
	}

	private void refresh() {
		
	}
	
	public void onRefreshDone(Status status) {
		Log.d(App.TAG, "StatusActivity.onStatusSent");
		
	}
	
}