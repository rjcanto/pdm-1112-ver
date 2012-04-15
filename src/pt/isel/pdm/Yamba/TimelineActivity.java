package pt.isel.pdm.Yamba;

import java.util.Date;
import java.util.List;

import winterwell.jtwitter.Twitter;
import android.app.ListActivity;
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
import android.widget.ListView;
import android.widget.TextView;

public class TimelineActivity extends ListActivity{

	private App _app;
	private LayoutInflater _mInflater;
	private GeneralMenu _generalMenu;
	private boolean _timelineLoaded;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(App.TAG, "TimelineActivity.onCreate");
		super.onCreate(savedInstanceState);
		_app = (App) getApplication() ;	
		_mInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		ensureRequiredPreferences();		
		setContentView(R.layout.timeline);
		
		if (_app.prefs().hasRequired())
			new GetTimelineTask(this).execute();
		else {
			Log.d(App.TAG, "Required preferences are missing");
			Utils.showToast(_app.context(), getString(R.string.fillRequiredPreferences));
			startActivity(new Intent(this, PrefsActivity.class));
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if (_app.prefs().hasRequired() && !_timelineLoaded)
			new GetTimelineTask(this).execute();
	}




	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub
	}


	/** Ensure required preferences are filled */
	private void ensureRequiredPreferences() {		
		if (_app.prefs().hasRequired())
			return;		
		Log.d(App.TAG, "Required preferences are missing");
		Utils.showToast(_app.context(), getString(R.string.fillRequiredPreferences));
		startActivity(new Intent(this, PrefsActivity.class));	
	}
	
	/** Initialize options menu */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		//getMenuInflater().inflate(R.menu.general, m);
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
	
	private class GetTimelineTask extends AsyncTask<Void, Void, Void> {
		List<winterwell.jtwitter.Twitter.Status> _list ;
		Context _context;
		
		public GetTimelineTask(Context context) {
			_context = context ;
		}
		
		
		@Override
		protected void onPostExecute(Void res) {
			// TODO Auto-generated method stub
			//super.onPostExecute(result);
			setListAdapter(new ArrayAdapter<Twitter.Status>(
	    			_context,
	    			R.layout.timeline_item,
	    			_list
	    		) {
	        		 @Override
	        		 public View getView(int position, View convertView, ViewGroup parent) {
	        			 View item;
	    		 
	        			 if (null == convertView) {
	        				 item = _mInflater.inflate(R.layout.timeline_item, null);
	        			 } else {
	        				 item = convertView;
	        			 }
	        			 TextView tvUser = (TextView) item.findViewById(R.id.tl_item_textUser);
	        			 tvUser.setText(getItem(position).getUser().toString());
	     
	        			 TextView tvTime = (TextView) item.findViewById(R.id.tl_item_textTime);
	        			 Date itemDate = getItem(position).getCreatedAt();
	        			 tvTime.setText(
	        					 //itemDate.toString()
	        					 /**
	        					 String.valueOf(itemDate.getDate()) +
	        					 String.valueOf(itemDate.getMonth())
	        					 /**/
	        					 itemDate.toGMTString()
						 );
	        			 
	        			 TextView tvMessage = (TextView) item.findViewById(R.id.tl_item_textMessage);
	        			 tvMessage.setText(getItem(position).getText());
	        			 
	        			 return item;
	        		 }
	        	}
	        );
			
			_timelineLoaded = true;
		}

		@Override
		protected Void doInBackground(Void... params) {
			_list = _app.twitter().getUserTimeline();
			return null ;
			
		}
	}
	

}
