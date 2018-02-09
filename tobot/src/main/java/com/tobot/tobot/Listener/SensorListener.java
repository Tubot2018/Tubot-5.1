package com.tobot.tobot.Listener;

import android.content.Context;
import android.util.Log;

import com.tobot.tobot.presenter.BRealize.BFrame;
import com.turing123.robotframe.function.selfprotect.SelfProtectEvent;
import com.turing123.robotframe.internal.modbus.protocol.core.OnProtocolListener;

/**
 * Created by Javen on 2017/9/21.
 */

public class SensorListener implements OnProtocolListener {
    private Context mContext;

    public SensorListener(Context context){
        this.mContext = context;
    }

    @Override
    public void onMotionStart(byte[] bytes) {
        Log.i("Javen","onMotionStart:"+bytes.length);
    }

    @Override
    public void onMotionCompleted(byte[] bytes) {
        Log.i("Javen","onMotionCompleted:"+bytes.length);
    }

    @Override
    public void onOrderFeedback(byte[] data) {
//        Log.i("Javen","识别位:"+bytesToInt(data,7)+"数据位:"+bytesToInt(data,4));
        switch (bytesToInt(data,7)){
            case 0:
                setInfrared(data);
                break;
            case 1:
                setSupersonic(data);
                break;
            case 2:
                setShakeFall(data);
                break;
        }
    }

    @Override
    public void onMotionError(byte[] bytes) {
        Log.i("Javen","onMotionError..."+bytes.length);
    }

    @Override
    public void onMotionInterrupt() {
        Log.i("Javen","onMotionInterrupt");
    }

    private void setInfrared(byte[] infrared){
        switch (bytesToInt(infrared,4)){
            case SelfProtectEvent.ATTITUDE_FALL_BACKWARD://向后倒
                BFrame.TTS("哎呀!好疼啊");
                break;
            case SelfProtectEvent.ATTITUDE_FALL_FORWARD://向前倒
                BFrame.TTS("刚那个瓜娃子推我");
                break;
            case SelfProtectEvent.ATTITUDE_NORMAL://正常

                break;
            case SelfProtectEvent.ATTITUDE_OTHER://其他

                break;
            case SelfProtectEvent.ATTITUDE_SHAKE://摇晃
                BFrame.TTS("别晃了,我都快被你摇散架了");
                break;
        }
    }

    private void setSupersonic(byte[] supersonic){
        switch (bytesToInt(supersonic,4)){
            case 15://最短障碍距离
                BFrame.TTS("你跟个傻瓜式的挡在这干嘛");
                break;
            case 60://最远感应距离
                BFrame.TTS("等等我,你跑那么快干嘛");
                break;
        }
    }

    private void setShakeFall(byte[] shakeFall){
        switch (bytesToInt(shakeFall,4)){
            case 30://红外距离
                BFrame.TTS("你已被红外锁定");
                break;
            case 50://
                BFrame.TTS("红外解除锁定");
                break;
        }
    }


    public static int bytesToInt(byte[] src, int offset) {
        int value;
        value = (int) ((src[offset] & 0xFF));
        return value;
    }


}
