package com.tobot.tobot.presenter.BRealize;

import android.content.Context;
import android.util.Log;

import com.tobot.tobot.R;
import com.tobot.tobot.presenter.IPort.VolumeControlBehavior;
import com.tobot.tobot.utils.AudioUtils;
import com.turing.loginauthensdk.LoginResponseDataEntity;
import com.turing123.robotframe.localcommand.LocalCommand;
import com.turing123.robotframe.localcommand.LocalCommandCenter;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by YF-04 on 2018/1/4.
 */

public class VolumeControl implements VolumeControlBehavior {
    private static final String TAG = "VolumeControlBehavior";

    private static Context mContext;

    private LocalCommandCenter localCommandCenter;
    private LocalCommand sleepCommand;

    private static final String REG_RAISE_VOLUME="(?<!最)大|太吵了";
    private static final String REG_LOWER_VOLUME="(?<!最)小|听不见";
    private static final String REG_TUNE_TO_THE_LOUDEST="最大";
    private static final String REG_TUNE_TO_THE_SMALLEST_VOICE="最小";

    private int type;

    private static final int TYPE_RAISE_VOLUME=1;
    private static final int TYPE_LOWER_VOLUME=2;
    private static final int TYPE_TUNE_TO_THE_LOUDEST=4;
    private static final int TYPE_TUNE_TO_THE_SMALLEST_VOICE=8;

    private Pattern pattern;
    private Matcher matcher;

    private static List<String> regList;
    private static List<Integer> typeList;

    private AudioUtils audioUtils;

    public VolumeControl(Context context){
        this.mContext=context;
        adjustVolume();
    }
    
    
    @Override
    public void adjustVolume() {
        Log.d(TAG, "VolumeControl inDormant: 音量控制---初始化");
        //1. 获取LocalCommandCenter 对象
        localCommandCenter = LocalCommandCenter.getInstance(mContext);
        //2. 定义本地命令的名字
        String name = "VolumeControl";
        //3. 定义匹配该本地命令的关键词，包含这些关键词的识别结果将交由该本地命令处理。
        String[] array=mContext.getResources().getStringArray(R.array.volume_keyWords_array);
        List<String> keyWords = new ArrayList<String>();
        for (int i=0;i<array.length;i++){
            keyWords.add(array[i]);
        }
        //4. 定义本地命令对象
        sleepCommand = new LocalCommand(name, keyWords) {
            //4.1. 在process 函数中实现该命令的具体动作。
            @Override
            protected void process(String name, String var1) {
                Log.d(TAG, "VolumeControl process: 音量控制---执行命令");
                Log.d(TAG, "process name : "+name);
                Log.d(TAG, "process var1 : "+var1);

                dealWithVolume(var1);


            }

            //4.2. 执行命令前的处理
            @Override
            public void beforeCommandProcess(String s) {
                Log.d(TAG, "VolumeControl beforeCommandProcess:音量控制---命令开始前 ");


            }

            //4.3. 执行命令后的处理
            @Override
            public void afterCommandProcess() {

            }
        };
        //5. 将定义好的local command 加入 LocalCommandCenter中。
        localCommandCenter.add(sleepCommand);

    }

    private void dealWithVolume(String var1) {
        Log.d(TAG, "dealWithVolume: ");
        if (var1==null || var1.isEmpty()){
            return;
        }

        initVolumeType(var1);
        Log.d(TAG, "type: "+type);
        switch (type){
            case TYPE_RAISE_VOLUME:
                raiseVolume();
                break;

            case TYPE_LOWER_VOLUME:
                lowerVolume();
                break;

            case TYPE_TUNE_TO_THE_LOUDEST:
                tuneToMaxVolume();
                break;

            case TYPE_TUNE_TO_THE_SMALLEST_VOICE:
                tuneToMinVolume();
                break;

            default:

                break;

        }


    }

    private void tuneToMinVolume() {
        Log.d(TAG, "tuneToMinVolume: ");
        int currentVolume=audioUtils.getCurrentVolume();
        if (isMinVolume(currentVolume)){
            minVolumeResponse();
        }else {
            audioUtils.setMusicVolume(AudioUtils.MUSIC_MIN_VOLUME_LEVEL);
            tuneToTheSmalleseVoiceResponse();
        }

    }

    private void tuneToMaxVolume() {
        Log.d(TAG, "tuneToMaxVolume: ");
        int currentVolume=audioUtils.getCurrentVolume();
        int maxVolume=audioUtils.getMaxVolume();
        if (isMaxVolume(currentVolume)){
            maxVolumeResponse();
        }else {
            audioUtils.setMusicVolume(maxVolume);
            tuneToTheLoudestResponse();
        }
        
    }

    private void lowerVolume() {
        Log.d(TAG, "lowerVolume: ");

    }

    private void raiseVolume() {
        Log.d(TAG, "raiseVolume: ");

    }

    private boolean isMaxVolume(int volume){
        Log.d(TAG, "isMaxVolume: ");
        boolean isMaxVolume=false;
        isMaxVolume=audioUtils.getMaxVolume()==volume;

        return isMaxVolume;
    }

    private boolean isMinVolume(int volume){
        Log.d(TAG, "isMinVolume: ");
        boolean isMinVolume=false;
        isMinVolume=AudioUtils.MUSIC_MIN_VOLUME_LEVEL==volume;
        
        return isMinVolume;
    }

    private void maxVolumeResponse(){
        Log.d(TAG, "maxVolumeResponse: ");
        try {
            BFrame.response(R.string.maxMusicVolume);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void minVolumeResponse(){
        Log.d(TAG, "minVolumeResponse: ");
        try {
            BFrame.response(R.string.minMusicVolume);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void raiseVolumeResponse(){
        Log.d(TAG, "raiseVolumeResponse: ");
        try {
            BFrame.response(R.string.raiseMusicVolume);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void lowerVolumeResponse(){
        Log.d(TAG, "lowerVolumeResponse: ");
        try {
            BFrame.response(R.string.lowerMusicVolume);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void tuneToTheLoudestResponse(){
        Log.d(TAG, "tuneToTheLoudestResponse: ");
        try {
            BFrame.response(R.string.tuneToTheLoudest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void tuneToTheSmalleseVoiceResponse(){
        Log.d(TAG, "tuneToTheSmalleseVoiceResponse: ");
        try {
            BFrame.response(R.string.tuneToTheSmalleseVoice);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void initVolumeType(String var1){
        Log.d(TAG, "initVolumeType: ");

        initData();

        type=-1;

        for (int i=0;i<regList.size();i++){
            pattern=Pattern.compile(regList.get(i));
            matcher=pattern.matcher(var1);
            boolean isFind=matcher.find();
            if (isFind){
                type=typeList.get(i);
                break;
            }
        }

    }

    private void initData(){
        Log.d(TAG, "initData: ");

        if (audioUtils==null){
            audioUtils=new AudioUtils(mContext);
        }

        if (regList==null){
            regList=new ArrayList<>();
        }
        if (regList.isEmpty()){
            regList.add(REG_RAISE_VOLUME);
            regList.add(REG_LOWER_VOLUME);
            regList.add(REG_TUNE_TO_THE_LOUDEST);
            regList.add(REG_TUNE_TO_THE_SMALLEST_VOICE);
        }

        if (typeList==null ){
            typeList=new ArrayList<>();
        }
        if (typeList.isEmpty()){
            typeList.add(TYPE_RAISE_VOLUME);
            typeList.add(TYPE_LOWER_VOLUME);
            typeList.add(TYPE_TUNE_TO_THE_LOUDEST);
            typeList.add(TYPE_TUNE_TO_THE_SMALLEST_VOICE);
        }

    }

}
