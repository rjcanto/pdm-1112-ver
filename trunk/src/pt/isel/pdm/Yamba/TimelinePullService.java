package pt.isel.pdm.Yamba;

import java.util.List;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import winterwell.jtwitter.Twitter;
import winterwell.jtwitter.Twitter.Status;

public class TimelinePullService extends IntentService{
	private App _app;
	
	public TimelinePullService() {
		super("TimelinePullService");
		_app = (App) getApplication() ;
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Log.d(App.TAG, "TimelinePullService.onHandleIntent");
		try {
			List<Twitter.Status> result = _app.twitter().getUserTimeline();
		} catch (Exception e) {
			Log.d(App.TAG, "TimelinePullService.onHandleIntent: Exception " + e.getMessage());
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(App.TAG, "TimelinePullService.onStartCommand");
		return super.onStartCommand(intent, flags, startId);
	}

}
