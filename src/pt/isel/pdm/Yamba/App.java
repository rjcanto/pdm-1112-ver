package pt.isel.pdm.Yamba;

import java.util.List;

import winterwell.jtwitter.Twitter;
import winterwell.jtwitter.Twitter.Status;
import android.app.Application;
import android.content.Context;
import android.util.Log;
import android.widget.Button;

public class App extends Application implements OnPreferenceChangeListener {
	public static final String TAG = "PDM";
	private Preferences _prefs;
	private Twitter _twitter;
	public Button lastSubmit; // apagar?
	
	@Override
	public void onCreate() {
		super.onCreate();
		_prefs = new Preferences(getApplicationContext());
	}



	/** Returns the Preferences object */
	public Preferences prefs() {
		return _prefs;
	}
	
	/** Returns the Twitter object */ 
	public Twitter twitter() {
		if (_twitter != null)
			return _twitter;
		
		Log.d(TAG, "new Twitter");		
		_twitter = new Twitter(_prefs.user(), _prefs.pass());
		_twitter.setAPIRootUrl(_prefs.url());
		return _twitter;
	}
	
	public void onPreferenceChanged(Preferences sp, String key) {
		if (key == "url" || key == "user" || key == "pass")
			_twitter = null;
			
	}
}
