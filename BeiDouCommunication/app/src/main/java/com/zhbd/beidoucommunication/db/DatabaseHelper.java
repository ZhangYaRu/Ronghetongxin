package com.zhbd.beidoucommunication.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.zhbd.beidoucommunication.utils.DataProcessingUtil;

/**
 * Created by Administrator on 2017/7/29.
 */

public class DatabaseHelper extends SQLiteOpenHelper {

    //类没有实例化,是不能用作父类构造器的参数,必须声明为静态

    private static final String database_name = "beidoucommunicate_"; //数据库名称

    private static final int version = 1; //数据库版本
    private SQLiteDatabase db;
    /**
     * 创建注册人员表
     */
    private static final String CREATE_USER_TAB =
            "CREATE TABLE IF NOT EXISTS user (" +
                    "_id integer primary key autoincrement, " +
                    "user_id integer unique, " +
                    "nick_name varchar(20), " +
                    "phone_number varchar(12) unique, " +
                    "id_card_number varchar(18) unique, " +
                    "password varchar(6))";

    /**
     * 创建消息表
     */
    private static final String CREATE_MESSAGE_TAB =
            "CREATE TABLE IF NOT EXISTS message (" +
                    "_id integer primary key autoincrement, " +
                    "sender_number integer, " +
                   // "message_number integer, " +
                    "sender_name varchar(15), " +
                    "content text, " +
                    "time varchar(25), " +
                    "second integer, " +
                    "state integer, " +
                    "type integer, " +
                    "is_read integer)";
    /**
     * /**
     * 创建联系人表
     */
    private static final String CREATE_FRIENDS_TAB =
            "CREATE TABLE IF NOT EXISTS friends (" +
                    "_id integer primary key autoincrement, " +
                    "user_id integer, " +
                    "add_type integer, " +
                    "name varchar(20), " +
                    "phone_number varchar(11), " +
                    "id_card varchar(20), " +
                    "sim_number varchar(15))";
    /**
     * 创建短信信息表
     */
    private static final String CREATE_SMS_TAB =
            "CREATE TABLE IF NOT EXISTS sms (" +
                    "_id integer primary key autoincrement, " +
                    "phone_number varchar(12), " +
                    "name varchar(15), " +
                    "content text, " +
                    "send_time varchar(25), " +
                    "state integer, " +
                    "is_read integer)";
    /**
     * 创建邮件表
     */
    private static final String CREATE_EMAIL_TAB =
            "CREATE TABLE IF NOT EXISTS tab_email (" +
                    "_id integer primary key autoincrement, " +
                    "address varchar(50), " +
                    "content text, " +
                    "send_time varchar(25))";


    /**
     * 创建群租信息表
     */
    private static final String CREATE_GROUPINFO_TAB =
            "CREATE TABLE IF NOT EXISTS group_info (" +
                    "_id integer primary key autoincrement, " +
                    "group_id integer, " +
                    "name varchar(25), " +
                    "is_wner integer)";
    /**

    /**
     * 创建ic卡号表
     */
    private static final String CREATE_ICINFO_TAB =
            "CREATE TABLE IF NOT EXISTS ic_info (" +
                    "_id integer primary key autoincrement, " +
                    "ic_number integer, " +
                    "is_control integer, " +   // 0非抑制/1抑制
                    "is_quiesce integer, " +    // 0静默/1非静默
                    "service_frequency integer, " +
                    "grade integer, " +
                    "last_send_time varchar(25))";
    /**
     * 创建群组消息表
     */
    private static final String CREATE_GROUP_MESSAGE_TAB = "CREATE TABLE IF NOT EXISTS group_message (_id integer primary key autoincrement, group_number integer, group_name varchar(25), sender_ic_number integer, sender_name varchar(15), g_msg_content text, g_send_time varchar(25), g_second_num integer, g_msg_state integer, g_msg_type integer, g_read_state integer)";

    public DatabaseHelper(Context context, int userId) {

        //第三个参数CursorFactory指定在执行查询时获得一个游标实例的工厂类,设置为null,代表使用系统默认的工厂类
        super(context, database_name + userId, null, version);
        Log.e("abc",database_name + userId);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        this.db = db;
        db.execSQL(CREATE_USER_TAB);
        db.execSQL(CREATE_MESSAGE_TAB);
        db.execSQL(CREATE_FRIENDS_TAB);
        db.execSQL(CREATE_SMS_TAB);
        db.execSQL(CREATE_EMAIL_TAB);
        db.execSQL(CREATE_GROUPINFO_TAB);
        db.execSQL(CREATE_ICINFO_TAB);


//        db.execSQL(CREATE_GROUP_MESSAGE_TAB);
        //Log.e("aaa","数据库创建了");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}