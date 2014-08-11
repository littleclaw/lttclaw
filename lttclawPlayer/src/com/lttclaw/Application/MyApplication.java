package com.lttclaw.Application;

import com.lttclaw.receiver.PlayerReceiver;
import com.lttclaw.service.PlayerService;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;

public class MyApplication extends Application {

	public PlayerReceiver receiver;
	public PlayerService service;
	public Notification notification;
	public NotificationManager notificationManager;
	private static MyApplication myApplication;
	@Override
	public void onCreate() {
		super.onCreate();
		myApplication=this;
	}
	
	public static MyApplication getInstance(){
		return myApplication;
	}
}
