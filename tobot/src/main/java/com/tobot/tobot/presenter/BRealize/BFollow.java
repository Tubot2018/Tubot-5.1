package com.tobot.tobot.presenter.BRealize;

import android.content.Context;
import android.util.Log;

import com.tobot.tobot.Listener.SensorListener;
import com.tobot.tobot.Listener.SomaModeListener;
import com.tobot.tobot.MainActivity;
import com.tobot.tobot.R;
import com.tobot.tobot.presenter.ICommon.ISomaMode;
import com.turing123.robotframe.localcommand.LocalCommand;
import com.turing123.robotframe.localcommand.LocalCommandCenter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Javen on 2018/2/2.
 */

public class BFollow {
    private static String TAG = "Javen BFollow";
    private Context context;
    private LocalCommandCenter localCommandCenter;
    private LocalCommand localCommand;
    public static boolean follow;
    private static BFollow bFollow;
    private ISomaMode mISomaMode;

    public BFollow(ISomaMode mISomaMode){
        this.context = MainActivity.mContext;
        this.mISomaMode = mISomaMode;
        followLocal();
    }

    public static synchronized BFollow instance(ISomaMode mISomaMode) {
        if (bFollow == null) {
            bFollow = new BFollow(mISomaMode);
        }
        return bFollow;
    }

    public void FollowState(int feedback) {
        setFollow(feedback);
    }

    public void enterFollow(){
        Log.i(TAG,"进入跟随模式");
        BFrame.mBSensor.write2019Pattern((byte) 0x01);//进入跟随
    }

    public static void quitrFollow(){
        Log.i(TAG,"退出跟随模式");
        BFrame.mBSensor.write2019Pattern((byte) 0x00);//进入跟随
    }


    private void setFollow(int feedback){
        switch (feedback){
            case 1://进入跟随模式
                if (!follow) {
                    follow = true;
                    try {
                        BFrame.response(R.string.beginFollowMode_response);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    BFrame.shutChat();
//                    DormantManager.fixation = false;//防止进入坐下休眠
//                    BFrame.FallAsleep();
                }
                break;
            case 0://退出
                if (follow) {
                    follow = false;
                    BFrame.disparkChat();
//                    BFrame.Wakeup();
                    try {
                        BFrame.response(R.string.endFollowMode_response);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }

    public void followLocal() {
        //1. 获取LocalCommandCenter 对象
        localCommandCenter = LocalCommandCenter.getInstance(context);
        //2. 定义本地命令的名字
        String name = "follow";
        //3. 定义匹配该本地命令的关键词，包含这些关键词的识别结果将交由该本地命令处理。
        List<String> keyWords = getBatteryKeyWords();
//        for (int i=0;i<getBatteryKeyWords().size();i++){
//            keyWords.add(getBatteryKeyWords().get(i));
//        }
        //4. 定义本地命令对象
        localCommand = new LocalCommand(name, keyWords) {
            //4.1. 在process 函数中实现该命令的具体动作。
            @Override
            protected void process(String name, String s) {
                //4.1.1. 本示例中，当喊关键词中配置的词时将使机器人进入拍照
                enterFollow();
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

    public List<String> getBatteryKeyWords() {
        List<String> keyWords = new ArrayList<>();
        String[] array = context.getResources().getStringArray(R.array.follow_keyWords_array);
        for (int i = 0; i < array.length; i++) {
            keyWords.add(array[i]);
        }
        return keyWords;
    }


}
