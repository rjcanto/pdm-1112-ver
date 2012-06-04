package pt.isel.pdm.Yamba.services;

import java.util.List;

import pt.isel.pdm.Yamba.App;
import pt.isel.pdm.Yamba.R;
import pt.isel.pdm.Yamba.R.string;
import pt.isel.pdm.Yamba.util.Utils;

import winterwell.jtwitter.Twitter.Status;
import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;


public class StatusUploadService extends IntentService {
	private App _app;
	private Handler _hMainThread;
	BroadcastReceiver _rec ;
	
	public StatusUploadService() {
		super("StatusUploadService");
	}
	
	@Override
	public void onCreate() {
		Log.d(App.TAG, "StatusUpload.onCreate");
		super.onCreate();
		_app = (App) getApplication();		
		_hMainThread = new Handler();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Log.d(App.TAG, "StatusUpload.onHandleIntent");
		String statusText = intent.getStringExtra("statusText");
					
		Runnable run;

		if (statusText == null) {
			List<String> offlineStatusList = _app.db().getOfflineStatus() ;
			
			for (String str:offlineStatusList) {
				sendStatus(str) ;
			}
			
		} else {
			if (_app.isNetworkAvailable())
				sendStatus(statusText) ;
			else {
				Log.d(App.TAG, "StatusUpload.onHandleIntent - Storing Status in DB");
				_app.db().insertOfflineStatus(statusText) ;
				run = new Runnable() {
					public void run() {
						Utils.showToast(_app.context(), getString(R.string.statusOfflineMessage));
						_app.onServiceNewStatusSent(null);
					}
				};
				_hMainThread.post(run);
			}
		}		
	}
	
	private void sendStatus(String statusMsg) {
		Runnable run;
		try {
			final Status status = _app.twitter().updateStatus(statusMsg);
			Log.d(App.TAG, "Status submited, text = " + statusMsg);
			run = new Runnable() {
				public void run() {
					Utils.showToast(_app.context(), getString(R.string.successMessage));
					_app.onServiceNewStatusSent(status);
				}
			};					
		} 
		catch (final Exception e) {
			Log.d(App.TAG, "StatusUpload.onHandleIntent: Exception " + e.getMessage());
			run = new Runnable() {
				public void run() {
					Utils.showToast(getApplicationContext(), getString(R.string.connectionError));
					_app.onServiceNewStatusSent(null);
				}
			};
		}
		_hMainThread.post(run);
	}
}
