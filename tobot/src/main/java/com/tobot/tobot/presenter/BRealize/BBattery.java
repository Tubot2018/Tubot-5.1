package com.tobot.tobot.presenter.BRealize;

import android.content.Context;
import android.util.Log;

import com.tobot.tobot.MainActivity;
import com.tobot.tobot.Listener.SimpleFrameCallback;
import com.tobot.tobot.R;
import com.tobot.tobot.presenter.ICommon.ISceneV;
import com.tobot.tobot.presenter.IPort.IBattery;
import com.tobot.tobot.scene.BaseScene;
import com.turing123.robotframe.function.bodystate.BodyState;
import com.turing123.robotframe.function.tts.TTS;
import com.turing123.robotframe.localcommand.LocalCommand;
import com.turing123.robotframe.localcommand.LocalCommandCenter;
import com.turing123.robotframe.multimodal.action.Action;
import com.turing123.robotframe.multimodal.action.BodyActionCode;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.turing123.robotframe.multimodal.action.Action.PRMTYPE_EXECUTION_TIMES;

/**
 * Created by Javen on 2017/8/29.
 */

public class BBattery implements IBattery{
    private String TAG = "Javen BBattery";
    private static Context mContext;
    private ISceneV mISceneV;
    private BodyState mBodyState;
    private TTS tts;
    private Timer mTimer = new Timer(true);
    private long T1 = 15000, T2 = 2000, T3 = 3*60*1000, T4 = 9000;
    private int FULL,CHARGING,LOW,DISCHARGE,NATURE_DISCHARGE;//播报次数
    //0:冲满电;;1:充电状态;2:低电量状态;3:自然放电状态;4:拔电时低电量提示5:拔电时电量不足提示
//    private int FULL_STATE,CHARGING_STATE,LOW_STATE,NATURE_DISCHARGE = -1,PLUCK_LOW,PLUCK_INSUFFCIENT;
    private int Battery_pull;//拔电标志  0:非拔电;1:拔电
    private LocalCommandCenter localCommandCenter;
    private LocalCommand localCommand;


    public BBattery(ISceneV mISceneV){
        this.mISceneV = mISceneV;
        this.mContext = (Context)mISceneV;
        mBodyState = new BodyState(mContext,new BaseScene(mContext, "os.sys.chat"));
        tts = new TTS(mContext, new BaseScene(mContext, "os.sys.chat"));
        mTimer.schedule(new BatteryTimer(),T1,T2);//电量查看
        batteryLocal();
    }


    @Override
    public void energy() {
//        Log.i(TAG, "电量:" + mBodyState.getBatteryLevel() + "电池状态:" +  mBodyState.getBatteryState());
        if (mBodyState.getBatteryState() == BodyState.BATTERY_STATE_FULL){
            if (mBodyState.getBatteryLevel() == 100 && setFrequency("FULL") < 1){
                mTimer.cancel();
                mTimer = new Timer();
                mTimer.schedule(new BatteryTimer(),T3,T2);
//                tts.speak("当前电量满格");
            }
        }
        if (mBodyState.getBatteryState() == BodyState.BATTERY_STATE_CHARGING){
            if (mBodyState.getBatteryLevel() < 100 && setFrequency("CHARGING") < 1){
                tts.speak("已插入电源,充电中");
            }else if(mBodyState.getBatteryLevel() >= 100 && setFrequency("FULL") < 3){
//                tts.speak("亲,已充满电,请帮我拔掉电源");
            }
            Battery_pull = 1;
        }
        if (mBodyState.getBatteryState() == BodyState.BATTERY_STATE_LOW){
            if ((mBodyState.getBatteryLevel() < 20 && Battery_pull ==0)){
//                mTimer.cancel();
//                mTimer = new Timer();
//                mTimer.schedule(new BatteryTimer(),T2,T3);
//                tts.speak("电量不足，请给我充电吧");
            }
        }
        if (mBodyState.getBatteryState() == BodyState.BATTERY_STATE_DISCHARGE && setFrequency("DISCHARGE") > 0){
            if ((mBodyState.getBatteryLevel() < 20 && Battery_pull == 0)){
                mTimer.cancel();
                mTimer = new Timer();
                mTimer.schedule(new BatteryTimer(),T2,T3);
                tts.speak("电量不足，请给我充电吧");
//            }else if((mBodyState.getBatteryLevel() < 30 && Battery_pull == 1)) {
//                Battery_pull = 0;
//                tts.speak("拔出电源,目前电量只有百分之" + mBodyState.getBatteryLevel());
//            }else if((mBodyState.getBatteryLevel() < 60 && Battery_pull == 1)) {
//                Battery_pull = 0;
//                tts.speak("拔出电源,目前电量只有百分之" + mBodyState.getBatteryLevel());
            }else if((mBodyState.getBatteryLevel() <= 100 && Battery_pull == 1)){
                Battery_pull = 0;
                tts.speak("拔出电源,当前电量百分之" + mBodyState.getBatteryLevel());
            }
        }
    }

    @Override
    public void balance() {
        if (mBodyState.getBatteryLevel() >= 50) {
            BFrame.motion(BodyActionCode.ACTION_CHEST);
            tts.speak("我的电量还有百分之" + mBodyState.getBatteryLevel() + "我们可以尽情的玩耍");
        } else if (mBodyState.getBatteryLevel() > 20){
            BFrame.motion(BodyActionCode.ACTION_26);
            tts.speak("我的电量还有百分之" + mBodyState.getBatteryLevel() + "我们继续玩吧");
        } else if (mBodyState.getBatteryLevel() <= 20 && Battery_pull == 1){
            tts.speak("糟糕只剩不到百分之" + mBodyState.getBatteryLevel() + "的电量,我需要补充能量");
        } else if (mBodyState.getBatteryLevel() <= 20 && Battery_pull == 0){
            tts.speak("只有不到百分之" + mBodyState.getBatteryLevel() + "的电了,等我满血复活就可以陪你继续玩了");
        }
    }

    private class BatteryTimer extends TimerTask {
        public void run() {
            energy();
        }
    }

    private int setFrequency(String frequency){
        switch (frequency) {
            case "FULL":
                CHARGING = 0; LOW = 0; DISCHARGE = 0;
                return FULL++;
            case "CHARGING":
                FULL = 0; LOW = 0; DISCHARGE = 0;
                return CHARGING++;
            case "LOW":
                CHARGING = 0; FULL = 0; DISCHARGE = 0;
                return LOW++;
            case "DISCHARGE":
                CHARGING = 0;LOW = 0;FULL = 0;
                return DISCHARGE++;
            case "NATURE_DISCHARGE":
                CHARGING = 0;LOW = 0;FULL = 0; DISCHARGE = 0;
                return NATURE_DISCHARGE++;

            default:
                return 0;
        }
    }

    @Override
    public void batteryLocal() {
        //1. 获取LocalCommandCenter 对象
        localCommandCenter = LocalCommandCenter.getInstance(mContext);
        //2. 定义本地命令的名字
        String name = "battery";
        //3. 定义匹配该本地命令的关键词，包含这些关键词的识别结果将交由该本地命令处理。
        List<String> keyWords = new ArrayList<String>();
//        keyWords.add("电量");
//        keyWords.add("当前电量");
//        keyWords.add("当前电量多少");
//        keyWords.add("当前电量还有多少");
//        keyWords.add("还有多少电");
//        keyWords.add("你还有多少电");
//        keyWords.add("电量多少");
//        keyWords.add("电还有多少");
        for (int i=0;i<getBatteryKeyWords().size();i++){
            keyWords.add(getBatteryKeyWords().get(i));
        }
        //4. 定义本地命令对象
        localCommand = new LocalCommand(name, keyWords) {
            //4.1. 在process 函数中实现该命令的具体动作。
            @Override
            protected void process(String name, String s) {
                //4.1.1. 本示例中，当喊关键词中配置的词时将使机器人进入拍照
                balance();
                //5. 命令执行完成后需明确告诉框架，命令处理结束，否则无法继续进行主对话流程。
                this.localCommandComplete.onComplete();
            }

            //4.2. 执行命令前的处理
            @Override
            public void beforeCommandProcess(String s) {

            }

            //4.3. 执行命令后的处理
            @Override
            public void afterCommandProcess() {

            }
        };
        //5. 将定义好的local command 加入 LocalCommandCenter中。
        localCommandCenter.add(localCommand);
    }

    public static List<String> getBatteryKeyWords() {
        List<String> keyWords = new ArrayList<>();
        String[] array = mContext.getResources().getStringArray(R.array.battery_keyWords_array);
        for (int i = 0; i < array.length; i++) {
            keyWords.add(array[i]);
        }
        return keyWords;
    }

}
