package com.tobot.tobot.control.demand;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Message;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.Toast;

import com.tobot.tobot.Listener.SimpleFrameCallback;
import com.tobot.tobot.R;
import com.tobot.tobot.presenter.BRealize.BFrame;
import com.tobot.tobot.presenter.BRealize.BaseTTSCallback;
import com.tobot.tobot.presenter.BRealize.InterruptTTSCallback;
import com.tobot.tobot.scene.BaseScene;
import com.tobot.tobot.scene.CustomScenario;
import com.tobot.tobot.utils.CommonRequestManager;
import com.tobot.tobot.utils.TobotUtils;
import com.tobot.tobot.utils.socketblock.Joint;
import com.turing123.robotframe.function.motor.Motor;
import com.turing123.robotframe.function.tts.TTS;
import com.turing123.robotframe.multimodal.action.Action;
import com.turing123.robotframe.multimodal.action.EarActionCode;

import java.io.File;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by YF-04 on 2017/10/9.
 */

public class DemandDance implements DemandBehavior {
    private static final String TAG = "DemandDance";

    private DemandModel demandModel;
    private CommonRequestManager manager;
    private Context context;

    private MediaPlayer mediaPlayer;

    private String playUrl;
    private int bodyActionCode;

    private DemandUtils demandUtils;
    private Map<Integer,String> actionMap;

    private String appointTime;
    private Timer danceTimer = new Timer(true);

    private String songName;

    public DemandDance(Context context,DemandModel danceModel){
        this.context=context;
        this.demandModel=danceModel;


        this.manager=CommonRequestManager.getInstanse(context);
        demandUtils =new DemandUtils(context);

        //TODO  mohuaiyuan 20171009: 初始化 playUrl 和 bodyActionCode
        try {
            initData();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "DemandDance: "+e.getMessage() );
        }

    }

    private void initData() throws Exception {

        String playUrl32 = demandModel.getPlayUrl32();
        if (playUrl32 == null) {
            throw new Exception("Init Dance Info error :playUrl or bodyActionCode init error!");
        }
        String[] result = playUrl32.split(Constants.SEPARATOR_BETWEEN_PLAYURL_ACTION);

        if (result == null) {
            throw new Exception("Init Dance Info error :playUrl or bodyActionCode init error!");
        }
        Log.d(TAG, "result.length(): " + result.length);

        if (result.length == 1) {
            bodyActionCode = Integer.valueOf(result[0].trim());
            //init playUrl
            //play music file in local
            boolean initState = initPlayUrl();
            if (!initState) {
                throw new Exception("Init Dance Info error :playUrl  init error!");
            }
        } else if (result.length == 2) {
            playUrl = result[0].trim();
            bodyActionCode = Integer.valueOf(result[1].trim());
        } else {
            throw new Exception("Init Dance Info error :playUrl or bodyActionCode init error!");
        }



    }

    /**
     * init playUrl
     * @return true when the file is exist,others return false
     */
    private boolean initPlayUrl()throws Exception {

        //读取配置文件
       if (demandUtils.isConfigChange() || actionMap==null || actionMap.isEmpty()){
           actionMap= demandUtils.initActionConfig();
           demandUtils.setIsConfigChange(false);
       }else {
           Log.d(TAG, "There is no need to re-read the configuration file: ");
       }
        Log.d(TAG, "actionMap: "+actionMap);

        // 根据舞蹈序列号 找到对应的背景音乐的文件名
        String backgroundMusicName=actionMap.get(bodyActionCode);
        Log.d(TAG, "backgroundMusicName: "+backgroundMusicName);
        songName = backgroundMusicName.substring(0,backgroundMusicName.length()-4);
        Log.d(TAG, "songName: "+songName);

        // 根据文件名 获取完整的文件路径
        File playUrlFile=manager.getSDcardFile(DemandUtils.DANCE_BACKGROUND_MUSIC_Dir+File.separator+backgroundMusicName);
        Log.d(TAG, "playUrlFile: "+playUrlFile.getAbsolutePath());
        if (playUrlFile.exists()){
            playUrl=playUrlFile.getAbsolutePath();
            Log.d(TAG, "playUrl: "+playUrl);
            return true;
        }
       return false;

    }


    public DemandModel getDemandModel() {
        return demandModel;
    }

    public void setDemandModel(DemandModel demandModel) {
        this.demandModel = demandModel;
    }

    @Override
    public void executeDemand() {

//        String songName=demandModel.getTrack_title();
        String speech=manager.getString(R.string.beforeDemandDance,songName);
        Map<String,String> map=null;
        try {
            map= BFrame.getString(speech);
        } catch (Exception e) {
            e.printStackTrace();
        }

        BaseTTSCallback baseTTSCallback=new BaseTTSCallback(){
            @Override
            public void onCompleted() {

                //播放背景音乐
                try {
                    manager.playMusic(playUrl, new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            mediaPlayer = mp;
                            //发送舞蹈指令
                            //Javen 20180122注释
//                          sendBodyAction();
                            try {
                                AppointTime();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }, null, null);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        };
        BFrame.setInterruptTTSCallback(new InterruptTTSCallback(BFrame.main,baseTTSCallback));

        try {
            BFrame.responseWithCallback(map);
        } catch (Exception e) {
            e.printStackTrace();
        }



    }

    private void AppointTime() throws Exception{
        Log.i("Javen","服务器下发时间:"+TobotUtils.transformDateTime(demandModel.getTimestamp()));
        appointTime = TobotUtils.DateAddTime(TobotUtils.transformDateTime(demandModel.getTimestamp()),+5);
        Log.i("Javen","增加后时间:"+appointTime);
        long l = TobotUtils.DateMinusTime(TobotUtils.getCurrentlyDate(), appointTime, 2);
        Log.i("Javen", "与约定差距时间:" + l + "当前时间:" + TobotUtils.getCurrentlyDate());
        danceTimer.schedule(new DanceTimerTask(), l);//约定时间
//       danceTimer.schedule(new DanceTimerTask(),TobotUtils.DateMinusTime(appointTime,TobotUtils.getCurrentlyDate()));//约定时间
    }

    private class DanceTimerTask extends TimerTask {
        public void run() {
            Log.i("Javen", "下发时间:" + TobotUtils.getCurrentlyDate());
            sendBodyAction();
            danceTimer.cancel();
            danceTimer = new Timer();
        }
    }

    /**
     * 发送舞蹈指令 ，即机器人开始跳舞
     */
    private void sendBodyAction() {
        BFrame.motion(bodyActionCode,new SimpleFrameCallback(){
            @Override
            public void onStarted() {
                super.onStarted();
                Log.d(TAG, "onStarted: ");

                //开始播放背景音乐
                mediaPlayer.start();

            }

            @Override
            public void onStopped() {
                super.onStopped();
                //20180206 Javen 新增
                BFrame.Ear(EarActionCode.EAR_MOTIONCODE_3);
                BFrame.motion(1);
                Log.d(TAG, "onStopped: ");
            }

            @Override
            public void onPaused() {
                super.onPaused();
                Log.d(TAG, "onPaused: ");
            }

            @Override
            public void onResumed() {
                super.onResumed();
                Log.d(TAG, "onResumed: ");
            }

            @Override
            public void onInterrupted() {
                super.onInterrupted();
                Log.d(TAG, "onInterrupted: ");
            }

            @Override
            public void onCompleted() {
                super.onCompleted();
                Log.d(TAG, "onCompleted: ");
            }

            @Override
            public void onError(String s) {
                super.onError(s);
                Log.d(TAG, "onError: "+s);
            }
        });

    }
}
