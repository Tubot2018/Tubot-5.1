package com.tobot.tobot.presenter.BRealize;

import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.tobot.tobot.Listener.ExpressionCallback;
import com.tobot.tobot.Listener.LocalCommandGather;
import com.tobot.tobot.Listener.MainScenarioCallback;
import com.tobot.tobot.Listener.SimpleFrameCallback;
import com.tobot.tobot.Listener.SomaModeListener;
import com.tobot.tobot.MainActivity;
import com.tobot.tobot.base.Constants;
import com.tobot.tobot.base.Frequency;
import com.tobot.tobot.control.Demand;
import com.tobot.tobot.db.bean.MemoryDBManager;
import com.tobot.tobot.db.model.Memory;
import com.tobot.tobot.function.AssembleFunction;
import com.tobot.tobot.function.QASRFunction;
import com.tobot.tobot.presenter.ICommon.ICommonInterface;
import com.tobot.tobot.presenter.IPort.IFrame;
import com.tobot.tobot.scene.BaseScene;
import com.tobot.tobot.scene.CustomScenario;
import com.tobot.tobot.scene.SceneManager;
import com.tobot.tobot.utils.TobotUtils;
import com.turing123.robotframe.RobotFrameManager;
import com.turing123.robotframe.RobotFramePreparedListener;
import com.turing123.robotframe.RobotFrameShutdownListener;
import com.turing123.robotframe.config.SystemConfig;
import com.turing123.robotframe.event.AppEvent;
import com.turing123.robotframe.function.FunctionManager;
import com.turing123.robotframe.function.IInitialCallback;
import com.turing123.robotframe.function.cloud.Cloud;
import com.turing123.robotframe.function.expression.Expression;
import com.turing123.robotframe.function.motor.IMotorCallback;
import com.turing123.robotframe.function.motor.Motor;
import com.turing123.robotframe.function.tts.ITTSCallback;
import com.turing123.robotframe.function.tts.TTS;
import com.turing123.robotframe.function.wakeup.VoiceWakeUp;
import com.turing123.robotframe.interceptor.StateBuilder;
import com.turing123.robotframe.multimodal.action.Action;
import com.turing123.robotframe.multimodal.action.EarActionCode;
import com.turing123.robotframe.multimodal.expression.FacialExpression;
import com.turing123.robotframe.scenario.ScenarioManager;

import java.util.HashMap;
import java.util.Map;

import static com.turing123.robotframe.multimodal.action.Action.PRMTYPE_EXECUTION_TIMES;


/**
 * Created by Javen on 2017/12/7.
 */

public class BFrame implements IFrame {
    private static final String TAG = "Javen BFrame";
    private static BFrame mBFarme;
    private static Context mContent;
    private ICommonInterface mISceneV;
    public static MainActivity main;
    private boolean whence;
    private static RobotFrameManager mRobotFrameManager;
    private FunctionManager functionManager;
    private CustomScenario customScenario;
    private static FacialExpression mFacialExpression;
    private static Expression mExpression;
    private static Motor motor;
    private Cloud mCloud;
    private static TTS tts;
    private static ScenarioManager scenarioManager;
    private BScenario mBScenario;
    private BConnect mBConnect;
    private BMonitor mBMonitor;
    private BDormant mBDormant;
    //mohuaiyuan  //mohuaiyuan 20171226 原来的代码
//    private BDormant mBDormant;
    //mohuaiyuan 20171226 新的代码 20171226
    /**
     * 站着休眠
     */
    private StraightToSleep mStraightToSleep;
    /**
     * 坐下休息（休眠）
     */
    private SitDownAndSleep mSitDownAndSleep;
    /**
     * 躺下休息（休眠）
     */
    private LieDownAndSleep mLieDownAndSleep;

    /**
     * 音量控制
     */
    private VolumeControl volumeControl;

    private static BPhoto mBPhoto;
    private SomaModeListener mSomaModeListener;
    private static BBattery mBBattery;
    private static BArmtouch mBArmtouch;
    private BProtect mBProtect;
    public static BSensor mBSensor;
    //    private static MotionFunction mMotionFunction;
    public static boolean replace;
    public static boolean prevent;//是否允许asr打断true/允许;false/阻止
    public static boolean isInterrupt;
    private static Memory memory;
    //    private ServiceHandler serviceHandler;
    public static boolean robotState = true;
    public static boolean initiate;
    private boolean astrict;

    private static InterruptTTSCallback interruptTTSCallback;
    private static SimpleFrameCallback actionSimpleFrameCallback;
    private static ExpressionCallback expressionCallback;
    private static SimpleFrameCallback earLightCircleCallback;

    public static synchronized BFrame instance(ICommonInterface mISceneV) {
        if (mBFarme == null) {
            mBFarme = new BFrame(mISceneV);
        }
        return mBFarme;
    }

    private BFrame(ICommonInterface mISceneV) {
        this.mISceneV = mISceneV;
        this.mContent = (Context) mISceneV;
        this.main = (MainActivity) mISceneV;
        Log.e(TAG, "正常加载 onInitiate()");
        onInitiate(false);
    }

    @Override
    public void onInitiate(boolean whence) {
        this.whence = whence;
        //0. 因为各功能的使用都需要携带使用该功能的场景，所以先创建一个场景，如果脱离场景使用，请使用FailOver 类。
        customScenario = new CustomScenario(mContent);
        //1. 设置对话模式为自动对话，主场景将维护对话的输入和输出。
        try {
            if (!initiate) {
                startRobotFramework();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //1. 获取ScenarioManager.
        scenarioManager = new ScenarioManager(mContent);
    }

    @Override
    public RobotFrameManager startRobotFramework() throws Exception {
        // 取得框架实例
        mRobotFrameManager = RobotFrameManager.getInstance(mContent);
        //设置apikey
        mRobotFrameManager.setApiKeyAndSecret(Constants.APIKEY, Constants.SERVICE);
        // 设置框架聊天模式
        setChatMode(SystemConfig.CHAT_MODE_AUTO);
        // 设置状态机工作模式。查看API Ref以了解更多关于框架工作模式的信息
        int state = new StateBuilder(StateBuilder.DefaultMode).build();
        // prepare（）这个方法必须在你做任何事情之前被调用
        Log.d(TAG, "框架初始化");
        mRobotFrameManager.prepare(state, new RobotFramePreparedListener() {

            @Override
            public void onPrepared() {
                // 激活
                if (!astrict) {
                    astrict = true;
                    Log.d(TAG, "框架激活");
                    frameHandle.sendEmptyMessage(Constants.REPLACE_ASR);
//                mRobotFrameManager.start();
//                frameHandle.sendEmptyMessage(Constants.START_SUCESS_MSG);
                }
            }

            @Override
            public void onError(String errorMsg) {
                // error occurred, check errorMsg and have all error fixed
                Message message = Message.obtain();
                message.what = Constants.START_ERROR_MSG;
                message.obj = errorMsg;
                frameHandle.sendMessage(message);
            }
        });
        return mRobotFrameManager;
    }

    private Handler frameHandle = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case Constants.START_ERROR_MSG://框架加载失败
//                    mBConnect.isLoad(false);
                    initiate = false;
                    mBConnect.shunt();
                    main.FrameLoadFailure();
                    Log.e(TAG, "start error ⊙﹏⊙b\n" + msg.obj);
                    break;

                case Constants.REPLACE_ASR:
                    //替换
                    replaceFunction();
                    mRobotFrameManager.start();
                    Log.d(TAG, "框架启动");
                    frameHandle.sendEmptyMessage(Constants.START_SUCESS_MSG);
                    break;

                case Constants.START_SUCESS_MSG:
                    Log.e(TAG, "⊙_⊙  框架加载成功");
                    initiate = true;
//                    mBConnect.isLoad(true);
                    //运行TTS
                    onTTS();
                    //初始化功能
                    onFunction();
                    //调度
                    onAssemble();
                    //进入次场景
                    onMinorscene();
                    //通知
                    onNotification();
                    //手臂触摸
                    onBArmtouch();
                    //mohuaiyuan 20171226 原来的代码
                    //休眠
                    //onDormant();
                    //mohuaiyuan 20171226 新的代码 20171226
                    //站着休眠
                    onStraightToSleep();
                    //坐下休息（休眠）
                    onSitDownAndSleep();
                    //躺下休息（休眠）
                    onLieDownAndSleep();
                    //音量控制
                    onAdjustVolume();
                    //唤醒
                    onRouse();
                    //注册监听器
                    onBSensor();
                    //本地命令
                    onLocal();
                    //自我保护
                    onBProtect();
                    //加载成功
                    main.FrameLoadSuccess(whence);

                    break;
                default:
                    break;
            }
        }
    };

    public void setConnectState(BConnect mBConnect) {
        this.mBConnect = mBConnect;
    }

    // TTS的使用
    private void onTTS() {
        tts = new TTS(main, new BaseScene(main, "os.sys.chat"));
    }

    // 初始化功能
    private void onFunction() {
        functionManager = new FunctionManager(main);
        motor = new Motor(main, new CustomScenario(main));
        mCloud = new Cloud(main, new MainScenarioCallback());
        mFacialExpression = new FacialExpression();
        mFacialExpression.displayMode = FacialExpression.DISPLAY_MODE_PROTOCOL_PREDEFINED;
        mFacialExpression.executeMode = Action.MODE_COVER;
        mFacialExpression.eyeParams.put(PRMTYPE_EXECUTION_TIMES, 1);
        mExpression = new Expression(main, new BaseScene(main, "os.sys.chat"));
        memory = new Memory();
    }

    private void onAssemble() {
        //1. 创建Assemble Function 实例。
        final AssembleFunction assembleFunction = new AssembleFunction(main);
        //2. 初始化
        assembleFunction.init(new IInitialCallback() {
            @Override
            public void onSuccess() {
                //3. 初始化成功后将assemble function加入RobotFrame.
                //3.1 获取Function 的管理类
                //3.2 调用addFunction, 将assembleFunction加入系统
                functionManager.addFunction(assembleFunction);
            }

            @Override
            public void onError(String s) {
            }
        });

        assembleFunction.setAssembleFunction(new AssembleFunction.IAssembleFunction() {
            @Override
            public void Permit(Object interrupt) {
                prevent = (boolean) interrupt;
                Log.i(TAG, "prevent:" + prevent);
            }
        });
    }

    //替换asr
    private void replaceFunction() {
        //1. 创建自定义Function 实例。
        final QASRFunction mQASRFunction = new QASRFunction(main);
        //2. 初始化自定义的Function.
        mQASRFunction.initASR(new IInitialCallback() {
            @Override
            public void onSuccess() {
                //3. 初始化成功后将自定义function加入RobotFrame.
                //3.1 获取Function 的管理类
                FunctionManager functionManager = new FunctionManager(main);
                //3.2 调用replaceFunction 替换系统中type相同的默认function（本示例替换asr）.
                boolean replaced = functionManager.replaceFunction(mQASRFunction);
                replace = replaced;
                Log.i(TAG, "替换成功:" + replaced);
            }

            @Override
            public void onError(String errorMessage) {
                Log.i(TAG, "替换失败:" + errorMessage);
            }
        });
    }

    // 进入次场景
    private void onMinorscene() {
        mBScenario = new BScenario(main);
    }

    // 监听
    private void onNotification() {
        mBMonitor = new BMonitor(main);
    }

    //mohuaiyuan 20171226 原来的代码
    /*// 休眠
    private void onDormant() {
        mBDormant = new BDormant(main);
    }*/

    //mohuaiyuan 20171226 新的代码 20171226
    /**
     * 站着休眠
     */
    private void onStraightToSleep() {
        mStraightToSleep = new StraightToSleep(main);
    }

    /**
     * 坐下休息（休眠）
     */
    private void onSitDownAndSleep() {
        mSitDownAndSleep = new SitDownAndSleep(main);
    }

    /**
     * 躺下休息（休眠）
     */
    private void onLieDownAndSleep() {
        mLieDownAndSleep = new LieDownAndSleep(main);
    }

    /**
     * 音量控制
     */
    private void onAdjustVolume() {
        volumeControl = new VolumeControl(mContent);
    }

    // 本地命令
    private void onLocal() {
        mBPhoto = BPhoto.instance(main);
        mSomaModeListener = SomaModeListener.instance(main);
        mBBattery = new BBattery(main);
    }

    // 手臂触摸
    private void onBArmtouch() {
        mBArmtouch = new BArmtouch(main);
    }

    // 自我保护机制
    private void onBProtect() {
        mBProtect = new BProtect(main);
    }

    // 传感器监听
    private void onBSensor() {
        mBSensor = new BSensor(main);
    }

    // 唤醒功能
    private void onRouse() {
        VoiceWakeUp mVoiceWakeUp = new VoiceWakeUp(main, customScenario);
        mVoiceWakeUp.configWakeUp(Constants.WAKEUP);
//            mVoiceWakeUp.configWakeUp("assets/WakeUp.bin");
    }

    // ASR提醒音
    public static void hint() {
        if (replace) {
            Frequency.hint();
        } else if (scenarioManager == null) {
            //1. 获取ScenarioManager.
            scenarioManager = new ScenarioManager(main);
            //2. 设置开关，true 为开， false 为关。
            scenarioManager.switchDefaultChatAsrPrompt(true, false);
        } else {
            scenarioManager.switchDefaultChatAsrPrompt(true, false);
        }
    }


//------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------


    public static RobotFrameManager getRobotFrameManager() {
        if (TobotUtils.isNotEmpty(mRobotFrameManager)) {
            return mRobotFrameManager;
        } else {
            return null;
        }
    }

    public static BPhoto getmBPhoto() {
        if (TobotUtils.isNotEmpty(mBBattery)) {
            return mBPhoto;
        } else {
            return null;
        }
    }

    public static BBattery getBBattery() {
        if (TobotUtils.isNotEmpty(mBBattery)) {
            return mBBattery;
        } else {
            return null;
        }
    }

    public static BArmtouch getBArmtouch() {
        if (TobotUtils.isNotEmpty(mBBattery)) {
            return mBArmtouch;
        } else {
            return null;
        }
    }


//--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------


    //选择asr
    public void choiceFunctionProcessor(int type) {
        functionManager.choiceFunctionProcessor(AppEvent.FUNC_TYPE_ASR, type);
    }

    public void resetFunction() {
        functionManager.resetFunction(AppEvent.FUNC_TYPE_ASR);
    }

    //下发动作
    public static void motion(int code) {
        motion(code, Action.PRMTYPE_EXECUTION_TIMES, 1, false, false, new SimpleFrameCallback());
    }

    public static void motion(int code, int type) {
        motion(code, type, 1, false, false, new SimpleFrameCallback());
    }

    public static void motion(int code, int type, int value) {
        motion(code, type, value, false, false, new SimpleFrameCallback());
    }

    public static void motion(int code, boolean order) {
        motion(code, Action.PRMTYPE_EXECUTION_TIMES, 1, order, false, new SimpleFrameCallback());
    }

    public static void motion(int code, boolean order, boolean reset) {
        motion(code, PRMTYPE_EXECUTION_TIMES, 1, order, reset, new SimpleFrameCallback());
    }

    public static void motion(int code, SimpleFrameCallback simpleFrameCallback) {
        motion(code, PRMTYPE_EXECUTION_TIMES, 1, false, false, simpleFrameCallback);
    }

    private static void motion(int code, int type, int value, boolean order, boolean reset, SimpleFrameCallback simpleFrameCallback) {
        int must = IsContinue();
        Log.w(TAG, "有连续动作 must:" + must);
        if (order) {//命令型动作
            orderMemoryAction(code, type, value, must, order, simpleFrameCallback);
        } else {
            if (reset) {//普通动作带复位
                notOrderMemoryResetAction(code, type, value, must, order, simpleFrameCallback);
            } else {//普通动作不带复位
                notOrderMemoryAction(code, type, value, must, order, simpleFrameCallback);
            }
        }
    }

    private static void orderMemoryAction(int code, int type, int value, int must, boolean order, SimpleFrameCallback simpleFrameCallback) {
        if (must != 0 && !TobotUtils.isReset(code)) {//非正常状态
            TTS("没看到我现在正" + nowState(must) + "吗?你应该先让我站起来");
        } else if (TobotUtils.isReset(code)) {//命令动作是否为后续动作
            Log.w(TAG, "命令动作自动重置 code:" + code);
            outAction(resetState(code), type, value, simpleFrameCallback);
            Log.w(TAG, "命令动作自动重置是否保存 code:" + code);
            IsMemory(code, order);
        } else if (must == 0) {//之前无记忆的命令动作
            Log.w(TAG, "正常命令动作 code:" + code);
            outAction(code, type, value, simpleFrameCallback);
            Log.w(TAG, "正常命令动作是否保存 code:" + code);
            IsMemory(code, order);
        }
    }

    private static void notOrderMemoryAction(int code, int type, int value, int must, boolean order, SimpleFrameCallback simpleFrameCallback) {
        if (must != 0) {
            if (code != must) {
                Log.w(TAG, "非命令有记忆动作,不执行复位 must:" + must);
//                    outAction(must, type, value);
                try {
                    Thread.sleep(10);
                    Log.w(TAG, "非命令有记忆动作,不复位也不执行动作 must:" + must);
//                        outAction(code, type, value);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                Log.w(TAG, "非命令有记忆正确动作 code:" + code);
                if (orderState().contains("00")) {//记忆的动作为非命令型
                    outAction(code, type, value, simpleFrameCallback);
                }
            }
        } else {
            Log.w(TAG, "非命令无记忆平常动作 code:" + code);
            outAction(code, type, value, simpleFrameCallback);
        }
        Log.w(TAG, "非命令无复位动作是否保存 code:" + code);
        IsMemory(code, order);
    }

    private static void notOrderMemoryResetAction(int code, int type, int value, int must, boolean order, SimpleFrameCallback simpleFrameCallback) {
        if (must != 0) {
            if (code != must) {
                Log.w(TAG, "非命令有记忆动作,执行复位 must:" + must);
                outAction(must, type, value, simpleFrameCallback);
                try {
                    Thread.sleep(10);
                    Log.w(TAG, "非命令有记忆动作,复位后执行动作 must:" + must);
                    outAction(code, type, value, simpleFrameCallback);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                Log.w(TAG, "非命令有记忆正确动作执行 code:" + code);
                outAction(code, type, value, simpleFrameCallback);
            }
        } else {
            Log.w(TAG, "非命令无记忆平常动作执行 code:" + code);
            outAction(code, type, value, simpleFrameCallback);
        }
        Log.w(TAG, "非命令复位动作是否保存 code:" + code);
        IsMemory(code, order);
    }

    private static void outAction(int code, int type, int value, SimpleFrameCallback simpleFrameCallback) {
        motor.doAction(Action.buildBodyAction(code, type, value), simpleFrameCallback);
    }


    /**
     * 下发耳部灯圈
     */
    public static void Ear(int code) {
        Ear(code, 80, 1, 1, new SimpleFrameCallback());
    }

    public static void Ear(int code, int priority) {
        Ear(code, 80, 1, priority, new SimpleFrameCallback());
    }

    public static void Ear(int code, int priority, IMotorCallback iMotorCallback) {
        Ear(code, 80, 1, priority, iMotorCallback);
    }

//    public static void Ear(int code, int pleasantness, int priority) {
//        Ear(code, 80, pleasantness, priority, new SimpleFrameCallback());
//    }
//
//    public static void Ear(int code, int brightness, int pleasantness, int priority) {
//        Ear(code, brightness, pleasantness,priority, new SimpleFrameCallback());
//    }

    public static void Ear(int code, int brightness, int pleasantness, int priority, IMotorCallback iMotorCallback) {
        Log.w(TAG, "下发耳部灯圈 code:" + code+ "优先级:" +priority);
        if (TobotUtils.isPriority(code,priority)) {
            Log.w(TAG, "耳部灯圈－事件 code:" + code+ "优先级:" +priority);
            motor.doAction(Action.buildEarAction(code, brightness, pleasantness), iMotorCallback);
        }else if (TobotUtils.isRank(code,priority)){
            Log.w(TAG, "耳部灯圈－逻辑 code:" + code+ "优先级:" +priority);
            motor.doAction(Action.buildEarAction(code, brightness, pleasantness), iMotorCallback);
        }
    }

    public static void EarWithCallback(int code, int brightness, int pleasantness, IMotorCallback iMotorCallback) {
        Ear(code, brightness, pleasantness,1, iMotorCallback);
    }




    /**
     * 下发表情
     */
    public static void Facial(String facial) {
        mFacialExpression.emoj = facial;
        mExpression.showExpression(mFacialExpression, new ExpressionCallback());
    }

    /**
     * 下发表情
     *
     * @param facial
     * @param callback
     */
    public static void FacialWithCallback(String facial, ExpressionCallback callback) {
        mFacialExpression.emoj = facial;
        mExpression.showExpression(mFacialExpression, callback);
    }

    /**
     * 脱离主场景
     */
    public static void shutChat() { mRobotFrameManager.toLostScenario(); }

    /**
     * 回到主场景
     */
    public static void disparkChat() {
        mRobotFrameManager.backMainScenario();
    }


    /**
     * tts语音
     *
     * @param voice
     */
    public static void TTS(String voice) {
        TTS(voice, ittsCallback);
    }

    public static void TTS(String voice, ITTSCallback ittsCallback) {
        tts.speak(voice, ittsCallback);
    }
    
    /**
     * 执行tts
     *
     * @param voice
     * @param mIttsCallback
     */
    public static synchronized void ttsWithCallback(String voice, ITTSCallback mIttsCallback) {
        Log.d(TAG, "ttsWithCallback: ");
        if (mIttsCallback == null) {
            Log.d(TAG, "mIttsCallback 为空");
            mIttsCallback = ittsCallback;
        }
        Log.d(TAG, "voice: " + voice);
        TTS(voice, mIttsCallback);
    }

    static ITTSCallback ittsCallback = new ITTSCallback() {

        @Override
        public void onStart(String s) {
            Log.i(TAG, "开始语音播报TTS:" + s);
            isInterrupt = true;
            if (!TobotUtils.isInScenario(SceneManager.SCENE)) {
                Ear(EarActionCode.EAR_MOTIONCODE_2);//发声效果
            }
        }

        @Override
        public void onPaused() {
            isInterrupt = false;
            if (!TobotUtils.isInScenario(SceneManager.SCENE)) {
                Ear(EarActionCode.EAR_MOTIONCODE_3);//asr效果
            }
        }

        @Override
        public void onResumed() {
            isInterrupt = true;
            if (!TobotUtils.isInScenario(SceneManager.SCENE)) {
                Ear(EarActionCode.EAR_MOTIONCODE_2);//发声效果
            }
        }

        @Override
        public void onCompleted() {
            Log.i(TAG, "TTS结束:");
            isInterrupt = false;
            if (!TobotUtils.isInScenario(SceneManager.SCENE)) {
                Ear(EarActionCode.EAR_MOTIONCODE_3);//asr效果
            }
            hint();//提示音
        }

        @Override
        public void onError(String s) {
            if (!TobotUtils.isInScenario(SceneManager.SCENE)) {
                Ear(EarActionCode.EAR_MOTIONCODE_3);//asr效果
            }
        }
    };

    /**
     * 进入睡眠
     */
    public static void FallAsleep() {
        if (TobotUtils.isNotEmpty(mRobotFrameManager)) {
            mRobotFrameManager.sleep();
            //5.2 命令执行完成后需明确告诉框架，命令处理结束，否则无法继续进行主对话流程。
            new LocalCommandGather().onComplete();
        }
    }

    public static void Wakeup() {
        if (TobotUtils.isNotEmpty(mRobotFrameManager)) {
            mRobotFrameManager.wakeup();
        }
        mBSensor.write2019Pattern((byte)0x00,SomaModeListener.instance(main));
    }


    public static void Wakeup(int type) {
        Wakeup();
    }

    /**
     * 设置聊天模式
     *
     * @param mode
     */
    public static void setChatMode(int mode) {
        mRobotFrameManager.setChatMode(mode);
    }

    //打断
    public static void proceedInterrupt() {
        mRobotFrameManager.interrupt(SystemConfig.INTERRUPT_TYPE_TOUCH, null);
        if (prevent) {
            //统一下发tts打断处理
            mFrameThing.setAssemble(false);
        }
        if (isInterrupt) {
            //框架tts打断处理
//            tts.speak(" ");
            ttsStop();
            if (!TobotUtils.isInScenario(SceneManager.SCENE)) {
                BFrame.Ear(EarActionCode.EAR_MOTIONCODE_3);//asr效果
            }
        }
        prevent = false;
        isInterrupt = false;
        Demand.instance(mContent).demandStop();//停止点播
        BFrame.Ear(EarActionCode.EAR_MOTIONCODE_3);
    }

    //触摸打断
    public static void touchInterrupt() {
        mRobotFrameManager.interrupt(SystemConfig.INTERRUPT_TYPE_TOUCH, null);
        if (prevent) {
            //统一下发tts打断处理
            Log.i(TAG, "统一下发tts打断处理");
            mFrameThing.setAssemble(true);
        }
        if (isInterrupt) {
            //框架tts打断处理
            Log.i(TAG, "框架tts打断处理");
            tts.speak(" ", ittsCallback);
        }
        BFrame.Ear(EarActionCode.EAR_MOTIONCODE_3);
    }

    //通知关机
    public static void shutDown() {
        mRobotFrameManager.shutDown(new RobotFrameShutdownListener() {

            @Override
            public void onShutDown() {
                Log.i(TAG, "已通知关机");
            }

            @Override
            public void onError(String s) {
                Log.i(TAG, "通知关机 onError" + s);
            }
        });
    }

    private static IFrameThing mFrameThing;

    public static IFrameThing getFrameThing() {
        return mFrameThing;
    }

    public static void setFrameThing(IFrameThing frameThing) {
        mFrameThing = frameThing;
    }

    public interface IFrameThing { void setAssemble(Object dispose); }

    public static void ttsPause() {
        tts.pause();
    }

    public static void ttsResume() {
        tts.resume();
    }

    public static void ttsStop() {
        tts.stop();
    }


//-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------


    //检索连贯动作
    public static int IsContinue() {
        int must = 0;
        try {
            memory = MemoryDBManager.getManager().queryById("memory");
            Log.w("Javen", "Global:" + memory.getGlobal());
            if (memory.getGlobal().equals("1111")) {
                switch (Integer.parseInt(memory.getMotion())) {
                    case Constants.squat:
                        must = Constants.squat_stand;
                        break;
                    case Constants.sitDown:
                        must = Constants.sitDown_stand;
                        break;
                    case Constants.lieDown:
                        must = Constants.lieDown_stand;
                        break;
                    case Constants.goProne:
                        must = Constants.goProne_stand;
                        break;
                    case Constants.SitBack:
                        must = Constants.SitBack_stand;
                        break;
                    case Constants.SideDown:
                        must = Constants.SideDown_stand;
                        break;
                }
            }
        } catch (NullPointerException e) {
            return must;
        }
        Log.i("Javen", "检索连贯动作接下来应该做 must:" + must);
        return must;
    }

    //检索当前状态
    public static String nowState(int action) {
        String state = "";
        switch (action) {
            case Constants.squat_stand:
                state = "蹲着";
                break;
            case Constants.sitDown_stand:
                state = "坐着";
                break;
            case Constants.lieDown_stand:
                state = "躺着";
                break;
            case Constants.goProne_stand:
                state = "趴着";
                break;
            case Constants.SitBack_stand:
                state = "坐着";
                break;
            case Constants.SideDown_stand:
                state = "倒在地上";
                break;
        }
        return state;
    }

    //是否为命令动作
    private static String orderState() {
        String order = "00";
        try {
            memory = MemoryDBManager.getManager().queryById("memory");
            order = memory.getOrderType();
            Log.w("Javen", "order:" + memory.getOrderType());
        } catch (NullPointerException e) {
        }
        return order;
    }

    //动作重置
    public static int resetState(int action) {
        int reset = 0;
        try {
            if (TobotUtils.isReset(action)) {
                reset = IsContinue();
            }
        } catch (Exception e) {
            return reset;
        }
        return reset != 0 ? reset : 1;
    }

    //检索是否记忆
    public static void IsMemory(int action, boolean order) {
        try {
            if (TobotUtils.isEmpty(memory)) {
                Log.w("Javen", "memory 为空:");
                memory = new Memory();
            }
            Log.w("Javen", "检索该动作是否需要保存 action:" + action);
            if (memory.getGlobal().equals("1111")) {//1.1.0首先检索该动作之前是否有记忆动作
                if (memory.getOrderType().contains("11")) {//1.1.1如果有,则检索之前记忆动作是否为命令动作
                    if (TobotUtils.isReset(action) && order) {//1.1.2如果是,则只检索该动作是否为后续动作且当前命令也是命令动作
                        memory.setMotion("0");
                        memory.setGlobal("0000");
                        Log.w("Javen", "之前有命令动作记忆,的后续动作(执行,不记忆,不保留之前记忆) action:" + action);
                        setMemory(order);
                    } else if (TobotUtils.isReset(action)) {//1.1.3.如果不是,则单独检索该动作是否为后续动作
                        Log.w("Javen", "之前有命令动作记忆,的后续动作,现命令为非命令动作(不记忆,不执行,但保留之前记忆) action:" + action);
                    } else if (TobotUtils.isMemory(action)) {//1.1.3.如果不是,则检索该动作是否为需记忆动作
//                    memory.setMotion("0");
//                    memory.setGlobal("0000");
                        Log.w("Javen", "之前有命令动作记忆,的需记忆动作(非正常,不记忆,不执行,但保留之前记忆) action:" + action);
                    } else {//1.1.4.如果该动作为非后续动作,也非需记忆动作的正常动作
//                    memory.setMotion("0");
//                    memory.setGlobal("0000");
                        Log.w("Javen", "之前有命令动作记忆,的普通动作(不记忆,不执行,但保留之前记忆) action:" + action);
                    }
                } else {//1.2.1如果不是命令动作
//                    if (TobotUtils.isReset(action) && !order){//1.2.2则检索是否为后续动作,且现动作为非命令
//                        memory.setMotion("0");
//                        memory.setGlobal("0000");
//                        Log.w("Javen", "之前有非命令动作记忆,的非命令的后续动作执行,不记忆 action:" + action);
//                    } else
                    if (TobotUtils.isReset(action)) {//1.2.2则检索是否为后续动作
                        memory.setMotion("0");
                        memory.setGlobal("0000");
                        Log.w("Javen", "之前有非命令动作记忆,的后续动作执行(执行,不记忆,不保留之前记忆) action:" + action);
                        setMemory(order);
                    } else if (TobotUtils.isMemory(action)) {//1.2.3如果不是,则检索该动作是否为需记忆动作
                        Log.w("Javen", "之前有非命令动作记忆,的需记忆动作(非正常,不记忆,不执行,但保留之前记忆) action:" + action);
                    } else {//1.2.4.如果该动作为非后续动作,也非需记忆动作的正常动作
                        Log.w("Javen", "之前有非命令动作记忆,的普通动作(不记忆,不执行,但保留之前记忆) action:" + action);
                    }
                }


//                if (TobotUtils.isReset(action) && !order){////1.1.如果有,则检索该动作是否为后续动作,且为非命令动作
//                    Log.w("Javen", "为非命令的后续动作不记忆(不执行,保留之前记忆) action:" + action);
//                }else if (TobotUtils.isReset(action)) {//1.2.如果不是,则只检索该动作是否为后续动作
//                    memory.setMotion("0");
//                    memory.setGlobal("0000");
//                    Log.w("Javen", "后续动作不记忆(执行,不保留之前记忆) action:" + action);
//                    setMemory(order);
//                }else if (TobotUtils.isMemory(action)) {//1.3.如果没有,则检索该动作是否为需记忆动作
////                    memory.setMotion("0");
////                    memory.setGlobal("0000");
//                    Log.w("Javen", "之前已经有记忆的需记忆动作(非正常,不记忆,不执行,但保留之前记忆) action:" + action);
//                }else {//1.4.如果该动作为非后续动作,也非需记忆动作的正常动作
////                    memory.setMotion("0");
////                    memory.setGlobal("0000");
//                    Log.w("Javen", "有记忆后的普通动作(不记忆,不执行,但保留之前记忆) action:" + action);
//                }
            } else if (TobotUtils.isMemory(action)) {//2.其次检索该动作是否在记忆序列;注意是否还需要在验证是否为命令动作
                memory.setMotion(action + "");
                memory.setGlobal("1111");
                Log.w("Javen", "要记忆动作 action:" + action);
                setMemory(order);
            } else {//3.该动作之前无记忆动作,也非需要记忆动作(既普通动作)
                memory.setMotion("0");
                memory.setGlobal("0000");
                Log.w("Javen", "普通动作不记忆 action:" + action);
                setMemory(order);
            }
        } catch (Exception e) {
        }
    }

    private static void setMemory(boolean order) {
        if (order) {
            memory.setOrderType("11");
        } else {
            memory.setOrderType("00");
        }
        MemoryDBManager.getManager().insertOrUpdate(memory);
    }


    public static final String RESPONSE_SPEECH = "speech";
    public static final String RESPONSE_ACTION = "action";
    public static final String RESPONSE_EXPRESSION = "expression";
    public static final String RESPONSE_EAR_LIGHT_CIRCLE = "earLightCircle";
    public static final String RESPONSE_EAR_PRIORITY = "earPriority";//Javen 20180208

    /**
     * 反馈，包括语音、动作、表情、灯圈的反馈，有哪些反馈由配置信息决定
     *
     * @param id ：配置信息的id
     * @throws Exception
     */
    public static void response(int id) throws Exception {
        Log.d(TAG, "response: ");
        Map<String, String> map = getString(id);
        Log.d(TAG, "map: " + map);
        response(map);
    }

    /**
     * 反馈，包括语音、动作、表情、灯圈的反馈，有哪些反馈由配置信息决定
     *
     * @param line:配置信息
     * @throws Exception
     */
    public static void response(String line) throws Exception {
        Log.d(TAG, "response: ");
        Map<String, String> map = getString(line);
        Log.d(TAG, "map: " + map);
        response(map);

    }

    /**
     * 反馈，包括语音、动作、表情、灯圈的反馈，有哪些反馈由配置信息决定
     *
     * @param dataMap:配置信息
     * @throws Exception
     */
    public static void response(Map<String, String> dataMap) throws Exception {
        Log.d(TAG, "response: ");
        if (dataMap == null || dataMap.isEmpty()) {
            throw new Exception("dataMap==null ||dataMap.isEmpty()");
        }
        //优先级 Javen 20180208
        String earPriority = dataMap.get(RESPONSE_EAR_PRIORITY);
        int earPriorityCode = 1;
        if (earPriority != null && earPriority.length() > 0) {
            try {
                earPriorityCode = Integer.valueOf(earPriority);
            } catch (NumberFormatException e) { }
        }
        //讲话
        String speech = dataMap.get(RESPONSE_SPEECH);
        if (speech != null && speech.length() > 0) {
            ttsWithCallback(speech, null);
        }
        //动作
        String action = dataMap.get(RESPONSE_ACTION);
        if (action != null && action.length() > 0) {
            if (action.contains("#a_a#")) {
                String[] actionTemp = action.split("#a_a#");
                for (int i = 0; i < actionTemp.length; i++) {
                    int actionCode = -1;
                    try {
                        actionCode = Integer.valueOf(actionTemp[i].trim());
                    } catch (NumberFormatException e) {
                        throw new Exception("umberFormatException e:" + e.getMessage());
                    }
                    motion(actionCode);
                }

            } else {
                int actionCode = -1;
                try {
                    actionCode = Integer.valueOf(action);
                } catch (NumberFormatException e) {
                    throw new Exception("umberFormatException e:" + e.getMessage());
                }
                motion(actionCode);
            }
        }
        //表情
        String expression = dataMap.get(RESPONSE_EXPRESSION);
        if (expression != null && expression.length() > 0) {
            Facial(expression);
        }
        //灯圈
        String earLightCircle = dataMap.get(RESPONSE_EAR_LIGHT_CIRCLE);
        if (earLightCircle != null && earLightCircle.length() > 0) {
            int earLightCircleCode = -1;
            try {
                earLightCircleCode = Integer.valueOf(earLightCircle);
            } catch (NumberFormatException e) {
                throw new Exception("NumberFormatException e:" + e.getMessage());
            }
            Ear(earLightCircleCode,earPriorityCode);
        }

    }

    /**
     * 反馈，包括语音、动作、表情、灯圈的反馈，有哪些反馈由配置信息决定,可以设置回调接口
     *
     * @param dataMap
     * @throws Exception
     */
    public static void responseWithCallback(Map<String, String> dataMap) throws Exception {
        Log.d(TAG, "response: ");
        if (dataMap == null || dataMap.isEmpty()) {
            throw new Exception("dataMap==null ||dataMap.isEmpty()");
        }
        //优先级 Javen 20180208
        String earPriority = dataMap.get(RESPONSE_EAR_PRIORITY);
        int earPriorityCode = 1;
        if (earPriority != null && earPriority.length() > 0) {
            try {
                earPriorityCode = Integer.valueOf(earPriority);
            } catch (NumberFormatException e) { }
        }
        //讲话
        String speech = dataMap.get(RESPONSE_SPEECH);
        if (speech != null && speech.length() > 0) {
            ttsWithCallback(speech, interruptTTSCallback);
        }
        //动作
        String action = dataMap.get(RESPONSE_ACTION);
        if (action != null && action.length() > 0) {
            if (action.contains("#a_a#")) {
                String[] actionTemp = action.split("#a_a#");
                for (int i = 0; i < actionTemp.length; i++) {
                    int actionCode = -1;
                    try {
                        actionCode = Integer.valueOf(actionTemp[i].trim());
                    } catch (NumberFormatException e) {
                        throw new Exception("umberFormatException e:" + e.getMessage());
                    }
                    motion(actionCode, actionSimpleFrameCallback);
                }
            } else {
                int actionCode = -1;
                try {
                    actionCode = Integer.valueOf(action);
                } catch (NumberFormatException e) {
                    throw new Exception("umberFormatException e:" + e.getMessage());
                }
                motion(actionCode, actionSimpleFrameCallback);
            }
        }
        //表情
        String expression = dataMap.get(RESPONSE_EXPRESSION);
        if (expression != null && expression.length() > 0) {
            FacialWithCallback(expression, expressionCallback);
        }
        //灯圈
//        String earLightCircle = dataMap.get(RESPONSE_EAR_LIGHT_CIRCLE);
//        if (earLightCircle != null && earLightCircle.length() > 0) {
//            int earLightCircleCode = -1;
//            try {
//                earLightCircleCode = Integer.valueOf(earLightCircle);
//            } catch (NumberFormatException e) {
//                throw new Exception("NumberFormatException e:" + e.getMessage());
//            }
//            EarWithCallback(earLightCircleCode, 80, 1, earLightCircleCallback);
//        }
        //灯圈
        String earLightCircle = dataMap.get(RESPONSE_EAR_LIGHT_CIRCLE);
        if (earLightCircle != null && earLightCircle.length() > 0) {
            int earLightCircleCode = -1;
            try {
                earLightCircleCode = Integer.valueOf(earLightCircle);
            } catch (NumberFormatException e) {
                throw new Exception("NumberFormatException e:" + e.getMessage());
            }
            Ear(earLightCircleCode,earPriorityCode,earLightCircleCallback);
        }

    }


    /**
     * 初始化配置信息
     *
     * @param id:配置信息的id
     * @return
     * @throws Exception
     */
    public static Map<String, String> getString(int id) throws Exception {
        Log.d(TAG, "getString: ");
        String line = mContent.getResources().getString(id);
        if (line == null) {
            throw new Resources.NotFoundException("the given ID does not exist!");
        }

        Log.d(TAG, "line: " + line);
        Map<String, String> map = new HashMap<>();
        String[] item = line.split("#,#");
        if (item != null) {
            for (int i = 0; i < item.length; i++) {
                Log.d(TAG, "item[" + i + "]: " + item[i]);
                String[] parameter = item[i].trim().split(":");
                if (parameter != null && parameter.length == 2) {
                    map.put(parameter[0].trim(), parameter[1].trim());
                } else {
                    throw new Exception("the given ID has some errors !");
                }
            }
        } else {
            throw new Exception("the given ID has some errors !");
        }
        return map;
    }

    /**
     * 初始化配置信息
     *
     * @param line:配置信息
     * @return
     * @throws Exception
     */
    public static Map<String, String> getString(String line) throws Exception {
        Log.d(TAG, "BFrame getString(): ");
        if (line == null || line.trim().length() < 1) {
            throw new Resources.NotFoundException("the given ID does not exist!");
        }

        Log.d(TAG, "line: " + line);
        Map<String, String> map = new HashMap<>();
        String[] item = line.split("#,#");
        if (item != null) {
            for (int i = 0; i < item.length; i++) {
                Log.d(TAG, "item[" + i + "]: " + item[i]);
                String[] parameter = item[i].trim().split(":");
                if (parameter != null && parameter.length == 2) {
                    map.put(parameter[0].trim(), parameter[1].trim());
                } else {
                    throw new Exception("the given ID has some errors !");
                }
            }
        } else {
            throw new Exception("the given ID has some errors !");
        }
        return map;
    }

    public static String getString(int id, Object... formatAars) {
        String string = mContent.getResources().getString(id, formatAars);
        return string;
    }

    public static InterruptTTSCallback getInterruptTTSCallback() {
        return interruptTTSCallback;
    }

    public static void setInterruptTTSCallback(InterruptTTSCallback interruptTTSCallback) {
        BFrame.interruptTTSCallback = interruptTTSCallback;
    }

    public static SimpleFrameCallback getActionSimpleFrameCallback() {
        return actionSimpleFrameCallback;
    }

    public static void setActionSimpleFrameCallback(SimpleFrameCallback actionSimpleFrameCallback) {
        BFrame.actionSimpleFrameCallback = actionSimpleFrameCallback;
    }

    public static ExpressionCallback getExpressionCallback() {
        return expressionCallback;
    }

    public static void setExpressionCallback(ExpressionCallback expressionCallback) {
        BFrame.expressionCallback = expressionCallback;
    }

    public static SimpleFrameCallback getEarLightCircleCallback() {
        return earLightCircleCallback;
    }

    public static void setEarLightCircleCallback(SimpleFrameCallback earLightCircleCallback) {
        BFrame.earLightCircleCallback = earLightCircleCallback;
    }


}
