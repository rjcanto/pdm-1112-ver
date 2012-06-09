package pt.isel.pdm.Yamba.services;

import java.util.List;

import pt.isel.pdm.Yamba.App;
import pt.isel.pdm.Yamba.R;
import pt.isel.pdm.Yamba.activity.TimelineActivity;
import pt.isel.pdm.Yamba.util.Utils;

import winterwell.jtwitter.Twitter;
import winterwell.jtwitter.TwitterException;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.Log;

public class TimelinePullService extends Service {
	private App _app;
	private Handler _hMainThread;
	private Looper _serviceLooper;
	private ServiceHandler _serviceHandler;
	private NotificationManager _notificationManager;
	private Notification _notification ;
	
	private final static int CODE_AUTO_UPDATE = 0;	

	// Handler that receives messages from the thread
	private final class ServiceHandler extends Handler {
		public ServiceHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			retrieveTimeline();
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();
		_app = (App) getApplication();
		_hMainThread = new Handler();

		// Start up the thread running the service. We also make it
		// background priority so CPU-intensive work will not disrupt our UI.
		HandlerThread thread = new HandlerThread("ServiceStartArguments",
				Process.THREAD_PRIORITY_BACKGROUND);
		thread.start();

		// Get the HandlerThread's Looper and use it for our Handler
		_serviceLooper = thread.getLooper();
		_serviceHandler = new ServiceHandler(_serviceLooper);
		
		_app.getApplicationContext();
		_notificationManager = (NotificationManager) getSystemService(
				Context.NOTIFICATION_SERVICE);
		
		_notification = new Notification(
				android.R.drawable.stat_notify_chat, 
				getString(R.string.tl_notification_text), 
				System.currentTimeMillis());
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(App.TAG, "TimelinePullService.onStartCommand");

		// For each start request, send a message to start a job and deliver the
		// start ID so we know which request we're stopping when we finish the
		// job
		Message msg = _serviceHandler.obtainMessage();
		msg.arg1 = startId;
		_serviceHandler.sendMessage(msg);

		// If we get killed, after returning from here, restart
		return START_STICKY;
	}
	
	private void retrieveTimeline() {
		Log.d(App.TAG, "TimelinePullService.retrieveTimeline");
		try {
			
			List<Twitter.Status> timeline = _app.twitter().getUserTimeline();
			_app.timeline().insertStatus(timeline);
			
			sendNotification(timeline.size(), timeline.get(timeline.size()-1).text) ;
			
			_hMainThread.post(new Runnable() {
				public void run() {
					_app.onServiceNewTimelineResult();
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
			
			stopSelf();
			return;
		}
		
		
		if (_app.prefs().autoRefresh()) {
			_serviceHandler.removeMessages(CODE_AUTO_UPDATE);
			_serviceHandler.sendMessageDelayed(
					_serviceHandler.obtainMessage(CODE_AUTO_UPDATE),
					_app.prefs().autoRefreshTime());
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

	@Override
	public IBinder onBind(Intent intent) {
		// We don't provide binding, so return null
		return null;
	}

	@Override
	public void onDestroy() {
		Log.d(App.TAG, "TimelinePullService.onDestroy");		
		_serviceLooper.quit();
		_serviceHandler.removeMessages(CODE_AUTO_UPDATE);
		super.onDestroy();
	}

}
