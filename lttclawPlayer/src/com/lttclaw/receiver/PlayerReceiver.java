package com.lttclaw.receiver;

import com.lttclaw.Application.MyApplication;
import com.lttclaw.lttclawplayer.R;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class PlayerReceiver extends BroadcastReceiver {

	MyApplication myApplication = MyApplication.getInstance();

	public PlayerReceiver() {

	}

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getStringExtra("action");
		Log.d("action", action);
		if (action.equals("close")) {
			myApplication.notificationManager.cancelAll();
			myApplication.service.stopMusic();
			myApplication.service.stopSelf();
			System.exit(0);
		} else if (action.equals("playing")) {
			myApplication.notification.contentView.setImageViewResource(
					R.id.paly_pause_music, R.drawable.music_play);
			myApplication.notificationManager.notify(1,
					myApplication.notification);
		} else if (action.equals("pause")) {
			myApplication.notification.contentView.setImageViewResource(
					R.id.paly_pause_music, R.drawable.music_pause);
			myApplication.notificationManager.notify(1,
					myApplication.notification);
		}
	}

}
