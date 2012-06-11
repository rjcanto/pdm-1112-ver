package pt.isel.pdm.Yamba.services;

import pt.isel.pdm.Yamba.App;
import android.app.IntentService;
import android.content.Intent;
import android.database.Cursor;
import android.os.Handler;
import android.util.Log;


public class DbService extends IntentService {
	
	public static final String EXTRA_ACTIONS = "ACTIONS";
	public static final String EXTRA_ID = "ID";
	public static final int ACTION_REFRESH_TIMELINE = 1;
	public static final int ACTION_SET_STATUS_READ = 2;
	
	private App _app;
	private Handler _hMainThread;
	
	public DbService() {
		super("DbService");
	}
	
	@Override
	public void onCreate() {
		Log.d(App.TAG, "DbService.onCreate");
		super.onCreate();
		_app = (App) getApplication();
		_hMainThread = new Handler();
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Log.d(App.TAG, "DbService.onHandleIntent");
		int actions = intent.getIntExtra(EXTRA_ACTIONS, 0);
        
		if ((actions & ACTION_SET_STATUS_READ) == ACTION_SET_STATUS_READ) {
			Log.d(App.TAG, "DbService.onHandleIntent ACTION_SET_STATUS_READ");
			long id = intent.getLongExtra(EXTRA_ID, -1);
			_app.timeline().setRead(id);
		}
		
		if ((actions & ACTION_REFRESH_TIMELINE) == ACTION_REFRESH_TIMELINE) {
			Log.d(App.TAG, "DbService.onHandleIntent ACTION_REFRESH_TIMELINE");
			
			_app.timeline().deleteOlderMessages(_app.prefs().maxPostsStored());
			
			final Cursor c = _app.timeline().getTimeline(_app.prefs().maxPosts());
			
			_hMainThread.post(new Runnable() {
				public void run() {
					_app.onServiceNewTimelineResult(c);
				}
			});			
		}
	}
}
