package com.tobot.tobot.scene;

import android.content.Context;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import com.tobot.tobot.Listener.SimpleFrameCallback;
import com.tobot.tobot.R;
import com.tobot.tobot.entity.DetailsEntity;
import com.tobot.tobot.entity.SongEntity;
import com.tobot.tobot.presenter.BRealize.BBattery;
import com.tobot.tobot.presenter.BRealize.BFrame;
import com.tobot.tobot.presenter.BRealize.BaseTTSCallback;
import com.tobot.tobot.presenter.BRealize.InterruptTTSCallback;
import com.tobot.tobot.presenter.BRealize.VolumeControl;
import com.tobot.tobot.presenter.ICommon.ICommonInterface;
import com.tobot.tobot.utils.AudioUtils;
import com.tobot.tobot.utils.CommonRequestManager;
import com.tobot.tobot.utils.TobotUtils;
import com.turing123.robotframe.function.tts.TTS;
import com.turing123.robotframe.multimodal.Behavior;
import com.turing123.robotframe.multimodal.action.EarActionCode;
import com.turing123.robotframe.scenario.IScenario;
import com.turing123.robotframe.scenario.ScenarioManager;
import com.turing123.robotframe.scenario.ScenarioRuntimeConfig;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Created by Javen on 2017/8/31.
 */

public class DanceScenario implements IScenario{
    private static final String TAG = "DanceScenario";
    private static String APPKEY = "os.sys.dance";

    public static final String DANCE_BACKGROUND_MUSIC_Dir="TuubaDanceBackgroundMusic";
    public static final String DANCE_CONFIG_DIR="TuubaDanceConfig";
    public static final String DANCE_CONFIG_FILE_NAME="danceActionConfig";


    private Context mContext;
    private ICommonInterface mISceneV;
//    private MediaPlayer mediaPlayer;
    private String interrupt;
    private boolean createState;
    private DetailsEntity details;
    private SongEntity songEntity;

    /**
     * mohuaiyuan : 歌曲名称
     */
    private String songName;
    private String singer;

    private CommonRequestManager manager;

    private int position;

    private List<String> musicNames;
    /**
     * 存放机器人舞蹈动作和背景音乐的对应关系
     */
    Map<Integer,String>actionMap;

    //mohuaiyuan 仅仅用于测试
//    public DanceScenario(Context context){
//        this.mContext=context;
//
//        manager=CommonRequestManager.getInstanse(mContext);
//        manager.setTAG(TAG);
//    }
    private TTS tts;
    private ScenarioManager scenarioManager;

    private AudioUtils audioUtils;

    private List<String> volumeKeyWords;//音量
    private List<String> batteryKeyWords;//电量
    private VolumeControl volumeControl;

    public DanceScenario(ICommonInterface mISceneV){
        Log.d(TAG, "DanceScenario: ");
        this.mContext = (Context)mISceneV;
        this.mISceneV = mISceneV;

        manager=CommonRequestManager.getInstanse(mContext);
        actionMap=new HashMap<>();
        manager.setTAG(TAG);


        scenarioManager = new ScenarioManager(mContext);

        audioUtils=new AudioUtils(mContext);
    }

    private void initData(){
        initVoluemKeyWord();
        initBatteryKeyWord();
    }

    @Override
    public void onScenarioLoad() {
    }

    @Override
    public void onScenarioUnload() {
        mISceneV.getScenario("os.sys.Dance_stop");
    }

    @Override
    public boolean onStart() {
        tts = new TTS(mContext,new BaseScene(mContext,"os.sys.chat"));
        return true;
    }

    @Override
    public boolean onExit() {
        Log.d(TAG, "onExit: ");
        Log.d(TAG, "退出舞蹈场景 ");
        SceneManager.setPlayStatus(SceneManager.STATUS_OTHER);
        mISceneV.getScenario("os.sys.dance_stop");
        try {
            if (getMediaPlayer()!=null && getMediaPlayer().isPlaying()){
                getMediaPlayer().stop();
                getMediaPlayer().release();//释放资源
            }
            manager.setMediaPlayer(null);
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
		
        scenarioManager.quitCurrentScenario();

//        manager.mediaPlayonExit(mediaPlayer);

        return true;
    }


    @Override
    public boolean onTransmitData(Behavior behavior) {
        Log.d(TAG, "onTransmitData: ");
        if (behavior.results != null) {
            Log.i("Javen","进入跳舞场景.......");
            Log.d(TAG, "进入跳舞场景.......: ");

            if (SceneManager.getPlayStatus()==SceneManager.STATUS_PLAYING){
                Log.d(TAG, "正在播放音乐: ");
                return false;
            }else {
                Log.d(TAG, "没有在播放音乐: ");
            }
            SceneManager.setPlayStatus(SceneManager.STATUS_PLAYING);

            initData();
            //用于跟踪代码
            manager.setTAG(TAG);

//            mISceneV.getScenario("os.sys.song");
            mISceneV.getScenario("os.sys.dance");
            Behavior.IntentInfo intent = behavior.intent;
            JsonObject parameters = intent.getParameters();
            songEntity = new Gson().fromJson(parameters, SongEntity.class);
            songName=songEntity.getSong();
            singer=songEntity.getSinger();
            Log.d(TAG, "songName: "+songName);
            Log.d(TAG, "singer: "+singer);


//            initBackgroundMusic();

            try {
                initBackgroundMusic(DANCE_BACKGROUND_MUSIC_Dir);
                initActionConfig();
                Log.d(TAG, "actionMap: "+actionMap.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }


            try {
                if (songName==null || songName.length()<1){
                    executeSong();
                }else {
                    try {
                        prepareExecuteSong(songName);
                    }catch (Exception e){
                        Log.e(TAG, "指定舞蹈 不存在 e : "+e.getMessage() );
                        //mohuaiyuan 20180111 原来的代码
//                        tts.speak(manager.getString(R.string.noExistDance));
                        //mohuaiyuan 20180111 新的代码 20180111
                        try {
                            BFrame.response(R.string.noExistDance);
                        } catch (Exception e1) {
                            Log.e(TAG, "noExistDance 出现Exception e1 : "+e1.getMessage() );
                            e1.printStackTrace();
                        }
						
                        e.printStackTrace();
                        onExit();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        return true;
    }



    private void prepareExecuteSong(String songName) throws Exception{
        Log.d(TAG, "prepareExecuteSong: ");
        if (songName==null || songName.length()<1){
            throw new Exception("Illegal  parameter");
        }
        boolean isContains=false;
        for (int i=0;i<musicNames.size();i++){
            String temp=musicNames.get(i);
            if (temp.toLowerCase().contains(songName.toLowerCase())  ){
                position=i;
                isContains=true;
                break;
            }
        }
        if (isContains){
            executeSong(position);
        }else {
            throw new Exception("no exist dance");
        }

    }


    @Override
    public boolean onUserInterrupted(int i, Bundle bundle) {
        Log.d(TAG, "DanceScenario onUserInterrupted: ");

        Log.d(TAG, "bundle!=null: " + (bundle != null));
        try {
            if (bundle != null) {
                Log.i("Javen", bundle.toString() + "..." + i);
                Log.i(TAG, bundle.toString() + "..." + i);
                interrupt = bundle.getString("interrupt_extra_voice_cmd");
            }

            if (TobotUtils.isNotEmpty(interrupt) && i == 1) {
                try {
//                    if (interrupt.contains("暂停")) {
//                        Log.d(TAG, "暂停: ");
//                        mISceneV.getScenario("os.sys.dance_stop");
////                    mediaPlayer.pause();
//                    }
//                    if (interrupt.contains("不想听了") || interrupt.contains("好了") || interrupt.contains("可以了")) {
//                        Log.d(TAG, "不想听了 or 好了  or 可以了: ");
////                    mediaPlayer.stop();
//                        mISceneV.getScenario("os.sys.dance_stop");
//                        interruptDance();
//                        onExit();
//                    }
//                    if (interrupt.contains("继续") && !getMediaPlayer().isPlaying()) {
//                        Log.d(TAG, "继续: ");
//                        mISceneV.getScenario("os.sys.dance");
////                    mediaPlayer.start();
//                    }
//                    //mohuaiyuan 仅仅用于测试 20170912
//                    if (interrupt.contains("退出") || interrupt.contains("推出")) {
//                        Log.d(TAG, "退出: ");
//                        mISceneV.getScenario("os.sys.dance_stop");
//                        //mohuaiyuan 20171208   5、摸头或语音舞蹈打断，音乐马上停止，并下发直立动作，
////                        beforeExit();
//                        interruptDance();
//                        onExit();
//
//                    }

//                //mohuaiyuan 测试 跳下一个舞蹈 ，仅仅用于测试 20170912
//                if (interrupt.contains("再来一个") || interrupt.contains("再跳一个")|| interrupt.contains("跳支舞") ||interrupt.contains("下一个")){
//                    Log.d(TAG, "跳下一个舞蹈: ");
//                    playNextBackgroundMusic();
//                }

                    //mohuaiyuan 20180123 新的代码 20180123
                    //音量控制
                    boolean isContaintsVolumeKeyWord=false;
                    Log.d(TAG, "interrupt:"+interrupt);
                    for (int size=0;size<volumeKeyWords.size();size++){
                        String keywork=volumeKeyWords.get(size);
                        boolean isContains=interrupt.contains(keywork);
                        Log.d(TAG, "onUserInterrupted: ");
                        if (isContains){
                            isContaintsVolumeKeyWord=true;
                            break;
                        }
                    }
                    Log.d(TAG, "isContaintsVolumeKeyWord: "+isContaintsVolumeKeyWord);
                    if (isContaintsVolumeKeyWord){
                        volumeControl.dealWithVolume(interrupt);
                    }

                    //场景中电量查询
                    boolean isContaintsBatteryKeyWord = false;
                    for (int size = 0; size < batteryKeyWords.size(); size++) {
                        String keywork = batteryKeyWords.get(size);
                        boolean isContains = interrupt.contains(keywork);
                        if (isContains) {
                            isContaintsBatteryKeyWord = true;
                            break;
                        }
                    }
                    if (isContaintsBatteryKeyWord){
                        BFrame.getBBattery().balance();
                    }

                } catch (IllegalStateException e) {
                    Log.d(TAG, "IllegalStateException e: "+e.getMessage());


                }
            } else if (TobotUtils.isNotEmpty(bundle.getString("interrupt_extra_touch_keyEvent")) && i == 2) {
                Log.d(TAG, "进入打断处理");
                interruptDance();
                this.onExit();
            }
        } catch (NullPointerException e) {
            Log.d(TAG, "进入打断处理:catch 1");
            if (TobotUtils.isEmpty(bundle) && i == 2) {
                Log.d(TAG, "进入打断处理:catch 2");
                interruptDance();
                this.onExit();
            }
        }
        return true;
    }

    
    private void beforeExit() {
        Log.d(TAG, "beforeExit: ");
        try{
            if (getMediaPlayer() != null && getMediaPlayer().isPlaying()) {
//                mediaPlayer.stop();
//                mediaPlayer.release();
//                mediaPlayer = null;
            } else {
                Log.d(TAG, "mediaPlayer!=null: " + (getMediaPlayer() != null));
                onExit();
            }
        }catch (Exception e){

        }

    }

    /**
     * 打断舞蹈
     */
    private void interruptDance(){
        Log.d(TAG, "interruptDance 中断跳舞: ");
        // 动作打断paramType = 4
        BFrame.motion(1,new SimpleFrameCallback(){
            @Override
            public void onStarted() {
                Log.d(TAG, "中断跳舞 motor onStarted: ");

            }

            @Override
            public void onStopped() {
                Log.d(TAG, "中断跳舞 motor onStopped: ");

            }

            @Override
            public void onPaused() {
                Log.d(TAG, "中断跳舞 motor onPaused: ");

            }

            @Override
            public void onResumed() {
                Log.d(TAG, "中断跳舞 motor onResumed: ");

            }

            @Override
            public void onInterrupted() {
                Log.d(TAG, "中断跳舞 motor onInterrupted: ");

            }

            @Override
            public void onCompleted() {
                Log.d(TAG, "中断跳舞 motor onCompleted: ");

            }

            @Override
            public void onError(String s) {
                Log.d(TAG, "中断跳舞 motor onError: ");
                Log.e(TAG, "onError  s: "+s);

            }
        });


    }
    
    @Override
    public String getScenarioAppKey() {
        return APPKEY;
    }

    @Override
    public ScenarioRuntimeConfig configScenarioRuntime(ScenarioRuntimeConfig scenarioRuntimeConfig) {
        scenarioRuntimeConfig.allowDefaultChat = false;
        scenarioRuntimeConfig.interruptMatchMode = scenarioRuntimeConfig.INTERRUPT_CMD_MATCH_MODE_FUZZY;
        //为场景添加打断语，asr 识别到打断语时将产生打断事件，回调到场景的onUserInterrupted() 方法。
//        scenarioRuntimeConfig.addInterruptCmd("暂停");
//        scenarioRuntimeConfig.addInterruptCmd("继续");
//        scenarioRuntimeConfig.addInterruptCmd("不想听了");
//        scenarioRuntimeConfig.addInterruptCmd("好了");
//        scenarioRuntimeConfig.addInterruptCmd("可以了");
//        scenarioRuntimeConfig.addInterruptCmd("快进");
//        scenarioRuntimeConfig.addInterruptCmd("前进");
//        scenarioRuntimeConfig.addInterruptCmd("退出");
//        scenarioRuntimeConfig.addInterruptCmd("推出");
//
//        //mohuaiyuan 测试 跳下一个舞蹈,仅仅用于测试 20170912
//        scenarioRuntimeConfig.addInterruptCmd("再来一个");
//        scenarioRuntimeConfig.addInterruptCmd("再跳一个");
//        scenarioRuntimeConfig.addInterruptCmd("跳支舞");
//        scenarioRuntimeConfig.addInterruptCmd("下一个");

        //mohuaiyuan 20180123 新的代码 20180123
        //音量控制
        for (int i=0;i<volumeKeyWords.size();i++){
            scenarioRuntimeConfig.addInterruptCmd(volumeKeyWords.get(i));
        }
        //电量控制
        for (int i=0;i<batteryKeyWords.size();i++){
            scenarioRuntimeConfig.addInterruptCmd(batteryKeyWords.get(i));
        }

        return scenarioRuntimeConfig;
    }

    private void initVoluemKeyWord(){
        Log.d(TAG, "initVoluemKeyWord: ");
        if (volumeControl==null){
            volumeControl=new VolumeControl();
            volumeControl.setmContext(mContext);
        }
        if (volumeKeyWords==null || volumeKeyWords.isEmpty()){
            volumeKeyWords=volumeControl.getVolumeKeyWords();
        }
    }

    //电量
    private void initBatteryKeyWord(){
        if (batteryKeyWords==null || batteryKeyWords.isEmpty()){
            batteryKeyWords = BBattery.getBatteryKeyWords();
        }
    }


    /**
     * 初始化背景音乐,获取所有背景音乐的文件路径
     */
    private void initBackgroundMusic() {
        //读取assects 音乐文件 文件名
        String[] fileNames=null;

        try {
            fileNames=mContext.getResources().getAssets().list("");
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (int length=0;length<fileNames.length;length++){
            Log.d(TAG, "fileName: "+fileNames[length]);
        }
        Log.d(TAG, "----------------------------------------: ");

        if (musicNames==null){
            musicNames=new LinkedList<>();
        }
        if (!musicNames.isEmpty()){
            musicNames.clear();
        }


        for (int i=0;i<fileNames.length;i++){
            if (isMusicName(fileNames[i])){
                musicNames.add(fileNames[i]);
            }
        }
        for (int index=0;index<musicNames.size();index++){
            Log.d(TAG, "musicName: "+musicNames.get(index));
        }

    }

    /**
     * 初始化背景音乐：获取所有背景音乐的文件路径
     */
    private void initBackgroundMusic(String musicDir) throws Exception {
        Log.d(TAG, "initBackgroundMusic(String fileName): ");
        //如果手机插入了SD卡，而且应用程序具有访问SD卡的权限
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File musicFile = null;
            musicFile = manager.getSDcardFile(musicDir);
            if(musicFile==null){
                new Exception("There is not DanceBackgroundMusic dir !");
            }

//            String[] fileNames = null;
            File [] files=null;
            boolean exists=musicFile.exists();
            Log.d(TAG, "exists: "+exists);
            if (exists) {
                Log.d(TAG, "musicFile: " + musicFile.getAbsolutePath());
//                fileNames = musicFile.list();
                files=musicFile.listFiles();
            } else {
                new Exception("There is not DanceBackgroundMusic dir !");
            }

//            for (int i=0;i<files.length;i++){
//                Log.d(TAG, "files[i]: "+files[i].getAbsolutePath());
//            }

            if (musicNames == null) {
                musicNames = new LinkedList<>();
            }

            if (!musicNames.isEmpty()){
                musicNames.clear();
            }

            for (int i = 0; i < files.length; i++) {
                musicNames.add(files[i].getAbsolutePath());
            }

            for (int index = 0; index < musicNames.size(); index++) {
                Log.d(TAG, "musicName: " + musicNames.get(index));
            }


        }else {
            new Exception("Ther is not SD card or permission denied");
        }

    }

    /**
     * 初始化（读取）配置文件：配置文件中包含背景音乐与舞蹈动作的对应关系
     */
    private void initActionConfig()throws Exception{
        Log.d(TAG, "initActionConfig: ");
        String configFileName=DANCE_CONFIG_DIR+File.separator+DANCE_CONFIG_FILE_NAME;
        File file=manager.getSDcardFile(configFileName);

        if (file==null){
            throw  new Exception("illegal null file!");
        }

        FileInputStream is=null;
        BufferedReader br=null;
        String line="";
        try {
            is=new FileInputStream(file);
            br=new BufferedReader(new InputStreamReader(is));
            while ((line=br.readLine())!=null){
                String [] temp=line.split(",");
                if (temp!=null && temp.length==2){
                    actionMap.put(Integer.valueOf(temp[1].trim()),temp[0].trim());
                }else {
                    Log.e(TAG, "There are some errors in your configuration file:"+ DANCE_CONFIG_FILE_NAME);
                }

            }
            br.close();
            is.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (br!=null){
                    br.close();
                }
                if (is!=null){
                    is.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }


    public void executeSong()throws Exception{
        Log.d(TAG, "executeSong(): ");

        Random random=new Random();
        position=random.nextInt(musicNames.size());
        Log.d(TAG, "position: "+position);
        executeSong(position);

    }

    private void executeSong(int position)throws Exception{
        Log.d(TAG, "executeSong(int position): ");
        if (position>=musicNames.size() || position<0){
            Log.e(TAG, "illegal position: " );
            throw new Exception("Illegal position to get musicName!" );
        }

        String musicName=musicNames.get(position);
        Log.d(TAG, "musicName: "+musicName);

        try {
            executeSong(musicName);
        } catch (IOException e) {
            e.printStackTrace();
			
            //mohuaiyuan 20180111 原来的代码
//            tts.speak(manager.getString(R.string.noExistDance));
            //mohuaiyuan 20180111 新的代码 20180111
            try {
                BFrame.response(R.string.noExistDance);
            } catch (Exception e1) {
                Log.e(TAG, "executeSong 出现 Exception e1: "+e1.getMessage());
                e1.printStackTrace();
            }

            Log.e(TAG, "executeSong e: "+e.getMessage() );
        }


    }

    private void executeSong(final String fileName) throws IOException {
        Log.d(TAG, "executeSong(String fileName): ");

        MediaPlayer.OnPreparedListener onPreparedListener=new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                Log.d(TAG, "MediaPlayer.OnPreparedListener onPrepared: ");
                //mohuaiyuan 增加语音播报 20171207
                Log.d(TAG, "songName: "+songName);
                Log.d(TAG, "fileName: "+fileName);


                final int actionCode=getActionCode();
                Log.d(TAG, "actionCode: "+actionCode);
                if (songName==null || songName.trim().length()<1){
                    songName=actionMap.get(actionCode);
                    if (songName.contains(".")){
                        songName=songName.substring(0,songName.indexOf("."));
                    }
                }
                Log.d(TAG, "songName: "+songName);
                //mohuaiyuan 20180111 原来的代码
//                String speech=manager.getString(R.string.beforePlayDance)+":"+songName;
//                tts.speak(speech, new ITTSCallback() {
//                    @Override
//                    public void onStart(String s) {
//                        Log.d(TAG, "tts onStart: ");
//
//                    }
//
//                    @Override
//                    public void onPaused() {
//                        Log.d(TAG, "tts onPaused: ");
//
//                    }
//
//                    @Override
//                    public void onResumed() {
//                        Log.d(TAG, "tts onResumed: ");
//
//                    }
//
//                    @Override
//                    public void onCompleted() {
//                        Log.d(TAG, "tts onCompleted: ");
//                        //机器人开始跳舞动作
//                        getMediaPlayer().start();
//                        sendBodyAction(actionCode);
//
//                    }
//
//                    @Override
//                    public void onError(String s) {
//                        Log.d(TAG, "tts onError: ");
//
//                    }
//                });
                //mohuaiyuan 20180111 新的代码 20180111
                String speech=manager.getString(R.string.beforePlayDance,songName);
                Map<String,String> map=null;
                try {
                    map= BFrame.getString(speech);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                BaseTTSCallback baseTTSCallback=new BaseTTSCallback(){
                    @Override
                    public void onCompleted() {
                        BFrame.Ear(EarActionCode.EAR_MOTIONCODE_8);
                        //机器人开始跳舞动作
                        getMediaPlayer().start();
                        sendBodyAction(actionCode);

                    }
                };
                BFrame.setInterruptTTSCallback(new InterruptTTSCallback(BFrame.main,baseTTSCallback));

                try {
                    BFrame.responseWithCallback(map);
                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
        };

        MediaPlayer.OnCompletionListener onCompletionListener = new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                Log.d(TAG, " MediaPlayer.OnCompletionListener onCompletion: ");
                SceneManager.setPlayStatus(SceneManager.STATUS_OTHER);
                mISceneV.getScenario("os.sys.dance");
                onExit();
//                scenarioManager.quitCurrentScenario();
            }
        };

        MediaPlayer.OnErrorListener onErrorListener = new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                Log.d(TAG, "MediaPlayer.OnErrorListener onError: ");
                Log.d(TAG, "what: "+what);
                Log.d(TAG, "extra: "+extra);
                SceneManager.setPlayStatus(SceneManager.STATUS_OTHER);
                return false;
            }
        };

        try {
            manager.playMusic(fileName,onPreparedListener,onCompletionListener,onErrorListener);
        } catch (Exception e) {
            Log.d(TAG, "播放音乐 出现 Exception e : "+e.getMessage());
            e.printStackTrace();
        }

    }

    /**
     * 播放当前舞蹈的背景音乐
     */
//    private void replayBackgroundMusic(){
//        Log.d(TAG, "replayBackgroundMusic: ");
//        try {
//            executeSong(position);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//    }

    /**
     * 播放下一个舞蹈的背景音乐
     */
//    private void playNextBackgroundMusic(){
//        Log.d(TAG, "playNextBackgroundMusic: ");
//        int musicSize=musicNames.size();
//        if (musicSize<1){
//            Log.e(TAG, "(musicSize<1 " );
//            return;
//        }
//        if (musicSize==1){
//
//        }else {
//            position+=1;
//            if (position>=musicSize){
//                position-=musicSize;
//            }
//        }
//
//        String musicName=musicNames.get(position);
//        Log.d(TAG, "musicName: "+musicName);
//
//        try {
//            executeSong(musicName);
//        } catch (IOException e) {
//            e.printStackTrace();
//            Log.e(TAG, "executeSong e: "+e.getMessage() );
//        }
//
//    }

    private boolean isMusicName(String fileName) {
        boolean isMusic = false;
        if (fileName.endsWith(".mp3") || fileName.endsWith(".wav")) {
            isMusic = true;
        }
        return isMusic;
    }


    /**
     *
     * @param bodyActionCode:BodyActionCode.ACTION_6
     */
    private void sendBodyAction(int bodyActionCode ){
        Log.d(TAG, "sendBodyAction: ");
        BFrame.motion(bodyActionCode,new SimpleFrameCallback(){
            @Override
            public void onStarted() {
                super.onStarted();
                Log.d(TAG, "sendBodyAction onStarted: ");
                //播放背景音乐
                getMediaPlayer().start();
            }

            @Override
            public void onStopped() {
                super.onStopped();
                Log.d(TAG, "sendBodyAction onStopped: ");
            }

            @Override
            public void onPaused() {
                super.onPaused();
                Log.d(TAG, "sendBodyAction onPaused: ");
            }

            @Override
            public void onResumed() {
                super.onResumed();
                Log.d(TAG, "sendBodyAction onResumed: ");
            }

            @Override
            public void onInterrupted() {
                super.onInterrupted();
                Log.d(TAG, "sendBodyAction onInterrupted: ");
            }

            @Override
            public void onCompleted() {
                super.onCompleted();
                Log.d(TAG, "sendBodyAction onCompleted: ");
            }

            @Override
            public void onError(String s) {
                super.onError(s);
                Log.d(TAG, "sendBodyAction onError: ");
                Log.d(TAG, "onError: "+s);
            }
        });

    }

    private int getActionCode(){
        Log.d(TAG, "getActionCode: ");
        int actonCode=-1;
        Iterator<Integer>iterator= actionMap.keySet().iterator();
        while (iterator.hasNext()){
            int key=iterator.next();
//            Log.d(TAG, "------------------------: ");
//            Log.d(TAG, "key: "+key);
            String musicName=musicNames.get(position).trim();
            String action=actionMap.get(key).trim();
//            Log.d(TAG, "musicName: "+musicName);
//            Log.d(TAG, "action: "+action);
//            Log.d(TAG, "musicName.contains(action): "+musicName.contains(action));
            if (musicName.contains(action)){
                actonCode=key;
                break;
            }
        }
        if (actonCode==-1){
            return -1;
        }
        return actonCode;
    }

    private MediaPlayer getMediaPlayer(){
        return manager.getMediaPlayer();
    }


}
