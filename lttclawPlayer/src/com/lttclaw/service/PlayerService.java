package com.lttclaw.service;

import java.io.IOException;
import java.util.List;

import com.lttclaw.bean.Music;
import com.lttclaw.receiver.PlayerReceiver;

import android.app.IntentService;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.SeekBar;

public class PlayerService extends IntentService {

	public PlayerService(String name) {
		super(name);
	}

	public PlayerService() {
		super("player!");
	}

	private final IBinder mBinder = new LocalBinder();
	/* MediaPlayer对象 */
	private MediaPlayer mMediaPlayer = null;
	private int currentTime = 0;// 歌曲播放进度
	private int currentListItme = -1;// 当前播放第几首歌
	private List<Music> songs;// 要播放的歌曲集合
	private SeekBar seekBar;
	private Handler handler = new Handler();

	@Override
	public void onCreate() {
		super.onCreate();
		if (mMediaPlayer == null) {
			mMediaPlayer = new MediaPlayer();
		}
	}

	/**
	 * 得到当前播放进度
	 */
	public int getCurrent() {
		if (mMediaPlayer.isPlaying()) {
			return mMediaPlayer.getCurrentPosition();
		} else {
			return currentTime;
		}
	}

	/**
	 * 跳到输入的进度
	 */
	public void movePlay(int progress) {
		mMediaPlayer.seekTo(progress);
		currentTime = progress;
	}

	/**
	 * @return 得到正在播放曲目的Music对象
	 */
	public Music getCurrentMusic() {
		return songs.get(currentListItme);
	}

	public void setCurrentItem(int position) {
		currentListItme = position;
	}

	/**
	 * 根据歌曲存储路径播放歌曲
	 */
	private void playMusic(String path) {
		try {
			/* 重置MediaPlayer */
			mMediaPlayer.reset();
			/* 设置要播放的文件的路径 */
			mMediaPlayer.setDataSource(path);
			/* 准备播放 */
			mMediaPlayer.prepare();
			/* 开始播放 */
			mMediaPlayer.start();
			if (seekBar != null) {
				seekBar.setMax(mMediaPlayer.getDuration());
				Log.d("duration", mMediaPlayer.getDuration() + "");
				new MyTimerThread().start();
			}
			mMediaPlayer.setOnCompletionListener(new OnCompletionListener() {
				public void onCompletion(MediaPlayer arg0) {
					// 播放完成一首之后进行下一首
					nextMusic();
					currentTime = 0;
				}
			});
		} catch (IOException e) {
		}
	}
	public void stopMusic(){
		mMediaPlayer.stop();
		mMediaPlayer.release();
		mMediaPlayer=null;
	}

	/* 下一首 */
	public void nextMusic() {
		if (++currentListItme >= songs.size()) {
			currentListItme = 0;
		}
		//重要！不停止会造成播放异常，重影效果
		mMediaPlayer.stop();
		playMusic(songs.get(currentListItme).getUrl());
		currentTime = 0;
	}

	/* 上一首 */
	public void frontMusic() {
		Log.v("itme", currentListItme + "hree");
		if (--currentListItme < 0) {
			currentListItme = songs.size() - 1;
		}
		mMediaPlayer.stop();
		playMusic(songs.get(currentListItme).getUrl());
		currentTime = 0;
	}

	/**
	 * 歌曲是否真在播放
	 */
	public boolean isPlay() {
		return mMediaPlayer.isPlaying();
	}

	/**
	 * 暂停或开始播放歌曲
	 */
	public void pauseOrPlay() {
		if (mMediaPlayer.isPlaying()) {
			currentTime = mMediaPlayer.getCurrentPosition();
			mMediaPlayer.pause();

		} else {
			mMediaPlayer.start();

		}
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return mBinder;
	}

	/**
	 * 自定义绑定Service类，通过这里的getService得到Service，之后就可调用Service这里的方法了
	 */
	public class LocalBinder extends Binder {
		public PlayerService getService() {
			Log.d("playerService", "getService");
			return PlayerService.this;
		}
	}

	public void setSongs(List<Music> songs) {
		this.songs = songs;
	}

	@Override
	public void onDestroy() {

	}

	@Override
	protected void onHandleIntent(Intent intent) {
		String type = intent.getStringExtra("action");
		Log.d("intent", type);
		if (type.equals("next")) {
			nextMusic();
		} else if (type.equals("pre")) {
			frontMusic();
		} else if (type.equals("pause")) {
			Intent i=new Intent(this,PlayerReceiver.class);
			Log.d("isplaying", isPlay()+"");
			if(isPlay())
				i.putExtra("action", "playing");
			else
				i.putExtra("action", "pause");
			sendBroadcast(i);
			pauseOrPlay();
		} 

	}

	public SeekBar getSeekBar() {
		return seekBar;
	}

	public void setSeekBar(SeekBar seekBar) {
		this.seekBar = seekBar;
	}

	private class MyTimerThread extends Thread {
		@Override
		public void run() {
			while (mMediaPlayer.isPlaying()) {
				if (seekBar != null)
					handler.post(new Runnable() {
						@Override
						public void run() {
							seekBar.setProgress(currentTime += 1000);
						}
					});
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					Log.e("error", e.getLocalizedMessage());
				}

			}
		}
	}
}
