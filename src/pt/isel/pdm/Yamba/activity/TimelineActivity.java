package pt.isel.pdm.Yamba.activity;


import pt.isel.pdm.Yamba.App;
import pt.isel.pdm.Yamba.R;
import pt.isel.pdm.Yamba.providers.TimelineContract;
import pt.isel.pdm.Yamba.services.DbService;
import pt.isel.pdm.Yamba.services.TimelinePullService;
import pt.isel.pdm.Yamba.util.*;
import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.TextView;

public class TimelineActivity 
	extends ListActivity
	implements OnPreferenceChangeListener, ViewBinder, OnItemClickListener {

	private final static int UNREAD_COLOR = 0xFFFFFFFF;
	
	private App _app;
	private GeneralMenu _generalMenu;
	private SQLiteDatabase _db;
	private Cursor _cursor;
	private Intent _timelinePullServiceIntent, _dbServiceIntent, _detailIntent;
	
	
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
		_dbServiceIntent = new Intent(this, DbService.class);
		_detailIntent = new Intent(this, DetailActivity.class);
	}
	
	
	
	@Override
	protected void onDestroy() {
		Log.d(App.TAG, "TimelineActivity.onDestroy");
		
		if (isFinishing()) {
			Log.d(App.TAG, "TimelineActivity is finishing");
			if (_cursor != null)
				_cursor.close();
			_app.timelineRetrieved = false;
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
		else {
			_dbServiceIntent.putExtra(DbService.EXTRA_ACTIONS, DbService.ACTION_REFRESH_TIMELINE);
			startService(_dbServiceIntent);
		}
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
			Log.d(App.TAG, "TimelineActivity.refresh: Getting preferences");
			return;
		}

		Log.d(App.TAG, "TimelineActivity.refresh: starting TimelinePullService");
		
		if (_app.prefs().autoRefresh() && _app.isWifiAvailable()) {
			_app.setAutoUpdate(0);
		}
		else startService(_timelinePullServiceIntent);
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
	public void onTimelineRefreshed(Cursor c) {
		
		if (c == null) {
			Utils.Log("TimelineActivity.onTimelineRefreshed. Cache miss!");
			return;
		}
		
		Utils.Log(String.format("TimelineActivity.onTimelineRefreshed. Rows: %d", c.getCount()));

		if (_app.timelineAdapter == null) {
			_app.timelineAdapter = new SimpleCursorAdapter(this,
				R.layout.timeline_item, //layout
				c,
				new String[] {TimelineContract.AUTHOR_NAME, TimelineContract.TEXT, TimelineContract.CREATED_AT },
				new int[] {R.id.tl_item_textUser, R.id.tl_item_textMessage, R.id.tl_item_textTime });
			_app.timelineAdapter.setViewBinder(this);
		}
		else
			_app.timelineAdapter.changeCursor(c); // Closes previous cursor

		setListAdapter(_app.timelineAdapter);
		
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
			boolean isRead = cursor.getInt(cursor.getColumnIndex(TimelineContract.IS_READ)) == 1;
			if (!isRead) {
				status.setTypeface(Typeface.DEFAULT_BOLD);
				status.setTextColor(UNREAD_COLOR);				
			}
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
	
	public void onItemClick(AdapterView<?> parent, View view, int position, long rowId) {
		Log.d(App.TAG, "TimelineAdapter.onItemClick");
		
		// Show clicked status in DetailActivity
		Cursor c = (Cursor) parent.getItemAtPosition(position);
		long id = c.getLong(c.getColumnIndex(TimelineContract._ID));
		
		boolean isRead = c.getInt(c.getColumnIndex(TimelineContract.IS_READ)) == 1;
		if (!isRead) {
			// Mark as read
			_dbServiceIntent.putExtra(DbService.EXTRA_ACTIONS, DbService.ACTION_SET_STATUS_READ);
			_dbServiceIntent.putExtra(DbService.EXTRA_ID, id);
			startService(_dbServiceIntent);
		}
		
		_detailIntent.putExtra("detailTextUser", c.getString(c.getColumnIndex(TimelineContract.AUTHOR_NAME)));
		_detailIntent.putExtra("detailTextMessage", c.getString(c.getColumnIndex(TimelineContract.TEXT)));
		_detailIntent.putExtra("detailTextTime",  c.getLong(c.getColumnIndex(TimelineContract.CREATED_AT)));
		_detailIntent.putExtra("detailTextId", "#" + id);
		
		_detailIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);		
		startActivity(_detailIntent);		
	}

	public void onPreferenceChanged(Preferences prefs, String key, boolean sessionInvalidated) {
		Log.d(App.TAG, "TimelineActivity.onPreferenceChanged");
		if (sessionInvalidated) {
			_app.timelineRetrieved = false;
			return;
		}
		
		if (key.equals("previewChars")) {
			SimpleCursorAdapter adapter = (SimpleCursorAdapter) getListAdapter();
			adapter.notifyDataSetChanged();
		}
	}
	
}
