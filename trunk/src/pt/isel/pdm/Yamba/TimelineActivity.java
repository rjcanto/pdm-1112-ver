package pt.isel.pdm.Yamba;

import java.util.Date;
import java.util.List;

import winterwell.jtwitter.Twitter;
import winterwell.jtwitter.Twitter.Status;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class TimelineActivity 
	extends ListActivity
	implements OnPreferenceChangeListener {

	private App _app;
	private GeneralMenu _generalMenu;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(App.TAG, "TimelineActivity.onCreate");
		super.onCreate(savedInstanceState);
		_app = (App) getApplication() ;	
		_app._timelineAct = this ;
		setContentView(R.layout.timeline);
		
		if(_app._timelineResult != null)
			this.onTaskDone(_app._timelineResult) ;
		
	/*	// Show current status list, if any
		if (_app.statusAdapter != null)
			setListAdapter(_app.statusAdapter);
		
		// Was the screen rotated while retrieving timeline?
		List<Twitter.Status> timelineResult = _app.timelineResult;
		if (timelineResult == null)
			return;
		
		Log.d(App.TAG, "TimelineActivity.onCreate: screen was rotated while retrieving timeline");
		if (timelineResult.isDone()) {
			Log.d(App.TAG, "TimelineActivity.onCreate: task is done, process results"); 
			onTaskDone(timelineResult);
		}
		else {
			Log.d(App.TAG, "TimelineActivity.onCreate: receive notification when task is complete");
			_app.progressDialog = ProgressDialog.show(TimelineActivity.this, "", getString(R.string.tl_dialog_message),true);
			timelineResult.setOnAsyncTaskDone(this);
		}
		*/
		
		/*
		Log.d(App.TAG, "TimelineActivity.onCreate: Calling TimelinePullService");
		Intent intent = new Intent(this, TimelinePullService.class);
		startService(intent);
		*/
	}
	
	
	
	@Override
	protected void onDestroy() {
		Log.d(App.TAG, "TimelineActivity.onDestroy");
		_app._timelineAct = null ;
		super.onDestroy();
	}



	@Override
	protected void onResume() {
		Log.d(App.TAG, "TimelineActivity.onResume");
		super.onResume();
		_app._timelineAct = this ;
		if (_app.statusAdapter == null)
			refresh();
	}
	
	@Override
	protected void onPause() {
		Log.d(App.TAG, "TimelineActivity.onPause");
		_app._timelineAct = null ;
		super.onPause();
	}



	@Override
	protected void onStop() {
		super.onStop();
		if (_app.progressDialog != null &&_app.progressDialog.isShowing()) {
			_app.progressDialog.dismiss();
			_app.progressDialog = null;
        }
	}

	public void refresh() {
		Log.d(App.TAG, "TimelineActivity.refresh");
		
		if (!_app.prefs().hasRequired()) {
			Log.d(App.TAG, "Required preferences are missing");
			Utils.showToast(_app.context(), getString(R.string.fillRequiredPreferences));
			startActivity(new Intent(this, PrefsActivity.class));
			return;
		}
		
		Log.d(App.TAG, "TimelineActivity.refresh: Calling TimelinePullService");
		Intent intent = new Intent(this, TimelinePullService.class);
		startService(intent);
//		
//		TimelineTask task = new TimelineTask(this);
//		_app.timelineResult = task;
//		task.execute();
	}
	
	
	/** Initialize options menu */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		_generalMenu = new GeneralMenu(this, menu);
		_generalMenu.timeline().setVisible(false);
		_generalMenu.status().setVisible(true);
		_generalMenu.refresh().setVisible(true);
		return true;
	}

	/** Process Item Menu selected */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {		
		return _generalMenu.processSelection(item) ? true : super.onOptionsItemSelected(item);
	}
		
	//new onTaskDone to work with TimelinePullService
	public void onTaskDone(List<Twitter.Status> timeline) {
		Log.d(App.TAG, "TimelineActivity.onTaskDone");
		
		
	
		if (_app.statusAdapter == null) {
			Log.d(App.TAG, "TimelineActivity.onTaskDone: First time");
			_app.statusAdapter = new StatusAdapter(this, R.layout.timeline_item, timeline);
		}
		else {
			Log.d(App.TAG, "TimelineActivity.onTaskDone: Refresh statusAdapter");
			_app.statusAdapter.clear();
			for (Status status : timeline)
				_app.statusAdapter.add(status);
		}
		
		if (_app.progressDialog != null &&_app.progressDialog.isShowing()) {
			_app.progressDialog.dismiss();
			_app.progressDialog = null;
        }
		
		setListAdapter(_app.statusAdapter);
	}
	
	class StatusAdapter extends ArrayAdapter<Twitter.Status> implements OnClickListener {		
		int _position ;
		
		public StatusAdapter(Context context, int textViewResourceId, List<Twitter.Status> objects) {
			super(context, textViewResourceId, objects);
		}			
			
   		@Override
   		public View getView(int position, View convertView, ViewGroup parent) {
   			_position = position ;
   			
   			if (null == convertView) {
   				LayoutInflater mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
   				convertView = mInflater.inflate(R.layout.timeline_item, null);
   			}	
   			TextView tvUser = (TextView) convertView.findViewById(R.id.tl_item_textUser);
   			tvUser.setText(getItem(position).getUser().toString());

   			TextView tvTime = (TextView) convertView.findViewById(R.id.tl_item_textTime);
   			Date itemDate = getItem(position).getCreatedAt();
   			tvTime.setText(dateToAge(itemDate));

   			TextView tvMessage = (TextView) convertView.findViewById(R.id.tl_item_textMessage);
   			
   			String msg = getItem(position).getText();
   			int previewChars = _app.prefs().previewChars(); 
   			int length = (previewChars > msg.length()) ? msg.length() : previewChars;
   			tvMessage.setText(msg.substring(0, length));
   			convertView.setOnClickListener(this);
   			return convertView;
   		}
   			

		public void onClick(View v) {
			Log.d(App.TAG, "TimelineActivity.onItemClick");
			Intent intent = new Intent(v.getContext(), DetailActivity.class);
			Status item = (Status) this.getItem(_position) ;
			intent.putExtra("detailTextUser", item.getUser().toString());
			intent.putExtra("detailTextMessage", item.getText());
			intent.putExtra("detailTextTime", item.getCreatedAt().toGMTString());
			intent.putExtra("detailTextId", "#"+String.valueOf(item.getId()));
			intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			
			startActivity(intent);
			
		}   	
	}

	
	
	public void onPreferenceChanged(Preferences prefs, String key, boolean sessionInvalidated) {
		Log.d(App.TAG, "TimelineActivity.onPreferenceChanged");
		if (sessionInvalidated) {
			_app.statusAdapter = null;
			return;
		}
		if (key == "maxPosts") {
			_app.twitter().setCount(prefs.maxPosts());
			return;
		}
		
	}
	
	public String dateToAge(Date date) {
		final int secondsInADay = 60 * 60 * 24;
		
		Date now = new Date();
		long days = (now.getTime() - date.getTime()) / secondsInADay;
		
		if (days == 0)
			return getString(R.string.today);
		if (days == 1)
			return getString(R.string.yesterday);
		return getString(R.string.daysAgo, days);
	}
}
