package com.tobot.tobot.presenter.BRealize;

import android.content.Context;
import android.util.Log;

import com.tobot.tobot.R;
import com.tobot.tobot.presenter.ICommon.ISceneV;
import com.tobot.tobot.presenter.IPort.AwakenBehavior;
import com.tobot.tobot.presenter.IPort.DormantBehavior;
import com.tobot.tobot.presenter.IPort.IDormant;
import com.tobot.tobot.presenter.IPort.PrepareSleepBehavior;
import com.tobot.tobot.utils.TobotUtils;
import com.turing123.robotframe.function.tts.ITTSCallback;
import com.turing123.robotframe.localcommand.LocalCommand;
import com.turing123.robotframe.localcommand.LocalCommandCenter;

import org.xmlpull.v1.XmlPullParser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;

/**
 * Created by mohuaiyuan on 2017/12/25.
 */

/**
 * 站着休眠
 */
public class StraightToSleep  implements IDormant ,DormantBehavior,AwakenBehavior{
    private static final String TAG = "IDormant";

    private static Context mContext;
    private ISceneV mISceneV;
    private LocalCommandCenter localCommandCenter;
    private LocalCommand sleepCommand;
//    private RobotFrameManager mRobotFrameManager;//20171211-previous code


    private DormantManager dormantManager;

    private static PrepareSleepBehavior prepareSleepBehavior;

    public StraightToSleep(){

    }

    public StraightToSleep(ISceneV mISceneV){
        this.mISceneV = mISceneV;
        this.mContext = (Context)mISceneV;
        inDormant();
    }

    @Override
    public void inDormant() {
        Log.d(TAG, "StraightToSleep inDormant: 直立休眠---初始化");
        //1. 获取LocalCommandCenter 对象
        localCommandCenter = LocalCommandCenter.getInstance(mContext);
        //2. 定义本地命令的名字
        String name = "StraightToSleep";
        //3. 定义匹配该本地命令的关键词，包含这些关键词的识别结果将交由该本地命令处理。
        String[] array=mContext.getResources().getStringArray(R.array.straightToSleep_keyWords_array);
        List<String> keyWords = new ArrayList<String>();
        for (int i=0;i<array.length;i++){
            keyWords.add(array[i]);
        }
        //4. 定义本地命令对象
        sleepCommand = new LocalCommand(name, keyWords) {
            //4.1. 在process 函数中实现该命令的具体动作。
            @Override
            protected void process(String name, String var1) {
                Log.d(TAG, "StraightToSleep process: 直立休眠---执行命令");
                Log.d(TAG, "process name : "+name);
                Log.d(TAG, "process var1 : "+var1);
                //4.1.1. 本示例中，当喊关键词中配置的词时将使机器人进入睡眠状态
                //注意： 若要唤醒机器人，可调用wakeup,或者使用语言唤醒词唤醒。

                dormant();
                //设置休眠类型
                DormantManager.setType(DormantManager.DORMANT_TYPE_STRAIGHT_TO_SLEEP);


            }

            //4.2. 执行命令前的处理
            @Override
            public void beforeCommandProcess(String s) {
                Log.d(TAG, "StraightToSleep beforeCommandProcess:直立休眠---命令开始前 ");
//                if (dormantManager==null){
//                    dormantManager=new DormantManager();
//                }
//                dormantManager.setType(DormantManager.DORMANT_TYPE_STRAIGHT_TO_SLEEP);
//
//                dormantManager.sitDownAndSleepTrigger();

            }

            //4.3. 执行命令后的处理
            @Override
            public void afterCommandProcess() {
                //进入睡眠
//                BFrame.FallAsleep();

            }
        };
        //5. 将定义好的local command 加入 LocalCommandCenter中。
        localCommandCenter.add(sleepCommand);

    }

    @Override
    public void dormant() {
        Log.d(TAG, "StraightToSleep dormant:直立休眠---休眠 ");
        String voice=mContext.getResources().getString(R.string.straightToSleep_Response);
        Map<String,String> map=null;
        try {
            map=BFrame.getString(voice);
        } catch (Exception e) {
            Log.d(TAG, "直立休眠 初始化配置信息出现错误 Exception e : "+e.getMessage());
            e.printStackTrace();
        }

        BaseTTSCallback baseTTSCallback=new BaseTTSCallback(){
            @Override
            public void onCompleted() {
                Log.d(TAG, "StraightToSleep onCompleted: ");
                //进入睡眠
                BFrame.FallAsleep();

                //站着休眠N分钟不唤醒 ,触发 坐下休眠
                if (dormantManager==null){
                    dormantManager=new DormantManager();
                }
                dormantManager.sitDownAndSleepTrigger();
            }
        };
        BFrame.setInterruptTTSCallback(new InterruptTTSCallback(BFrame.main,baseTTSCallback));

        try {
            BFrame.responseWithCallback(map);
        } catch (Exception e) {
            e.printStackTrace();
        }




    }

    @Override
    public void awaken() {
        Log.d(TAG, "StraightToSleep awaken: 直立休眠---唤醒");
        try {
            BFrame.response(R.string.straightToSleep_awaken);
        } catch (Exception e) {
            Log.e(TAG, "直立休眠---唤醒 出现Exception e : "+e.getMessage() );
            e.printStackTrace();
        }

    }

    public static PrepareSleepBehavior getPrepareSleepBehavior() {
        return prepareSleepBehavior;
    }

    public static void setPrepareSleepBehavior(PrepareSleepBehavior prepareSleepBehavior) {
        StraightToSleep.prepareSleepBehavior = prepareSleepBehavior;
    }
}
