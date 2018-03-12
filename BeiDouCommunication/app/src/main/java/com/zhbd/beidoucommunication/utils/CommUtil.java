package com.zhbd.beidoucommunication.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Dialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.media.CamcorderProfile;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.zhbd.beidoucommunication.MyApplication;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.content.Context.ACTIVITY_SERVICE;
import static android.media.CamcorderProfile.get;

/**
 * Created by zhangyaru on 2017/8/23.
 * 常用工具类
 */
public class CommUtil {

    private static ThreadLocal<SimpleDateFormat> DateLocal = new ThreadLocal<SimpleDateFormat>();

    /**
     * 判断某个服务是否正在运行的方法
     *
     * @param serviceName 是包名+服务的类名（例如：net.loonggg.testbackstage.TestService）
     * @return true代表正在运行，false代表服务没有正在运行
     */
    public static boolean isServiceWork(String serviceName) {

        boolean isWork = false;
        ActivityManager myAM = (ActivityManager) MyApplication.getContextObject()
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> myList = myAM.getRunningServices(40);
        if (myList.size() <= 0) {
            return false;
        }
        for (int i = 0; i < myList.size(); i++) {
            String mName = myList.get(i).service.getClassName().toString();
            if (mName.equals(serviceName)) {
                isWork = true;
                break;
            }
        }
        return isWork;
    }


    /**
     * 检测某ActivityUpdate是否在当前Task的栈顶
     */

    public static boolean isTopActivy(String cmdName) {
        Context context = MyApplication.getContextObject();
        ActivityManager manager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> runningTaskInfos = manager.getRunningTasks(1);
        String cmpNameTemp = null;
        if (null != runningTaskInfos) {
            cmpNameTemp = (runningTaskInfos.get(0).topActivity).toString(); //这里cmpNameTemp是package name/class name
            Log.e("CommUtils", "cmpNameTemp:" + cmpNameTemp);
        }
        if (null == cmpNameTemp)
            return false;
        return cmpNameTemp.contains(cmdName); //如果cmdName是package name得用contains，但是cmdName是package name/class name，则用equals
    }


    /**
     * 获取.spx文件路径
     *
     * @param filePath
     * @return
     */
    public static String getSpxPath(String filePath) {
        String spxName = filePath.substring(0, filePath.lastIndexOf('.'));
        spxName += ".spx";
        return spxName;
    }

    /**
     * 获取.raw文件路径
     *
     * @param filePath
     * @return
     */
    public static String getRawPath(String filePath) {
        String spxName = filePath.substring(0, filePath.lastIndexOf('.'));
        spxName += ".raw";
        return spxName;
    }

    /**
     * 获取.wav文件路径
     *
     * @param filePath
     * @return
     */
    public static String getWavPath(String filePath) {
        String spxName = filePath.substring(0, filePath.lastIndexOf('.'));
        spxName += ".wav";
        return spxName;
    }

    /*
     * 根据电话号码取得联系人姓名
     */
    public static String getContactNameByPhoneNumber(String address) {
        Cursor cursorOriginal =
                MyApplication.getContextObject().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
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

    /**
     * 发送消息时，获取当前时间
     *
     * @return 当前时间
     */
    public static String getDate() {
//        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        // 24小时制
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }

    /**
     * 固定格式解析时间字符串
     * @param time
     * @return
     */
    public static long parseDate(String time) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date parse = null;
        try {
            parse = sdf.parse(time);
        } catch (ParseException e) {
            return -1;
        }
        long msec = parse.getTime();
        return msec;
    }

    /**
     * 初始化ic卡最初时间
     *
     * @return 1970-1-1
     */
    public static String initDate() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        return format.format(new Date(0));
    }

    /**
     * 判断是否为今天(效率比较高)
     *
     * @param day 传入的 时间  "2016-06-28 10:10:30" "2016-06-28" 都可以
     * @return true今天 false不是
     * @throws ParseException
     */
    public static boolean isToday(String day) throws ParseException {

        Calendar pre = Calendar.getInstance();
        Date predate = new Date(System.currentTimeMillis());
        pre.setTime(predate);
        Calendar cal = Calendar.getInstance();
        Date date = getDateFormat().parse(day);
        cal.setTime(date);
        if (cal.get(Calendar.YEAR) == (pre.get(Calendar.YEAR))) {
            int diffDay = cal.get(Calendar.DAY_OF_YEAR)
                    - pre.get(Calendar.DAY_OF_YEAR);

            if (diffDay == 0) {
                return true;
            }
        }
        return false;
    }

    public static SimpleDateFormat getDateFormat() {
        if (null == DateLocal.get()) {
            DateLocal.set(new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA));
        }
        return DateLocal.get();
    }

    /**
     * 得到设备屏幕的宽度
     */
    public static int getScreenWidth(Context context) {
        return context.getResources().getDisplayMetrics().widthPixels;
    }

    /**
     * 得到设备屏幕的高度
     */
    public static int getScreenHeight(Context context) {
        return context.getResources().getDisplayMetrics().heightPixels;
    }

    /**
     * 得到设备的密度
     */
    public static float getScreenDensity(Context context) {
        return context.getResources().getDisplayMetrics().density;
    }

    /**
     * 把密度转换为像素
     */
    public static int dip2px(Context context, float px) {
        final float scale = getScreenDensity(context);
        return (int) (px * scale + 0.5);
    }


    /**
     * 是不是手机号码，以13，14，15，17，18开头
     *
     * @param phone
     * @return
     */
    public static boolean isPhone(String phone) {
//        String regEx = "^(13[0-9])|(14[5,7,9])|(15[^4,\\D])|((17[0-9])|(18[0,5-9]))\\d{8}$";
        String regEx = "^[1]{1}[3,4,5,7,8]{1}[0-9]{9}$";
        return Pattern.matches(regEx, phone);
    }

    /**
     * 是不是邮箱地址
     *
     * @param address
     * @return
     */
    public static boolean isEmailAddress(String address) {
        String regEx = "^\\w+((-\\w+)|(\\.\\w+))*\\@[A-Za-z0-9]+((\\.|-)[A-Za-z0-9]+)*\\.[A-Za-z0-9]+$";
        return address.matches(regEx);
    }

    /**
     * 判断身份证号格式
     * 由15位数字或18位数字（17位数字加“x”）组成，15位纯数字没什么好说的，18位的话，
     * 可以是18位纯数字，或者17位数字加“x”
     *
     * @param idCard
     * @return
     */
    public static boolean isIdCard(String idCard) {
        String regEx1 = "^[1-9]\\d{7}((0\\d)|(1[0-2]))(([0|1|2]\\d)|3[0-1])\\d{3}$";
        String regEx2 = "^[1-9]\\d{5}[1-9]\\d{3}((0\\d)|(1[0-2]))(([0|1|2]\\d)|3[0-1])\\d{3}([0-9]|X|x)$";
        return idCard.matches(regEx1) || idCard.matches(regEx2);
    }

    /**
     * 判断是否是6位数字组成
     *
     * @param str
     * @return
     */
    public static boolean isNumber(String str) {
        if (str.length() != 6) {
            return false;
        }
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(str);
        if (!isNum.matches()) {
            return false;
        }

        return true;
    }


    /**
     * 特殊字符过滤，只允许字母、数字、中文
     *
     * @param str
     * @return
     */
    public static boolean isString(String str) {
        String regEx = "^[A-Za-z0-9\u4e00-\u9fa5]+$";
        return Pattern.compile(regEx).matcher(str).find();
    }

    /**
     * 电话号码格式规范,去除特殊字符,或+86这样的
     *
     * @param str
     * @return
     */
    public static String filterString(String str) {
        // 去除前面的前缀
        str.replaceAll("/+86", "");
        // 只保留数字部分
        String regEx = "[^(0-9)]";
        return str.replaceAll(regEx, "");
    }

    /**
     * 输入昵称的时候判断是否是字母和数字或是汉字(不能是特殊字符)
     *
     * @return true 表示不正确
     */
    public static boolean isCheckName(String text) {
        return !text.matches("^[\\u4E00-\\u9FA5\\uf900-\\ufa2d\\w\\.\\s]{2,10}$");
        //return !text.matches("^([\u4e00-\u9fa5]+|[a-zA-Z0-9]+)$");
    }

    /**
     * 输入密码的时候判断是否是字母和数字
     *
     * @return true 表示不正确
     */
    public static boolean isLimit(String text) {
        return !text.matches("^[a-zA-Z0-9]*");
    }

    /**
     * 返回当前系统语音环境是否是英文
     *
     * @param context
     * @return true为英文 false 为中文
     */
    public static boolean isEnLanguage(Context context) {
        Locale locale = context.getResources().getConfiguration().locale;
        String language = locale.getLanguage();
        if (language.endsWith("en"))
            return true;
        else
            return false;
    }

    /**
     * 检查当前网络是否可用
     *
     * @return true:可用 false:不可用
     */
    public static boolean isNetWorkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null)
            return false;
        boolean wifi = false;
        boolean internet = false;
        if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI) == null) {
            wifi = false;
        } else {
            wifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnectedOrConnecting();
        }
        if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE) == null) {
            internet = false;
        } else {
            internet = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnectedOrConnecting();
        }
        if (wifi || internet) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 得到一个字符串的长度,显示的长度,一个汉字或日韩文长度为2,英文字符长度为1
     *
     * @return boolean true为超过20或是少于4，false为符合要求
     * @params 需要得到长度的字符串
     */
    public static boolean getLength(String s) {
        int valueLength = 0;
        String chinese = "[\u4e00-\u9fa5]";
        // 获取字段值的长度，如果含中文字符，则每个中文字符长度为2，否则为1
        for (int i = 0; i < s.length(); i++) {
            // 获取一个字符
            String temp = s.substring(i, i + 1);
            // 判断是否为中文字符
            if (temp.matches(chinese)) {
                // 中文字符长度为1
                valueLength += 2;
            } else {
                // 其他字符长度为0.5
                valueLength += 1;
            }
        }
        // 进位取整
        int len = (int) Math.ceil(valueLength);
        if (len < 4 || len > 20) {
            return true;
        }
        return false;
//		return (int)Math.ceil(valueLength);
    }

    /**
     * 创建文件夹
     *
     * @param dir
     */
    public static void createFileDir(String dir) {
        File file = new File(dir);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    /**
     * 删除文件夹
     *
     * @param folderPath 文件夹完整绝对路径
     */
    public static void delFileDir(String folderPath) {
        try {
            delAllFile(folderPath); // 删除完里面所有内容
            String filePath = folderPath;
            filePath = filePath.toString();
            File myFilePath = new File(filePath);
            myFilePath.delete(); // 删除空文件夹
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 删除指定文件夹下所有文件
    // param path 文件夹完整绝对路径
    public static boolean delAllFile(String path) {
        boolean flag = false;
        File file = new File(path);
        if (!file.exists()) {
            return flag;
        }
        if (!file.isDirectory()) {
            return flag;
        }
        String[] tempList = file.list();
        File temp = null;
        for (int i = 0; i < tempList.length; i++) {
            if (path.endsWith(File.separator)) {
                temp = new File(path + tempList[i]);
            } else {
                temp = new File(path + File.separator + tempList[i]);
            }
            if (temp.isFile()) {
                temp.delete();
            }
            if (temp.isDirectory()) {
                delAllFile(path + "/" + tempList[i]);// 先删除文件夹里面的文件
                delFileDir(path + "/" + tempList[i]);// 再删除空文件夹
                flag = true;
            }
        }
        return flag;
    }

    /**
     * 删除文件
     */
    public static void deleteFile(String fileName) {
        File file = new File(fileName);
        file.delete();
    }

    public static boolean isWifi(Context mContext) {
        ConnectivityManager connectivityManager = (ConnectivityManager) mContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
        if (activeNetInfo != null
                && activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI) {
            return true;
        }
        return false;
    }

    /**
     * 如果为空，返回默认值
     *
     * @param s
     * @param defValue
     * @return
     * @see {@link #isEmpty(Object)}
     */
    public static String getString(String s, String defValue) {
        return isEmpty(s) ? defValue : s;
    }

    /**
     * 如果为空，返回空串
     *
     * @param s
     * @return
     * @see {@link #isEmpty(Object)}
     */
    public static String getString(String s) {
        return getString(s, "");
    }

    /**
     * 是否为空，null、空串、字符串"null"均判断为空
     *
     * @param o
     * @return
     */
    public static boolean isEmpty(Object o) {
        return o == null || "".equals(o.toString().trim()) || "null".equalsIgnoreCase(o.toString().trim());
    }

    /**
     * 如果是null，返回空
     *
     * @param o
     * @return
     */
    public static String null2Space(Object o) {
        return isEmpty(o) ? "" : o.toString().trim();
    }

    public static byte[] intList2byteArray(List<Integer> list) {
        int size = list != null ? list.size() : 0;
        byte[] result = new byte[size];
        if (size > 0) {
            for (int i = 0; i < size; i++) {
//				result[i] = (byte)(int)list.get(i);
                result[i] = 80;
            }
//			Byte[] b = list.toArray(new Byte[size]);
//			for (int i = 0; i < b.length; i++) {
////				result[i] = b[i];
//				result[i] = 80;
//			}
        }
        return result;
    }


//    /**
//     * 获取登录app类型
//     *
//     * @return
//     */
//    public static String getLoginAppType() {
//        if (Constants.IS_WANFU) {
//            return Constants.lOGIN_APP_TYPR_WANFU;
//        }
//        return Constants.lOGIN_APP_TYPR_USER;
//    }


    /**
     * 保存图片到系统相册
     *
     * @param context
     * @param bmp
     */
    public static void saveImageToGallery(Context context, Bitmap bmp) {
        // 首先保存图片
        File appDir = new File(Environment.getExternalStorageDirectory(), "mhealth365");
        if (!appDir.exists()) {
            appDir.mkdir();
        }
        String fileName = System.currentTimeMillis() + ".jpg";
        File file = new File(appDir, fileName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bmp.compress(CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 其次把文件插入到系统图库
        try {
            MediaStore.Images.Media.insertImage(context.getContentResolver(), file.getAbsolutePath(), fileName, null);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        // 最后通知图库更新
        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(new File(file.getPath()))));
    }

    /**
     * 将图片插入到系统图库
     *
     * @param context
     * @param file
     */
    public static void insertImage2Gallery(Context context, File file) {
        String fileName = System.currentTimeMillis() + ".jpg";
        try {
            MediaStore.Images.Media.insertImage(context.getContentResolver(), file.getAbsolutePath(), fileName, null);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        // 通知图库更新
        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
    }

    /**
     * 设置弹出框宽度（占屏幕宽度的百分比）
     *
     * @param activity
     * @param dialog
     * @param f        占屏幕宽度的百分比
     */
    @SuppressWarnings("deprecation")
    public static void setDialogWidth(Activity activity, Dialog dialog, float f) {
        Window window = dialog.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = (int) (activity.getWindowManager().getDefaultDisplay().getWidth() * f);
        window.setAttributes(lp);
    }

    /**
     * 设置view的宽度
     *
     * @param v
     * @param width
     */
    public static void setWidth(View v, int width) {
        ViewGroup.LayoutParams lp = v.getLayoutParams();
        lp.width = width;
        v.setLayoutParams(lp);
    }

    /**
     * 设置view的高度
     *
     * @param v
     * @param height
     */
    public static void setHeight(View v, int height) {
        ViewGroup.LayoutParams lp = v.getLayoutParams();
        lp.height = height;
        v.setLayoutParams(lp);
    }

    /**
     * 设置view的宽度和高度
     *
     * @param v
     * @param width
     * @param height
     */
    public static void setSize(View v, int width, int height) {
        ViewGroup.LayoutParams lp = v.getLayoutParams();
        lp.width = width;
        lp.height = height;
        v.setLayoutParams(lp);
    }

    /**
     * drawable转为bitmap
     *
     * @param drawable
     * @return
     */
    public static Bitmap drawable2Bitmap(Drawable drawable) {
        int width = drawable.getIntrinsicHeight();
        int height = drawable.getIntrinsicHeight();

        Bitmap bitmap = Bitmap.createBitmap(width, height, drawable.getOpacity() != PixelFormat.OPAQUE ? Config.ARGB_8888 : Config.RGB_565);

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, width, height);
        drawable.draw(canvas);

        return bitmap;
    }

    /**
     * 获取圆角bitmap
     *
     * @param bitmap
     * @param roundPX
     * @return
     */
    public static Bitmap getRoundCornerBitmap(Bitmap bitmap, float roundPX) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        Bitmap bitmap2 = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap2);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, width, height);
        final RectF rectF = new RectF(rect);

        paint.setColor(color);
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        canvas.drawRoundRect(rectF, roundPX, roundPX, paint);

        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return bitmap2;
    }

    /**
     * 解析jsons
     *
     * @param jsons
     * @param stringName 字段名
     * @return
     */
    public static String analysis(String jsons, String stringName) {
        JSONObject jsonSs;
        try {
            jsonSs = new JSONObject(jsons);
            return jsonSs.getString(stringName);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static boolean isAge(String ageStr) {
        if (ageStr == null || ageStr.equals("")) {
            return true;
        }
        int age = 0;
        try {
            age = Integer.parseInt(ageStr);
        } catch (Exception e) {
            return false;
        }
        if (age >= 0 && age <= 200) {
            return true;
        } else {
            return false;
        }
    }

    public static JSONObject getResult(String content) {
        JSONObject result = null;
        try {
            result = new JSONObject(content);
        } catch (Exception e) {
        }
        return result;
    }

    /**
     * 判断是否是手机号
     *
     * @param mobile
     * @return
     */
    public static boolean isMobile(String mobile) {
        Pattern pattern = Pattern.compile("\\d{11}");
        Matcher matcher = pattern.matcher(mobile);
        boolean b = matcher.matches();
        if (b)
            return true;
        else
            return false;
    }

    public static boolean parseBoolean(String str) {
        try {
            return Boolean.parseBoolean(str);
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean[] getBooleanArray(String str, String split) {
        String[] booleans = str.split(split);
        boolean[] vals = new boolean[3];
        if (booleans != null) {
            vals = new boolean[booleans.length];
            for (int i = 0; i < booleans.length; i++) {
                vals[i] = parseBoolean(booleans[i]);
            }
            return vals;
        }
        return vals;
    }

    public static String multiChioceItems2String(String[] datas, boolean[] flags) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < datas.length; i++) {
            String str = datas[i];
            if (flags[i]) {
                builder.append(str + ",");
            }
        }
        if (builder.length() > 1) {
            builder.deleteCharAt(builder.length() - 1);
        }
        if (builder.length() == 0) {
            return "无";
        }
        return builder.toString();
    }

//	/**
//	 * 解压一个压缩文档 到指定位置
//	 * 
//	 * @param zipFileString
//	 *            压缩包的名字
//	 * @param outPathString
//	 *            指定的路径
//	 * @throws Exception
//	 */
//	public static void unZip(String zipFileString, String outPathString,String fileName) {
//		try {
////			List<File> files = ZipUtil.GetFileList(zipFileString, false, true);
////			File file = new File(outPathString +"/"+ files.get(0).getName());
//			File file = new File(outPathString +"/"+ fileName);
//			if (file.exists()) {
//				ZipUtil.UnZipFolder(zipFileString, outPathString,fileName.replace(".zip", ".MED"));
//			} else {
//				ZipUtil.UnZipFolder(zipFileString, outPathString,fileName.replace(".zip", ".MED"));
//			}
//
//		} catch (Exception e1) {
//			e1.printStackTrace();
//		}
//	}

    /**
     * 通过Uri获取实际路径
     *
     * @param context
     * @param uri
     * @return
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static String getPath(final Context context, final Uri uri) {
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
        //针对部分安卓4.4系统手机返回的Uri格式为content://com.android.providers.media.documents/document/image:3951的处理
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            } else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            } else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{split[1]};

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {//部分手机(如华为P1)返回的Uri路径格式为file://
            return uri.getPath();
        }
        return null;
    }

    /**
     * 获取实际路径
     *
     * @param context
     * @param uri
     * @param selection
     * @param selectionArgs
     * @return
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String[] proj = {MediaStore.Images.Media.DATA};

        try {
            cursor = context.getContentResolver().query(uri, proj, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int columnIndex = cursor.getColumnIndexOrThrow(proj[0]);
                return cursor.getString(columnIndex);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * 检查当前网络是否可用
     *
     * @return true:可用  false:不可用
     */
    public static boolean netWorkIsAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null)
            return false;
        boolean wifi = false;
        boolean internet = false;
        if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI) == null) {
            wifi = false;
        } else {
            wifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnectedOrConnecting();
        }
        if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE) == null) {
            internet = false;
        } else {
            internet = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnectedOrConnecting();
        }
        if (wifi || internet) {
            return true;
        } else {
            return false;
        }
    }

    /* * @category 判断是否有外网连接（普通方法不能判断外网的网络是否连接，比如连接上局域网）
        * @return
    */
    public static final boolean ping() {

        String result = null;
        try {
            String ip = "www.baidu.com";// ping 的地址，可以换成任何一种可靠的外网
            Process p = Runtime.getRuntime().exec("ping -c 3 -w 100 " + ip);// ping网址3次
            // 读取ping的内容，可以不加
            InputStream input = p.getInputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(input));
            StringBuffer stringBuffer = new StringBuffer();
            String content = "";
            while ((content = in.readLine()) != null) {
                stringBuffer.append(content);
            }
            Log.d("------ping-----", "result content : " + stringBuffer.toString());
            // ping的状态
            int status = p.waitFor();
            if (status == 0) {
                result = "success";
                return true;
            } else {
                result = "failed";
            }
        } catch (IOException e) {
            result = "IOException";
        } catch (InterruptedException e) {
            result = "InterruptedException";
        } finally {
            Log.d("----result---", "result = " + result);
        }
        return false;
    }

    /**
     * 设置状态栏颜色 * * @param activity 需要设置的activity * @param color 状态栏颜色值
     */

    public static void setColor(Activity activity, int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // 设置状态栏透明
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            // 生成一个状态栏大小的矩形
            View statusView = createStatusView(activity, color);
            // 添加 statusView 到布局中
            ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
            decorView.addView(statusView);
            // 设置根布局的参数
            ViewGroup rootView = (ViewGroup) ((ViewGroup) activity.findViewById(android.R.id.content)).getChildAt(0);
            rootView.setFitsSystemWindows(true);
            rootView.setClipToPadding(true);
        }
    }

    /**
     * 生成一个和状态栏大小相同的矩形条 * * @param activity 需要设置的activity * @param color 状态栏颜色值 * @return 状态栏矩形条
     */
    private static View createStatusView(Activity activity, int color) {
        // 获得状态栏高度
        int resourceId = activity.getResources().getIdentifier("status_bar_height", "dimen", "android");
        int statusBarHeight = activity.getResources().getDimensionPixelSize(resourceId);

        // 绘制一个和状态栏一样高的矩形
        View statusView = new View(activity);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                statusBarHeight);
        statusView.setLayoutParams(params);
        statusView.setBackgroundColor(color);
        return statusView;
    }

    /**
     * 使状态栏透明 * <p> * 适用于图片作为背景的界面,此时需要图片填充到状态栏 * * @param activity 需要设置的activity
     */
    public static void setTranslucent(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // 设置状态栏透明
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            // 设置根布局的参数
            ViewGroup rootView = (ViewGroup) ((ViewGroup) activity.findViewById(android.R.id.content)).getChildAt(0);
            rootView.setFitsSystemWindows(true);
            rootView.setClipToPadding(true);
        }
    }
}