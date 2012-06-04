package pt.isel.pdm.Yamba.util;

import pt.isel.pdm.Yamba.App;
import pt.isel.pdm.Yamba.R;
import pt.isel.pdm.Yamba.R.id;
import pt.isel.pdm.Yamba.R.menu;
import pt.isel.pdm.Yamba.activity.PrefsActivity;
import pt.isel.pdm.Yamba.activity.StatusActivity;
import pt.isel.pdm.Yamba.activity.TimelineActivity;
import pt.isel.pdm.Yamba.activity.UserInfoActivity;
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
	
	public MenuItem userInfo() {
		return _menu.findItem(R.id.menuUserInfo);
	}
	
	public boolean processSelection(MenuItem item) {
		Intent intent;
		
		switch (item.getItemId()) {
		case R.id.menuTerminate:
			_activity.finish();
			return true;
		case R.id.menuPrefs:
			 intent = new Intent(_activity, PrefsActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			_activity.startActivity(intent);
			return true;
		case R.id.menuStatus:
			intent = new Intent(_activity, StatusActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			_activity.startActivity(intent);
			return true;
		case R.id.menuTimeline:
			intent = new Intent(_activity, TimelineActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			_activity.startActivity(intent);
			return true;
		case R.id.menuRefresh:
			if (!(_activity instanceof TimelineActivity)) {
				Log.w(App.TAG, "Refresh selection called while not in TimelineAcivity");
				return false;
			}
			((TimelineActivity) _activity).refresh();			
			return true;
		case R.id.menuUserInfo:
			intent = new Intent(_activity, UserInfoActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			_activity.startActivity(intent);
			return true;
		default:
			Log.w(App.TAG, "Menu selection not processed");	
		}
		return false;
	}
}
