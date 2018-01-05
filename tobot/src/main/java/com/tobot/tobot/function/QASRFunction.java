package com.tobot.tobot.function;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.google.gson.Gson;
import com.qdreamer.qvoice.QEngine;
import com.qdreamer.qvoice.QModule;
import com.qdreamer.qvoice.QSession;
import com.tobot.tobot.MainActivity;
import com.tobot.tobot.R;
import com.tobot.tobot.base.Constants;
import com.tobot.tobot.base.TobotApplication;
import com.tobot.tobot.entity.QASREntity;
import com.tobot.tobot.presenter.BRealize.BFrame;
import com.tobot.tobot.presenter.BRealize.BaseTTSCallback;
import com.tobot.tobot.presenter.BRealize.DormantManager;
import com.tobot.tobot.presenter.BRealize.DormantUtils;
import com.tobot.tobot.presenter.BRealize.InterruptTTSCallback;
import com.tobot.tobot.utils.TobotUtils;
import com.turing123.robotframe.config.SystemConfig;
import com.turing123.robotframe.event.AppEvent;
import com.turing123.robotframe.function.FunctionState;
import com.turing123.robotframe.function.IFunctionStateObserver;
import com.turing123.robotframe.function.IInitialCallback;
import com.turing123.robotframe.function.asr.IASRFunction;
import com.turing123.robotframe.internal.function.asr.IFrameASRCallback;
import com.turing123.robotframe.internal.function.asr.IFrameASRHotWordUploadCallback;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Javen on 2017/10/31.
 */

public class QASRFunction implements IASRFunction {
//    private static final String TAG = "IDormant";
    private static final String TAG = "Javen QASRFunction";
    private static final String TAG1 = "QASRFunction";
    private Context mContext;
    private long session;
    private boolean start;
    private boolean init;
    private boolean asrInit;
    private List<String> list = new ArrayList<>();
    private IInitialCallback initialCallback;
    private IFrameASRCallback iFrameASRCallback;
    private FunctionState state;
    private String ASR;
    private QASREntity mQASREntity;
    private Gson gson = new Gson();
    private QEngine asrEngine;
    private MainActivity mainActivity;
    private QSession mQSession;


    public QASRFunction(Context context) {
        this.mContext = context;
        mainActivity = (MainActivity)mContext;
    }
    @Override
    public void initASR(IInitialCallback iInitialCallback) {
        this.initialCallback = iInitialCallback;
        mQSession = new QSession(TobotApplication.getInstance());
        session = mQSession.initSession(Constants.QVOICE_APPID, Constants.QVOICE_KEY);//初始化session
        Log.i(TAG, "session:" + session);
        mQSession.setQSessionCallback(new QSession.QSessionCallBack() {

            @Override
            public void errorCode(String arg0) {
                Log.d("qdreamer", "error code:"+arg0);
            }
        });
        init = QModule.init(session, Constants.QVOICE_PARAMS, Constants.QVOICE_PATH);
        Log.i(TAG, "QModule.init:" + init);
        if (init) {
            asrEngine = new QEngine();// 创建识别引擎实例
            asrInit = asrEngine.init(session, Constants.QVOICE, asrHandler);// 初始化asr引擎
            Log.i(TAG, "asrEngine.init:" + asrEngine);
            if (asrInit) {
                start = QModule.start(sessionhandler);// 启动引擎
                Log.i(TAG, "QModule.start:" + start);
                if (start) {
                    state = FunctionState.IDLE;
                    if (initialCallback != null) {
                        initialCallback.onSuccess();
                        asrInterrupted();//提供asr打断回调
                    }
                } else {
                    state = FunctionState.ERROR;
                    if (initialCallback != null) {
                        initialCallback.onError("ReplaceFunctionErrorCode: " + start);
                    }
                }
            }
        }
    }
    @Override
    public void startRecord(IFrameASRCallback iFrameASRCallback, boolean b) {
        Log.e(TAG,"startRecord...开始录音" + state);
        this.iFrameASRCallback = iFrameASRCallback;
        state = FunctionState.RUNNING;

//        if (b){
//            Frequency.start("/sdcard/.TuringResource/audio/asr_prompt_tone.mp3");
//        }
    }

    @Override
    public void stopRecord() {        Log.i(TAG,"stopRecord...结束录音" + state);
        state = FunctionState.IDLE;
    }

    @Override
    public void cancelRecord() {
//        Log.i(TAG,"cancelRecord...取消录音" + state);
        state = FunctionState.IDLE;
    }

    @Override
    public void uploadHotWords(List<String> list, IFrameASRHotWordUploadCallback iFrameASRHotWordUploadCallback) {
//        Log.i(TAG,"uploadHotWords..." + state);
    }

    @Override
    public void config(Bundle bundle) {
//        Log.i(TAG,"config...配置" + state);
    }

    @Override
    public void onFunctionLoad() {
//        Log.i(TAG,"onFunctionLoad...加载" + state);
    }

    @Override
    public void onFunctionUnload() {
//        Log.i(TAG,"onFunctionUnload...卸载" + state);
    }

    @Override
    public void onFunctionInterrupted() {
        state = FunctionState.IDLE;
//        Log.i(TAG,"onFunctionInterrupted...打断" + state);
    }

    @Override
    public int getFunctionType() {
        Log.i(TAG,"getFunctionType...类型" + state);
        return AppEvent.FUNC_TYPE_ASR;
    }

    @Override
    public void choiceProcessor(int i) {
        Log.i(TAG,"choiceProcessor...处理机" + state);
    }

    @Override
    public void resetFunction() {
        Log.i(TAG,"resetFunction...重置" + state);
    }

    @Override
    public FunctionState getState() {
        Log.i(TAG,"getState...状态" + state);
        return state;
    }

    @Override
    public void setStateObserver(IFunctionStateObserver iFunctionStateObserver) {
        Log.i(TAG,"setStateObserver...状态监听" + state);
    }

    byte[] data;
    Handler sessionhandler = new Handler() {

        @Override
        public void handleMessage(final Message msg) {
            if (msg.obj != null) {
                data = (byte[]) msg.obj;
            }
            switch (msg.what) {
                case QModule.QVOICE_SPEECH_START:// 检测到用户开始说话回调
                    Log.d(TAG1, "SPEECH_BFIO_START:检测到用户开始说话回调");
                    asrEngine.start();// 启动识别引擎
                    if (TobotUtils.isNotEmpty(iFrameASRCallback)) {
                        iFrameASRCallback.onStartRecord();
                    }
                    break;
                case QModule.QVOICE_SPEECH_END:// 检查到用户说话结束回调
                    Log.d(TAG1, "SPEECH_BFIO_END:检查到用户说话结束回调");
                    asrEngine.feed(data, QEngine.FeedType.QENGINE_FEED_END);//
                    asrEngine.reset();// 重置识别引擎
                    if (TobotUtils.isNotEmpty(iFrameASRCallback)) {
                        iFrameASRCallback.onEndofRecord();
                    }
                    break;
                case QModule.QVOICE_SPEECH_DATA:// 回声消除后音频回调，用户可把data送入第三方引擎
                    Log.d(TAG1, "QVOICE_SPEECH_DATA:回声消除后音频回调，用户可把data送入第三方引擎aec data :" + data.length);
                    asrEngine.feed(data, QEngine.FeedType.QENGINE_FEED_DATA);// 识别引擎处理音频数据
                    break;
                case QModule.QVOICE_AEC_CANCEL://取消唤醒词识别
                    Log.d(TAG, "取消唤醒词识别:");
                    // 取消识别唤醒词
                    asrEngine.cancelEngine();
                    break;
                case QModule.QVOICE_AEC_WAKED:// 唤醒回调
                    Log.d(TAG, "唤醒回调");
                    BFrame.TTS("我在");

                    BFrame.getRobotFrameManager().interrupt(SystemConfig.INTERRUPT_TYPE_TOUCH, null);
                    break;
                case QModule.QVOICE_AEC_DIRECTION:// 唤醒方位角回调
                    String direction = new String(data);
                    Log.d(TAG, "唤醒方位角回调:" + direction + "\n");
                    break;
                case QModule.QVOICE_ASR_RESULT://识别结果
                    Log.d(TAG, "识别结果:" + new String(data));
//                    ASR = new String(data);
//                    mQASREntity = gson.fromJson(ASR,QASREntity.class);
//                    if (!(list.size() > 1)){
//                        list.clear();
//                    }
//                    list.add(mQASREntity.getRec().replaceAll("\\s*", ""));
//                    if (TobotUtils.isNotEmpty(iFrameASRCallback)) {
//                        iFrameASRCallback.onResults(list);
//                    }
//                    Log.d(TAG, "识别结果asr result:" +mQASREntity.getRec().replaceAll("\\s*", ""));
                    break;

                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };

//    private static final String SLEEP_AWAKEN_CANCEL_TASK="sleep_awaken";
//    /**
//     * 关键词 唤醒机器人，取消任务（取消N分钟之后由直立休眠变成坐下休眠的任务）
//     */
//    Handler  sleepAwakenHandler =new Handler(){
//        @Override
//        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
//            Bundle bundle=msg.getData();
//            String kind=bundle.getString(Constants.KIND_KEY);
//            Log.d(TAG, "handleMessage: ");
//            switch (kind){
//
//                case SLEEP_AWAKEN_CANCEL_TASK:
//                    Log.d(TAG, "sleepAwakenHandler handleMessage: ");
//                    Log.d(TAG, "cancelSitDownAndSleepTrigger:取消N分钟之后由直立休眠变成坐下休眠的任务 ");
//                    if (DormantManager.getType()==DormantManager.DORMANT_TYPE_STRAIGHT_TO_SLEEP){
//                        DormantManager dormantManager=new DormantManager();
//                        dormantManager.cancelSitDownAndSleepTrigger();
//                    }
//                    break;
//
//                default:
//                    break;
//
//            }
//
//        }
//    };

    private byte[] discern;
    private String discernASR;
    private boolean interruptIsFeasible;
    // 识别引擎消息处理handler
    Handler asrHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            if (msg.obj != null) {
                discern = (byte[]) msg.obj;
            }
            switch (msg.what) {
                case QEngine.QENGINE_ASR_DATA:// 识别结果回调
                    String result = new String(discern);
                    Log.d(TAG, "result=======>:" + result);
                    mQASREntity = gson.fromJson(result, QASREntity.class);
                    if (!(list.size() > 1)) {
                        list.clear();
                    }
                    discernASR = mQASREntity.getRec().replaceAll("\\s*", "");
                    if (discernASR.equals("你好小图") || discernASR.equals("你好小头")) {
                        Log.i(TAG,"interruptIsFeasible:"+interruptIsFeasible);
                        if (interruptIsFeasible) {
                            BFrame.getRobotFrameManager().interrupt(SystemConfig.INTERRUPT_TYPE_TOUCH, null);
                        }
                    } else {
                        list.add(discernASR);
                        if (TobotUtils.isNotEmpty(iFrameASRCallback)) {
                            iFrameASRCallback.onResults(list);
                        }
                    }
                    break;

                default:
                    break;
            }
//            deleteFile(new File(Constants.QVOICE_MIC));
            super.handleMessage(msg);
        }
    };


    public void deleteFile(File file) {
        Log.e(TAG,"deleteFile:");
        if (file.exists()) { // 判断文件是否存在
            Log.e(TAG,"文件存在");
            if (file.isFile()) { // 判断是否是文件
                file.delete(); //删除文件
                Log.e(TAG,"删除文件");
            } else if (file.isDirectory()) { // 否则如果它是一个目录
                for (File files : file.listFiles()) { // 遍历目录下所有的文件
                    this.deleteFile(files); // 把每个文件进行迭代
                }
            }
            file.delete();
        } else {
            Log.e(TAG, "文件不存在！" + "\n");
        }
    }

    private void asrInterrupted(){
        if (TobotUtils.isNotEmpty(mainActivity)){
            mainActivity.setConductInterrupt(new MainActivity.VoiceInterrupted() {

                @Override
                public void Voice(Object interrupt) {
                    Log.i(TAG,"Voice===>interrupt:"+interrupt);
                    interruptIsFeasible = (Boolean) interrupt;
                }
            });
        }
    }


}
