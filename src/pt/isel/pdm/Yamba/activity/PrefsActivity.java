package pt.isel.pdm.Yamba.activity;

import pt.isel.pdm.Yamba.App;
import pt.isel.pdm.Yamba.R;
import pt.isel.pdm.Yamba.R.string;
import pt.isel.pdm.Yamba.R.xml;
import pt.isel.pdm.Yamba.util.Utils;
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
