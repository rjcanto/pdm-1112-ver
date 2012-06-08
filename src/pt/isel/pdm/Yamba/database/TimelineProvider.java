package pt.isel.pdm.Yamba.database;

import pt.isel.pdm.Yamba.util.Utils;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public class TimelineProvider extends ContentProvider {

	private final static class DbHelper extends SmartDbHelper {
		private static final String DB_NAME = "pdm.db";
		private static final int DB_VERSION = 1;
		
		public static DbHelper createDbHelper(Context context) {
			Utils.Log("DbHelper.createDbHelper");
			SmartCursorFactory scf = new SmartCursorFactory();
			DbHelper dbHelper = new DbHelper(context, scf);
			scf.setSmartDbHelper(dbHelper);
			return dbHelper;
		}

		private DbHelper(Context ctx, SmartCursorFactory smartCursorFactory) {
			super(ctx, DB_NAME, smartCursorFactory, DB_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			String columns = TimelineContract._ID + " bigint primary key, "
					+ TimelineContract.AUTHOR_ID + " bigint not null, " // foreign key?
					+ TimelineContract.AUTHOR_NAME + " text not null, "
					+ TimelineContract.CREATED_AT + " datetime not null, "
					+ TimelineContract.TEXT + " text not null, "
					+ TimelineContract.IS_READ + " boolean not null";
			String sql = "CREATE TABLE "+ TimelineContract.TABLE + "( "+ columns + " )";			
			Utils.Log("DbHelper.onCreate: sql = " + sql);
			db.execSQL(sql);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE if exists " + TimelineContract.TABLE); 
			Utils.Log("DbHelper.onUpgrade");
			onCreate(db);		
		}
	}
	
	public static final String AUTHORITY = "pt.isel.pdm.Yamba.providers";
	public static final Uri TIMELINE_URI =
			Uri.parse(ContentResolver.SCHEME_CONTENT+"://" + AUTHORITY + TimelineContract.TABLE);
	public static final Uri PENDING_URI =
			Uri.parse(ContentResolver.SCHEME_CONTENT+"://" + AUTHORITY + PendingContract.TABLE);
	
	private static final int TIMELINE_ALL = 1;
    private static final int TIMELINE_ID = 2;
    private static final int PENDING_ALL = 3;
    private static final int PENDING_ID = 4;

    private static final UriMatcher uriMatcher;
    
    private DbHelper _dbHelper;
    
    static {
    	uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    	uriMatcher.addURI(AUTHORITY, TimelineContract.TABLE, TIMELINE_ALL);
    	uriMatcher.addURI(AUTHORITY, TimelineContract.TABLE + "/#", TIMELINE_ID);
    	uriMatcher.addURI(AUTHORITY, PendingContract.TABLE, PENDING_ALL);
    	uriMatcher.addURI(AUTHORITY, PendingContract.TABLE + "/#", PENDING_ID);
    }
    
	@Override
	public boolean onCreate() {
		Utils.Log("TimelineProvider.onCreate");
		_dbHelper = DbHelper.createDbHelper(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		
		Utils.Log("TimelineProvider.query(). Uri: " + uri.toString());
		SQLiteDatabase db = _dbHelper.getReadableDatabase();
		
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();		
		
		switch (uriMatcher.match(uri)) {
		case TIMELINE_ID:		  
			qb.appendWhere(TimelineContract._ID + "=" + uri.getLastPathSegment());
		case TIMELINE_ALL:
			qb.setTables(TimelineContract.TABLE);
			break;
		case PENDING_ID:		  
			qb.appendWhere(PendingContract._ID + "=" + uri.getLastPathSegment());
		case PENDING_ALL:
			qb.setTables(PendingContract.TABLE);
			break;
		default:
			throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
		
		return qb.query(db,
				projection, selection, selectionArgs, null, null,
				sortOrder);
	}

	@Override
	public String getType(Uri uri) {
		switch (uriMatcher.match(uri)) {
		case TIMELINE_ALL:
			return "vnd.android.cursor.dir/vnd.pt.isel.pdm.Yamba.timeline";
		case TIMELINE_ID:
			return "vnd.android.cursor.item/vnd.pt.isel.pdm.Yamba.timeline";
		case PENDING_ALL:
			return "vnd.android.cursor.dir/vnd.pt.isel.pdm.Yamba.pending";
		case PENDING_ID:
			return "vnd.android.cursor.item/vnd.pt.isel.pdm.Yamba.pending";
		default:
			throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		Utils.Log("TimelineProvider.insert(). Uri: " + uri.toString());
		SQLiteDatabase db = _dbHelper.getWritableDatabase();
		
		long id;
		switch (uriMatcher.match(uri)) {
		
		case TIMELINE_ID:
			id = db.insertWithOnConflict(TimelineContract.TABLE, null, values, SQLiteDatabase.CONFLICT_REPLACE);
			_dbHelper.close();
			break;
			
		case PENDING_ALL:
			id = db.insert(PendingContract.TABLE, null, values);
			_dbHelper.close();
			break;
			
		default:
			_dbHelper.close();
			throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
		
		if (id < 0)
			return null;
		
		getContext().getContentResolver().notifyChange(uri, null);		
		return uri;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		Utils.Log("TimelineProvider.delete(). Uri: " + uri.toString());
		SQLiteDatabase db = _dbHelper.getWritableDatabase();
		
		int count;
		switch (uriMatcher.match(uri)) {
	
		case PENDING_ALL:
			count = db.delete(PendingContract.TABLE, selection, selectionArgs);
			_dbHelper.close();
			break;
			
		default:
			_dbHelper.close();
			throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
		
		if (count > 0)			
			getContext().getContentResolver().notifyChange(uri, null);		
		
		return count;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		throw new UnsupportedOperationException("update unsupported for the supplied URI");
	}

}
