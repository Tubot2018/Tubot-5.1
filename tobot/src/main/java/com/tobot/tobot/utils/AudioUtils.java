package com.tobot.tobot.utils;

import android.content.Context;
import android.media.AudioManager;

/**
 * Created by mohuaiyuan  on 2017/11/29.
 */

public class AudioUtils {
    private static final String TAG = "AudioUtils";

    public static final int MUSIC_MIN_VOLUME_LEVEL=1;
    public static final int MUSIC_MAX_VOLUME_LEVEL=15;
    public static final int VOLUME_COPIES =15;

    public static final int SET_MUSIC_VOLUME_SUCCESS=1;
    public static final int SET_MUSIC_VOLUME_FAILED=-1;
    public static final int CURRENT_LEVEL_IS_MIN_VOLUME_LEVEL=-2;
    public static final int CURRENT_LEVEL_IS_MAX_VOLUME_LEVEL=-3;

    private AudioManager manager;

    private Context mContext;

    private int errorCode;

    private static MaxVolumeListener maxVolumeListener;
    private static MinVolumeListener minVolumeListener;
    private static VolumeLegalListener volumeLegalListener;
    private static VolumeCopyListener volumeCopyListener;

    public AudioUtils(Context context){
        this.mContext=context;
        getManager();
    }

    public AudioManager getManager() {
        if (manager==null){
            manager= (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        }
        return manager;
    }

    public int getCurrentVolume(){
        int volumeLevel=-1;
        volumeLevel=manager.getStreamVolume(AudioManager.STREAM_MUSIC);
        return volumeLevel;
    }

    public int getMaxVolume(){

        if (maxVolumeListener!=null){
            return maxVolumeListener.getMaxVolume();
        }else {
            int maxVolumeLevel=-1;
            maxVolumeLevel=manager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            return maxVolumeLevel;
        }

    }

    public int getMinVolume(){

        if (minVolumeListener!=null){
            return minVolumeListener.getMinVolume();
        }else {
            return MUSIC_MIN_VOLUME_LEVEL;
        }

    }

    public int getVolumeCopies(){
        if (volumeCopyListener!=null){
            return volumeCopyListener.getVolumeCopies();
        }else {
            return VOLUME_COPIES;
        }
    }


    public int setMusicVolume(int volumeLevel){

        if (!isLegal(volumeLevel)){
            return errorCode;
        }
        //设置音量：当前音量已经是最小的了
        if (volumeLevel==getMinVolume() && volumeLevel==getCurrentVolume()){
            errorCode=CURRENT_LEVEL_IS_MIN_VOLUME_LEVEL;
            return errorCode;
        }
        //设置音量：当前音量已经是最大的了
        if (volumeLevel==getMaxVolume() && volumeLevel==getCurrentVolume()){
            errorCode=CURRENT_LEVEL_IS_MAX_VOLUME_LEVEL;
            return errorCode;
        }
        manager.setStreamVolume(AudioManager.STREAM_MUSIC,volumeLevel,AudioManager.FLAG_PLAY_SOUND);
        errorCode=SET_MUSIC_VOLUME_SUCCESS;

        return errorCode;
    }

    public int setMaxVolume(){
        //设置音量：当前音量已经是最大的了
        if (getMaxVolume()==getCurrentVolume()){
            errorCode=CURRENT_LEVEL_IS_MAX_VOLUME_LEVEL;
            return errorCode;
        }
        manager.setStreamVolume(AudioManager.STREAM_MUSIC,getMaxVolume(),AudioManager.FLAG_PLAY_SOUND);
        return SET_MUSIC_VOLUME_SUCCESS;
    }

    public int setMinVolume(){
        //设置音量：当前音量已经是最小的了
        if (getMinVolume() == getCurrentVolume()){
            errorCode=CURRENT_LEVEL_IS_MIN_VOLUME_LEVEL;
            return errorCode;
        }
        manager.setStreamVolume(AudioManager.STREAM_MUSIC,getMinVolume(),0);
        return SET_MUSIC_VOLUME_SUCCESS;
    }

    public int adjustRaiseMusicVolume(){
        int currentVolumeLevel=getCurrentVolume();
        if (!isLegal(currentVolumeLevel)){
            return errorCode;
        }
        //设置音量：当前音量已经是最大的了
        if (getMaxVolume() ==getCurrentVolume()){
            errorCode=CURRENT_LEVEL_IS_MAX_VOLUME_LEVEL;
            return errorCode;
        }
        return setMusicVolume(getCurrentVolume()+1);

    }

//    public int raiseMusicVolume(){
//
//    }

    public int adjustLowerMusicVolume(){
        if (!isLegal(getCurrentVolume())){
            return errorCode;
        }
        //设置音量：当前音量已经是最小的了
        if (getMinVolume() ==getCurrentVolume()){
            errorCode=CURRENT_LEVEL_IS_MIN_VOLUME_LEVEL;
            return errorCode;
        }
        return setMusicVolume(getCurrentVolume()-1);
    }

    /**
     * 检验音量值是否合法
     * @param volumeLevel：音量值
     * @return :
     */
    public boolean isLegal(int volumeLevel){
        if (volumeLegalListener!=null){
            return volumeLegalListener.isLegal(volumeLevel);
        }else {
            boolean isLegal=false;
            if (volumeLevel<getMinVolume() || volumeLevel>getMaxVolume()){
                errorCode=SET_MUSIC_VOLUME_FAILED;
                return isLegal;
            }
            return true;
        }

    }

    public int getErrorCode() {
        return errorCode;
    }

    public static MaxVolumeListener getMaxVolumeListener() {
        return maxVolumeListener;
    }

    public static void setMaxVolumeListener(MaxVolumeListener maxVolumeListener) {
        AudioUtils.maxVolumeListener = maxVolumeListener;
    }

    public static MinVolumeListener getMinVolumeListener() {
        return minVolumeListener;
    }

    public static void setMinVolumeListener(MinVolumeListener minVolumeListener) {
        AudioUtils.minVolumeListener = minVolumeListener;
    }

    public static VolumeLegalListener getVolumeLegalListener() {
        return volumeLegalListener;
    }

    public static void setVolumeLegalListener(VolumeLegalListener volumeLegalListener) {
        AudioUtils.volumeLegalListener = volumeLegalListener;
    }

    public static VolumeCopyListener getVolumeCopyListener() {
        return volumeCopyListener;
    }

    public static void setVolumeCopyListener(VolumeCopyListener volumeCopyListener) {
        AudioUtils.volumeCopyListener = volumeCopyListener;
    }

    interface MaxVolumeListener{
        int getMaxVolume();
    }
    interface MinVolumeListener{
        int getMinVolume();
    }

    interface VolumeLegalListener{
        boolean isLegal(int volumeLevel);
    }

    interface VolumeCopyListener{
        int getVolumeCopies();
    }


}
