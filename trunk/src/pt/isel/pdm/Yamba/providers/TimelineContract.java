package pt.isel.pdm.Yamba.providers;

import android.provider.BaseColumns;

public final class TimelineContract {
	public static final String TABLE 	= "timeline";
	//public static final Uri CONTENT_URI = Uri.withAppendedPath(LeicProvider.CONTENT_URI, TABLE);
	
	public static final String 
	  _ID 			= BaseColumns._ID,
  	  CREATED_AT 	= "createdAt",
	  TEXT 			= "text",
	  IS_READ	 	= "isRead",
	  AUTHOR_ID		= "authorId",
	  AUTHOR_NAME	= "authorName";
}
