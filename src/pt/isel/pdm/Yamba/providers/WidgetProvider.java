package pt.isel.pdm.Yamba.providers;

import pt.isel.pdm.Yamba.R;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.widget.RemoteViews;

public class WidgetProvider extends AppWidgetProvider {

	public void onUpdate(Context ctx, AppWidgetManager mgr, int[] ids) {
		String stat = null ;
		PendingIntent pi = null ;
		for(int id : ids) {
			RemoteViews remoteViews = new RemoteViews(
					ctx.getPackageName(), R.layout.widget);
			remoteViews.setTextViewText(R.id.update, stat);
			remoteViews.setOnClickPendingIntent(R.id.update, pi);
			mgr.updateAppWidget(id, remoteViews);
		}
	}
}
