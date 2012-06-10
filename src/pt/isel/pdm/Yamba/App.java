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
import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.SimpleCursorAdapter;

public class App extends Application implements OnPreferenceChangeListener {
	public static final String TAG = "PDM";
	private Preferences _prefs;
	private Twitter _twitter;
	private Timeline _timeline;
	private ConnectivityManager _connMan = null;
	private NetworkInfo _netInfo = null;
	private PendingIntent _alarmIntent;
	private AlarmManager _alarmManager;

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
		_timeline = new Timeline(this);
		
		_alarmIntent = PendingIntent.getService(this, -1,
				new Intent(this, TimelinePullService.class),
				PendingIntent.FLAG_UPDATE_CURRENT);
		_alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
	}
	
	/** Returns the content provider adapter */
	public Timeline timeline() {
		return _timeline;
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
		
		if (key.equals(!_prefs.autoRefresh())) {
			stopService(new Intent(this, TimelinePullService.class));
			return;
		}
		
		if (key.equals("maxPosts") && _twitter != null) {
			_twitter.setCount(_prefs.maxPosts());
			return;
		}
		
		if (key.equals("autoRefresh") || key.equals("autoRefreshTime") && timelineRetrieved) {
			if (prefs().autoRefresh()) {
				Utils.Log("App.onPreferenceChanged: setting alarm");
				setAutoUpdate(prefs().autoRefreshTime());
			}
			else {
				Utils.Log("App.onPreferenceChanged: cancelling alarm");
				_alarmManager.cancel(_alarmIntent);
			}
			
			return;
		}
	}
	
	public void onServiceNewTimelineResult(Cursor c) {
		timelineRetrieved = true;
		if (timelineAct != null)
			timelineAct.onTimelineRefreshed(c);
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
	
	public void setAutoUpdate(long startIn) {
		_alarmManager.setInexactRepeating(
				AlarmManager.ELAPSED_REALTIME, startIn,
				prefs().autoRefreshTime(),	_alarmIntent);
	}
}
