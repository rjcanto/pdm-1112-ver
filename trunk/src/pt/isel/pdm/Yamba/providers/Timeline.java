package pt.isel.pdm.Yamba.providers;

import java.util.ArrayList;
import java.util.List;

import pt.isel.pdm.Yamba.util.Utils;
import winterwell.jtwitter.Twitter.Status;
import android.app.Application;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.RemoteException;

public class Timeline {

	//private final Application _app;
	private final ContentResolver _resolver;
	Cursor timelineCursor;
	
	public Timeline(Application app) {
		//_app = app;
		_resolver = app.getContentResolver();
		
	}
	
	/** Gets the latest retrieved timeline, if any. Safe to call on UI thread */
	public synchronized Cursor getCachedTimeline() {
		if (timelineCursor != null && timelineCursor.isClosed())
			timelineCursor = null;
		return timelineCursor;
	}
	
	/** 
	 * Creates a cursor to access all of the Status in the database. 
	 * Don't call on UI thread 
	 **/
	public synchronized Cursor getTimeline() {
		Utils.Log("Timeline.getAllStatus");
		
		return timelineCursor = _resolver.query(
				TimelineProvider.TIMELINE_URI,				
				null, null, null,
				TimelineContract.CREATED_AT + " DESC");
	}
	
	/** Inserts a collection of Status into the database. Don't call on UI thread */
	public synchronized void insertStatus(List<Status> timeline) {
		Utils.Log(String.format("Timeline.insertStatus (%d)", timeline.size()));
		
		ContentProviderClient client = _resolver.acquireContentProviderClient(TimelineProvider.AUTHORITY);
		ContentValues values = new ContentValues();
		
		try {						
			for (Status status : timeline) {
				values.clear();
				values.put(TimelineContract._ID, status.id);
				values.put(TimelineContract.CREATED_AT, status.createdAt.getTime());
				values.put(TimelineContract.AUTHOR_ID, status.user.id);
				values.put(TimelineContract.AUTHOR_NAME, status.user.name);
				values.put(TimelineContract.IS_READ, true);
				values.put(TimelineContract.TEXT, status.text);
				client.insert(TimelineProvider.TIMELINE_URI, values);
			}
		}
		catch (RemoteException re) {
			Utils.Log(String.format("Timeline.insertStatus exception: ", re.getMessage()));
		}
		finally {
			client.release();
		}
		
		
	}
	
	public synchronized List<String> getPendingStatus() {
		List<String> result = new ArrayList<String>();
		
		Cursor statusCursor = _resolver.query(TimelineProvider.PENDING_URI,
				null, null, null, null); 
		
		statusCursor.moveToFirst();
        while (statusCursor.isAfterLast() == false) {
        	result.add(statusCursor.getString(statusCursor.getColumnIndex(PendingContract.TEXT)));
       	    statusCursor.moveToNext();
        }
		_resolver.delete(TimelineProvider.PENDING_URI, null, null);
		statusCursor.close() ;
		return result;
	}
	
	public synchronized void insertPendingStatus(String msg) {
		ContentValues values = new ContentValues();
		values.put(PendingContract.TEXT, msg);
		_resolver.insert(TimelineProvider.PENDING_URI, values);		
	}
}
