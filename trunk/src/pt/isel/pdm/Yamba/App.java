package pt.isel.pdm.Yamba;

import java.util.List;

import pt.isel.pdm.Yamba.database.PdmDb;

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

public class App extends Application implements OnPreferenceChangeListener {
	public static final String TAG = "PDM";
	private Preferences _prefs;
	private Twitter _twitter;
	private PdmDb _pdmDb;
	private ConnectivityManager _connMan = null;
	private NetworkInfo _netInfo = null;

	/**
	 * Shared state
	 */
	public TimelineActivity.StatusAdapter statusAdapter;
	public ProgressDialog progressDialog;
	public List<Twitter.Status> _timelineResult;
	public TimelineActivity _timelineAct ;
	public StatusActivity statusAct;
	public boolean sendingStatus;
	public boolean isPendingStatus;
	
	
	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(App.TAG, "App.onCreate");
		isPendingStatus = false ;
		_prefs = new Preferences(getApplicationContext());
		_prefs.registerOnPreferenceChangeListener(this);
		_pdmDb = new PdmDb(this);
	}
	
	/** Returns the DAL object */	
	public PdmDb db() {
		return _pdmDb;
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
		}
	}
	
	public void onServiceNewTimelineResult() {
		if(_timelineAct != null)
			_timelineAct.onTaskDone();
	}
	
	public void onServiceNewStatusSent(Status status) {
		sendingStatus = false;
//		if (status != null) {
//			_timelineResult.add(0, status);
//			statusAdapter.notifyDataSetChanged();
//		}
		if (statusAct != null)
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
