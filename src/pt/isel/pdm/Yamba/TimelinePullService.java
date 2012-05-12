package pt.isel.pdm.Yamba;

import android.app.IntentService;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

public class TimelinePullService extends IntentService {
	private App _app;
	private Handler _hMainThread ;
	
	public TimelinePullService() {
		super("TimelinePullService");
		_app = (App) getApplication() ;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		_hMainThread = new Handler();
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Log.d(App.TAG, "TimelinePullService.onHandleIntent");
		try {
			_app.onServiceNewTimelineResult(_app.twitter().getUserTimeline());
		} catch (final Exception e) {
			_hMainThread.post(new Runnable() {
				//TODO differentiate errors?
				public void run() {
					Utils.showToast(getApplicationContext(), getString(R.string.connectionError)) ;
				}
			});
			Log.d(App.TAG, "TimelinePullService.onHandleIntent: Exception " + e.getMessage());
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(App.TAG, "TimelinePullService.onStartCommand");
		return super.onStartCommand(intent, flags, startId);
	}


}
