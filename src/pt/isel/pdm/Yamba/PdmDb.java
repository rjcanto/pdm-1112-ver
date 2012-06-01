package pt.isel.pdm.Yamba;

import java.util.List;

import winterwell.jtwitter.Twitter;
import winterwell.jtwitter.Twitter.Status;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/** 
 * Data Access Layer 
 */
public class PdmDb {
	
	private static final String DB_NAME = "pdm.db";
	private static final int DB_VERSION = 1;
	private DbHelper _dbHelper;

	
	private class DbHelper extends SQLiteOpenHelper {
		
		public DbHelper(Context ctx) {
			super(ctx, DB_NAME, null, DB_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			String columns = TimelineContract._ID + " bigint primary key, "
					       + TimelineContract.AUTHOR_ID + " bigint not null, " // foreign key?
					       + TimelineContract.AUTHOR_NAME + " text not null"
					       + TimelineContract.CREATED_AT + " datetime not null, "
					       + TimelineContract.TEXT + " text not null, "
					       + TimelineContract.IS_READ + " boolean not null";
			String sql = "CREATE TABLE "+ TimelineContract.TABLE + "( "+ columns + " )";
			db.execSQL(sql);
			Utils.Log("DbHelper.onCreate: sql = " + sql);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE if exists " + TimelineContract.TABLE); 
			Utils.Log("DbHelper.onUpgrade");
			onCreate(db);		
		}
	}
	
	
	public PdmDb(Context ctx) {
		_dbHelper = new DbHelper(ctx);
		Utils.Log("PdmDb: ready");
	}
	
	
	/** Gets a cursor to access all of the Status in the database. */
	public Cursor getAllStatus() {
		Utils.Log("PdmDb.getAllStatus");
		SQLiteDatabase db = _dbHelper.getReadableDatabase();
		return db.query(
				TimelineContract.TABLE,
				null, null, null, null, null,
				TimelineContract.CREATED_AT + " DESC");
	}

	
	/** Inserts a collection of Status into the database. */
	public void insertStatus(List<Status> timeline) {
		Utils.Log(String.format("PdmDb.insertStatus (%d)", timeline.size()));
		SQLiteDatabase db = _dbHelper.getWritableDatabase();
		try { 
			ContentValues values = new ContentValues();			
			for (Status status : timeline)
				insertStatus(db, values, status);
		}
		finally {
			db.close();
		}
	}
	
	/** Inserts a Status into the database. */
	public void insertStatus(Status status, Boolean isNew) {
		Utils.Log(String.format("PdmDb.insertStatus (%s)", isNew ? "new" : "not new"));
		SQLiteDatabase db = _dbHelper.getWritableDatabase();
		try {
			insertStatus(db, new ContentValues(), status);
		}
		finally {
			db.close();
		}
	}
	
	private void insertStatus(SQLiteDatabase db, ContentValues values, Status status) {
		values.clear();
		values.put(TimelineContract._ID, status.id);
		values.put(TimelineContract.CREATED_AT, status.createdAt.getTime());
		values.put(TimelineContract.AUTHOR_ID, status.user.id);
		values.put(TimelineContract.AUTHOR_NAME, status.user.name);
		values.put(TimelineContract.IS_READ, true);
		values.put(TimelineContract.TEXT, status.text);
		db.insertWithOnConflict(TimelineContract.TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE);
	}
}
