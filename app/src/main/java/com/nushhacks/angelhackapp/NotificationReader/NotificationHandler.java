package com.nushhacks.angelhackapp.NotificationReader;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.ArrayList;

/**
 * Created by danie on 17/6/2017.
 */

public class NotificationHandler extends BroadcastReceiver {

	/**
	 * Just a structure for containing all relevant notification information (why does java not have struct)
	 */
	public class NotificationInfo {
		public String packageName;
		public String tickerText;
		public String title;
		public String text;

		public NotificationInfo(String packageName, String tickerText, String title, String text) {
			this.packageName = packageName;
			this.tickerText = tickerText;
			this.title = title;
			this.text = text;
		}
	}

	ArrayList<NotificationInfo> notifications;

	public NotificationHandler() {
		notifications = new ArrayList<>();
	}

	/**
	 * Overrides onReceive and runs the provided NotificationFunction on the received information
	 * @param context
	 * @param intent
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		String packageName = intent.getStringExtra("package");
		String tickerText = intent.getStringExtra("ticker");
		String title = intent.getStringExtra("title");
		String text = intent.getStringExtra("text");
		NotificationInfo notificationInfo = new NotificationInfo(packageName, tickerText, title, text);
		notifications.add(notificationInfo);
	}

	public ArrayList<NotificationInfo> getNotifications() {
		return notifications;
	}
}
