package com.tobot.tobot.presenter.BRealize;

import android.util.Log;

import java.util.TimerTask;

/**
 * Created by YF-04 on 2017/12/30.
 */
public class SitDownAndSleepTimeTask extends TimerTask {
    private static final String TAG = "IDormant";

    @Override
    public void run() {
        Log.d(TAG, "SitDownAndSleepTimeTask run: 执行任务");

        DormantManager.setType(DormantManager.DORMANT_TYPE_ABNORMAL_SLEEP);
        //唤醒
        BFrame.Wakeup(1950);

    }
    
    public void execute(){
        Log.d(TAG, "execute: ");
        Log.d(TAG, "已经唤醒了，开始执行任务！");
        //发送动作、表情等等
        //休眠
        SitDownAndSleep sitDownAndSleep = new SitDownAndSleep();
        sitDownAndSleep.dormant();
        //设置休眠 类型
        DormantManager.setType(DormantManager.DORMANT_TYPE_SIT_DOWN_AND_SLEEP);
        
    }
    
}
