package com.lttclaw.lttclawplayer;

import java.util.ArrayList;

import com.lttclaw.bean.Music;
import com.lttclaw.utils.MusicUtils;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends Activity {
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.act_main);
		ListView listv=(ListView) findViewById(R.id.listView1);
		ArrayList<Music> musiclist=MusicUtils.getAllSongs(this);
		listv.setAdapter(new MusicListAdapter(musiclist));
		listv.setOnItemClickListener(new MonItemClickListener());
		
	}
	
	private class MusicListAdapter extends BaseAdapter{
		ArrayList<Music> musiclist;
		public MusicListAdapter(ArrayList<Music> musiclist) {
			this.musiclist=musiclist;
		}
		
		@Override
		public int getCount() {
			return musiclist.size();
		}
		@Override
		public Object getItem(int position) {
			return musiclist.get(position);
		}
		@Override
		public long getItemId(int position) {
			return musiclist.get(position).getId();
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder=null;
			if(convertView==null){
				convertView=LayoutInflater.from(MainActivity.this).inflate(R.layout.list_item, null);
				holder=new ViewHolder();
				holder.title=(TextView) convertView.findViewById(R.id.tvtitle);
				holder.artist=(TextView) convertView.findViewById(R.id.tvartist);
				holder.url=(TextView) convertView.findViewById(R.id.tvurl);
				convertView.setTag(holder);
			}else{
				holder=(ViewHolder) convertView.getTag();
			}
			
			holder.title.setText(musiclist.get(position).getName());
			holder.artist.setText(musiclist.get(position).getSingerName());
			holder.url.setText(musiclist.get(position).getUrl());
			return convertView;
		}
		
		private class ViewHolder{
			TextView title,artist,url;
		}
	}//end of MusicListAdapter
	
	private class MonItemClickListener implements OnItemClickListener{
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			Intent i=new Intent(MainActivity.this,PlayerActivity.class);
			i.putExtra("position", position);
			startActivity(i);
			finish();
		}
	}
	
	
}
