package com.tobot.tobot.utils.socketblock;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.tobot.tobot.MainActivity;
import com.tobot.tobot.base.Constants;
import com.tobot.tobot.base.UpdateAnswer;
import com.tobot.tobot.control.Demand;
import com.tobot.tobot.control.demand.DemandModel;
import com.tobot.tobot.db.bean.UserDBManager;
import com.tobot.tobot.db.model.User;
import com.tobot.tobot.presenter.BRealize.BFrame;
import com.tobot.tobot.utils.TobotUtils;
import com.tobot.tobot.utils.Transform;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import static com.tobot.tobot.utils.socketblock.Const.HEART;

/**
 * Created by TAG on 2017/10/9.
 */
public class SocketConnectCoherence {
    private String TAG = "Javen Socket";
    private WeakReference<Socket> mSocket;
    private ReadThread mReadThread;
    private long sendTime = 0L;
    private static final long HEART_BEAT_RATE = 20 * 1000;
    public static final String HOST = "39.108.134.20";
    public static final int PORT = 81;
    private static DemandListener mdemandListener;
    private DemandModel model = new DemandModel();
    private Timer desenoTimer = new Timer(true);//休眠时间
    private boolean isRegister;
    private Demand mDemand;
    private static SocketConnectCoherence mCoherence;
    // For heart Beat
    private Handler mHandler = new Handler();

    private SocketConnectCoherence(){
        new InitSocketThread().start();
    }

    public static synchronized SocketConnectCoherence instance() {
        if (mCoherence == null) {
            mCoherence = new SocketConnectCoherence();
            Demand.instance(MainActivity.mContext).setDemand(mCoherence);
        }
        return mCoherence;
    }

    public void sendData(){
        if (!isRegister){
            boolean isSuccess = sendMsg(Joint.setRegister());
            if (isSuccess){
                Log.i(TAG,"TCP注册请求发送成功:");
            }
        }
    }

    public boolean sendMsg(String msg) {
        if (null == mSocket || null == mSocket.get()) {
            return false;
        }
        Socket soc = mSocket.get();
        try {
            if (!soc.isClosed() && !soc.isOutputShutdown()) {
                OutputStream os = soc.getOutputStream();
                os.write(Transform.HexString2Bytes(msg));
                os.flush();
                sendTime = System.currentTimeMillis();//每次发送成数据，就改一下最后成功发送的时间，节省心跳间隔时间
            } else {
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private void initSocket() {//初始化Socket
        try {
            Socket so = new Socket(HOST, PORT);
            mSocket = new WeakReference<Socket>(so);
            mReadThread = new ReadThread(so);
            mReadThread.start();
            sendMsg(Joint.setRegister());
            mHandler.postDelayed(heartBeatRunnable, HEART_BEAT_RATE);//初始化成功后，就准备发送心跳包
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void releaseLastSocket(WeakReference<Socket> mSocket) {
        try {
            if (null != mSocket) {
                Socket sk = mSocket.get();
                if (!sk.isClosed()) {
                    sk.close();
                }
                sk = null;
                mSocket = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class InitSocketThread extends Thread {
        @Override
        public void run() {
            super.run();
            initSocket();
        }
    }

    private Runnable heartBeatRunnable = new Runnable() {

        @Override
        public void run() {
            if (System.currentTimeMillis() - sendTime >= HEART_BEAT_RATE) {
                boolean isSuccess = sendMsg(HEART);//就发送一个心跳包过去 如果发送失败，就重新初始化一个socket
                if (!isSuccess) {
                    Log.i(TAG,"心跳包发送失败:");
                    isRegister = false;
                    mHandler.removeCallbacks(heartBeatRunnable);
                    mReadThread.release();
                    releaseLastSocket(mSocket);
                    new InitSocketThread().start();
                }else {
                    isRegister = true;
                    Log.i(TAG,"心跳包发送成功:");
                }
            }
            mHandler.postDelayed(this, HEART_BEAT_RATE);
        }
    };

    // Thread to read content from Socket
    class ReadThread extends Thread {
        private WeakReference<Socket> mWeakSocket;
        private boolean isStart = true;

        public ReadThread(Socket socket) {
            mWeakSocket = new WeakReference<Socket>(socket);
        }

        public void release() {
            isStart = false;
            releaseLastSocket(mWeakSocket);
        }

        @Override
        public void run() {
            super.run();
            Socket socket = mWeakSocket.get();
            if (null != socket) {
                try {
                    InputStream is = socket.getInputStream();
                    byte[] buffer = new byte[1024 * 2];
                    int length = 0;
                    while (!socket.isClosed() && !socket.isInputShutdown() && isStart && ((length = is.read(buffer)) != -1))
                        if (length > 0) {
                            String message = new String(Arrays.copyOf(buffer, length),"GB2312");
                            Log.i(TAG, "Socket回应消息:"+message);
                            //收到服务器过来的消息
                            resolveMessage(message);
                        }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void resolveMessage(String message) {
        if (message.equals("[12]")) {//处理心跳回复

        } else if (message.equals("[10]")) {
//          new InitSocketThread().start();
        } else if (message.equals("[1100011960B]")) {//注册成功

        } else if (message.equals("[1100010160E]")) {//注册失败
            sendMsg(Joint.setRegister());
        } else {
            messageDispose(message,message.substring(2, 3));
        }
    }

    private void messageDispose(String message, String function){
        switch (function){
            case "3"://拍照
                sendMsg(Joint.setResponse(Joint.PHOTO, message));
                dispose(13,message);
                break;
            case "4"://点播
                sendMsg(Joint.setDemandResponse(message));
                dispose(4,message);
                break;
            case "5"://角色自定义
                sendMsg(Joint.setRoleResponse());
                //还没做
                break;
            case "6"://出厂设置
                sendMsg(Joint.setResponse(Joint.RESTORE, message));
                TobotUtils.DBClear();
                break;
            case "8"://舞蹈
                sendMsg(Joint.setDanceResponse(message));
                dispose(8,message);
                break;
            case "9"://点播停止
                sendMsg(Joint.setResponseSemicolon(Joint.DEMAND_STOP,message));
                dispose(9,message);
                break;
            case "A":
                sendMsg(Joint.setResponseSemicolon(Joint.ANSWER,message));
                dispose(10,message);
                break;
            case "B"://跟随
                sendMsg(Joint.setResponseSemicolon(Joint.FOLLOW,message));
                dispose(11,message);
                break;
            case "C"://蓝牙
                sendMsg(Joint.setResponseSemicolon(Joint.BLE,message));
                dispose(12,message);
                break;

            default:
                break;
        }
    }

    private void dispose(int function,String message){
        Log.i(TAG,"进入............................");
        Message Msg = Message.obtain();
        Msg.what = function;
        Msg.obj = message;
        handlerDispose.sendMessage(Msg);
    }

    private boolean deseno;
    Handler handlerDispose = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            String message = (String)msg.obj;
            model.setInitialize();
            desenoTimer.schedule(new DesenoTimerTask(), 1000);
            if (deseno){
                return;
            }
            deseno = true;
            Log.i(TAG,"执行次数:");
            switch (msg.what){
                case 3:
                    BFrame.getmBLocal().carryThrough(Joint.getSpecialRunning(message));
                    break;
                case 4:
                    model.setCategoryId(Integer.parseInt(Joint.getCommaAmong(message,1)));
                    model.setTrack_title(Joint.getCommaAmong(message,2));
                    model.setPlayUrl32(Joint.getPeelVerify(message));
                    mdemandListener.setDemandResource(model);
                    break;
                case 5:
                    break;
                case 6:
                    break;
                case 8:
                    model.setCategoryId(88);
                    model.setPlayUrl32(Joint.getCommaAmong(message,1));
                    model.setTimestamp(Joint.getPeelVerify(message));
                    Log.i(TAG,"点播的舞蹈编号:"+model.getPlayUrl32()+"舞蹈指令:"+model.getCategoryId()+"舞蹈时间戳:"+model.getTimestamp());
                    mdemandListener.setDemandResource(model);
                    break;
                case 9:
                    Log.i(TAG,"进入点播停止");
                    mdemandListener.stopDemand();
                    break;
                case 10:
                    new UpdateAnswer();
                    break;
                case 11:
                    Log.i(TAG,"进入跟随模式");

                    break;
                case 12:
                    Log.i(TAG,"进入蓝牙");
//                    bleDispose(Joint.getCommaAmong(message,1));
                    break;

                default:
                    break;
            }
        }
    };

    private void bleDispose(String status){
        switch (status){
            case "1":
                BFrame.TTS("已进入遥控模式");
                //下发紫色灯圈,进入休眠模式
                break;
            case "2":
                BFrame.TTS("已退出遥控模式");
                //下发白色呼吸灯,退出休眠
                break;
        }
    }

    private class DesenoTimerTask extends TimerTask {
        public void run() {
            deseno = false;
        }
    }

    public void setDemandListener(DemandListener demandListener) {
        mdemandListener = demandListener;
    }

    public interface DemandListener{
        void setDemandResource(DemandModel demand);
        void stopDemand();
    };
    
}
