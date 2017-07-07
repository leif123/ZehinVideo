package com.zehin.videosdk.adapter;

import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.zehin.video.R;
import com.zehin.videosdk.utils.DateUtil;
import com.zehin.videosdk.entity.VideoPlayRecord;

/**
 *	日期		:	2016-1-16<br>
 *	作者		:	wang_le_fei<br>
 *	项目		:	GuoXin<br>
 *	功能		:	视频列表适配器<br>
 */
public class ListAdapter_Video extends BaseAdapter {
	
	private Context context;
	private List<VideoPlayRecord> data;

	private DateUtil dateUtil = new DateUtil();

	public ListAdapter_Video(Context context, List<VideoPlayRecord> data) {
		super();
		this.context = context;
		this.data = data;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return data.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return data.get(position);
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return arg0;
	}
	
	private class ViewHolder{
		public TextView tv_time; // 时间
		public ImageView iv_video; // 图标
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder holder;
		if(convertView == null){
			holder = new ViewHolder();
			convertView = LayoutInflater.from(context).inflate(R.layout.item_video_list, null);
			holder.tv_time = (TextView) convertView.findViewById(R.id.tv_time);
			holder.iv_video = (ImageView) convertView.findViewById(R.id.iv_video);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		holder.tv_time.setText(dateUtil.getStringDate(data.get(position).getStartTime(),"HH:mm:ss")+"~"
				+dateUtil.getStringDate(data.get(position).getStopTime(),"HH:mm:ss"));
		if("1".equals(data.get(position).getStatus())){ 
			holder.tv_time.setTextColor(Color.parseColor("#B300C6FF"));
			holder.iv_video.setImageResource(R.drawable.video_play_list_open);
		} else {
			holder.tv_time.setTextColor(Color.parseColor("#B3ffffff"));
			holder.iv_video.setImageResource(R.drawable.video_play_list_close);
		}
		return convertView;
	}

}
