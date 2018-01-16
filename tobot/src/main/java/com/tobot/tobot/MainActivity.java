package com.tobot.tobot;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.tobot.tobot.Listener.MainScenarioCallback;
import com.tobot.tobot.Listener.SimpleFrameCallback;
import com.tobot.tobot.base.BaseActivity;
import com.tobot.tobot.base.Constants;
import com.tobot.tobot.base.MyTouchResponse;
import com.tobot.tobot.base.UpdateAction;
import com.tobot.tobot.base.UpdateAnswer;
import com.tobot.tobot.control.Demand;
import com.tobot.tobot.control.SaveAction;
import com.tobot.tobot.db.bean.UserDBManager;
import com.tobot.tobot.db.model.User;
import com.tobot.tobot.presenter.BRealize.BConnect;
import com.tobot.tobot.presenter.BRealize.BFrame;
import com.tobot.tobot.presenter.BRealize.BaseTTSCallback;
import com.tobot.tobot.presenter.BRealize.DormantManager;
import com.tobot.tobot.presenter.BRealize.DormantUtils;
import com.tobot.tobot.presenter.BRealize.InterruptTTSCallback;
import com.tobot.tobot.presenter.BRealize.LieDownAndSleep;
import com.tobot.tobot.presenter.BRealize.SitDownAndSleep;
import com.tobot.tobot.presenter.BRealize.SitDownAndSleepTimeTask;
import com.tobot.tobot.presenter.BRealize.StraightToSleep;
import com.tobot.tobot.presenter.ICommon.ISceneV;

import com.tobot.tobot.presenter.IPort.AwakenBehavior;
import com.tobot.tobot.utils.AppTools;
import com.tobot.tobot.utils.AudioUtils;
import com.tobot.tobot.utils.SHA1;
import com.tobot.tobot.utils.Transform;
import com.tobot.tobot.utils.okhttpblock.OkHttpUtils;
import com.tobot.tobot.utils.okhttpblock.callback.StringCallback;
import com.tobot.tobot.utils.socketblock.Joint;
import com.tobot.tobot.utils.TobotUtils;
import com.tobot.tobot.base.TouchResponse;
import com.tobot.tobot.utils.bluetoothblock.Ble;
import com.tobot.tobot.utils.socketblock.Const;
import com.tobot.tobot.utils.socketblock.NetManager;
import com.tobot.tobot.utils.socketblock.SocketThreadManager;

import com.turing123.robotframe.function.asr.IASRFunction;
import com.turing123.robotframe.function.cloud.Cloud;
import com.turing123.robotframe.function.cloud.IAutoCloudCallback;
import com.turing123.robotframe.function.keyin.KeyInputEvent;
import com.turing123.robotframe.multimodal.Behaviors;
import com.turing123.robotframe.multimodal.action.BodyActionCode;
import com.turing123.robotframe.multimodal.action.EarActionCode;
import com.turing123.robotframe.multimodal.expression.EmojNames;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.OnClick;
import okhttp3.Call;

import static com.turing123.robotframe.function.keyin.KeyInputEvent.KEYCODE_HEAD;
import static com.turing123.robotframe.multimodal.action.Action.PRMTYPE_EXECUTION_TIMES;
import static java.lang.Thread.sleep;

public class MainActivity extends BaseActivity implements ISceneV {
	
    private static final String TAG = "MainActivity";
    @BindView(R.id.ed_account)
    EditText account;
    @BindView(R.id.ed_password)
    EditText password;
    @BindView(R.id.btn_conn)
    Button btn_conn;
    @BindView(R.id.tvConnResult)
    public TextView tvConnResult;
    @BindView(R.id.tvASR)
    TextView tvASR;
    @BindView(R.id.im_picture)
    ImageView im_picture;
    @BindView(R.id.etphone)
    EditText editText;


    private Cloud mCloud;
    private BConnect mBConnect;
    private Ble mBle;
    private Timer dormantTimer = new Timer(true);//等待休眠时间
    private Timer activeTimer = new Timer(true);//主动交互时间
    private Timer awakenTimer = new Timer(true);//休眠时间
    private Timer detectionTime = new Timer(true);//异常断网检测时间
//    private Timer TimeMachine = new Timer(true);//异常断网语音播报时间



    /**
     * 自动休眠
     */
    private boolean isDormant;//休眠
    /**
     * 是否处于可唤醒状态，接下来就可以进行唤醒了。
     * mohuaiyuan 20171226 添加注释。
     */
    private boolean isWakeup;//唤醒
    private boolean isNotWakeup = true;//禁止唤醒
    private boolean isInterrupt;//打断
    private boolean isSquagging = true;//自锁
//    private boolean anewConnect;//进入重新联网
    private boolean isInitiativeOff;//判断是否主动断网
    public static boolean ACTIVATESIGN;//框架启动标志
    private Bundle packet;
    private long exitTime; // 短时间内是否连续点击返回键
    private boolean whence;
    private boolean isOFF_HINT;//休眠期间断网不提示

    public static Context mContext;
    private BroadcastReceiver mReceiver;
    private SocketThreadManager manager;
    private BFrame mBFrame;

    private List<String> expressionList;


    @Override
    public int getGlobalLayout() {
            return R.layout.activity_main;
    }

    @Override
    public void initial(Bundle savedInstanceState) {
        Log("initial");
        mContext = this;
        NetManager.instance().init(this);
        manager = SocketThreadManager.sharedInstance();

        //初始化AP联网
        onSetAP();

//        if (!AppTools.netWorkAvailable(MainActivity.this)) {
//            //启动框架
                mBFrame = BFrame.instance(MainActivity.this);
                mBFrame.setConnectState(mBConnect);
//        }
//        regBroadcast();

        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        Log("设备id"+tm.getDeviceId());

    }

    //联网
    private void onSetAP(){ mBConnect = new BConnect(MainActivity.this); }

    //一些功能实现
   private void manifestation(){
//        if (TobotUtils.isEmployFack()){
//            //首次使用提示语,动作等
//        }
       if (AppTools.netWorkAvailable(this) && !isInitiativeOff && !whence) {//自动联网成功
           mCloud = new Cloud(this, new MainScenarioCallback());
           //mohuaiyuan 20171221 原来的代码
//           //mBFrame.TTS(getResources().getString(R.string.Connection_Succeed));
//           TobotUtils.getIPAddress(this);
//           mBFrame.Facial(EmojNames.HAPPY);
//           mBFrame.outAction(BodyActionCode.ACTION_17);
//           mBFrame.Ear(EarActionCode.EAR_MOTIONCODE_1);

           //mohuaiyuan 20171221 新的代码 20171221
           Map<String,String> map=null;
           try {
               map=BFrame.getString(R.string.Connection_Succeed);
           } catch (Exception e) {
               e.printStackTrace();
           }

           BaseTTSCallback baseTTSCallback=new BaseTTSCallback(){
               @Override
               public void onCompleted() {
                   TobotUtils.getIPAddress(mContext);
               }
           };
           BFrame.setInterruptTTSCallback(new InterruptTTSCallback(this,baseTTSCallback));

           try {
               BFrame.responseWithCallback(map);
           } catch (Exception e) {
               e.printStackTrace();
           }
       }
   }

   //蓝牙
   private void onBle(){ mBle = new Ble(this); }


//----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------


    Handler mainHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.NOTIFICATION_MSG:
                    packet = (Bundle)msg.obj;
                    try{
                        switch (packet.getString("action")) {
                            case "tts.status":
                                if (packet.getInt("arg1",0) == 0){
//                                    if (TobotUtils.isNotEmpty(mInterrupted)){
//                                        mInterrupted.Voice(true);//可打断
//                                    }
//                                    BFrame.isInterrupt = true;//可打断//20171226注释一直停留在打断
                                    mBFrame.Ear(EarActionCode.EAR_MOTIONCODE_2);//发声效果
                                    activeTimer.cancel();
                                    activeTimer = new Timer();
                                }
                                break;
                            case "connection.status":
                                if (packet.getInt("arg1") == 1 && isInitiativeOff) {//非主动
                                    Log("网络状态监测:断网了");
                                    detectionTime.schedule(new DetectionTimerTask(),5000,10000);//10秒钟
                                }
                                break;
                            case "asr.status":
//                                if (TobotUtils.isNotEmpty(mInterrupted)) {
//                                    mInterrupted.Voice(false);//不可打断
//                                }
//                                BFrame.prevent = false;
//                                BFrame.isInterrupt = false;//不可打断//20171229考虑到全局tts已自主控制,asr不在暂停
                                String asrContent = packet.getString("arg2");
                                mBFrame.Ear(EarActionCode.EAR_MOTIONCODE_3);//录音效果
                                if(packet.getInt("arg1") == 4){
                                    if(asrContent.contains("没有检查到网络")) {
                                        if (!hintConnect) {
                                            Log("ASR没有检查到网络");
                                            detectionTime.schedule(new DetectionTimerTask(),10*1000,10*1000);//10秒钟
                                        }
                                    }
                                }else if(packet.getInt("arg1") == 3 && asrContent != null){// packet.getString("arg2") != null  //收到对话
                                    if (!isSquagging){
                                        //等待睡眠
                                        dormantTimer.cancel();
                                        dormantTimer = new Timer();
                                        //等待主动交互
                                        activeTimer.cancel();
                                        activeTimer = new Timer();


                                    }
                                    if(hintConnect){//断网收到语音提示-->离线语音
                                        //mohuaiyuan 20171220 原来的代码
//                                        mBFrame.TTS(getResources().getString(R.string.Connection_Break_Hint));
                                        //mohuaiyuan 20171220 新的代码
                                        try {
                                            mBFrame.response(R.string.Connection_Break_Hint);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    isSquagging = true;
                                    Log("结束倒计时");
                                }else if(packet.getInt("arg1") != 3 && mBFrame.replace ? asrContent == null : asrContent != null) {//无对话
                                    if (isSquagging){//自锁
                                        Log("开始倒计时");
                                        isSquagging = false;
                                        dormantTimer.schedule(new DormantTimerTask(),3*60*1000);//N分钟
//                                        activeTimer.schedule(new ActiveTimerTask(),20000,1000);//主动交互请求
                                    }
                                }
                                break;
								
                            case "robot.state":
                                if(packet.getInt("arg1") == 5){
                                    BFrame.robotState = false;
                                }else if(packet.getInt("arg1") == 4){

                                }else if(packet.getInt("arg1") == 3){
                                    isOFF_HINT = false;
                                    BFrame.robotState = true;
                                    //mohuaiyuan 20171226 新的代码 20171226
                                    //TODO 唤醒的地方
                                    Log.d("IDormant", "摸头唤醒 之后 回调: ");
                                    try {
                                        dealAwakenBehavior();
                                    } catch (Exception e) {
                                        Log.e("IDormant", "摸头唤醒 之后 回调，出现Exception e : "+e.getMessage());
                                        e.printStackTrace();
                                    }

                                }
                                break;
                        }
                    }catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                    break;
                case Constants.AWAIT_DORMANT ://自动休眠
                    if (isDormant && !TobotUtils.isInScenario(mScenario)) {
                        isDormant = false;
                        isWakeup = true;
                        isOFF_HINT = true;

                        //mohuaiyuan 20171226 原来的代码
//                        mBFrame.outAction(BodyActionCode.ACTION_8);
//                        mBFrame.FallAsleep();
                        //mohuaiyuan 20171226 新的代码 20171226
                        Log.d("IDormant", "自动休眠: ");
                        StraightToSleep straightToSleep=new StraightToSleep();
                        straightToSleep.dormant();
                        DormantManager.setType(DormantManager.DORMANT_TYPE_STRAIGHT_TO_SLEEP);

                        //站着休眠N分钟不唤醒 ,触发 坐下休眠
//                        Message message=new Message();
//                        message.what=Constants.SIT_DOWN_AND_SLEEP_DORMANT;
//                        mainHandler.sendMessage(message);

                    }
                    break;

                //mohuaiyuan 20171226 新的代码 20171226
                case Constants.SIT_DOWN_AND_SLEEP_DORMANT:
                    Log.d("IDormant", "mainHandler: Constants.SIT_DOWN_AND_SLEEP_DORMANT:");
                    //站着休眠N分钟不唤醒 ,触发 坐下休眠
                    DormantManager dormantManager=new DormantManager();
                    dormantManager.sitDownAndSleepTrigger();


                    break;

                case Constants.AWAIT_AWAKEN ://等待唤醒
                    mBFrame.Wakeup();
                    isNotWakeup = true;
                    mBFrame.TTS(getResources().getString(R.string.Mend_Error));
                    break;
                case Constants.AWAIT_ACTIVE ://主动交互
                    mCloud.requestActiveTalk(new IAutoCloudCallback() {
                        @Override
                        public void onResult(Behaviors behaviors) {
                            Log("主动交互请求成功:"+behaviors);
                        }

                        @Override
                        public void onError(String s) {
                            Log("主动交互请求失败:"+s);
                        }
                    });
                    break;

                default:
                    super.handleMessage(msg);
            }
        }
    };

    /**
     * 处理 唤醒的逻辑
     */
    private void dealAwakenBehavior() {
        Log.d(TAG, "MainActivity dealAwakenBehavior: ");
        DormantUtils dormantUtils=new DormantUtils();
        dormantUtils.dealAwakenBehavior();

    }

//------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------


    boolean isFeelHead = true;//摸头启动ap联网

    @Override
    public void isKeyDown(int keyCode, KeyEvent event) {
        Log("触摸事件===>keyCode:"+keyCode+"KeyEvent:"+event);
        if (ACTIVATESIGN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_BACK:
                    if (isWakeup && isNotWakeup) {
                        Log("触摸--唤醒");
                        isDormant = true;
                        isWakeup = false;
                        mBFrame.Wakeup();
                    } else if (BFrame.isInterrupt || BFrame.prevent || TobotUtils.isInScenario(mScenario)) {
                        Log("触摸--打断:" + mScenario + "  BFrame.isInterrupt:" + BFrame.isInterrupt + "  BFrame.prevent:" + BFrame.prevent);
//                        switch (mScenario) {
//                            case "os.sys.song":
////                                SongScenario.instance(this).Backspacing();
//                            break;
//                            case "os.sys.story":
//
//                                break;
//                            case "os.sys.dance":
//
//                                break;
//                        }
                        KeyInputEvent mKeyInputEvent = new KeyInputEvent(keyCode, KEYCODE_HEAD);
                        mBFrame.InterruptTouch();
//                        if (TobotUtils.isNotEmpty(mInterrupted)){
//                            mInterrupted.Voice(false);//不可打断
//                        }
//                        BFrame.isInterrupt = false;//不可打断
//                        BFrame.prevent = false;
                    } else {
                        Log("触摸--调侃聊天");
                        Log.d("helloworld", "触摸--调侃聊天: ");
                        try {
                            long l = (System.currentTimeMillis() - exitTime);
                            if (l < 4000) {//连续点击
                                Log("触摸--连续点击");
                                Log.d("helloworld", "触摸--连续点击: ");

                               // onBle();
                                //mohuaiyuan 20180106 原来的代码
//                                TobotUtils.getIPAddress(MainActivity.this);//播报ip
								
                                //mohuaiyuan 20171228 新的代码 新增的代码
                                exitTime = 0;

                                //mohuaiyuan 20171220 新的代码 新增的代码
                                MyTouchResponse myTouchResponse=new MyTouchResponse(mContext);
                                BFrame.response(myTouchResponse.doubleTouchHeadResponse());

                                /*Map<String,String> map=null;
                                try {
                                    map=BFrame.getString(myTouchResponse.doubleTouchHeadResponse());
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                BaseTTSCallback baseTTSCallback=new BaseTTSCallback(){
                                    @Override
                                    public void onCompleted() {
                                        TobotUtils.getIPAddress(mContext);//播报ip
                                    }
                                };
                                BFrame.setInterruptTTSCallback(new InterruptTTSCallback(this,baseTTSCallback));

                                try {
                                    BFrame.responseWithCallback(map);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }*/


                                //mohuaiyuan 20180104 测试 获取音量
                                //AudioUtils audioUtils=new AudioUtils(mContext);
                                //int currentVolume=audioUtils.getCurrentVolume();
                                //int maxVolume=audioUtils.getMaxVolume();
                                //Log.d("IDormant", "currentVolume: "+currentVolume);
                                //Log.d("IDormant", "maxVolume: "+maxVolume);
                                //int code = audioUtils.adjustLowerMusicVolume();
                                //Log.d("IDormant", "code: "+code);


                            } else {
                                Log("触摸--单击");
                                Log.d("helloworld", "触摸--单击: ");
                                exitTime = System.currentTimeMillis();

                                //mohuaiyuan 20171220 原来的代码
//                                mBFrame.TTS(TouchResponse.getResponse(this));
                                //mohuaiyuan 20171220 新的代码 20171220
                                MyTouchResponse myTouchResponse=new MyTouchResponse(mContext);
                                mBFrame.response(myTouchResponse.onceTouchHeadResponse());

                                Demand.instance(this).stopDemand();//停止点播

                                //mohuaiyuan 20180104 测试 获取音量
                               // AudioUtils audioUtils=new AudioUtils(mContext);
                               // int currentVolume=audioUtils.getCurrentVolume();
                               // int maxVolume=audioUtils.getMaxVolume();
                               // Log.d("IDormant", "currentVolume: "+currentVolume);
                               // Log.d("IDormant", "maxVolume: "+maxVolume);
                               // int code = audioUtils.adjustRaiseMusicVolume();
                               // Log.d("IDormant", "code: "+code);
                               

                               //mohuaiyuan  20171225 测试 表情 序号
    /*                            if (expressionList==null){
                                    expressionList=new ArrayList<>();
                                    String []expressionArray=mContext.getResources().getStringArray(R.array.expressionArray);
                                    for (int i=0;i<expressionArray.length;i++){
                                        expressionList.add(expressionArray[i]);
                                    }
                                }
                                Random random=new Random();
                                int index=random.nextInt(expressionList.size());
                                Log.d(TAG, "index: "+index);
                                String currentExpression=expressionList.get(index);
                                Log.d(TAG, "expression: "+currentExpression);
                                BFrame.TTS("当前的表情是 ："+currentExpression);
                                BFrame.Facial(currentExpression);

                                expressionList.remove(index);*/
								
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case KeyEvent.FLAG_LONG_PRESS:
                    Log("触摸--进入长按事件");
                    if (isFeelHead) {
                        isInitiativeOff = true;//主动断网
                        mBConnect.shunt();//启动ap联网

                        //mohuaiyuan 20171220 原来的代码
//                        mBFrame.TTS(getResources().getString(R.string.Connection_Start));
//                        mBFrame.Facial(EmojNames.EXPECT);
//                        mBFrame.outAction(BodyActionCode.ACTION_95);
                        //mohuaiyuan 20171220 新的代码 20171220
                        try {
                            BFrame.response(R.string.Connection_Start);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }


                        isFeelHead = false;
                    } else {
                        isInitiativeOff = false;//关掉主动断网
                        mBConnect.shut();//关闭ap联网

                        //mohuaiyuan 20180103 原来的代码
//                        mBFrame.TTS(getResources().getString(R.string.Connection_Close));
//                        mBFrame.Facial(EmojNames.DEPRESSED);
//                        mBFrame.outAction(BodyActionCode.ACTION_STAND_STILL);
                        //mohuaiyuan 20180103 新的代码 20180103
                        try {
                            BFrame.response(R.string.Connection_Close);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        isFeelHead = true;
                    }
                    break;

                default:
                    break;
            }
        }else{
            mBConnect.shuntVoice();
        }

    }


//---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------


    @Override
    public void getResult(Object result) {
        mainHandler.sendMessage((Message)result);
    }

    @Override
    public void getInitiativeOff(boolean initiative) {
        this.isInitiativeOff = initiative;
    }

    @Override
    public void getFeelHead(boolean feel) {
        isFeelHead = feel;
    }

	//mohuaiyuan  20180105 所有情况下的网络断了
    @Override
    public void getConnectFailure(boolean failure) {
        if (failure){
            mBFrame.Facial(EmojNames.DEPRESSED);
            mBFrame.motion(BodyActionCode.ACTION_45);
        }
    }

    @Override
    public void getDormant(boolean dormant) {
        Log("休眠:"+dormant);
        isDormant = dormant;//自动休眠
        isWakeup = true;//允许唤醒
        isOFF_HINT = true;
    }

    private String mScenario = "stop";
    @Override
    public void getScenario(String scenario) {
        mScenario = scenario;
        mBFrame.getBArmtouch().getScenario(scenario);
        //理论上不需要,因为asr检测很频繁(断网后asr会切换成离线)//5.1更换asr后有百年
        if (!TobotUtils.isInScenario(mScenario) && isDormant){
            //等待睡眠
            dormantTimer.cancel();
            dormantTimer = new Timer();
            dormantTimer.schedule(new DormantTimerTask(),3000000);//等待5分钟进入休眠
        }
    }

    @Override
    public void getSongScenario(Object song) {
        mBFrame.getBArmtouch().getSongScenario(song);
    }

    @Override
    public void FrameLoadSuccess(boolean whence) {
        this.whence = whence;
        isDormant = true;
        ACTIVATESIGN = true;
        manifestation();
    }

    @Override
    public void FrameLoadFailure() {

    }


//-------------------------------------------------------------------------------------------------------------------------------------------------------------------


    @OnClick(R.id.btn_conn)
    public void send(){
        //启动ap联网
        mBConnect.shunt();
    }

    @OnClick(R.id.btn_close)
    public void close(){
        //关闭ap联网
        mBConnect.shut();
    }

    @OnClick(R.id.btn_shutdown1)
    public void shutdown1(){
       //下发动作
//        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        Log("动作下发时间..1...:"+dateFormat.format(new Date()));
//        motor.doAction(Action.buildEarAction(EarActionCode.EAR_MOTIONCODE_0,PRMTYPE_EXECUTION_TIMES,1),new SimpleFrameCallback());

        //发送注册
//        manager.sendMsg(Transform.HexString2Bytes(Joint.setRegister()));
//        manager.demandDance();

//        bindRobot();

//        StartOtherApplications();
//        try {
//               Log(UserDBManager.getManager().queryById("tobot").getMotion()+"");
//              }catch (NullPointerException e){
//                e.printStackTrace();
//        }

//        BFrame.getmBLocal().carryThrough("");

        mBFrame.FallAsleep();

    }

    @OnClick(R.id.btn_shutdown2)
    public void shutdown2() {
//        try {
//            String answer = AnswerDBManager.getManager().queryByElement(editText.getText().toString().replaceAll("[\\p{P}‘’“”]", "")).getAnswer();
//            Log("问答数据保存:" + answer != null ? answer : "answer 无数据");
//        } catch (Exception e) {
//
//        }cd
        try{
            mInterrupted.Music(editText.getText().toString());
        }catch (NullPointerException e){

        }
//        User memory = new User();
//        memory.setMotion(15);
//        UserDBManager.getManager().insert(memory);
    }


//-------------------------------------------------------------------------------------------------------------------------------------------------------------------


    private boolean hintConnect;

    private class DetectionTimerTask extends TimerTask {
        public void run() {
            Log("离线五秒检测到断网:");
            if (!AppTools.netWorkAvailable(MainActivity.this) && !hintConnect) {
                Log("离线五秒检测到断网进入语音提示:");
                mBFrame.choiceFunctionProcessor(IASRFunction.DEFAULT_ASR_PROCESSOR_OFFLINE);//离线asr
//                anewConnect = true;
                hintConnect = true;

                //mohuaiyuan 20180103 原来的代码
//                mBFrame.Facial(EmojNames.DEPRESSED);
//                mBFrame.outAction(BodyActionCode.ACTION_80);
//                mBFrame.Ear(EarActionCode.EAR_MOTIONCODE_6,10);
                detectionTime.schedule(new TimeMachineTimerTask(),0,30000);
//                TimeMachine.schedule(new TimeMachineTimerTask(),0,30000);
            }else if (AppTools.netWorkAvailable(MainActivity.this)){
                Log("检测到异常断网已重新连接:");
                mBFrame.resetFunction();//重置asr--需要注意替换ASR后重置的asr是哪个
                hintConnect = false;
                mBConnect.onAgain();//检测是否需要绑定
                SocketThreadManager.sharedInstance().sendMsg(Transform.HexString2Bytes(Joint.setRegister()));//发起tcp注册
				
                //mohuaiyuan 20171220 原来的代码
//                mBFrame.Facial(EmojNames.TIRED);
//                mBFrame.outAction(BodyActionCode.ACTION_45);
//                mBFrame.TTS(getResources().getString(R.string.Connection_Recover));
//                detectionTime.cancel();
//                detectionTime = new Timer();
//                TobotUtils.getIPAddress(MainActivity.this);

                //mohuaiyuan 20171221 新的代码 20171221
                Map<String,String> map=null;
                try {
                    map=BFrame.getString(R.string.Connection_Recover);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                BaseTTSCallback baseTTSCallback=new BaseTTSCallback(){
                    @Override
                    public void onCompleted() {
                        TobotUtils.getIPAddress(mContext);
                    }
                };
                BFrame.setInterruptTTSCallback(new InterruptTTSCallback(MainActivity.this,baseTTSCallback));

                try {
                    BFrame.responseWithCallback(map);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                detectionTime.cancel();
                detectionTime = new Timer();

            }
        }
    }

    private class TimeMachineTimerTask extends TimerTask {
        public void run() {
			
            if (hintConnect && !isOFF_HINT){
                //mohuaiyuan 20171220 原来的代码
//                mBFrame.TTS(getResources().getString(R.string.Connection_Break_Hint));
                //mohuaiyuan 20171220 新的代码 20171220
                try {
                    //mohuaiyuan 20180103 原来的代码
//                    mBFrame.response(R.string.Connection_Break_Hint);
                    //mohuaiyuan 20180103 新的代码 20180103
                    mBFrame.response(R.string.the_network_is_broken);
                } catch (Exception e) {
                    Log.e(TAG, " 网络断了 ，重复性提示 出现 Exception e : "+e.getMessage());
                    e.printStackTrace();
                }
            }

        }
    }

    private class DormantTimerTask extends TimerTask {
        public void run() {
            Message message = new Message();
            message.what = Constants.AWAIT_DORMANT;
            mainHandler.sendMessage(message);
        }
    }

	
    private class AwakenTimerTask extends TimerTask {
        public void run() {
            Message message = new Message();
            message.what = Constants.AWAIT_AWAKEN;
            mainHandler.sendMessage(message);
        }
    }

    private class ActiveTimerTask extends TimerTask {
        public void run() {
            Message message = new Message();
            message.what = Constants.AWAIT_ACTIVE;
            mainHandler.sendMessage(message);
        }
    }


//-------------------------------------------------------------------------------------------------------------------------------------------------------------------


    @Override
    protected void onStart() {
        super.onStart();
        eliminate();
        new Thread(new Runnable() {
            @Override
            public void run() {
                StartOtherApplications();
            }
        }).start();
    }

    private void eliminate() {
        try {
			
                String time1 = UserDBManager.getManager().getCurrentUser().getRequestTime();
                String time2 = TobotUtils.getCurrentlyDate();
                long date = TobotUtils.DateMinusTime(time1, time2);
                Log("date:" + date);
                if (date > 1) {
                    UpdateAction updateAction = new UpdateAction(mContext);
                    SaveAction saveAction = new SaveAction(mContext,updateAction);
                    saveAction.setDanceResource();
                    saveAction.setActionResource();
                    updateAction.getList();
                }
        } catch (NullPointerException e) {
            User user = new User();
            user.setRequestTime(TobotUtils.getCurrentlyDate());
            UserDBManager.getManager().insertOrUpdate(user);
        } catch (IllegalArgumentException e){
            new UpdateAction(MainActivity.this);
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (!ACTIVATESIGN) {
            mBFrame.onInitiate(true);
        }
    }


//        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
//        Log(tm.getDeviceId());
//        Log("系统版本号"+android.os.Build.VERSION.RELEASE);


//asr----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------


    public VoiceInterrupted mInterrupted;

    public VoiceInterrupted ConductInterrupt() {
        return mInterrupted;
    }

    public void setConductInterrupt(VoiceInterrupted interrupted) {
        this.mInterrupted = interrupted;
    }

    public interface VoiceInterrupted{
		
//        void Voice(Object interrupt);
        void Music(String music);
    };


//TEST------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------


    private  void StartOtherApplications(){
        Intent intent = getPackageManager().getLaunchIntentForPackage("com.robot.bridge");
        if (intent != null) {
//            intent.setAction("com.adb.start");
//            intent.addCategory("android.intent.category.DEFAULT");
            Log.i("Javen","已启动应用");
            startActivity(intent);
        } else {
            // 没有安装要跳转的app应用，提醒一下
            Log.i("Javen","没有要启动的应用");
        }

//        Intent intent = new Intent(Intent.ACTION_MAIN);
//        intent.addCategory(Intent.CATEGORY_DEFAULT);
//        ComponentName cn = new ComponentName("com.robot.bridge", "BridgeService");
//        intent.setComponent(cn);
//        startService(intent);
    }



    public void regBroadcast() {
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final String value = intent.getStringExtra("response");
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        tvASR.setText(value);
                        Log("广播信息:" + value);
                    }
                });
            }
        };
        IntentFilter intentToReceiveFilter = new IntentFilter();
        intentToReceiveFilter.addAction(Const.BC);
        registerReceiver(mReceiver, intentToReceiveFilter);
    }



    int bind = 0;
    private void bindRobot() {
        String uuid = Transform.getGuid();

        OkHttpUtils.get()
                .url(Constants.ROBOT_BOUND + uuid + "/" + SHA1.gen(Constants.identifying + uuid)
                        + "/" + TobotUtils.getDeviceId(Constants.DeviceId, Constants.Path)
                        + "/" + TobotUtils.getDeviceId(Constants.Ble_Name, Constants.Path)
                        + "/" + editText.getText().toString())
                .addParams("nonce", uuid)//伪随机数
                .addParams("sign", SHA1.gen(Constants.identifying + uuid))//签名
                .addParams("robotId", TobotUtils.getDeviceId(Constants.DeviceId, Constants.Path))//机器人设备ID
                .addParams("bluetooth", TobotUtils.getDeviceId(Constants.Ble_Name, Constants.Path))//蓝牙名称
                .addParams("mobile", editText.getText().toString())//手机号
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        bind++;
                        if (bind < 3) {
                            bindRobot();
                        } else {
                            bind = 0;
                        }
                        Log.i("Javen", "绑定失败===>call:" + call + "bind:" + bind);
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        Log.i("Javen", "绑定===>response:" + response + "id:" + id);
                    }
                });
    }




}

 /* * ━━━━━━感觉萌萌哒━━━━━━
 * 　　　　　　　　┏┓　　　┏┓
 * 　　　　　　　┏┛┻━━━┛┻┓
 * 　　　　　　　┃　　　　　　　┃ 　
 * 　　　　　　　┃　　　━　　　┃
 * 　　　　　　　┃　＞　　　＜　┃
 * 　　　　　　　┃　　　　　　　┃
 * 　　　　　　　┃...　⌒　...　┃
 * 　　　　　　　┃　　　　　　　┃
 * 　　　　　　　┗━┓　　　┏━┛
 * 　　　　　　　　　┃　　　┃　Code is far away from bug with the animal protecting　　　　　　　　　　
 * 　　　　　　　　　┃　　　┃       神兽保佑,代码无bug
 * 　　　　　　　　　┃　　　┃　　　　　　　　　　　
 * 　　　　　　　　　┃　　　┃ 　　　　　　
 * 　　　　　　　　　┃　　　┃
 * 　　　　　　　　　┃　　　┃　　　　　　　　　　　
 * 　　　　　　　　　┃　　　┗━━━┓
 * 　　　　　　　　　┃　　　　　　　┣┓
 * 　　　　　　　　　┃　　　　　　　┏┛
 * 　　　　　　　　　┗┓┓┏━┳┓┏┛
 * 　　　　　　　　　　┃┫┫　┃┫┫
 * 　　　　　　　　　　┗┻┛　┗┻┛
 *
 */

