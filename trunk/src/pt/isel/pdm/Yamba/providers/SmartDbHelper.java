package pt.isel.pdm.Yamba.providers;

import pt.isel.pdm.Yamba.util.Utils;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * An SQLiteOpenHelper that knows how to close the database when all connections are closed.
 */
abstract class SmartDbHelper extends SQLiteOpenHelper {
	private int _openConnections;

	public SmartDbHelper(Context context, String name, CursorFactory factory, int version) {
		super(context, name, factory, version);
	}

	/**
     * Closes any database object, if all connections were closed.
     */
	@Override
	public synchronized void close() {
		// Return immediately if no connections are open or 
		// if there are open connections after this close operation. 
		if (_openConnections == 0 || --_openConnections != 0)
			return;
		
		Utils.Log("DbHelper.close: All connections closed, closing database");
		try {			
			super.close();						
		}
		catch (IllegalStateException ie) {
			// Error closing the database
			++_openConnections;
			throw ie;
		}
	}

	@Override
	public void onOpen(SQLiteDatabase db) {
		super.onOpen(db);
		++_openConnections;
	}
}
