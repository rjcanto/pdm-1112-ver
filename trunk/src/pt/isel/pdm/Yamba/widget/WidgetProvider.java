package pt.isel.pdm.Yamba.widget;

import pt.isel.pdm.Yamba.R;
import pt.isel.pdm.Yamba.util.Utils;
import pt.isel.pdm.Yamba.providers.TimelineProvider;
import pt.isel.pdm.Yamba.providers.TimelineContract;
import pt.isel.pdm.Yamba.activity.TimelineActivity;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.text.format.DateUtils;
import android.widget.RemoteViews;

public class WidgetProvider extends AppWidgetProvider {

	public void onUpdate(Context ctx, AppWidgetManager mgr, int[] ids) {
		Cursor c = ctx.getContentResolver().query(TimelineProvider.TIMELINE_URI, null, null, null, null) ;
		Utils.Log("WidgetProvider.onUpdate") ;
		try {
			if (c.moveToLast()) {
				
				CharSequence user = c.getString(c.getColumnIndex(TimelineContract.AUTHOR_NAME)) ;
				CharSequence createdAt = DateUtils.getRelativeTimeSpanString(ctx, 
						c.getLong(c.getColumnIndex(TimelineContract.CREATED_AT))) ; 
				CharSequence message = c.getString(c.getColumnIndex(TimelineContract.TEXT)) ;
				
				for (int id : ids ) {
					Utils.Log("WidgetProvider.onUpdate: updating widget " + id) ;
					RemoteViews remoteViews = new RemoteViews(
							ctx.getPackageName(), R.layout.widget);
					remoteViews.setTextViewText(R.id.tl_item_textUser, user);
					remoteViews.setTextViewText(R.id.tl_item_textTime, createdAt);
					remoteViews.setTextViewText(R.id.tl_item_textMessage, message);
					remoteViews.setOnClickPendingIntent(R.id.widget_icon, 
							PendingIntent.getActivity(ctx, 0, new Intent(ctx, TimelineActivity.class), 0)
							);
					mgr.updateAppWidget(id, remoteViews);
					
				}
			} else {
				Utils.Log("WidgetProvider.onUpdate: no data to update") ;
			}
		} finally {
			c.close() ;
		}
	}
}
