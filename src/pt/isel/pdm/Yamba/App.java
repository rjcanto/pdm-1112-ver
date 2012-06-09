package pt.isel.pdm.Yamba;

import pt.isel.pdm.Yamba.activity.StatusActivity;
import pt.isel.pdm.Yamba.activity.TimelineActivity;
import pt.isel.pdm.Yamba.providers.Timeline;
import pt.isel.pdm.Yamba.services.TimelinePullService;
import pt.isel.pdm.Yamba.util.OnPreferenceChangeListener;
import pt.isel.pdm.Yamba.util.Preferences;
import pt.isel.pdm.Yamba.util.Utils;
import winterwell.jtwitter.Twitter;
import winterwell.jtwitter.Twitter.Status;
import winterwell.jtwitter.TwitterException;
import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.SimpleCursorAdapter;

public class App extends Application implements OnPreferenceChangeListener {
	public static final String TAG = "PDM";
	private Preferences _prefs;
	private Twitter _twitter;
	//private PdmDb _pdmDb;
	private Timeline _timeline;
	private ConnectivityManager _connMan = null;
	private NetworkInfo _netInfo = null;

	/**
	 * Shared state
	 */
	public SimpleCursorAdapter timelineAdapter;
	public ProgressDialog progressDialog;
	public TimelineActivity timelineAct ;
	public StatusActivity statusAct;
	public boolean sendingStatus;
	public boolean timelineRetrieved;
	public boolean isPendingStatus;
	
	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(App.TAG, "App.onCreate");
		isPendingStatus = false ;
		_prefs = new Preferences(getApplicationContext());
		_prefs.registerOnPreferenceChangeListener(this);
		//_pdmDb = new PdmDb(this);
		_timeline = new Timeline(this);
	}
	
	/** Returns the content provider adapter */
	public Timeline timeline() {
		return _timeline;
	}
	/*public PdmDb db() {
		return _pdmDb;
	}*/
	
	/** Returns the Preferences object */
	public Preferences prefs() {
		return _prefs;
	}
	
	/** Invalidate Twitter object if key preferences changed */
	public void onPreferenceChanged(Preferences sp, String key, boolean sessionInvalidated) {
		Log.d(TAG, "App.onPreferenceChanged");
		if (sessionInvalidated)
			_twitter = null;
		
		if (key.equals(!_prefs.autoRefresh())) {
			stopService(new Intent(this, TimelinePullService.class));
			return;
		}
		
		if (key.equals("maxPosts") && _twitter != null) {
			_twitter.setCount(_prefs.maxPosts());
			return;
		}
	}
	
	public void onServiceNewTimelineResult() {
		timelineRetrieved = true;
		if(timelineAct != null)
			timelineAct.onTimelineRefreshed();
	}
	
	public void onServiceNewStatusSent(Status status) {
		sendingStatus = false;
//		if (status != null) {
//			_timelineResult.add(0, status);
//			statusAdapter.notifyDataSetChanged();
//		}
		//if (statusAct != null)
			statusAct.onStatusSent(status);
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
	
	public boolean isNetworkAvailable() {
		if (_connMan==null) 
			_connMan = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE) ;
		_netInfo = _connMan.getActiveNetworkInfo();
		return _netInfo!=null && _netInfo.isConnected();
	}
}
