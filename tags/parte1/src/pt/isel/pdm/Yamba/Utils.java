package pt.isel.pdm.Yamba;

import android.content.Context;
import android.widget.Toast;

public final class Utils {

	/** Displays a Toast with long length duration */ 
	public static void showToast(Context ctx, String txt) {
		Toast.makeText(ctx, txt, Toast.LENGTH_LONG).show();
	}
	
}

