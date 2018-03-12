package com.zhbd.beidoucommunication.ui.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.zhbd.beidoucommunication.R;
import com.zhbd.beidoucommunication.adapter.FriendsAdapter;
import com.zhbd.beidoucommunication.config.Constants;
import com.zhbd.beidoucommunication.db.DatabaseDao;
import com.zhbd.beidoucommunication.domain.Friend;
import com.zhbd.beidoucommunication.utils.CharacterParser;
import com.zhbd.beidoucommunication.utils.PinyinComparator;
import com.zhbd.beidoucommunication.utils.SharedPrefUtil;
import com.zhbd.beidoucommunication.view.SideBar;

import java.util.ArrayList;
import java.util.Collections;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by zhangyaru on 2017/9/5.
 */

public class FriendsFragment extends Fragment {
    @Bind(R.id.sidrbar)
    SideBar sidrbar;

    @Bind(R.id.qucik_index_listview)
    ListView mListview;

    @Bind(R.id.tv_pop)
    TextView tv_pop;

    private ArrayList<Friend> mList = new ArrayList<>();
    private DatabaseDao dao;
    private FriendsAdapter mAdapter;

    /**
     * 汉字转换成拼音的类
     */
    private CharacterParser characterParser;
    /**
     * 根据拼音来排列ListView里面的数据类
     */
    private PinyinComparator pinyinComparator;

    private View view;

    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // 判断是adapter发送来的跟新数据的广播
            if ("com.contacts.listview.updata".equals(intent.getAction())) {
                onResume();
            }
        }
    };


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friends, null);
        ButterKnife.bind(this, view);
        initData();
        return view;
    }

    private void initData() {
        int userId = SharedPrefUtil.getInt(getActivity(), Constants.USER_ID, 0);
        dao = DatabaseDao.getInstance(getActivity(), userId);
        //实例化汉字转拼音类
        characterParser = CharacterParser.getInstance();

        pinyinComparator = new PinyinComparator();

        sidrbar.setTextView(tv_pop);

        //设置右侧触摸监听
        sidrbar.setOnTouchingLetterChangedListener(new SideBar.OnTouchingLetterChangedListener() {

            @Override
            public void onTouchingLetterChanged(String s) {
                //该字母首次出现的位置
                int position = mAdapter.getPositionForSection(s.charAt(0));
                if (position != -1) {
                    mListview.setSelection(position);
                }

            }
        });

        // 填充数据集合
        mList = filledData();

        mAdapter = new FriendsAdapter(getActivity(), mList);
        mListview.setAdapter(mAdapter);

        // 注册广播接收者
        IntentFilter filter = new IntentFilter("com.contacts.listview.updata");
        getActivity().registerReceiver(mReceiver, filter);
    }

    /**
     * 为ListView填充数据
     */
    private ArrayList<Friend> filledData() {
        mList.clear();
        //从数据库中查找联系人信息
        mList.addAll(dao.queryFriensInfo());
        //setData();
        for (int i = 0; i < mList.size(); i++) {
            //汉字转换成拼音
            String pinyin = characterParser.getSelling(mList.get(i).getName());
            String sortString = pinyin.substring(0, 1).toUpperCase();

            // 正则表达式，判断首字母是否是英文字母
            if (sortString.matches("[A-Z]")) {
                mList.get(i).setLetter(sortString.toUpperCase());
            } else {
                mList.get(i).setLetter("#");
            }
        }
        // 根据a-z进行排序源数据
        Collections.sort(mList, pinyinComparator);
        return mList;
    }


    @Override
    public void onResume() {
        super.onResume();
        mList = filledData();
        mAdapter.updateListView(mList);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        dao.close();
        getActivity().unregisterReceiver(mReceiver);
    }
}
