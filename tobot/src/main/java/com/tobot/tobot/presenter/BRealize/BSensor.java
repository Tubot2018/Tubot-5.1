package com.tobot.tobot.presenter.BRealize;

import android.content.Context;
import android.util.Log;

import com.tobot.tobot.Listener.SensorListener;
import com.tobot.tobot.presenter.ICommon.ICommonInterface;
import com.tobot.tobot.presenter.IPort.ISensor;
import com.tobot.tobot.utils.Transform;
import com.turing123.robotframe.internal.modbus.protocol.bean.BaseEntity;
import com.turing123.robotframe.internal.modbus.protocol.bean.Sensor;
import com.turing123.robotframe.internal.modbus.protocol.core.OnProtocolListener;
import com.turing123.robotframe.internal.modbus.protocol.core.ProtocolManager;

import java.util.Arrays;

import static com.turing123.robotframe.internal.modbus.protocol.core.ProtocolManager.getInstance;

/**
 * Created by Javen on 2017/8/31.
 */
public class BSensor implements ISensor{
    private static String TAG = "Javen BSensor";
    private Context mContent;
    private ICommonInterface mISceneV;
    private static ProtocolManager mProtocolManager;
    private static SensorListener mSensorListener;
    private BaseEntity write;

    public BSensor(ICommonInterface mISceneV){
        this.mISceneV = mISceneV;
        this.mContent = (Context)mISceneV;
        mProtocolManager = getInstance();
//        mProtocolManager.unRegisterSensorListener(Sensor.SENSOR_MERGE_ALL);
        mSensorListener = new SensorListener(mContent);
//        inShakeFall();
//        inSupersonic();
//        inInfrared();
    }

    @Override
    public void inSensor() {

    }

    private void inInfrared(){
        mProtocolManager.registerSensorListener(Sensor.SENSOR_INFRARED, new OnProtocolListener() {
            final byte[] mByte = new byte[8];

            @Override
            public void onMotionCompleted(byte[] receiveData) {
//                Log.d(TAG,"红外结束时接收到的数据"+ receiveData);
            }

            @Override
            public void onOrderFeedback(byte[] receiveData) {
//                Log.d(TAG,"红外反馈接收到的数据长："+ receiveData.length + ";红外数据:"+Arrays.toString(receiveData));
                mByte[0] = receiveData[0];
                mByte[1] = receiveData[1];
                mByte[2] = receiveData[2];
                mByte[3] = receiveData[3];
                mByte[4] = receiveData[4];
                mByte[5] = receiveData[5];
//                mByte[6] = receiveData[6];
                mByte[7] = 0x02;
                mSensorListener.onOrderFeedback(mByte);
            }

            @Override
            public void onMotionError(byte[] errorMessage) {
//                Log.d(TAG,"红外出错时接收到的数据"+ errorMessage);
            }

            @Override
            public void onMotionInterrupt() {

            }

            @Override
            public void onMotionStart(byte[] receiveData) {
//                Log.d(TAG,"红外开始时接收到的数据"+ receiveData);
            }
        });
    }

    private void inSupersonic(){
//        final SensorListener mSensorListener = new SensorListener();

        mProtocolManager.registerSensorListener(Sensor.SENSOR_SUPERSONIC_WAVE, new OnProtocolListener() {
            final byte[] mByte = new byte[8];

            @Override
            public void onMotionCompleted(byte[] receiveData) {
//                Log.d(TAG,"超声波结束时接收到的数据"+ receiveData);
            }

            @Override
            public void onOrderFeedback(byte[] receiveData) {
//                Log.d(TAG,"超声波反馈接收到的数据长："+ receiveData.length + ";超声波数据:"+Arrays.toString(receiveData));
                mByte[0] = receiveData[0];
                mByte[1] = receiveData[1];
                mByte[2] = receiveData[2];
                mByte[3] = receiveData[3];
                mByte[4] = receiveData[4];
                mByte[5] = receiveData[5];
//                mByte[6] = receiveData[6];
                mByte[7] = 0x01;
                mSensorListener.onOrderFeedback(mByte);
            }

            @Override
            public void onMotionError(byte[] errorMessage) {
//                Log.d(TAG,"超声波出错时接收到的数据"+ errorMessage);
            }

            @Override
            public void onMotionInterrupt() {

            }

            @Override
            public void onMotionStart(byte[] receiveData) {
//                Log.d(TAG,"超声波开始时接收到的数据"+ receiveData);
            }
        });
    }

    private void inShakeFall(){
//        final SensorListener mSensorListener = new SensorListener();

        mProtocolManager.registerSensorListener(Sensor.SENSOR_SHAKE_FALL, new OnProtocolListener() {
            final byte[] mByte = new byte[8];

            @Override
            public void onMotionCompleted(byte[] receiveData) {
//                Log.d(TAG,"摇晃结束时接收到的数据"+ receiveData);
            }

            @Override
            public void onOrderFeedback(byte[] receiveData) {
//                Log.d(TAG,"摇晃反馈接收到的数据长："+ receiveData.length + ";摇晃数据:"+Arrays.toString(receiveData));
                mByte[0] = receiveData[0];
                mByte[1] = receiveData[1];
                mByte[2] = receiveData[2];
                mByte[3] = receiveData[3];
                mByte[4] = receiveData[4];
                mByte[5] = receiveData[5];
//                mByte[6] = receiveData[6];
                mByte[7] = 0x00;
                mSensorListener.onOrderFeedback(mByte);
            }

            @Override
            public void onMotionError(byte[] errorMessage) {
//                Log.d(TAG,"摇晃出错时接收到的数据"+ errorMessage);
            }

            @Override
            public void onMotionInterrupt() {

            }

            @Override
            public void onMotionStart(byte[] receiveData) {
//                Log.d(TAG,"摇晃开始时接收到的数据"+ receiveData);
            }
        });
    }

    public void write2019Pattern(byte pattern){
        visit((byte) 16, new byte[]{0x20, 0x19}, pattern, 5, mSensorListener);
    }

    public void write2019Pattern(byte pattern, OnProtocolListener onProtocolListener){
        visit((byte) 16, new byte[]{0x20, 0x19}, pattern, 5, onProtocolListener);
    }

    public void read2019Pattern(byte[] address){
        visit((byte) 3, address, 4, mSensorListener);
    }

    public void read2019Pattern(byte[] address, OnProtocolListener onProtocolListener){
        visit((byte) 3, address, 4, onProtocolListener);
    }

    public void visit(byte functionCode, byte[] address, byte pattern, int identifying, OnProtocolListener onProtocolListener){
        visit(new BaseEntity(functionCode,address,new byte[]{0x00, 0x01},new byte[]{0x00, pattern}),identifying,onProtocolListener);
    }

    public void visit(byte functionCode, byte[] address, byte[] quantity, byte pattern, int identifying, OnProtocolListener onProtocolListener){
        visit(new BaseEntity(functionCode,address,quantity,new byte[]{0x00, pattern}),identifying,onProtocolListener);
    }

    public void visit(byte functionCode, byte[] address, byte[] quantity, byte[] data, int identifying, OnProtocolListener onProtocolListener){
        visit(new BaseEntity(functionCode,address,quantity,data),identifying,onProtocolListener);
    }

    public void visit(byte functionCode, byte[] address, int identifying, OnProtocolListener onProtocolListener){
        visit(new BaseEntity(functionCode,address,new byte[]{0x00, 0x01}),identifying,onProtocolListener);
    }

    public void visit(byte functionCode, byte[] address, byte[] quantity, int identifying, OnProtocolListener onProtocolListener){
        visit(new BaseEntity(functionCode,address,quantity),identifying,onProtocolListener);
    }

    private void visit(BaseEntity order, final int identifying, final OnProtocolListener onProtocolListener){

        mProtocolManager.sendOrderToBoard(order, new OnProtocolListener() {

            @Override
            public void onMotionCompleted(byte[] receiveData) {
//                Log.d(TAG,"结束时接收到的数据"+ Arrays.toString(receiveData));
            }

            @Override
            public void onOrderFeedback(byte[] receiveData) {
//                Log.d(TAG, "反馈接收到的数据长：" + receiveData.length + ";数据:" + Arrays.toString(receiveData));
                String occupied = Transform.DecimalismTOBinaryBy1((int) receiveData[identifying]);
                String[] lead = occupied.split(",");
                final byte[] mByte = new byte[receiveData.length + (lead.length > 0 ? lead.length : 1)];
                for (int i = 0; i < mByte.length; i++) {
                    if (i < receiveData.length){
                        mByte[i] = receiveData[i];
                    }else {
                        mByte[i] = new Byte(lead[mByte.length - (i + 1)]);
                    }
                }
//                for (byte var : mByte){
//                    Log.e(TAG,"mByte 数据:"+var);
//                }
                onProtocolListener.onOrderFeedback(mByte);
            }

            @Override
            public void onMotionError(byte[] errorMessage) {
//                Log.d(TAG,"出错时接收到的数据"+ errorMessage.length + ";出错数据:"+Arrays.toString(errorMessage));
            }

            @Override
            public void onMotionInterrupt() {

            }

            @Override
            public void onMotionStart(byte[] receiveData) {
//                Log.d(TAG,"开始时接收到的数据"+ Arrays.toString(receiveData));
            }
        });
    }


}
