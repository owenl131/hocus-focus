package com.nushhacks.angelhackapp.NotificationReader;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

/**
 * Created by danie on 17/6/2017.
 */
@TargetApi(19)
public class NotificationListener extends NotificationListenerService {

	Context context;

	@Override
	public void onCreate() {
		super.onCreate();
		context = getApplicationContext();
	}

	/**
	 * Sends a broadcast to LocalBroadcastManager when a notification is detected
	 * @param notification
	 */
	@Override
	public void onNotificationPosted(StatusBarNotification notification) {
		String packageName = notification.getPackageName();
		Bundle extras = notification.getNotification().extras;
		String title = extras.getString("android.title");
		String text = extras.getCharSequence("android.text").toString();

		Log.i("Notification", "Posted");
		Log.i("Package",packageName);
		Log.i("Title",title);
		Log.i("Text",text);

		Intent broadcast = new Intent("Notification");
		broadcast.putExtra("package", packageName);
		broadcast.putExtra("title", title);
		broadcast.putExtra("text", text);

		LocalBroadcastManager.getInstance(context).sendBroadcast(broadcast);
	}

	/**
	 * I just leave this here because it's forced to be overriden
	 * Don't think we need to do much with this
	 * Just log for now
	 * @param notification
	 */
	@Override
	public void onNotificationRemoved(StatusBarNotification notification) {
		String packageName = notification.getPackageName();
		Bundle extras = notification.getNotification().extras;
		String title = extras.getString("android.title");
		String text = extras.getCharSequence("android.text").toString();

		Log.i("Notification", "Removed");
		Log.i("Package",packageName);
		Log.i("Title",title);
		Log.i("Text",text);
	}
}
