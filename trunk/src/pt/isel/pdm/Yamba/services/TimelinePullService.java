package pt.isel.pdm.Yamba.services;

import java.util.List;

import pt.isel.pdm.Yamba.App;
import pt.isel.pdm.Yamba.R;
import pt.isel.pdm.Yamba.activity.TimelineActivity;
import pt.isel.pdm.Yamba.util.Utils;
import winterwell.jtwitter.Twitter.Status;
import winterwell.jtwitter.TwitterException;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Handler;
import android.util.Log;

public class TimelinePullService extends IntentService {	

	private App _app;
	private Handler _hMainThread;
	private NotificationManager _notificationManager;
	private Notification _notification ;
	
	public TimelinePullService() {
		super("TimelinePullService");
	}
	

	@Override
	public void onCreate() {
		super.onCreate();
		_app = (App) getApplication();
		_hMainThread = new Handler();

		_notificationManager = (NotificationManager) getSystemService(
				Context.NOTIFICATION_SERVICE);
		
		_notification = new Notification(
				android.R.drawable.stat_notify_chat, 
				getString(R.string.tl_notification_text), 
				System.currentTimeMillis());
		_notification.flags = Notification.DEFAULT_SOUND | Notification.FLAG_AUTO_CANCEL ;
		
	}
	
	@Override
	protected void onHandleIntent(Intent intent) {
		Log.d(App.TAG, "TimelinePullService.onHandleIntent");
		try {
			
			// if most recent status id is unknown and timeline wasn't already retrieved, query DB
			if (_app.mostRecentStatusId == null && !_app.timelineRetrieved)
				_app.mostRecentStatusId = _app.timeline().getMostRecentStatusId();

			_app.twitter().setSinceId(_app.mostRecentStatusId);
			
			if (_app.isNetworkAvailable()) {
				List<Status> timeline = _app.twitter().getUserTimeline();
								
				_app.timeline().insertStatus(timeline);				
								
				if (timeline.size() > 0) {
					Status mostRecentStatus = timeline.get(0);
					_app.mostRecentStatusId = mostRecentStatus.id;
					int unread = _app.timeline().getUnreadStatusCount();
					sendNotification(unread, mostRecentStatus.text);
				}
			}
			
			_app.timeline().deleteOlderMessages(_app.prefs().maxPostsStored());
			
			final Cursor c = _app.timeline().getTimeline(_app.prefs().maxPosts());			
			
			_hMainThread.post(new Runnable() {
				public void run() {
					_app.onServiceNewTimelineResult(c);
				}
			});
			
		} catch (final TwitterException e) {
			_hMainThread.post(new Runnable() {
				// TODO differentiate errors?
				public void run() {
					Utils.showToast(getApplicationContext(),
							getString(R.string.connectionError));
				}
			});
			Log.d(App.TAG,
					"TimelinePullService.retrieveTimeline: Exception "
							+ e.getMessage());
			return;
		}
	}
	
	/**
	 * Sends a notification to the notificationManager announcing that are new
	 * messages available in the timeline.
	 * 
	 * @param nStatusRetrieved	Number of messages retrieved, so it can be displayed in the notification
	 * @param lastStatus	The text from the last status, so it can be displayed in the notification
	 */
	private void sendNotification(int nStatusRetrieved, String lastStatusText) {
		Context context = getApplicationContext();
		Intent notificationIntent = new Intent(this, TimelineActivity.class);
		//notificationIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT) ;
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

		_notification.setLatestEventInfo(
				context, 
				(nStatusRetrieved==1)?  
						nStatusRetrieved + getString(R.string.tl_notification_title_one) :
						nStatusRetrieved + getString(R.string.tl_notification_title_several), 
				lastStatusText, 
				contentIntent
		);
		final int HELLO_ID = 1;
		_notificationManager.notify(HELLO_ID, _notification);
	}
}
