package com.tobot.tobot.scene;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.tobot.tobot.Listener.SimpleFrameCallback;
import com.tobot.tobot.utils.TobotUtils;
import com.turing123.robotframe.function.motor.Motor;
import com.turing123.robotframe.function.tts.TTS;
import com.turing123.robotframe.multimodal.Behavior;
import com.turing123.robotframe.multimodal.action.Action;
import com.turing123.robotframe.scenario.IScenario;
import com.turing123.robotframe.scenario.ScenarioRuntimeConfig;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.turing123.robotframe.multimodal.action.Action.PRMTYPE_EXECUTION_TIMES;

/**
 * Created by Jvaen on 2017/8/8.
 */

public class CustomActionScenario implements IScenario {
    private String APPKEY;
    private Context mContext;
    private Motor motor;
    private TTS tts;


    public CustomActionScenario(Context context,String appkey){
        this.mContext = context;
        this.APPKEY = appkey;
        tts = new TTS(mContext, new BaseScene(mContext,"os.sys.chat"));
        motor = new Motor(mContext, new BaseScene(mContext,"os.sys.chat"));
    }

    @Override
    public void onScenarioLoad() {
//        Log.i("Javen","加载场景");
    }

    //removerscenario()
    @Override
    public void onScenarioUnload() {
//        Log.i("Javen","卸载场景");
    }

    @Override
    public boolean onStart() {
//        Log.i("Javen","开始");
        return true;
    }

    @Override
    public boolean onExit() {
//        Log.i("Javen","退出");
        return true;
    }

    @Override
    public boolean onTransmitData(Behavior behavior) {
        tts.speak("好的");
        if (TobotUtils.isNotEmpty(behavior)){
            Pattern p = Pattern.compile("[^0-9]");
            Matcher m = p.matcher(behavior.getIntent().getIntentName());
            Log.i("Javen","运动代号..."+ m.replaceAll("").trim());
            motor.doAction(Action.buildBodyAction(Integer.parseInt(m.replaceAll("").trim()),PRMTYPE_EXECUTION_TIMES,1),new SimpleFrameCallback());
        }
//        Log.i("Javen","behavior"+behavior.toString());
        return true;
    }

    @Override
    public boolean onUserInterrupted(int i, Bundle bundle) {
        return true;
    }

    @Override
    public String getScenarioAppKey() {
        return APPKEY;
    }

    @Override
    public ScenarioRuntimeConfig configScenarioRuntime(ScenarioRuntimeConfig scenarioRuntimeConfig) {
        scenarioRuntimeConfig.allowDefaultChat = true;
        //为场景添加打断语，asr 识别到打断语时将产生打断事件，回调到场景的onUserInterrupted() 方法。
        return scenarioRuntimeConfig;
    }
}
