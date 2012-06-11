package pt.isel.pdm.Yamba.util;

import pt.isel.pdm.Yamba.App;
import pt.isel.pdm.Yamba.services.StatusUploadService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

public class NetworkReceiver extends BroadcastReceiver{

	
	@Override
	public void onReceive(Context context, Intent intent) {
		App _app = (App) context.getApplicationContext() ;
		
		boolean connect = ! intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
		Utils.Log("NetworkReceiver.onReceive: Connectivity="+connect);
		if (connect) {
			context.startService(new Intent(context, StatusUploadService.class));
			Utils.Log("NetworkReceiver.onReceive: Launching StatusUploadService");
		}
		
		NetworkInfo nt = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo() ;
		
		if ( nt != null) {
			if ((nt.getType() == ConnectivityManager.TYPE_WIFI) && nt.isConnected() && _app.prefs().autoRefresh()) {
				Utils.Log("NetworkReceiver.onReceive: Launching AlarmManager") ;
				_app.setAutoUpdate(0) ;
			}
			else {
				Utils.Log("NetworkReceiver.onReceive: Stop AlarmManager") ;
				_app.stopAutoUpdate() ;
			}
		}
	}
}
