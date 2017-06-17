package com.nushhacks.angelhackapp.NotificationReader;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by danie on 17/6/2017.
 */

public class NotificationHandler extends BroadcastReceiver {

	/**
	 * Just a structure for containing all relevant notification information (why does java not have struct)
	 */
	public class NotificationInfo {
		public String packageName;
		public String title;
		public String text;

		public NotificationInfo(String packageName, String title, String text) {
			this.packageName = packageName;
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
		String title = intent.getStringExtra("title");
		String text = intent.getStringExtra("text");
		NotificationInfo notificationInfo = new NotificationInfo(packageName, title, text);
		notifications.add(notificationInfo);
	}

	public ArrayList<NotificationInfo> getNotifications() {
		ArrayList<NotificationInfo> res = new ArrayList<>();
		for(NotificationInfo ni : notifications) {
			res.add(new NotificationInfo(ni.packageName, ni.title, ni.text));
		}
		notifications.clear();
		return res;
	}
}