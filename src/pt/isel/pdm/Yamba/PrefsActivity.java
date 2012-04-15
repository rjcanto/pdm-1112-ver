package pt.isel.pdm.Yamba;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.util.Log;

public class PrefsActivity extends PreferenceActivity {

	private App _app;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(App.TAG, "PrefsActivity.onCreate");
		super.onCreate(savedInstanceState);
		_app = (App) getApplication();
		addPreferencesFromResource(R.xml.app_prefs);
		Utils.showToast(this, getString(R.string.fillRequiredPreferences));
	}

	@Override
	public void onBackPressed() {

		if (_app.prefs().hasRequired()) {
			super.onBackPressed();		
			return;
		}
		
		Log.d(App.TAG, "Required preferences are missing");
		Utils.showToast(_app.context(), getString(R.string.fillRequiredPreferences));
	}
	
}
