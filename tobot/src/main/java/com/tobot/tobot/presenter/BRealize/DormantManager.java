package com.tobot.tobot.presenter.BRealize;

import android.os.Message;
import android.util.Log;


import com.tobot.tobot.presenter.IPort.AwakenBehavior;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by mohuaiyuan on 2017/12/26.
 */

public class DormantManager {
    private static final String TAG = "IDormant";

    public static final int DORMANT_TYPE_STRAIGHT_TO_SLEEP = 23;
    public static final int DORMANT_TYPE_SIT_DOWN_AND_SLEEP = 46;
    public static final int DORMANT_TYPE_LIE_DOWN_AND_SLEEP = 53;
    public static boolean fixation = true;//固定休眠模式,防止某些特殊场合进入坐下或躺下休眠模式

    /**
     * 非正常睡觉
     */
    public static final int DORMANT_TYPE_ABNORMAL_SLEEP = 685;

    private static int type;

    /**
     * 坐下休息（休眠）：站着休眠 N分钟不唤醒 之后就坐下休息了
     */
    private static Timer sitDownAndSleepTimer;

    private SitDownAndSleepTimeTask sitDownAndSleepTimeTask;


    public static int getType() {
        return type;
    }

    /**
     * 设置休眠类型
     * @param type
     */
    public static void setType(int type) {
        DormantManager.type = type;
    }

    /**
     * 创建任务：站着休眠N分钟不唤醒 ,进入坐下休息（休眠）
     */
    public void sitDownAndSleepTrigger() {
        Log.d(TAG, "DormantManager sitDownAndSleepTrigger: 开启任务");
//        if (sitDownAndSleepTimeTask==null){
            sitDownAndSleepTimeTask = new SitDownAndSleepTimeTask();
//        }
        if (fixation) {
            sitDownAndSleepTimer = new Timer(true);
            sitDownAndSleepTimer.schedule(sitDownAndSleepTimeTask, 10 * 60 * 1000);//N分钟之后
        }
    }

    public void cancelSitDownAndSleepTrigger() {
        Log.d(TAG, "DormantManager cancelSitDownAndSleepTrigger:取消任务 ");
        if (sitDownAndSleepTimer != null) {
            sitDownAndSleepTimer.cancel();
        }
//        sitDownAndSleepTimer=new Timer();
//        sitDownAndSleepTimeTask.cancel();
    }

    public static Timer getSitDownAndSleepTimer() {
        return sitDownAndSleepTimer;
    }

    public static void setSitDownAndSleepTimer(Timer sitDownAndSleepTimer) {
        DormantManager.sitDownAndSleepTimer = sitDownAndSleepTimer;
    }

}
