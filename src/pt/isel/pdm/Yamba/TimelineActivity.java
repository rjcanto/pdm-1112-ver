package pt.isel.pdm.Yamba;

import java.util.Date;
import java.util.List;

import winterwell.jtwitter.Twitter;
import android.app.ListActivity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class TimelineActivity extends ListActivity {

	App _app;
	ListView listStatusView ;
	ArrayAdapter<Twitter.Status> listStatusAdapter;
	LayoutInflater mInflater;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.timeline);
		_app = (App) getApplication() ;
		mInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		new GetTimelineTask(this).execute();
	}
	

	


	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub
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
	        				 item = mInflater.inflate(R.layout.timeline_item, null);
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
		}

		@Override
		protected Void doInBackground(Void... params) {
			_list = _app.twitter().getUserTimeline();
			return null ;
			
		}
	}
}
