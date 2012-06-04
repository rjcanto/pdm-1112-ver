package pt.isel.pdm.Yamba.util;

import pt.isel.pdm.Yamba.App;
import pt.isel.pdm.Yamba.services.StatusUploadService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.util.Log;

public class NetworkReceiver extends BroadcastReceiver{

	
	@Override
	public void onReceive(Context context, Intent intent) {
		boolean connect = ! intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
		Log.d(App.TAG, "MyReceiver.onReceive Connectivity="+connect);
		if (connect) {
			context.startService(new Intent(context, StatusUploadService.class));
			Log.d(App.TAG, "MyReceiver.onReceive Launching StatusUploadService");
		}
	}
}
