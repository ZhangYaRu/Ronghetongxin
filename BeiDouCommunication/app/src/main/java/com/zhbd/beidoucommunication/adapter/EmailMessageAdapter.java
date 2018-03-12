package com.zhbd.beidoucommunication.adapter;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.zhbd.beidoucommunication.R;
import com.zhbd.beidoucommunication.domain.EmailMessage;
import com.zhbd.beidoucommunication.domain.SmsMessage;
import com.zhbd.beidoucommunication.utils.CommUtil;
import com.zhbd.beidoucommunication.utils.ToastUtils;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhangyaru on 2017/9/4.
 */

public class EmailMessageAdapter extends BaseAdapter {
    private List<EmailMessage> mList;
    private Activity mActivity;

    public EmailMessageAdapter(List<EmailMessage> mList, Activity mActivity) {
        this.mList = mList;
        this.mActivity = mActivity;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public EmailMessage getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, final ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = View.inflate(parent.getContext(), R.layout.item_sms_list, null);
            // 初始化控件
            holder.tvName = (TextView) convertView.findViewById(R.id.tv_contacts_name);
            holder.tvLastTime = (TextView) convertView.findViewById(R.id.tv_last_sms_time);
            holder.tvLastMsg = (TextView) convertView.findViewById(R.id.tv_last_sms);
            //holder.tvDelete = (TextView) convertView.findViewById(R.id.tv_delete);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        EmailMessage email = getItem(position);

        // 填充数据
        // 名字/备注
        holder.tvName.setText(email.getAddress());

        // 最后一次时间, 时间长, 做截取
        String time = email.getSendTime();
        boolean isToday = false;
        try {
            isToday = CommUtil.isToday(time);
        } catch (ParseException e) {
            e.printStackTrace();
            ToastUtils.showToast(mActivity, mActivity.getResources().getString(R.string.small_problem));
        }
        // 是今天显示时间,不是今天显示日期
        if (isToday) {
            time = time.substring(11, time.length());
        } else {
            time = time.substring(0, 10);
        }
        holder.tvLastTime.setText(time);
        // 最后一条消息
        holder.tvLastMsg.setText(email.getContent());
        return convertView;
    }


    class ViewHolder {
        TextView tvName;
        TextView tvLastMsg;
        TextView tvLastTime;
        TextView tvDelete;
    }

    /**
     * 更新Adapter数据
     *
     * @param list
     */
    public void setList(ArrayList<EmailMessage> list) {
        mList = list;
        notifyDataSetChanged();
    }
}
