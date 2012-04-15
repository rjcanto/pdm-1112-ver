package pt.isel.pdm.Yamba;

import android.app.Activity;
import android.view.Menu;

public class GeneralMenu {

	private final Activity _activity;
	
	public GeneralMenu(Activity activity, Menu m) {
		_activity = activity;
		_activity.getMenuInflater().inflate(R.menu.general, m);
	}
	
	
	
	public void timelineVisible(Boolean visible) {
		
	}
}
