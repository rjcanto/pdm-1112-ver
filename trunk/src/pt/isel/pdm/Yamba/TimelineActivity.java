package pt.isel.pdm.Yamba;

import java.util.Date;
import java.util.List;

import winterwell.jtwitter.Twitter;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class TimelineActivity extends ListActivity{

	App _app;
	LayoutInflater mInflater;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(App.TAG, "TimelineActivity.onCreate");
		super.onCreate(savedInstanceState);
		_app = (App) getApplication() ;	
		mInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		ensureRequiredPreferences();		
		setContentView(R.layout.timeline);
		new GetTimelineTask(this).execute();			
	}
	
	
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		ensureRequiredPreferences();
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
	
	private class GetTimelineTask extends AsyncTask<Void, Void, Void> {
		List<winterwell.jtwitter.Twitter.Status> _list ;
		Context _context;
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
		protected void onPostExecute(Void res) {
			super.onPostExecute(res);
			
			if (_dialog.isShowing()) {
	            _dialog.dismiss();
	        }
			
			setListAdapter(new ArrayAdapter<Twitter.Status>(
	    			_context,
	    			R.layout.timeline_item,
	    			_list
	    		) {
	        		 @Override
	        		 public View getView(int position, View convertView, ViewGroup parent) {
	        			 View item;
	    		 
	        			 if (null == convertView) {
	        				 item = mInflater.inflate(R.layout.timeline_item, null);
	        			 } else {
	        				 item = convertView;
	        			 }
	        			 TextView tvUser = (TextView) item.findViewById(R.id.tl_item_textUser);
	        			 tvUser.setText(getItem(position).getUser().toString());
	     
	        			 TextView tvTime = (TextView) item.findViewById(R.id.tl_item_textTime);
	        			 //tvTime.setText(getItem(position).getCreatedAt().toGMTString());
	        			 Date itemTime = getItem(position).getCreatedAt() ;
	        			 tvTime.setText(
	        					 String.valueOf(itemTime.getDate()) + 
	        					 String.valueOf(itemTime.getMonth())
	        			 );
	        			 
	        			 TextView tvMessage = (TextView) item.findViewById(R.id.tl_item_textMessage);
	        			 tvMessage.setText(getItem(position).getText());
	        			 
	        			 return item;
	        		 }
	        	}
	        );
		}

		@Override
		protected Void doInBackground(Void... params) {
			_list = _app.twitter().getUserTimeline();
			return null ;
			
		}
	}
}