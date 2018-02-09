package com.tobot.tobot.scene;

/**
 * Created by mohuaiyuan on 2018/1/26.
 */

public class SceneManager {

    public static final Integer STATUS_PLAYING=1;
    public static final Integer STATUS_OTHER=-1;

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
}
