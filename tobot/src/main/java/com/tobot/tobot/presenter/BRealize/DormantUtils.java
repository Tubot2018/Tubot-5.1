package com.tobot.tobot.presenter.BRealize;

import android.util.Log;

import com.tobot.tobot.presenter.IPort.AwakenBehavior;

/**
 * Created by YF-04 on 2018/1/2.
 */

public class DormantUtils {
    private static final String TAG = "IDormant";
    
    public DormantUtils(){
        
    }


    public void dealAwakenBehavior(){
        Log.d(TAG, "DormantUtils dealAwakenBehavior: ");
        int type = DormantManager.getType();
        Log.d(TAG, "type: "+type);
        AwakenBehavior awakenBehavior = null;

        switch (type){

            case DormantManager.DORMANT_TYPE_STRAIGHT_TO_SLEEP:
                Log.d(TAG, "直立休眠类型: ");
                awakenBehavior=new StraightToSleep();

                DormantManager dormantManager = new DormantManager();
                //mohuaiyuan  取消任务
                Log.d(TAG, "mohuaiyuan  取消任务: ");
                dormantManager.cancelSitDownAndSleepTrigger();
                break;

            case DormantManager.DORMANT_TYPE_SIT_DOWN_AND_SLEEP:
                Log.d(TAG, "坐下休眠类型: ");
                awakenBehavior=new SitDownAndSleep();
                break;

            case DormantManager.DORMANT_TYPE_LIE_DOWN_AND_SLEEP:
                Log.d(TAG, "躺下休眠类型: ");
                awakenBehavior=new LieDownAndSleep();
                break;

            case DormantManager.DORMANT_TYPE_ABNORMAL_SLEEP:
                SitDownAndSleepTimeTask task=new SitDownAndSleepTimeTask();
                task.execute();
                break;

            default:

                break;


        }
        Log.d(TAG, "准备进行唤醒。。。: ");
        Log.d(TAG, "awakenBehavior==null: "+(awakenBehavior==null));
		
        if (awakenBehavior!=null){
            awakenBehavior.awaken();
        }
    }
    
}
