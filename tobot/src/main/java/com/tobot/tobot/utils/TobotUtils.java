package com.tobot.tobot.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;


import com.tobot.tobot.R;
import com.tobot.tobot.base.Constants;
import com.tobot.tobot.db.bean.AnswerDBManager;
import com.tobot.tobot.db.bean.UserDBManager;
import com.tobot.tobot.presenter.BRealize.BBattery;
import com.tobot.tobot.presenter.BRealize.BFrame;
import com.tobot.tobot.presenter.BRealize.VolumeControl;
import com.tobot.tobot.scene.SceneManager;
import com.turing123.libs.android.resourcemanager.ResourceManager;
import com.turing123.libs.android.resourcemanager.ResourceMap;
import com.turing123.robotframe.multimodal.action.EarActionCode;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Javen on 2017/8/3.
 */

public class TobotUtils {
    /**
     * 判断是否为空，或者全部空格
     * @return
     */
    public static boolean isEmpty(Object obj) {
        return null == obj || "".equals(obj.toString().trim());
    }

    /**
     * 判断是否空白
     * @return
     */
    public static boolean isBlank(Object obj) {
        return null == obj || "".equals(obj.toString());
    }

    /**
     * 判断是否不为空
     * @return
     */
    public static boolean isNotEmpty(Object obj) {
        return !isEmpty(obj);
    }

    /**
     * 判断是否不为空
     * @return
     */
    public static boolean isNotEmpty(Object obj1,Object obj2) {
        if (!isEmpty(obj1) && !isEmpty(obj2)){
            return true;
        }else{
            return false;
        }
    }

    /**
     * 判断是否不为空
     * @return
     */
    public static boolean isEqual(Object obj1,Object obj2) {
        if (obj1.equals(obj2) || obj1 == obj2){
            return true;
        }else{
            return false;
        }
    }



    /**
     * 机器人是否首次使用 true:首次/false:非首次
     * @return
     */
    public static boolean isEmploy(){
        String Ultr = null;
        try {
            Ultr = UserDBManager.getManager().getCurrentUser().getUltr();
        } catch (Exception e) {
            // TODO: handle exception
        }
        if (TobotUtils.isEmpty(Ultr)) {
            return true;
        }else {
            return false;
        }
    }

    /**
     * 机器人是否首次使用 true:首次/false:非首次
     * @return
     */
    public static boolean isEmployFack(){
        String UltrFack = null;
        try {
            UltrFack = UserDBManager.getManager().getCurrentUser().getUltrFack();
        } catch (Exception e) {
            // TODO: handle exception
        }
        if (TobotUtils.isEmpty(UltrFack)) {
            return true;
        }else {
            return false;
        }
    }

    /**
     * 是否在场景
     * @param Scenario
     * @return
     */
    public static boolean isInScenario(String Scenario){
        if (Scenario.equals("os.sys.song") || Scenario.equals("os.sys.story") || Scenario.equals("os.sys.dance")){
            return true;
        }else{
            return false;
        }
    }

    /**
     * 在哪个场景
     * @param Scenario
     * @return
     */
    public static boolean whichScenario(String Scenario){
        if (Scenario.equals("os.sys.song") || Scenario.equals("os.sys.story") || Scenario.equals("os.sys.dance")){
            return true;
        }else{
            return false;
        }
    }

    /**
     * 是否需要执行动作记忆
     * @param action
     * @return
     */
    public static boolean isMemory(int action){
        if (action == 6 || action == 8 || action == 10 || action == 12 || action == 14 || action == 28){
            return true;
        }else{
            return false;
        }
    }

    /**
     * 是否需要执行动作记忆
     * @param action
     * @return
     */
    public static boolean isReset(int action){
        if (action == 1 || action == 7 || action == 9 || action == 11 || action == 13 || action == 15 || action == 29){
            return true;
        }else{
            return false;
        }
    }

    /**
     * 是否在使用播放器
     * @param Scenario
     * @return
     */
    public static boolean isInPlay(int state){
        if (state == SceneManager.STATUS_PLAYING){
            return true;
        }else{
            return false;
        }
    }

    /**
     * 灯圈等级-事件驱动
     * @param ear
     * @param priority
     * @return
     */

    public static boolean isPriority(int ear,int priority){
        pass = false;
        if (priority >= grade){
            pass = true;
            grade = priority;
            lastLamp = ear;
        }else if (priority == 0){
            pass = true;
            grade = priority;
        }
        return pass;
    }
    private static int grade = 1;
    private static boolean pass;
    private static int lastLamp;
    private static boolean rank;

    /**
     * 灯圈等级-颜色驱动
     * @param ear
     * @param priority
     * @return
     */
    public static boolean isRank(int ear,int priority){
        rank = false;
        if ((ear == EarActionCode.EAR_MOTIONCODE_3) && (lastLamp == EarActionCode.EAR_MOTIONCODE_2)){
            rank = true;
            grade = priority;
            lastLamp = ear;
        }
        return rank;
    }

    public static void resetZero(){
        lastLamp = 0;
        grade = 0;
    }





    /**
     * 模糊唤醒
     * @return
     * @param discernASR
     */
    public static boolean isAwaken(String discernASR){
        if (discernASR.contains("小猪小猪") || discernASR.contains("小图小图") || discernASR.contains("小偷小偷")
                || discernASR.contains("晓彤晓彤") || discernASR.contains("小兔小兔") || discernASR.contains("下图下图")
                || discernASR.contains("海豚海豚") || discernASR.contains("插头插头") || discernASR.contains("呷哺呷哺")
                || discernASR.contains("下途下途") || discernASR.contains("下毒下毒") || discernASR.contains("小彭小彭")
                || discernASR.contains("消毒消毒")){
            return true;
        }else{
            return false;
        }
    }

    /**
     *
     * @return
     */
    public static boolean isLocalCommand(Context context,String asr) {
        for (String local : BBattery.getBatteryKeyWords()) {
            if (asr.contains(local)) {
                return true;
            }
        }
        for (String local : getVolumeKeyWords(context)) {
            if (asr.contains(local)) {
                return true;
            }
        }
        return false;
    }

    public static List<String> getVolumeKeyWords(Context context){
        List<String> keyWords=new ArrayList<>();
        String[] array = context.getResources().getStringArray(R.array.volume_keyWords_array);
        for (int i=0;i<array.length;i++){
            keyWords.add(array[i]);
        }
        return keyWords;
    }


    /**
     * 机器人联网状态
     * @return
     */
    public static String isEmployAP(){
        try {
            return UserDBManager.getManager().getCurrentUser().getUltrAP();
        } catch (Exception e) {
            // TODO: handle exceptionre
            return "0";
        }
    }

    /**
     * 获取当前时间戳
     * @return
     */
    public static String getTransform() {
        return String.valueOf(System.currentTimeMillis());
    }

    /**
     * 时间戳转换
     * @param time
     * @return
     */
    public static String transformDateTime(long time) {
        Date date = new Date(time);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return dateFormat.format(date);
    }

    /**
     * 时间戳转换
     * @param time
     * @return
     */
    public static String transformDateTime(String time) {
        Date date = new Date(new Long(time));
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return dateFormat.format(date);
    }

//    /**
//     *
//     * @param time
//     * @return
//     */
//    public static String DateTuanTime(String time) {
//        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        SimpleDateFormat sfd = new SimpleDateFormat("yyyyMMddHHmmss");
//        Date date = new Date();
//        try {
//            date = sfd.parse(time);
//        } catch (ParseException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//        return dateFormat.format(date);
//    }

    /**
     * 获取当前日期 格式：yyyy-MM-dd HH:mm:ss
     * @return
     */
    public static String getCurrentlyDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return dateFormat.format(new Date());
    }

    /**
     * 两个时间相减 格式：yyyy-MM-dd HH:mm:ss
     * @param time1
     * @param time2
     * @return
     */
    public static long DateMinusTime(String time1, String time2) {
        return DateMinusTime(time1,time2,(1000 * 60 * 60 * 24 * 7));//24小时 *7天
    }
    public static long DateMinusTime(String time1, String time2, long time){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date d1 = new Date();
        Date d2 = new Date();
        try {
            d1 = dateFormat.parse(time1);
            d2 = dateFormat.parse(time2);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        long diff = d2.getTime() - d1.getTime();// 这样得到的差值是微秒级别
        long days;
        if (time != 0) {
            days = diff / time;
        }else {
            days = diff;
        }
        return days;
    }

    /**
     * 增加time秒
     * @param time
     * @return
     */
    public static String DateAddTime(String time1,int time){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar calendar = new GregorianCalendar();
        Date date;
        try {
            calendar.setTime(dateFormat.parse(time1));//设置参数时间
            Log.i("Javen","时间转换无误"+calendar.getTime());
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            Log.i("Javen","时间转换错误");
            e.printStackTrace();
        }
        calendar.add(Calendar.SECOND,time);//把日期往后增加SECOND 秒.整数往后推,负数往前移动
        date = calendar.getTime();
        String diff = dateFormat.format(date);//字符型
//        long diff = calendar.getTime().getTime(); //long型
        return diff;
    }


    /**
     *读取文本文件中的内容 I/O
     * @param strFilePath
     * @return
     */
    public static String ReadTxtFile (String strFilePath) throws Exception{
        String path = strFilePath;
        String content = ""; //文件内容字符串
        //打开文件
        File file = new File(path);
        //如果path是传递过来的参数，可以做一个非目录的判断
        if (file.isDirectory()) {
            Log.d("TestFile", "The File doesn't not exist.");
        } else {
            try {
                InputStream instream = new FileInputStream(file);
                if (instream != null) {
                    InputStreamReader inputreader = new InputStreamReader(instream);
                    BufferedReader buffreader = new BufferedReader(inputreader);
                    String line;
                    //分行读取
                    while ((line = buffreader.readLine()) != null) {
                        content += line + "\n";
                    }
                    instream.close();
                }
            } catch (java.io.FileNotFoundException e) {
                Log.d("TestFile", "The File doesn't not exist.");
            } catch (IOException e) {
                Log.d("TestFile", e.getMessage());
            }
        }
        return content;
    }

    /**
     * 按指定行读取文本文件中的内容 I/O
     * @param strFilePath
     * @return
     */
    public static String ReadTxtFile (String strFilePath,int row) throws Exception{
        String path = strFilePath;
        String content = ""; //文件内容字符串
        int currentLine = 0;//当前行
        //打开文件
        File file = new File(path);
        //如果path是传递过来的参数，可以做一个非目录的判断
        if (file.isDirectory()) {
            Log.d("TestFile", "The File doesn't not exist.");
        } else {
            try {
                InputStream instream = new FileInputStream(file);
                if (instream != null) {
                    InputStreamReader inputreader = new InputStreamReader(instream);
                    BufferedReader buffreader = new BufferedReader(inputreader);
                    String line;
                    //分行读取
                    while ((line = buffreader.readLine()) != null) {
                        currentLine++;
                        if (currentLine==row) {
                            content += line + "\n";
                        }
                    }
                    instream.close();
                }
            } catch (java.io.FileNotFoundException e) {
                Log.d("TestFile", "The File doesn't not exist.");
            } catch (IOException e) {
                Log.d("TestFile", e.getMessage());
            }
        }
        Log.i("Javen","readFile:"+content);
        return content;
    }

    /**
     * 按指定行读取文本文件中的内容 FILE
     * @param strFilePath
     * @return
     */
    public static String AssignReadTxtFile (String strFilePath,int row) throws Exception{
        String music;
        StringBuffer stringBuffer = null;
        BufferedReader bufferedReader;
        int currentLine = 0;//当前行
        try {
            stringBuffer = new StringBuffer();
            bufferedReader = new BufferedReader(new FileReader(strFilePath));
            while ((music = bufferedReader.readLine()) != null) {
                currentLine++;
                if (currentLine == row) {
                    stringBuffer.append(music);
                    break;
                }
            }
            bufferedReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.i("Javen","指定行内容:"+stringBuffer.toString());
        return stringBuffer.toString();
    }

    /**
     * 取设备ID
     * @param strFilePath
     * @return
     */
    public static String getDeviceId(String matching, String strFilePath){
        String text = null;
        StringBuffer stringBuffer = null;
        try {
            text = ReadTxtFile(strFilePath);
            String regEx = matching+">(.+)<";
            Pattern pat = Pattern.compile(regEx);
            Matcher mat = pat.matcher(text);
            boolean rs = mat.find();
            stringBuffer = new StringBuffer();
            for(int i=1;i<=mat.groupCount();i++){
                Log.e("Javen","取设备ID:"+i);
                stringBuffer.append(mat.group(i));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stringBuffer.toString();
    }

    /**
     * 取统计
     * @param strFilePath
     * @return
     */
    public static String getGross(String strFilePath){
        String text = null;
        StringBuffer stringBuffer = null;
        try {
            text = AssignReadTxtFile(strFilePath,1);
            String regEx = ":(.+)";
            Pattern pat = Pattern.compile(regEx);
            Matcher mat = pat.matcher(text);
            boolean rs = mat.find();
            stringBuffer = new StringBuffer();
            for(int i=1;i<=mat.groupCount();i++){
                stringBuffer.append(mat.group(i));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.i("Javen","取总量"+stringBuffer.toString());
        return stringBuffer.toString();
    }

    /**
     * 取歌名
     * @param strFilePath
     * @return
     */
    public static String getMusic(String strFilePath) throws Exception{
        String gross,text;
        StringBuffer stringBuffer = null;
        try {
            gross = getGross(strFilePath);
            int assign = (Math.abs(new Random().nextInt())%Integer.parseInt(gross))+2;
            Log.i("Javen","指定行:" + assign);
            text = AssignReadTxtFile(strFilePath,assign);
            String regEx = "\\d+\\s+(.*)";
            Pattern pattern = Pattern.compile(regEx);
            Matcher matcher = pattern.matcher(text);
            stringBuffer = new StringBuffer();
            if (matcher.find()) {
                Log.i("Javen","matcher.group(1):" + matcher.group(1));
                stringBuffer.append(matcher.group(1));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stringBuffer.toString();
    }

//    /**
//     * 取歌名
//     * @param strFilePath
//     * @return
//     */
//    public static String getMusic(String strFilePath) throws Exception{
//        String gross,music;
//        StringBuffer stringBuffer = null;
//        BufferedReader bufferedReader;
//        int currentLine = 0;//当前行
//        try {
//            gross = getGross(strFilePath);
//            int assign = (Math.abs(new Random().nextInt())%Integer.parseInt(gross))+2;
//            Log.i("Javen","指定行:" + assign);
//            stringBuffer = new StringBuffer();
//            bufferedReader = new BufferedReader(new FileReader(strFilePath));
//            String regEx = "\\d+\\s+(.*)";
//            Pattern pattern = Pattern.compile(regEx);
//            while ((music = bufferedReader.readLine()) != null) {
//                currentLine++;
//                if (currentLine == assign) {
//                    Matcher matcher = pattern.matcher(music);
//                    if (matcher.find()) {
//                        Log.i("Javen","matcher.group(1):" + matcher.group(1));
//                        stringBuffer.append(matcher.group(1));
//                    }
//                    break;
//                }
//            }
//            bufferedReader.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return stringBuffer.toString();
//    }



    public static String getIPAddress(Context context) {
        NetworkInfo info = ((ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        if (info != null && info.isConnected()) {
            if (info.getType() == ConnectivityManager.TYPE_MOBILE) {//当前使用2G/3G/4G网络
                try {
                    //Enumeration<NetworkInterface> en=NetworkInterface.getNetworkInterfaces();
                    for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                        NetworkInterface intf = en.nextElement();
                        for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                            InetAddress inetAddress = enumIpAddr.nextElement();
                            if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                                //mohuaiyuan 20180108 原来的代码
//                                BFrame.TTS("本机当前IP地址为:"+inetAddress.getHostAddress());
                                //mohuaiyuan 20180108 新的代码 20180108
                                String speech="speech:本机当前IP地址为:"+inetAddress.getHostAddress();
                                try {
                                    BFrame.response(speech);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                return inetAddress.getHostAddress();
                            }
                        }
                    }
                } catch (SocketException e) {
                    e.printStackTrace();
                }

            } else if (info.getType() == ConnectivityManager.TYPE_WIFI) {//当前使用无线网络
                WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                String ipAddress = intIP2StringIP(wifiInfo.getIpAddress());//得到IPV4地址
                BFrame.TTS("本机当前IP地址为:"+ipAddress);
                return ipAddress;
            }
        } else {
            //当前无网络连接,请在设置中打开网络
        }
        return null;
    }

    /**
     * 将得到的int类型的IP转换为String类型
     *
     * @param ip
     * @return
     */
    public static String intIP2StringIP(int ip) {
        return (ip & 0xFF) + "." +
                ((ip >> 8) & 0xFF) + "." +
                ((ip >> 16) & 0xFF) + "." +
                (ip >> 24 & 0xFF);
    }


    /**
     *
     * @param cmds
     * @throws Exception
     */
    public static void doCmds(List<String> cmds) throws Exception {
        String result = "";
        Process process = Runtime.getRuntime().exec("su");
        DataOutputStream dos = new DataOutputStream(process.getOutputStream());
        DataInputStream dis = new DataInputStream(process.getInputStream());
        for (String tmpCmd : cmds) {
            dos.writeBytes(tmpCmd+"\n");
        }
        dos.writeBytes("exit\n");
        dos.flush();
        dos.close();
        String line = null;
        while ((line = dis.readLine()) != null) {
            Log.d("Javen","adb result" + line);
            result += line;
        }
        process.waitFor();
    }


    /**
     * 获取MAC地址
     * @return
     */
    public static String getMacAddress(){
        /*获取mac地址有一点需要注意的就是android 6.0版本后，以下注释方法不再适用，不管任何手机都会返回"02:00:00:00:00:00"这个默认的mac地址，这是googel官方为了加强权限管理而禁用了getSYstemService(Context.WIFI_SERVICE)方法来获得mac地址。*/
//        String macAddress= "";
//        WifiManager wifiManager = (WifiManager) MyApp.getContext().getSystemService(Context.WIFI_SERVICE);
//        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
//        macAddress = wifiInfo.getMacAddress();
//        return macAddress;
        String macAddress = null;
        StringBuffer buf = new StringBuffer();
        NetworkInterface networkInterface = null;
        try {
            networkInterface = NetworkInterface.getByName("eth1");
            if (networkInterface == null) {
                networkInterface = NetworkInterface.getByName("wlan0");
            }
            if (networkInterface == null) {
                return "02:00:00:00:00:02";
            }
            byte[] addr = networkInterface.getHardwareAddress();
            for (byte b : addr) {
                buf.append(String.format("%02X:", b));
            }
            if (buf.length() > 0) {
                buf.deleteCharAt(buf.length() - 1);
            }
            macAddress = buf.toString();
        } catch (SocketException e) {
            e.printStackTrace();
            return "02:00:00:00:00:02";
        }
        return macAddress;
    }


    public static Integer getTimeIndex() {
        Calendar c = Calendar.getInstance();// 可以对每个时间域单独修改
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int date = c.get(Calendar.DATE);
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        int second = c.get(Calendar.SECOND);
        System.out.println(year + "/" + month + "/" + date + " " + hour + ":"
                + minute + ":" + second);

        int index = 0;
        int minutes = hour * 60 + minute;
        System.out.println("minutes:" + minutes);
        String str = "";
        if (minutes > 360 && minutes < 661) {
            str = "早上";
            index = 0;
        } else if (minutes > 660 && minutes < 841) {
            str = "中午";
            index = 1;
        } else if (minutes > 840 && minutes < 1111) {
            str = "下午";
            index = 2;
        } else {
            str = "晚上";
            index = 3;
        }

        return index;
    }


    public static void DBClear(){
        UserDBManager.getManager().clear();
        AnswerDBManager.getManager().clear();
    }

    private static boolean REPORT_IP = false;
    private static boolean OPEN_BLUETOOTH = false;
    private static boolean IS_DEBUG = false;

    /**
     * 开发模式
     * @return
     */
    public static boolean isDebug(){
        return IS_DEBUG;
    }

    public static boolean isOpenBluetooth(){
        return OPEN_BLUETOOTH;
    }

    public static boolean isReportIp(){
        return REPORT_IP;
    }

}
