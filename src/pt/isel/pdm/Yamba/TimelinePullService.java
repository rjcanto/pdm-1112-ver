package pt.isel.pdm.Yamba;

import java.util.List;

import winterwell.jtwitter.Twitter;
import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
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
	
	private DbHelper _dbHelper;
	private SQLiteDatabase _database;
	
	private final static int CODE_AUTO_UPDATE = 0;	

	// Handler that receives messages from the thread
	private final class ServiceHandler extends Handler {
		public ServiceHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			onRefresh();
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
		
		_dbHelper = new DbHelper(this);

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
	
	private void onRefresh() {
		Log.d(App.TAG, "TimelinePullService.onRefresh");
		try {
			final List<Twitter.Status> timeline = _app.twitter().getUserTimeline();
			_hMainThread.post(new Runnable() {
				public void run() {
					_app.onServiceNewTimelineResult(timeline);
				}
			});

			// Update DB
			_database = _dbHelper.getWritableDatabase();
			 
			ContentValues values = new ContentValues();
			
			for (Twitter.Status status : timeline) {// Insert into database
				values.clear(); //
				values.put(TimelineContract._ID, status.id);
				values.put(TimelineContract.CREATED_AT, status.createdAt.getTime());
				values.put(TimelineContract.AUTHOR_ID, status.user.id);
				values.put(TimelineContract.IS_READ, true);
				values.put(TimelineContract.TEXT, status.text);				
				_database.insertOrThrow(TimelineContract.TABLE, null, values);
				_database.insertWithOnConflict(TimelineContract.TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE);
			}
			// Close the database
			_database.close();
			
		} catch (final Exception e) {
			_hMainThread.post(new Runnable() {
				// TODO differentiate errors?
				public void run() {
					Utils.showToast(getApplicationContext(),
							getString(R.string.connectionError));
				}
			});
			Log.d(App.TAG,
					"TimelinePullService.onHandleIntent: Exception "
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
