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
	/* MediaPlayer���� */
	private MediaPlayer mMediaPlayer = null;
	private int currentTime = 0;// �������Ž���
	private int currentListItme = -1;// ��ǰ���ŵڼ��׸�
	private List<Music> songs;// Ҫ���ŵĸ�������
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
	 * �õ���ǰ���Ž���
	 */
	public int getCurrent() {
		if (mMediaPlayer.isPlaying()) {
			return mMediaPlayer.getCurrentPosition();
		} else {
			return currentTime;
		}
	}

	/**
	 * ��������Ľ���
	 */
	public void movePlay(int progress) {
		mMediaPlayer.seekTo(progress);
		currentTime = progress;
	}

	/**
	 * @return �õ����ڲ�����Ŀ��Music����
	 */
	public Music getCurrentMusic() {
		return songs.get(currentListItme);
	}

	public void setCurrentItem(int position) {
		currentListItme = position;
	}

	/**
	 * ���ݸ����洢·�����Ÿ���
	 */
	private void playMusic(String path) {
		try {
			/* ����MediaPlayer */
			mMediaPlayer.reset();
			/* ����Ҫ���ŵ��ļ���·�� */
			mMediaPlayer.setDataSource(path);
			/* ׼������ */
			mMediaPlayer.prepare();
			/* ��ʼ���� */
			mMediaPlayer.start();
			if (seekBar != null) {
				seekBar.setMax(mMediaPlayer.getDuration());
				Log.d("duration", mMediaPlayer.getDuration() + "");
				new MyTimerThread().start();
			}
			mMediaPlayer.setOnCompletionListener(new OnCompletionListener() {
				public void onCompletion(MediaPlayer arg0) {
					// �������һ��֮�������һ��
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

	/* ��һ�� */
	public void nextMusic() {
		if (++currentListItme >= songs.size()) {
			currentListItme = 0;
		}
		//��Ҫ����ֹͣ����ɲ����쳣����ӰЧ��
		mMediaPlayer.stop();
		playMusic(songs.get(currentListItme).getUrl());
		currentTime = 0;
	}

	/* ��һ�� */
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
	 * �����Ƿ����ڲ���
	 */
	public boolean isPlay() {
		return mMediaPlayer.isPlaying();
	}

	/**
	 * ��ͣ��ʼ���Ÿ���
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
	 * �Զ����Service�࣬ͨ�������getService�õ�Service��֮��Ϳɵ���Service����ķ�����
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
