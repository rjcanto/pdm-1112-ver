package pt.isel.pdm.Yamba.database;

import java.util.ArrayList;
import java.util.List;

import pt.isel.pdm.Yamba.util.Utils;
import winterwell.jtwitter.Twitter.Status;
import android.app.Application;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.RemoteException;

public class Timeline {

	private final Application _app;
	private final ContentResolver _resolver;
	
	public Timeline(Application app) {
		_app = app;
		_resolver = app.getContentResolver();
		
	}
	
	/** Gets a cursor to access all of the Status in the database. */
	public Cursor getAllStatus() {
		Utils.Log("Timeline.getAllStatus");
		
		return _resolver.query(
				TimelineProvider.TIMELINE_URI,				
				null, null, null,
				TimelineContract.CREATED_AT + " DESC");
	}
	
	/** Inserts a collection of Status into the database. */
	public void insertStatus(List<Status> timeline) {
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
				client.insert(ContentUris.withAppendedId(TimelineProvider.TIMELINE_URI, status.id), values);
			}
		}
		catch (RemoteException re) {
			Utils.Log(String.format("Timeline.insertStatus exception: ", re.getMessage()));
		}
		finally {
			client.release();
		}
	}
	
	public List<String> getPendingStatus() {
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
	
	public void insertPendingStatus(String msg) {
		ContentValues values = new ContentValues();
		values.put(PendingContract.TEXT, msg);
		_resolver.insert(TimelineProvider.PENDING_URI, values);		
	}
}
