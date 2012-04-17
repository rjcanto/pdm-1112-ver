package pt.isel.pdm.Yamba;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class GeneralMenu {

	private final Activity _activity;
	private final Menu _menu;
	
	public GeneralMenu(Activity activity, Menu menu) {
		_activity = activity;
		_activity.getMenuInflater().inflate(R.menu.general, menu);
		_menu = menu;
	}
	
	public MenuItem timeline() { 
		return _menu.findItem(R.id.menuTimeline);
	}
	
	public MenuItem status() {
		return _menu.findItem(R.id.menuStatus);
	}
	
	public MenuItem refresh() {
		return _menu.findItem(R.id.menuRefresh);
	}

	public boolean processSelection(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menuTerminate:
			_activity.finish();
			return true;
		case R.id.menuPrefs:
			_activity.startActivity(new Intent(_activity, PrefsActivity.class));
			return true;
		case R.id.menuStatus:
			_activity.startActivity(new Intent(_activity, StatusActivity.class));
			return true;
		case R.id.menuTimeline:
			_activity.startActivity(new Intent(_activity, TimelineActivity.class));
			return true;
		case R.id.menuRefresh:
			if (!(_activity instanceof TimelineActivity)) {
				Log.w(App.TAG, "Refresh selection called while not in TimelineAcivity");
				return false;
			}
			((TimelineActivity) _activity).refresh();			
			return true;
		default:
			Log.w(App.TAG, "Menu selection not processed");	
		}
		return false;
	}
}
