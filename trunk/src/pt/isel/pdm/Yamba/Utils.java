package pt.isel.pdm.Yamba;

import android.content.Context;
import android.widget.Toast;
import android.util.Log;

public final class Utils {

	/** Displays a Toast with long length duration */ 
	public static void showToast(Context ctx, String txt) {
		Toast.makeText(ctx, txt, Toast.LENGTH_LONG).show();
	}
	
	private static final String TAG = "PDM";
	public static void Log(String txt) {
		Log.d(TAG, txt);
	}
	
}

