package com.zhbd.beidoucommunication.ui.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zhbd.beidoucommunication.R;
import com.zhbd.beidoucommunication.adapter.SendSmsAdapter;
import com.zhbd.beidoucommunication.base.TitlebarActivity;
import com.zhbd.beidoucommunication.config.Constants;
import com.zhbd.beidoucommunication.db.DatabaseDao;
import com.zhbd.beidoucommunication.domain.SmsMessage;
import com.zhbd.beidoucommunication.event.ReceiveSMSEvent;
import com.zhbd.beidoucommunication.event.SendMessage;
import com.zhbd.beidoucommunication.event.WaitTimeEvent;
import com.zhbd.beidoucommunication.http.NetWorkUtil;
import com.zhbd.beidoucommunication.utils.CommUtil;
import com.zhbd.beidoucommunication.utils.DataProcessingUtil;
import com.zhbd.beidoucommunication.utils.SharedPrefUtil;
import com.zhbd.beidoucommunication.utils.ToastUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SendSmsActivity extends TitlebarActivity implements View.OnFocusChangeListener {

    private static final String TAG = "SendSmsActivity";


    private static final int REQUEST_CODE_CONTENT = 10001;
    /**
     * 请求获取联系人的权限码
     */
    private static final int REQUEST_GET_CONTACTS = 100;

    // 要申请的权限
    private String[] permissions = {Manifest.permission.READ_CONTACTS};

    private AlertDialog dialog;
    @Bind(R.id.et_receiver_number)
    EditText mEtNumber;

    @Bind(R.id.et_sms_content)
    EditText mEtContent;

    @Bind(R.id.tv_sms_content)
    TextView mTvContent;

    @Bind(R.id.rl_rootview)
    RelativeLayout mRootView;

    @Bind(R.id.ll_top)
    LinearLayout mLlTop;

    @Bind(R.id.sms_listview)
    ListView mListView;

    @Bind(R.id.tv_send_sms)
    TextView mTvSend;

    private String number;
    private String content;
    private String name;
    private SendSmsAdapter mAdapter;
    private ArrayList<SmsMessage> mList = new ArrayList<>();

    private boolean isNew;
    private Phones phones;
    /**
     * 数据库操作类
     */
    DatabaseDao dao;


//    private boolean isSelectNumber;

    /**
     * 注册广播接受者,接收消息并显示到界面
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void revWaitTime(WaitTimeEvent event) {

        // 更新界面,向用户显示需要等待的秒数
        long waitTime = event.getWaitTime();
        if (waitTime > 0) {
            mEtContent.setVisibility(View.GONE);
            mEtContent.setText("");
            mTvContent.setText("请等待" + waitTime + "'s");
            mTvContent.setVisibility(View.VISIBLE);
            mTvSend.setEnabled(false);
        } else {
            mEtContent.setVisibility(View.VISIBLE);
            mEtContent.setText("");
            mTvContent.setVisibility(View.GONE);
            mTvContent.setText("");
            mTvSend.setEnabled(true);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void revSmsMsg(ReceiveSMSEvent event) {
        String address = event.getAddress();
        String content = event.getContent();
        Log.e(TAG, "receiver:" + address + "===" + content);
        //if (msg.getSenderUserId() == friend.getMsgNumber()) {
        SmsMessage sms = new SmsMessage();
        sms.setPhoneNumber(address);
        sms.setContent(content);
//        name = getContactNameByPhoneNumber(number);
        name = CommUtil.getContactNameByPhoneNumber(number);
        Log.e(TAG, "name=" + name);
        sms.setSenderName(name);
        sms.setType(Constants.MESSAGE_TYPE_TEXT);
        sms.setIsRead(Constants.MESSAGE_HAVE_READ);
        sms.setTime(CommUtil.getDate());
        sms.setStatus(Constants.MESSAGE_STATE_RECEIVER);
        Log.e(TAG, "receiver:SmsMessage=" + sms.toString());
        // 添加消息到数据库
        dao.addDataToSms(sms);
        // 如果收到与本页面号码一致的消息才更新界面
        if (number.equals(address)) {
            // 更新界面
            refreshListview(address);
        }
    }

    /*
     * 根据电话号码取得联系人姓名
     */
    public String getContactNameByPhoneNumber(String address) {
        Cursor cursorOriginal =
                this.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME},
                        ContactsContract.CommonDataKinds.Phone.NUMBER + "='" + address + "'", null, null);
        if (null != cursorOriginal) {
            if (cursorOriginal.getCount() > 1) {
                return null;
            } else {
                if (cursorOriginal.moveToFirst()) {
                    return cursorOriginal.getString(cursorOriginal.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                } else {
                    return null;
                }
            }
        } else {
            return null;
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setLayoutRes(R.layout.activity_send_sms);
        // 判断从那个界面进来的,不同界面进来,界面不同
        String entry = getIntent().getStringExtra("entry");
        // 发送新短信
        if ("PopWindowMenu".equals(entry)) {
            // 发新短信
            isNew = true;
        } else {
            // 和列表中的人发短信.直接显示列表人名字
            isNew = false;
        }
        initView();
        initData();
    }

    private void initView() {

        int userId = SharedPrefUtil.getInt(this, Constants.USER_ID, 0);
        dao = DatabaseDao.getInstance(this, userId);

        ButterKnife.bind(this);
        mRootView.bringToFront();
        // 判断是发新短信还是与曾发过短信的人发短信
        if (isNew) {
            setTitleText(R.string.new_sms);
            mLlTop.setVisibility(View.VISIBLE);
            mEtNumber.setOnFocusChangeListener(this);
            mEtContent.setOnFocusChangeListener(this);
        } else {
            number = getIntent().getStringExtra("phone");
            name = getIntent().getStringExtra("name");
            if (CommUtil.isEmpty(name)) {
                setTitleText(number);
            } else {
                setTitleText(name);
            }
            mLlTop.setVisibility(View.GONE);
        }
        setLeftIcon(R.drawable.back_arrow, true);
        setLeftText(R.string.home_pager, true);
        setRightIcon(0, false);
        setRightText(0, false);
    }

    private void initData() {
        // 吧此人的消息全都设置为已读
        dao.updateIsReadToHaveReadOfSms(number);

        mAdapter = new SendSmsAdapter(this, mList);
        mListView.setAdapter(mAdapter);
        if (!isNew) {
            refreshListview(number);
        }
    }

    @Override
    protected void clickLeft(Activity activity) {
        super.clickLeft(activity);
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    /**
     * 点击记号,跳转到系统联系人界面
     */
    @OnClick(R.id.iv_add_system_contacts)
    public void addSystemContacts() {
        // 获取权限,兼容6.0/7.0
        //判断Android版本是否大于23
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int checkCallPhonePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS);
            if (checkCallPhonePermission != PackageManager.PERMISSION_GRANTED) {
                // 如果没有授予该权限，就去提示用户请求
                showDialogTipUserRequestPermission();
            } else {
                Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                startActivityForResult(intent, REQUEST_CODE_CONTENT);
            }
        } else {
            Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
            startActivityForResult(intent, REQUEST_CODE_CONTENT);
        }

    }

    // 提示用户该请求权限的弹出框
    private void showDialogTipUserRequestPermission() {

        new AlertDialog.Builder(this)
                .setTitle(getResources().getString(R.string.permission_not_available))
                .setMessage(getResources().getString(R.string.permission_not_available_msg))
                .setPositiveButton(getResources().getString(R.string.immediately_open), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startRequestPermission();
                    }
                })
                .setNegativeButton(getResources().getString(R.string.cancel_text), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }).setCancelable(false).show();
    }

    // 开始提交请求权限
    private void startRequestPermission() {
        ActivityCompat.requestPermissions(this, permissions, REQUEST_GET_CONTACTS);
    }

    @OnClick(R.id.tv_send_sms)
    public void send() {
        // 判断是否发新短信
        if (isNew) {
            boolean checkFormat = checkFormat();
            if (!checkFormat) {
                return;
            }
        } else {
            content = mEtContent.getText().toString();
            // 判断内容长度
            if (content.length() > 25) {
                ToastUtils.showToast(this, getResources().getString(R.string.content_overlength));
                return;
            }
        }
        // 格式没问题, 封装并发送数据
        byte[] result = DataProcessingUtil.userDefinedDataPackage(
                DataProcessingUtil.USER_DEFINED_TYPE_SMS, number, content);
        EventBus.getDefault().post(new SendMessage(result));
        // 更新界面
        SmsMessage message = new SmsMessage();
        message.setPhoneNumber(number);
        if (isNew) {
            if (phones != null) {
                message.setSenderName(phones.name);
            }
        } else {
            message.setSenderName(name);
        }

        message.setTime(CommUtil.getDate());
        message.setContent(content);
        message.setStatus(Constants.MESSAGE_STATE_SEND_SUCCESS);
        message.setIsRead(Constants.MESSAGE_HAVE_READ);
        //Log.e(TAG, "send().SmsMessage=" + message.toString());
        // 发送的消息加入数据库
        dao.addDataToSms(message);
        // 更新界面
        refreshListview(number);
        // 显示成列表模式
        mLlTop.setVisibility(View.GONE);
        if (isNew) {
            if (phones != null) {
                setTitleText(phones.name);
            } else {
                setTitleText(number);
            }
        } else {
            setTitleText(name);
        }

        // 重置界面
        mEtContent.setText("");

    }


    /**
     * 注册权限申请回调
     *
     * @param requestCode  申请码
     * @param permissions  申请的权限
     * @param grantResults 结果
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_GET_CONTACTS:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                        // 判断用户是否 点击了不再提醒。(检测该权限是否还可以申请)
                        boolean b = shouldShowRequestPermissionRationale(permissions[0]);
                        if (!b) {
                            // 用户还是想用我的 APP 的
                            // 提示用户去应用设置界面手动开启权限
                            showDialogTipUserGoToAppSettting();
                        } else
                            finish();
                    } else {
                        ToastUtils.showToast(this, getResources().getString(R.string.permission_success));
                    }
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    // 提示用户去应用设置界面手动开启权限

    private void showDialogTipUserGoToAppSettting() {

        dialog = new AlertDialog.Builder(this)
                .setTitle(getResources().getString(R.string.permission_not_available))
                .setMessage(getResources().getString(R.string.open_permission_for_setting))
                .setPositiveButton(getResources().getString(R.string.immediately_open), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 跳转到应用设置界面
                        goToAppSetting();
                    }
                })
                .setNegativeButton(getResources().getString(R.string.cancel_text), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }).setCancelable(false).show();
    }

    // 跳转到当前应用的设置界面
    private void goToAppSetting() {
        Intent intent = new Intent();

        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);

        startActivityForResult(intent, 123);
    }

    /**
     * 检查号码格式和短信内容长度
     */
    private boolean checkFormat() {
        String temp = mEtNumber.getText().toString();
        // 防止手机号有空格或横线等特殊字符
        number = CommUtil.filterString(temp);
        content = mEtContent.getText().toString();
        // 判断不为空
        if (CommUtil.isEmpty(number)) {
            ToastUtils.showToast(this, getResources().getString(R.string.phone_number_not_none));
            return false;
        } else if (CommUtil.isEmpty(content)) {
            ToastUtils.showToast(this, getResources().getString(R.string.content_not_empty));
            return false;
            // 判断手机号格式
        } else if (!CommUtil.isPhone(number)) {
            ToastUtils.showToast(this, getResources().getString(R.string.phone_number_iswrong));
            return false;
            // 判断内容长度
        } else if (content.length() > 25) {
            ToastUtils.showToast(this, getResources().getString(R.string.content_overlength));
            return false;
        }
        return true;
    }

    /**
     * 获取选择的联系人
     *
     * @param cursor
     * @return
     */
    private Phones getContactPhone(Cursor cursor) {
        int phoneColumn = cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER);
        Phones phones = null;
        int phoneNum = cursor.getInt(phoneColumn);
        String result = "";
        if (phoneNum > 0) {
            // 获得联系人的ID号
            int idColumn = cursor.getColumnIndex(ContactsContract.Contacts._ID);
            String contactId = cursor.getString(idColumn);
            // 获得联系人电话的cursor
            Cursor phone = getContentResolver().query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + contactId, null, null);
            if (phone.moveToFirst()) {
                for (; !phone.isAfterLast(); phone.moveToNext()) {
                    int numberIndex = phone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                    int nameIndex = phone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                    int typeIndex = phone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE);
                    int phone_type = phone.getInt(typeIndex);
                    phones = new Phones();
                    String number = phone.getString(numberIndex);
                    phones.number = CommUtil.filterString(number);
                    phones.name = phone.getString(nameIndex);
                }
                if (!phone.isClosed()) {
                    phone.close();
                }
            }
        }
        return phones;
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        switch (v.getId()) {
            case R.id.et_receiver_number:
                // 电话号输入框失去焦点, 查找该联系人历史短信消息,填充listview
                if (!hasFocus) {
                    if (phones != null) {
                        // Log.e("error", "abc:" + phones.number);
                        refreshListview(phones.number);
                        mEtNumber.setTextColor(getResources().getColor(R.color.sender_number_col));
                        mEtNumber.setTextSize(18);
                    }
                } else {
                    mEtNumber.setTextColor(getResources().getColor(R.color.black));
                    mEtNumber.setTextSize(16);
                }
                break;
            case R.id.et_sms_content:
                if (hasFocus) {
                    mEtNumber.setTextColor(getResources().getColor(R.color.sender_number_col));
                    mEtNumber.setTextSize(18);
                }
                break;
        }
    }

    /**
     * 数据库查找数据, 填充集合
     */
    private void refreshListview(String phoneNumber) {
        mList.clear();
        ArrayList<SmsMessage> messages = dao.querySmsByPhoneNumber(phoneNumber);
        mList.addAll(messages);

        mAdapter.notifyDataSetChanged();
        // 选中消息最后一条
        mListView.setSelection(mList.size() - 1);
    }

    /**
     * 联系人选择的回调
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_CONTENT:
                if (resultCode == RESULT_OK) {
                    Uri contactData = data.getData();
                    Cursor cursor = getContentResolver().query(contactData, null, null, null, null);
                    cursor.moveToFirst();
                    phones = this.getContactPhone(cursor);
                    Log.e(TAG, "onActivity.Phones=" + phones.toString());
//                    isSelectNumber = true;
                    if (phones != null) {
                        mEtNumber.setText(phones.number);
                    }
                }
                break;
            case REQUEST_GET_CONTACTS:
                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    // 检查该权限是否已经获取
                    int i = ContextCompat.checkSelfPermission(this, permissions[0]);
                    // 权限是否已经 授权 GRANTED---授权  DINIED---拒绝
                    if (i != PackageManager.PERMISSION_GRANTED) {
                        // 提示用户应该去应用设置界面手动开启权限
                        showDialogTipUserGoToAppSettting();
                    } else {
                        if (dialog != null && dialog.isShowing()) {
                            dialog.dismiss();
                        }
                        ToastUtils.showToast(this, getResources().getString(R.string.permission_success));
                    }
                }
                break;
            default:
                break;
        }
    }


    /**
     * 点击右侧取消按钮
     *
     * @param activity
     */
    @Override
    protected void clickRight(Activity activity) {
        super.clickRight(activity);
        this.finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        setKeyboardLoc(mRootView);
        // 注册订阅事件
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dao != null) {
            dao.close();
        }
    }

    public class Phones {
        public String number;
        public String name;

        @Override
        public String toString() {
            return "Phones{" +
                    "number='" + number + '\'' +
                    ", name='" + name + '\'' +
                    '}';
        }

    }


    /**
     * 判断点击的是否是EditText区域
     *
     * @param v
     * @param event
     * @return
     */
    public boolean isShouldHideInput(View v, MotionEvent event) {
        if (v != null && (v instanceof EditText)) {
            int[] leftTop = {0, 0};
            //获取输入框当前的location位置
            v.getLocationInWindow(leftTop);
            int left = leftTop[0];
            int top = leftTop[1];
            int bottom = top + v.getHeight();
            int right = left + v.getWidth();
            if (event.getX() > left && event.getX() < right
                    && event.getY() > top && event.getY() < bottom) {
                // 点击的是输入框区域，保留点击EditText的事件
                return false;
            } else {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (isShouldHideInput(v, ev)) {

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            } else {
                //mListView.setSelection(mList.size()-1);
            }
            return super.dispatchTouchEvent(ev);
        }
        // 必不可少，否则所有的组件都不会有TouchEvent了
        if (getWindow().superDispatchTouchEvent(ev)) {
            return true;
        }
        return onTouchEvent(ev);
    }

}
