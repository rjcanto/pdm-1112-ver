package pt.isel.pdm.Yamba;

import java.util.List;

import winterwell.jtwitter.Twitter;
import winterwell.jtwitter.Twitter.Status;
import winterwell.jtwitter.TwitterException;
import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;
import android.widget.Button;

public class App extends Application implements OnPreferenceChangeListener {
	public static final String TAG = "PDM";
	private Preferences _prefs;
	private Twitter _twitter;	
	public Button lastSubmit;
	public TimelineActivity.StatusAdapter statusAdapter;
	public ProgressDialog progressDialog;
	public List<Twitter.Status> _timelineResult;
	public TimelineActivity _timelineAct ;
	
	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(App.TAG, "App.onCreate");
		_prefs = new Preferences(getApplicationContext());
		_prefs.registerOnPreferenceChangeListener(this);
		
	}

	/** Returns the Preferences object */
	public Preferences prefs() {
		return _prefs;
	}
	
	/** Invalidate Twitter object if key preferences changed */
	public void onPreferenceChanged(Preferences sp, String key, boolean sessionInvalidated) {
		Log.d(TAG, "App.onPreferenceChanged");
		if (sessionInvalidated)
			_twitter = null;		
	}
	
	public void onServiceNewTimelineResult(List<Status> list) {
		_timelineResult = list ;
		if (_timelineAct != null)
			_timelineAct.onTaskDone(_timelineResult) ;
	}
	
	/** Returns the Twitter object */ 
	public Twitter twitter() {
		if (_twitter != null)
			return _twitter;
		
		Log.d(TAG, "new Twitter");
		try {
			_twitter = new Twitter(_prefs.user(), _prefs.pass());
			_twitter.setAPIRootUrl(_prefs.url());
			_twitter.setCount(_prefs.maxPosts());
		}
		catch (TwitterException te) {
			Utils.showToast(context(), getString(R.string.connectionError));
		}
		return _twitter;
	}
	
	/** Returns application context */
	public Context context() {
		return getApplicationContext();
	}	
}
