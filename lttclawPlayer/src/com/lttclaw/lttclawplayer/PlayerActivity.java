package com.lttclaw.lttclawplayer;

import com.lttclaw.Application.MyApplication;
import com.lttclaw.receiver.PlayerReceiver;
import com.lttclaw.service.PlayerService;
import com.lttclaw.service.PlayerService.LocalBinder;
import com.lttclaw.utils.MusicUtils;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat.Builder;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.RemoteViews;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class PlayerActivity extends Activity implements OnClickListener {
	PlayerService playerservice;
	ServiceConnection serviceConnection;
	ImageButton play;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.player);
		play=(ImageButton) findViewById(R.id.imgbt_play);
		final SeekBar seekbar=(SeekBar) findViewById(R.id.seekBar1);
		seekbar.setOnSeekBarChangeListener(new MonSeekBarchangedListener());
		serviceConnection=new ServiceConnection() {
			@Override
			public void onServiceDisconnected(ComponentName name) {
			}
			
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				Log.d("onServiceConnected", "connected");
				playerservice=((LocalBinder)service).getService();
				MyApplication.getInstance().service=playerservice;
				playerservice.setSongs(MusicUtils.getAllSongs(PlayerActivity.this));
				playerservice.setSeekBar(seekbar);
				playerservice.setCurrentItem(getIntent().getIntExtra("position", 0)-1);
				playerservice.nextMusic();
				changeStatus(play,playerservice.isPlay());
			}
		};
		
		//创造Notification
		NotificationManager manager=showCustomView();
		MyApplication.getInstance().notificationManager=manager;
		
		//绑定服务
		Intent i=new Intent(this,PlayerService.class);
		bindService(i, serviceConnection, Context.BIND_AUTO_CREATE);
	}

	@Override
	public void onClick(View v) {
		if(playerservice==null)
			return;
		switch (v.getId()) {
		case R.id.imgbt_pre:
			playerservice.frontMusic();
			break;
		case R.id.imgbt_next:
			playerservice.nextMusic();
			break;
		case R.id.imgbt_play:
			playerservice.pauseOrPlay();
			break;
		default:
			break;
		}
		changeStatus(play,playerservice.isPlay());
		
	}

	/**  改变播放按钮的图标
	 * @param play 按钮的imageButton
	 * @param playing 是否播放
	 */
	private void changeStatus(ImageButton play, boolean playing) {
		if(playing)
			play.setImageResource(R.drawable.music_pause);
		else
			play.setImageResource(R.drawable.music_play);
		
	}
	
	private class MonSeekBarchangedListener implements OnSeekBarChangeListener{
		int progress;
		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			this.progress=progress;
		}
		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			
		}
		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			playerservice.movePlay(progress);
		}
	}//end of onseekbarchangedListener
	
	private NotificationManager showCustomView() {
		RemoteViews remoteViews = new RemoteViews(getPackageName(),
				R.layout.music_notification);
		
		remoteViews.setTextViewText(R.id.title_music_name, "~~player"); //设置textview
		Intent reActivity=new Intent(this,PlayerActivity.class);
		PendingIntent pIntent=PendingIntent.getActivity(this, 0, reActivity, 0);
		remoteViews.setOnClickPendingIntent(R.id.ll_parent, pIntent);
		//设置按钮事件 
		
		Intent preintent = new Intent(this,PlayerService.class); 
		preintent.putExtra("action", "pre");
		PendingIntent prepi = PendingIntent.getService(this, 0, preintent, PendingIntent.FLAG_UPDATE_CURRENT);
		remoteViews.setOnClickPendingIntent(R.id.last_music, prepi);//----设置对应的按钮ID监控
		
		Intent pauaseOrStartIntent=new Intent(this,PlayerService.class); 
		pauaseOrStartIntent.putExtra("action", "pause");
		PendingIntent pausepi = PendingIntent.getService(this, 1, pauaseOrStartIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		remoteViews.setOnClickPendingIntent(R.id.paly_pause_music, pausepi);//----设置对应的按钮ID监控
		
		Intent nextIntent=new Intent(this,PlayerService.class); 
		nextIntent.putExtra("action", "next");
		PendingIntent nextpi = PendingIntent.getService(this, 2, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		remoteViews.setOnClickPendingIntent(R.id.next_music, nextpi);//----设置对应的按钮ID监控
		
		Intent closeIntent=new Intent(this,PlayerReceiver.class); 
		closeIntent.putExtra("action", "close");
		closeIntent.setAction("com.lttclaw.playerReceiver");
		PendingIntent closepi=PendingIntent.getBroadcast(this, 0, closeIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		remoteViews.setOnClickPendingIntent(R.id.close, closepi);
		
		Builder builder = new Builder(PlayerActivity.this);
		builder.setContent(remoteViews).setSmallIcon(R.drawable.ic_launcher)
				.setOngoing(true)
				.setTicker("music is playing");
		Notification notification=builder.build();
		MyApplication.getInstance().notification=notification;
		NotificationManager manager=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
		manager.notify(1, notification);
		return manager;
	}
}
