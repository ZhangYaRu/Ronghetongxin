package com.zhbd.beidoucommunication.ui.fragment;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;

import com.zhbd.beidoucommunication.R;
import com.zhbd.beidoucommunication.adapter.MessageViewPagerAdapter;
import com.zhbd.beidoucommunication.base.BaseFragment;
import com.zhbd.beidoucommunication.widget.PopWindowMenu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by zhangyaru on 2017/8/30.
 */

public class MessageFragment extends BaseFragment {

    @Bind(R.id.tablayout)
    TabLayout mTabLayout;

    @Bind(R.id.common_message_viewPager)
    ViewPager mViewPager;

    /**
     * Fragment集合
     */
    private List<Fragment> mTabContents = new ArrayList<>();
    private FragmentPagerAdapter mAdapter;
    /**
     * tablayout标题集合
     */
    private List<String> mDatas;

    /**
     * 进行初始化,父类在onGreateView中调用
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected View initView() {
        View view = View.inflate(getContext(), R.layout.fragment_message, null);
        ButterKnife.bind(this, view);
        // 设置标题栏属性
        setTitleText(R.string.main_button_message);
        setLeftIcon(0,false);
        setRightIcon(R.drawable.add_icon_normal, true);
        setRightText(0, false);

        mDatas = Arrays.asList(
                getResources().getString(R.string.communication_message),
                getResources().getString(R.string.sms_message),
                getResources().getString(R.string.email));

        // 初始化Fragment
        init();
        return view;
    }

    private void init() {
        // 创建Fragment对象
        CommonMSGFragment commonMSGFragment = new CommonMSGFragment();
        SmsMSGFragment smsMSGFragment = new SmsMSGFragment();
        EmailMSGFragment emailMSGFragment = new EmailMSGFragment();

        // 添加到tabLayout
        mTabContents.add(commonMSGFragment);
        mTabContents.add(smsMSGFragment);
        mTabContents.add(emailMSGFragment);

        //设置TabLayout的模式
        mTabLayout.setTabMode(TabLayout.MODE_FIXED);

        //为TabLayout添加tab名称
        mTabLayout.addTab(mTabLayout.newTab().setText(mDatas.get(0)));
        mTabLayout.addTab(mTabLayout.newTab().setText(mDatas.get(1)));
        mTabLayout.addTab(mTabLayout.newTab().setText(mDatas.get(2)));

        mAdapter = new MessageViewPagerAdapter(getActivity().getSupportFragmentManager(),
                mTabContents, mDatas);

        //viewpager加载adapter
        mViewPager.setAdapter(mAdapter);

        //TabLayout加载viewpager
        mTabLayout.setupWithViewPager(mViewPager);

    }

    @Override
    protected void clickRight(BaseFragment fragment) {
        super.clickRight(fragment);
        PopWindowMenu pop = new PopWindowMenu(getActivity());
        pop.setClickOperate("message", R.string.send_sms, R.string.send_email);
        pop.showPopupWindow(mIvRightIcon);
    }
}
