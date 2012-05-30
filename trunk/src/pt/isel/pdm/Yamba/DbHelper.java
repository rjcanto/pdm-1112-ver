package pt.isel.pdm.Yamba;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DbHelper extends SQLiteOpenHelper {
	static final String DB_NAME = "pdm.db";
	static final int DB_VERSION = 1;

	public DbHelper(Context ctx) {
		super(ctx, DB_NAME, null, DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String columns = TimelineContract._ID + " bigint primary key, "
				       + TimelineContract.AUTHOR_ID + " bigint not null, " // foreign key
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
