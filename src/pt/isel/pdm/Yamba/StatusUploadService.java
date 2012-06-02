package pt.isel.pdm.Yamba;

import java.util.List;

import winterwell.jtwitter.Twitter.Status;
import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
		//unregisterReceiver(_rec);
		super.onDestroy();
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Log.d(App.TAG, "StatusUpload.onHandleIntent");
		String statusText = intent.getStringExtra("statusText");
					
		//Runnable run;

		if (statusText == null) {
			List<String> offlineStatusList = _app.db().getOfflineStatus() ;
			
			for (String str:offlineStatusList) {
				sendStatus(str) ;
			}
			
		} else {
			sendStatus(statusText) ;
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
//	
//	class StatusReceiver extends BroadcastReceiver{
//
//		App _app ;
//		
//		@Override
//		public void onReceive(Context context, Intent intent) {
//			//String action = intent.getAction() ;
//			_app = (App) getApplication();
//			boolean connect = ! intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
//			Log.d(App.TAG, "StatusReceiver.onReceive Connectivity="+connect);
//			
//		}
//		
//
//	}

}
