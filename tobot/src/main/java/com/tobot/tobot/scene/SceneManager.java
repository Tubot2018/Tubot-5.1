package com.tobot.tobot.scene;

import com.tobot.tobot.presenter.BRealize.BFrame;
import com.tobot.tobot.utils.TobotUtils;
import com.turing123.robotframe.multimodal.action.EarActionCode;

/**
 * Created by mohuaiyuan on 2018/1/26.
 */

public class SceneManager {

    public static final Integer STATUS_PLAYING = 1;
    public static final Integer STATUS_OTHER = -1;

    public static String SCENE = "STOP";
    public static String SCENE_STATE;


    /**
     * 播放的状态
     */
    private static Integer playStatus;

    public static Integer getPlayStatus() {
        return playStatus;
    }

    public static void setPlayStatus(Integer playStatus) {
        SceneManager.playStatus = playStatus;
    }



    public static void setScene(String scene){
        SCENE = scene;
    }

    public static String getScene(){
        return SCENE;
    }

    public static void setSceneState(String sceneState){
        SCENE_STATE = sceneState;
    }

    public static String getSceneState(){
        return SCENE_STATE;
    }

    public static void setExit(){
        BFrame.Ear(EarActionCode.EAR_MOTIONCODE_3,5);
        BFrame.motion(1);
        TobotUtils.resetZero();
    }


}
