package pt.isel.pdm.Yamba;

import winterwell.jtwitter.Twitter.Status;
import android.app.IntentService;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;


public class StatusUploadService extends IntentService {
	private App _app;
	private Handler _hMainThread;
	
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
	protected void onHandleIntent(Intent intent) {
		Log.d(App.TAG, "StatusUpload.onHandleIntent");
		String statusText = intent.getStringExtra("statusText");
		
		Runnable run;
		try {
			final Status status = _app.twitter().updateStatus(statusText);
			Log.d(App.TAG, "Status submited, text = " + statusText);
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
