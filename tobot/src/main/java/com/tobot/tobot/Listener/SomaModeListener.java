package com.tobot.tobot.Listener;

import android.content.Context;
import android.util.Log;

import com.tobot.tobot.R;
import com.tobot.tobot.presenter.BRealize.BFollow;
import com.tobot.tobot.presenter.BRealize.BFrame;
import com.tobot.tobot.presenter.BRealize.BSensor;
import com.tobot.tobot.presenter.BRealize.DormantManager;
import com.tobot.tobot.presenter.BRealize.SitDownAndSleep;
import com.tobot.tobot.presenter.BRealize.StraightToSleep;
import com.tobot.tobot.presenter.ICommon.ISomaMode;
import com.tobot.tobot.presenter.IPort.SomaModeTracking;
import com.tobot.tobot.utils.socketblock.SocketConnectCoherence;

import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Javen on 2018/2/1.
 */

public class SomaModeListener extends SensorListener implements ISomaMode {
    private String TAG = "Javen SomaModeListener";
    private Context context;
    private static SomaModeListener somaModeListener;
    private SocketConnectCoherence mCoherence;
    private long T1 = 15000, T2 = 5000;
    private Timer somaModeTimer = new Timer(true);
    private SomaModeTracking somaModeTracking;
    private BFollow mBFollow;
    private int SomaMode;
    private boolean SomaState = true;

    public SomaModeListener(Context context){
        super(context);
        this.context = context;
        initLoad();
        somaModeTimer.schedule(new SomaModeTimer(),T1,T2);
    }

    public static synchronized SomaModeListener instance(Context context) {
        if (somaModeListener == null) {
            somaModeListener = new SomaModeListener(context);
        }
        return somaModeListener;
    }

    private void initLoad(){
        mBFollow = BFollow.instance(this);
    }

    private class SomaModeTimer extends TimerTask {
        public void run() {
            tracking();
        }
    }

    private void tracking(){
        BFrame.mBSensor.read2019Pattern(new byte[]{0x20, 0x19},this);
    }

    @Override
    public void onOrderFeedback(byte[] data) {
        super.onOrderFeedback(data);
//        Log.i(TAG,";数据:"+Arrays.toString(data)+"识别位:"+bytesToInt(data,data.length-1)+"数据位:"+bytesToInt(data,4));
        for (int i = 7; i < data.length; i++) {
            if (data[1] == 3) {
                identity(data[i]);
            }
        }
    }

    private void identity(byte identity) {
        switch (identity){
            case 0:
                stateRestoration();
                SomaMode = 0;
                break;
            case 1:
                SomaMode = 1;
                mBFollow.FollowState(identity);
                break;
            case 5:
//                if (BFrame.robotState) {
                if (SomaState) {
                    Log.d(TAG, "触摸胸前按键进入休眠: ");
                    SomaMode = 5;
                    SomaState = false;
                    new StraightToSleep().dormant();
                    DormantManager.setType(DormantManager.DORMANT_TYPE_STRAIGHT_TO_SLEEP);
                }
                break;
        }
    }

    private void stateRestoration(){
        switch (SomaMode){
            case 0:
            case 1:
                mBFollow.FollowState(SomaMode);
                break;
            case 5:
//                if (!BFrame.robotState) {
                if (!SomaState) {
                    Log.d(TAG, "退出休眠: ");
                    SomaState = true;
                    BFrame.Wakeup();
                }
                break;
        }

    }

    @Override
    public void Feedback(byte[] feedback) {

    }

    public void setSomaListener(SocketConnectCoherence mCoherence){
        this.mCoherence = mCoherence;
        enterSomaListener();
    }

    public void enterSomaListener(){
        if (mCoherence != null) {
            mCoherence.setSomaModeTracking(new SomaModeTracking() {

                @Override
                public void Enter(int senseType) {//1跟随模式2蓝牙
                    enterDispose(senseType);
                }

                @Override
                public void Quit(int senseType) {
                    quitDispose(senseType);
                }

                @Override
                public void Feedback(byte[] feedback) {

                }
            });
        }
    }

    private void enterDispose(int senseType){
        if (senseType == 1){

        }else if (senseType == 2){
            try {
                BFrame.response(R.string.beginRemoteControlMode_response);
            } catch (Exception e) {
                e.printStackTrace();
            }
            BFrame.shutChat();
//          DormantManager.fixation = false;//防止进入坐下休眠
//          BFrame.FallAsleep();
        }
    }

    private void quitDispose(int senseType){
        if (senseType == 1){

        }else if (senseType == 2){
            BFrame.disparkChat();
//          BFrame.Wakeup();
            try {
                BFrame.response(R.string.endRemoteControlMode_response);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


}
