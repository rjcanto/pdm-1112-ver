package pt.isel.pdm.Yamba;

import java.util.Date;
import java.util.List;

import winterwell.jtwitter.Twitter;
import winterwell.jtwitter.TwitterException;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class TimelineActivity extends ListActivity implements OnPreferenceChangeListener {

	private App _app;
	private GeneralMenu _generalMenu;
	private StatusAdapter _statusAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(App.TAG, "TimelineActivity.onCreate");
		super.onCreate(savedInstanceState);
		_app = (App) getApplication() ;	
		setContentView(R.layout.timeline);
	}
	
	@Override
	protected void onResume() {
		Log.d(App.TAG, "TimelineActivity.onResume");
		super.onResume();
		if (_statusAdapter == null)
			refresh();		
	}

	public void refresh() {
		Log.d(App.TAG, "TimelineActivity.refresh");
		if (_app.prefs().hasRequired())
			new GetTimelineTask(this).execute();		
		else {
			Log.d(App.TAG, "Required preferences are missing");
			Utils.showToast(_app.context(), getString(R.string.fillRequiredPreferences));
			startActivity(new Intent(this, PrefsActivity.class));
		}
	}
	
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		// TODO Auto-generated method stub
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
	
	private class GetTimelineTask extends AsyncTask<Void, Void, List<Twitter.Status>> {
		private final Context _context;
		private Throwable _error;
		ProgressDialog _dialog;
		
		public GetTimelineTask(Context context) {
			_context = context ;
		}		
		
		@Override
        protected void onPreExecute() {
                super.onPreExecute();
                _dialog = ProgressDialog.show(_context, "", getString(R.string.tl_dialog_message),true);
        }
		
		@Override
		protected void onPostExecute(List<Twitter.Status> list) {
			Log.d(App.TAG, "GetTimelineTask.onPostExecute");
			if (_dialog.isShowing()) {
                _dialog.dismiss();
            }
                    
			if (_error != null) {
				Log.e(App.TAG, "Error: " + _error.getMessage());
				Utils.showToast(_app.context(), getString(R.string.connectionError));
				return;
			}
		
			_statusAdapter = new StatusAdapter(_context, R.layout.timeline_item, list);
			setListAdapter(_statusAdapter);
		}

		@Override
		protected List<Twitter.Status> doInBackground(Void... params) {
			Log.d(App.TAG, "GetTimelineTask.doInBackground");
			try {
				return _app.twitter().getUserTimeline();
			}
			catch (TwitterException te) {
				_error = te;
			}
			return null;			
		}		
		
	}
	
	private class StatusAdapter extends ArrayAdapter<Twitter.Status> {		
		
		public StatusAdapter(Context context, int textViewResourceId, List<Twitter.Status> objects) {
			super(context, textViewResourceId, objects);					
		}			
			
   		@Override
   		public View getView(int position, View convertView, ViewGroup parent) {
   			View item;
		 
   			if (null == convertView) {
   				LayoutInflater mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
   				item = mInflater.inflate(R.layout.timeline_item, null);
   			} else {
   				item = convertView;
   			}
   			TextView tvUser = (TextView) item.findViewById(R.id.tl_item_textUser);
   			tvUser.setText(getItem(position).getUser().toString());

   			TextView tvTime = (TextView) item.findViewById(R.id.tl_item_textTime);
   			Date itemDate = getItem(position).getCreatedAt();
   			tvTime.setText(dateToAge(itemDate));

   			TextView tvMessage = (TextView) item.findViewById(R.id.tl_item_textMessage);
   			tvMessage.setText(getItem(position).getText());

   			return item;
   		}   	
	}

	public void onPreferenceChanged(Preferences sp, String key, boolean sessionInvalidated) {
		Log.d(App.TAG, "TimelineActivity.onPreferenceChanged");
		if (sessionInvalidated)
			_statusAdapter = null;
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
