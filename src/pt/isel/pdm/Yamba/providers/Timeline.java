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

	private final ContentResolver _resolver;
	private final ContentValues _values;
	private final String[] _isReadColumn, _maxIdColumn, _idColumn;
	
	public Timeline(Application app) {
		_resolver = app.getContentResolver();
		_values = new ContentValues();
		_isReadColumn = new String[] { TimelineContract.IS_READ };
		_maxIdColumn = new String[] { String.format("max(%s)", TimelineContract._ID) };
		_idColumn = new String[] { TimelineContract._ID };
	}
	
	
	/** 
	 * Creates a cursor to access all of the Status in the database. 
	 * Don't call on UI thread. 
	 **/
	public synchronized Cursor getTimeline(int count) {
		Utils.Log("Timeline.getAllStatus");
		
		return _resolver.query(
				TimelineProvider.TIMELINE_URI,				
				null, null, null,
				String.format("%s DESC LIMIT %d", TimelineContract.CREATED_AT, count));
	}
	
	/** Inserts a collection of Status into the database. Don't call on UI thread */
	public synchronized int insertStatus(List<Status> timeline) {
		Utils.Log(String.format("Timeline.insertStatus (%d)", timeline.size()));
		
		ContentProviderClient client = _resolver.acquireContentProviderClient(TimelineProvider.AUTHORITY);
		
		try {						
			for (Status status : timeline) {
				_values.clear();
				_values.put(TimelineContract._ID, status.id);
				_values.put(TimelineContract.CREATED_AT, status.createdAt.getTime());
				_values.put(TimelineContract.AUTHOR_ID, status.user.id);
				_values.put(TimelineContract.AUTHOR_NAME, status.user.name);
				_values.put(TimelineContract.IS_READ, false);
				_values.put(TimelineContract.TEXT, status.text);
				client.insert(TimelineProvider.TIMELINE_URI, _values);
			}
		}
		catch (RemoteException re) {
			Utils.Log(String.format("Timeline.insertStatus exception: ", re.getMessage()));
		}
		finally {
			client.release();
		}
		
		return timeline.size();
	}
	
	public synchronized List<String> getPendingStatus() {
		Utils.Log("Timeline.getPendingStatus");
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
		Utils.Log("Timeline.insertPendingStatus");
		_values.clear();
		_values.put(PendingContract.TEXT, msg);
		_resolver.insert(TimelineProvider.PENDING_URI, _values);		
	}
	
	public synchronized void setRead(long statusId) {
		Utils.Log("Timeline.setRead");
		_values.clear();
		_values.put(TimelineContract.IS_READ, true);
		_resolver.update(TimelineProvider.TIMELINE_URI, _values, TimelineContract._ID + "=" + statusId, null);
	}
	
	public synchronized int getUnreadStatusCount() {
		Utils.Log("Timeline.getUnreadStatusCount");
		Cursor c = _resolver.query(
				TimelineProvider.TIMELINE_URI,				
				_isReadColumn, TimelineContract.IS_READ + "=0",
				null, null);
		int count = c.getCount();
		c.close();
		return count;
	}
	
	public synchronized Long getMostRecentStatusId() {
		Utils.Log("Timeline.getMostRecentStatusId");
		Cursor c = _resolver.query(
				TimelineProvider.TIMELINE_URI,				
				_maxIdColumn, null,	null, null);
		c.moveToFirst();
		
		if (c.isAfterLast())
			return null;

		long id = c.getLong(0);
		c.close();
		return id;
	}
	
	public synchronized void deleteOlderMessages(int keepLatest) {
		Utils.Log("Timeline.deleteOlderMessages");		
		Cursor c = _resolver.query(
				TimelineProvider.TIMELINE_URI,				
				_idColumn,
				null, null,
				String.format("%s DESC LIMIT 1 OFFSET %d", TimelineContract.CREATED_AT, keepLatest));
				
		c.moveToFirst();
		if (c.isAfterLast())
			return;
		
		long id = c.getLong(0);
		c.close();
		
		_resolver.delete(TimelineProvider.TIMELINE_URI, String.format("%s <= %d", TimelineContract._ID, id), null);
	}
}
