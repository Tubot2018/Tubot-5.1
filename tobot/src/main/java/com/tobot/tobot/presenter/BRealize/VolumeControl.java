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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

    public static final int VOLUME_MAX_VOLUME=13;
    public static final int VOLUME_MIN_VOLUME=4;
    public static final int VOLUME_COPIES=10;


    //mohuaiyuan 20180116 原来的代码
//    private static final String REG_RAISE_VOLUME="(?<!最)大|听不见";
    //mohuaiyuan 20180116 新的代码 20180116
    private static final String REG_RAISE_VOLUME="((?<!(最|多))大|听不见)";
    private static final String REG_LOWER_VOLUME="(?<!最)小|太吵了";
    private static final String REG_TUNE_TO_THE_LOUDEST="最大";
    private static final String REG_TUNE_TO_THE_SMALLEST_VOICE="最小";
//    private static final String REG_GET_CURRENT_VOLUME="音量|声音";
    private static final String REG_ADJUST_THE_VOLUME="((声音|音量)调到([一二三四五六七八九十]|(10|[1-9]))\\b)";
    //mohuaiyuan 20180112 新的代码 20180112
//    private static final String REG_ADJUST_THE_VOLUME="((声音|音量)(调|调整|增加|增大|减少|加大|减小)到([一二三四五六七八九十]|(10|[1-9]))\\b)";

    private int type;

    private static final int TYPE_RAISE_VOLUME=1;
    private static final int TYPE_LOWER_VOLUME=2;
    private static final int TYPE_TUNE_TO_THE_LOUDEST=4;
    private static final int TYPE_TUNE_TO_THE_SMALLEST_VOICE=8;
    private static final int TYPE_GET_CURRENT_VOLUME=16;
    private static final int TYPE_ADJUST_THE_VOLUME=32;


    private Pattern pattern;
    private Matcher matcher;

    private static List<String> regList;
    private static List<Integer> typeList;

    private AudioUtils audioUtils;
    private List<String> volumeKeyWords;

	
    public VolumeControl(){

    }

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

        volumeKeyWords = getVolumeKeyWords();

        //4. 定义本地命令对象
        sleepCommand = new LocalCommand(name, volumeKeyWords) {
            //4.1. 在process 函数中实现该命令的具体动作。
            @Override
            protected void process(String name, String var1) {
                Log.d(TAG, "VolumeControl process: 音量控制---执行命令");
                Log.d(TAG, "process name : "+name);
                Log.d(TAG, "process var1 : "+var1);

                dealWithVolume(var1);
                //5. 命令执行完成后需明确告诉框架，命令处理结束，否则无法继续进行主对话流程。
                this.localCommandComplete.onComplete();

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

    public void dealWithVolume(String var1) {
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

            case TYPE_GET_CURRENT_VOLUME: //mohuaiyuan  20180108 测试
                getCurrentVolume();
                break;

            case TYPE_ADJUST_THE_VOLUME:
                adjustTheVolumeLevel(var1);
                break;

            default:

                break;

        }


    }

    private void adjustTheVolumeLevel(String var1) {
        Log.d(TAG, "adjustTheVolumeLevel: ");

        pattern=Pattern.compile(REG_ADJUST_THE_VOLUME);
        matcher=pattern.matcher(var1);
        boolean isFind=matcher.find();
        if (isFind){
            String group=matcher.group(1);
            Log.d(TAG, "group: "+group);

            group=wordsTurnIntoNumbers(group);
            Log.d(TAG, "group: "+group);
            Pattern tempPattern=Pattern.compile("(\\d?\\d)");
            Matcher tempMatcher=tempPattern.matcher(group);
            boolean tempFind=tempMatcher.find();
            if (tempFind){
                String tempGroup=tempMatcher.group(1);
                Log.d(TAG, "tempGroup: "+tempGroup);

                int currentVolumeCount=Integer.valueOf(tempGroup);
                Log.d(TAG, "currentVolumeCount: "+currentVolumeCount);
                int currentVolumeCountResponse=currentVolumeCount;
                currentVolumeCount--;
                Log.d(TAG, "currentVolumeCount: "+currentVolumeCount);

                int currentVolume= (int) Math.round(audioUtils.getMinVolume()+currentVolumeCount*audioUtils.getVolumeSpace());
                audioUtils.setMusicVolume(currentVolume);
                adjustVolumeResponse(currentVolumeCountResponse);
            }

        }

    }

    private void getCurrentVolume() {
        Log.d(TAG, "getCurrentVolume: ");
//        getCurrentVolumeResponse();
        //mohuaiyuan  20180116 测试
        int currentVolumeLevel=audioUtils.getCurrentVolume();
        int minVolumeLevel=audioUtils.getMinVolume();
        double volumeSpace=audioUtils.getVolumeSpace();

        Log.d(TAG, "currentVolumeLevel: "+currentVolumeLevel);
        Log.d(TAG, "minVolumeLevel: "+minVolumeLevel);
        Log.d(TAG, "volumeSpace: "+volumeSpace);

        double countOriginal=  (currentVolumeLevel-minVolumeLevel)/volumeSpace;
        int currentCount= (int) Math.round(countOriginal);
        currentCount++;
        getCurrentVolumeResponse(currentCount);

    }

    private void tuneToMinVolume() {
        Log.d(TAG, "tuneToMinVolume: ");
        int currentVolume=audioUtils.getCurrentVolume();
        if (isMinVolume(currentVolume)){
            minVolumeResponse();
        }else {
            audioUtils.setMusicVolume(audioUtils.getMinVolume());
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
        Log.d(TAG, "lowerVolume 减小音量: ");
        int currentVolumeLevel=audioUtils.getCurrentVolume();
        int minVolumeLevel=audioUtils.getMinVolume();
        double volumeSpace=audioUtils.getVolumeSpace();

        Log.d(TAG, "currentVolumeLevel: "+currentVolumeLevel);
        Log.d(TAG, "minVolumeLevel: "+minVolumeLevel);
        Log.d(TAG, "volumeSpace: "+volumeSpace);

        if (isMinVolume(currentVolumeLevel)){
            minVolumeResponse();
        }else {
            double countOriginal=  (currentVolumeLevel-minVolumeLevel)/volumeSpace;
            int currentCount= (int) Math.round(countOriginal);
            currentCount--;
            double volume=minVolumeLevel+currentCount*volumeSpace;
            int volumeLevel= (int) Math.round(volume);

            Log.d(TAG, "countOriginal: "+countOriginal);
            Log.d(TAG, "currentCount: "+currentCount);
            Log.d(TAG, "volume: "+volume);
            Log.d(TAG, "volumeLevel: "+volumeLevel);
            audioUtils.setMusicVolume(volumeLevel);
            lowerVolumeResponse(currentCount+1);
        }

    }

    private void raiseVolume() {
        Log.d(TAG, "raiseVolume 增大音量: ");
        int currentVolumeLevel=audioUtils.getCurrentVolume();
        int minVolumeLevel=audioUtils.getMinVolume();
        double volumeSpace=audioUtils.getVolumeSpace();


        Log.d(TAG, "currentVolumeLevel: "+currentVolumeLevel);
        Log.d(TAG, "minVolumeLevel: "+minVolumeLevel);
        Log.d(TAG, "volumeSpace: "+volumeSpace);

        if (isMaxVolume(currentVolumeLevel)){
            maxVolumeResponse();
        }else {
            double countOriginal=  (currentVolumeLevel-minVolumeLevel)/volumeSpace;
            int currentCount= (int) Math.round(countOriginal);
            currentCount++;
            double volume=minVolumeLevel+currentCount*volumeSpace;
            int volumeLevel= (int) Math.round(volume);

            Log.d(TAG, "countOriginal: "+countOriginal);
            Log.d(TAG, "currentCount: "+currentCount);
            Log.d(TAG, "volume: "+volume);
            Log.d(TAG, "volumeLevel: "+volumeLevel);
            audioUtils.setMusicVolume(volumeLevel);
            raiseVolumeResponse(currentCount+1);
        }

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
        isMinVolume=audioUtils.getMinVolume()==volume;
        
        return isMinVolume;
    }

    private void maxVolumeResponse(){
        Log.d(TAG, "maxVolumeResponse: ");
        try {
            BFrame.response(R.string.maxMusicVolume);
        } catch (Exception e) {
            Log.e(TAG, "音量已经最大 反馈 出现Exception e: "+e.getMessage());
            e.printStackTrace();
        }
    }

    private void minVolumeResponse(){
        Log.d(TAG, "minVolumeResponse: ");
        try {
            BFrame.response(R.string.minMusicVolume);
        } catch (Exception e) {
            Log.e(TAG, "音量已经最小 反馈 出现Exception e: "+e.getMessage());
            e.printStackTrace();
        }
    }

	
    private void raiseVolumeResponse(int currentVolume){
        Log.d(TAG, "raiseVolumeResponse: ");
        String speech=BFrame.getString(R.string.raiseMusicVolume,String.valueOf(currentVolume));
        try {
            BFrame.response(speech);
        } catch (Exception e) {
            Log.e(TAG, "增大音量 反馈 出现Exception e: "+e.getMessage());
            e.printStackTrace();
        }
    }

    private void lowerVolumeResponse(int currentVolume){
        Log.d(TAG, "lowerVolumeResponse: ");
        String speech=BFrame.getString(R.string.lowerMusicVolume,String.valueOf(currentVolume));
        try {
            BFrame.response(speech);
        } catch (Exception e) {
            Log.e(TAG, "减小音量 反馈 出现Exception e: "+e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 将阿拉伯数字转换成相应的中文文字。
     * eg: 4 转换成 四
     * @param currentVolume
     * @return
     */
    private String  numbersTurnIntoWords(int currentVolume){
        Log.d(TAG, "numbersTurnIntoWords: ");
        Map<String,String>map=new HashMap<>();
        map.put("1","一");
        map.put("2","二");
        map.put("3","三");
        map.put("4","四");
        map.put("5","五");

        map.put("6","六");
        map.put("7","七");
        map.put("8","八");
        map.put("9","九");
        map.put("10","十");

        return map.get(currentVolume);
    }

    /**
     * 将字符串中的中文数字转换成阿拉伯数字。
     * eg:四 转换成 4
     * @param var1
     * @return
     */
    private String wordsTurnIntoNumbers(String var1){
        Log.d(TAG, "wordsTurnIntoNumbers: ");
        Map<String,String>map=new HashMap<>();
        map.put("一","1");
        map.put("二","2");
        map.put("三","3");
        map.put("四","4");
        map.put("五","5");

        map.put("六","6");
        map.put("七","7");
        map.put("八","8");
        map.put("九","9");
        map.put("十","10");

        Set<String> keySet=map.keySet();
        Iterator<String> iterator=keySet.iterator();
        while (iterator.hasNext()){
            String key=iterator.next();
            String value=map.get(key);
            var1=var1.replaceAll(key,value);
        }
        return var1;
    }

    private void adjustVolumeResponse(int currentVolume ){
        Log.d(TAG, "adjustVolumeResponse: ");
        String tempCurrentVolume=numbersTurnIntoWords(currentVolume);
        String speech=BFrame.getString(R.string.raiseMusicVolume,String.valueOf(currentVolume));
        try {
            BFrame.response(speech);
        } catch (Exception e) {
            Log.e(TAG, "调整音量 反馈 出现Exception e: "+e.getMessage());
            e.printStackTrace();
        }

    }

    private void raiseVolumeResponse(){
        Log.d(TAG, "raiseVolumeResponse: ");
        try {
            BFrame.response(R.string.raiseMusicVolume);
        } catch (Exception e) {
            Log.e(TAG, "增大音量 反馈 出现Exception e: "+e.getMessage());
            e.printStackTrace();
        }
    }
    private void lowerVolumeResponse(){
        Log.d(TAG, "lowerVolumeResponse: ");
        try {
            BFrame.response(R.string.lowerMusicVolume);
        } catch (Exception e) {
            Log.e(TAG, "减小音量 反馈 出现Exception e: "+e.getMessage());
            e.printStackTrace();
        }
    }

    private void tuneToTheLoudestResponse(){
        Log.d(TAG, "tuneToTheLoudestResponse: ");
        try {
            BFrame.response(R.string.tuneToTheLoudest);
        } catch (Exception e) {
            Log.e(TAG, "调到最大声 反馈 出现Exception e: "+e.getMessage());
            e.printStackTrace();
        }
    }

    private void tuneToTheSmalleseVoiceResponse(){
        Log.d(TAG, "tuneToTheSmalleseVoiceResponse: ");
        try {
            BFrame.response(R.string.tuneToTheSmalleseVoice);
        } catch (Exception e) {
            Log.e(TAG, "调到最小声 反馈 出现Exception e: "+e.getMessage());
            e.printStackTrace();
        }
    }

    //mohuaiyuan  20180116 测试
    private void getCurrentVolumeResponse(int currentVolume ){
        Log.d(TAG, "getCurrentVolumeResponse: ");
        String speech=BFrame.getString(R.string.getCurrentVolume,String.valueOf(currentVolume));
        try {
            BFrame.response(speech);
        } catch (Exception e) {
            Log.e(TAG, "获取 当前音量 反馈 出现Exception e: "+e.getMessage());
            e.printStackTrace();
        }

    }

    //mohuaiyuan  20180108 测试
    private void getCurrentVolumeResponse(){
        Log.d(TAG, "getCurrentVolumeResponse: ");
        try {
            BFrame.response("speech:当前音量为："+audioUtils.getCurrentVolume());
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
        audioUtils.setMaxVolumeListener(new AudioUtils.MaxVolumeListener() {
            @Override
            public int getMaxVolume() {
                return VOLUME_MAX_VOLUME;
            }
        });
        audioUtils.setMinVolumeListener(new AudioUtils.MinVolumeListener() {
            @Override
            public int getMinVolume() {
                return VOLUME_MIN_VOLUME;
            }
        });
        audioUtils.setVolumeCopyListener(new AudioUtils.VolumeCopyListener() {
            @Override
            public int getVolumeCopies() {
                return VOLUME_COPIES;
            }
        });


        if (regList==null){
            regList=new ArrayList<>();
        }
        if (regList.isEmpty()){
            regList.add(REG_ADJUST_THE_VOLUME);
            regList.add(REG_RAISE_VOLUME);
            regList.add(REG_LOWER_VOLUME);
            regList.add(REG_TUNE_TO_THE_LOUDEST);
            regList.add(REG_TUNE_TO_THE_SMALLEST_VOICE);
            //mohuaiyuan  20180108 测试
//            regList.add(REG_GET_CURRENT_VOLUME);
        }

        if (typeList==null ){
            typeList=new ArrayList<>();
        }
        if (typeList.isEmpty()){
            typeList.add(TYPE_ADJUST_THE_VOLUME);
            typeList.add(TYPE_RAISE_VOLUME);
            typeList.add(TYPE_LOWER_VOLUME);
            typeList.add(TYPE_TUNE_TO_THE_LOUDEST);
            typeList.add(TYPE_TUNE_TO_THE_SMALLEST_VOICE);
            //mohuaiyuan  20180108 测试
//            typeList.add(TYPE_GET_CURRENT_VOLUME);
        }

    }

    public List<String> getVolumeKeyWords(){
        Log.d(TAG, "getVolumeKeyWords: ");
        List<String> keyWords=new ArrayList<>();
        String[] array=mContext.getResources().getStringArray(R.array.volume_keyWords_array);
        for (int i=0;i<array.length;i++){
            keyWords.add(array[i]);
        }
        return keyWords;
    }

    public static Context getmContext() {
        return mContext;
    }

    public static void setmContext(Context mContext) {
        VolumeControl.mContext = mContext;
    }
}
