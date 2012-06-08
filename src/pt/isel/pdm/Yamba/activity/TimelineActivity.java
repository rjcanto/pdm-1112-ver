package pt.isel.pdm.Yamba.activity;

import java.util.Date;
import java.util.List;

import pt.isel.pdm.Yamba.App;
import pt.isel.pdm.Yamba.R;
import pt.isel.pdm.Yamba.R.id;
import pt.isel.pdm.Yamba.R.layout;
import pt.isel.pdm.Yamba.R.string;
import pt.isel.pdm.Yamba.database.TimelineContract;
import pt.isel.pdm.Yamba.services.TimelinePullService;
import pt.isel.pdm.Yamba.util.GeneralMenu;
import pt.isel.pdm.Yamba.util.OnPreferenceChangeListener;
import pt.isel.pdm.Yamba.util.Preferences;
import pt.isel.pdm.Yamba.util.Utils;

import winterwell.jtwitter.Twitter;
import winterwell.jtwitter.Twitter.Status;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.CursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class TimelineActivity 
	extends ListActivity
	implements OnPreferenceChangeListener, ViewBinder, OnItemClickListener {

	private App _app;
	private GeneralMenu _generalMenu;
	private SQLiteDatabase _db;
	private Cursor _cursor;
	private Intent _timelinePullServiceIntent;
	
	/**
	 * Android overrides
	 */
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(App.TAG, "TimelineActivity.onCreate");
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.timeline);
		
		_app = (App) getApplication() ;	
		_app.prefs().registerOnPreferenceChangeListener(this);	
		getListView().setOnItemClickListener(this);
				
		_timelinePullServiceIntent = new Intent(this, TimelinePullService.class);
		//onTaskDone() ;
	}
	
	
	
	@Override
	protected void onDestroy() {
		Log.d(App.TAG, "TimelineActivity.onDestroy");
		
		if (isFinishing()) {
			Log.d(App.TAG, "TimelineActivity is finishing");
			stopService(new Intent(this, TimelinePullService.class));
		}		
		
		_app.prefs().unregisterOnPreferenceChangeListener(this);
		
		super.onDestroy();
		
		if (_db != null)
			_db.close();
	}

	@Override
	protected void onResume() {
		Log.d(App.TAG, "TimelineActivity.onResume");
		super.onResume();
		
		_app.timelineAct = this;
		
		if (!_app.timelineRetrieved)
			refresh();
		
		/*if (_db == null) {
			_db = _app.db().openReadableDb(); 
			_cursor = _app.db().getAllStatus(_db);
			startManagingCursor(_cursor);
			
			SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
					R.layout.timeline_item, //layout
					_cursor,
					new String[] {TimelineContract.AUTHOR_NAME, TimelineContract.TEXT, TimelineContract.CREATED_AT },
					new int[] {R.id.tl_item_textUser, R.id.tl_item_textMessage, R.id.tl_item_textTime });
			
			adapter.setViewBinder(this);
			setListAdapter(adapter);
		}*/
		//if (_app.statusAdapter == null)
		//	refresh();
	}
	
	@Override
	protected void onPause() {
		Log.d(App.TAG, "TimelineActivity.onPause");
		super.onPause();
		_app.timelineAct = null;
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (_app.progressDialog != null &&_app.progressDialog.isShowing()) {
			_app.progressDialog.dismiss();
			_app.progressDialog = null;
        }
	}
	
	/** Initialize options menu */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		_generalMenu = new GeneralMenu(this, menu);
		_generalMenu.timeline().setVisible(false);
		_generalMenu.status().setVisible(true);
		_generalMenu.refresh().setVisible(true);
		_generalMenu.userInfo().setVisible(true);
		return true;
	}

	/** Process Item Menu selected */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {		
		return _generalMenu.processSelection(item) ? true : super.onOptionsItemSelected(item);
	}

	
	/**
	 * Aux methods
	 */
	
	public void refresh() {
		Log.d(App.TAG, "TimelineActivity.refresh");
		
		if (!hasRequiredPreferences()) {
			Log.d(App.TAG, "!!! DELETE THIS !!! TimelineActivity.refresh: Getting preferences");
			return;
		}

		Log.d(App.TAG, "TimelineActivity.refresh: Calling TimelinePullService");
		startService(_timelinePullServiceIntent);
	}

	private boolean hasRequiredPreferences() {
		if (_app.prefs().hasRequired())
			return true;
	
		Log.d(App.TAG, "Required preferences are missing");
		Utils.showToast(_app.context(), getString(R.string.fillRequiredPreferences));
		startActivity(new Intent(this, PrefsActivity.class));
		return false;
	}
			
	//new onTaskDone to work with TimelinePullService
	public void onTimelineRefreshed() {
		Log.d(App.TAG, "TimelineActivity.onTaskDone");
		Cursor c = _app.db().getAllStatus();
		c.close();
		
		/*_db = _app.db().openReadableDb(); 
		Cursor c = _app.db().getAllStatus(_db);
		startManagingCursor(c);
		
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
				R.layout.timeline_item, //layout
				c,
				new String[] {TimelineContract.AUTHOR_NAME, TimelineContract.TEXT, TimelineContract.CREATED_AT },
				new int[] {R.id.tl_item_textUser, R.id.tl_item_textMessage, R.id.tl_item_textTime });
		
		adapter.setViewBinder(this);
		setListAdapter(adapter);*/
		
		//setListAdapter(new TimelineAdapter(_app, this, _c));
		
		/*
		if (_app.statusAdapter == null) {
			Log.d(App.TAG, "TimelineActivity.onTaskDone: First time");
			_app.statusAdapter = new StatusAdapter(this, R.layout.timeline_item, timeline);
		}
		else {
			Log.d(App.TAG, "TimelineActivity.onTaskDone: Refresh statusAdapter");
			_app.statusAdapter.notifyDataSetChanged();
		}
		
		if (_app.progressDialog != null &&_app.progressDialog.isShowing()) {
			_app.progressDialog.dismiss();
			_app.progressDialog = null;
        }
		
		setListAdapter(_app.statusAdapter);*/
	}
	
	
	/**
	 * Interface implementations
	 */
	
	public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
		// Skip if this value doesn't need special treatment
		int id = view.getId();
		
		switch (id) {
		case R.id.tl_item_textTime:
			// Convert timestamp to relative date
			TextView statusAge = (TextView) view;
			long createdAt = cursor.getLong(cursor.getColumnIndex(TimelineContract.CREATED_AT));		
			statusAge.setText(DateUtils.getRelativeTimeSpanString(createdAt));
			break;
		case R.id.tl_item_textMessage:
			// Limit number of preview chars
			TextView status = (TextView) view;
			String statusText = cursor.getString(cursor.getColumnIndex(TimelineContract.TEXT));
			int previewChars = _app.prefs().previewChars(); 
			int length = (previewChars > statusText.length()) ? statusText.length() : previewChars;
			status.setText(statusText.substring(0, length));
			break;
		default:
			return false;
		}
		
		return true;
	}
	
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Log.d(App.TAG, "TimelineAdapter.onItemClick");
		
		// Show clicked status in DetailActivity
		Cursor c = (Cursor) parent.getItemAtPosition(position);
		
		Intent intent = new Intent(this, DetailActivity.class);
		intent.putExtra("detailTextUser", c.getString(c.getColumnIndex(TimelineContract.AUTHOR_NAME)));
		intent.putExtra("detailTextMessage", c.getString(c.getColumnIndex(TimelineContract.TEXT)));
		intent.putExtra("detailTextTime",  c.getLong(c.getColumnIndex(TimelineContract.CREATED_AT)));
		intent.putExtra("detailTextId", "#" + c.getLong(c.getColumnIndex(TimelineContract._ID)));
		
		intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);		
		startActivity(intent);		
	}

	public void onPreferenceChanged(Preferences prefs, String key, boolean sessionInvalidated) {
		Log.d(App.TAG, "TimelineActivity.onPreferenceChanged");
		if (sessionInvalidated) {
			//_app.statusAdapter = null;
			_app.timelineRetrieved = false;
			return;
		}
		/*if (key.equals("maxPosts")) {
			_app.twitter().setCount(prefs.maxPosts());
			return;
		}*/
		
		if (key.equals("previewChars")) {
			SimpleCursorAdapter adapter = (SimpleCursorAdapter) getListAdapter();
			adapter.notifyDataSetChanged();
			//_app.statusAdapter.notifyDataSetChanged();
		}
	}
	
}
