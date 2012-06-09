package pt.isel.pdm.Yamba.providers;

import pt.isel.pdm.Yamba.util.Utils;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteCursorDriver;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteQuery;

final class SmartCursorFactory implements CursorFactory {
    
	private SmartDbHelper _smartDbHelper;
	
    public Cursor newCursor(SQLiteDatabase db, SQLiteCursorDriver driver, String table, SQLiteQuery query) {
    	Utils.Log("SmartCursorFactory: new cursor " + toString());
    	if (_smartDbHelper == null)
    		throw new IllegalAccessError("SmartDbHelper not set");
    	
        return new SmartCursor(db, driver, table, query, _smartDbHelper);
    }
    
    public void setSmartDbHelper(SmartDbHelper smartDbHelper) {
		_smartDbHelper = smartDbHelper;
	}
}

final class SmartCursor extends SQLiteCursor {

	final private SmartDbHelper _smartDbHelper;

    public SmartCursor(
    		SQLiteDatabase db, SQLiteCursorDriver driver, String table, SQLiteQuery query,
    		SmartDbHelper smartDbHelper) {
    	
        super(db, driver, table, query);
        
        Utils.Log("SmartCursor: new cursor " + toString());
        _smartDbHelper = smartDbHelper;
    }

    /**
     * Closes the cursor and the associated database, if no more cursors are using it.
     */
    @Override
    public void close() {
    	Utils.Log("SmartCursor.close: Closing cursor " + toString());
        super.close();        
        _smartDbHelper.close();
    }

}
