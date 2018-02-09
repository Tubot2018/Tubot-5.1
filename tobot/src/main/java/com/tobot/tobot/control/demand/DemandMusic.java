package com.tobot.tobot.control.demand;

import android.content.Context;
import android.util.Log;

import com.tobot.tobot.R;
import com.tobot.tobot.presenter.BRealize.BFrame;
import com.tobot.tobot.presenter.BRealize.BaseTTSCallback;
import com.tobot.tobot.presenter.BRealize.InterruptTTSCallback;
import com.tobot.tobot.utils.CommonRequestManager;

import java.util.Map;

/**
 * Created by YF-04 on 2017/10/9.
 */

public class DemandMusic implements DemandBehavior {
    private static final String TAG = "DemandBehavior";
    private DemandModel demandModel;
    private CommonRequestManager manager;
    private Context context;

    public DemandMusic(Context context,DemandModel musicModel){
        this.context=context;
        this.manager=CommonRequestManager.getInstanse(context);
        this.demandModel=musicModel;
    }

    public DemandModel getDemandModel() {
        return demandModel;
    }

    public void setDemandModel(DemandModel demandModel) {
        this.demandModel = demandModel;
    }

    @Override
    public void executeDemand() {

        String songName=demandModel.getTrack_title();
        String speech=manager.getString(R.string.beforeDemandMusic,songName);
        Map<String,String> map=null;
        try {
            map=BFrame.getString(speech);
        } catch (Exception e) {
            e.printStackTrace();
        }

        BaseTTSCallback baseTTSCallback=new BaseTTSCallback(){
            @Override
            public void onCompleted() {
                //开始播放音乐
                try {
                    String playUrl32=demandModel.getPlayUrl32();
                    Log.d(TAG, "playUrl32: "+playUrl32);
                    manager.playMusic(demandModel.getPlayUrl32(),null,null,null);
                } catch (Exception e) {
                    Log.e(TAG, "点播 音乐 出现 Exception e : "+e.getMessage());
                    e.printStackTrace();
                }

            }
        };
        BFrame.setInterruptTTSCallback(new InterruptTTSCallback(BFrame.main,baseTTSCallback));

        try {
            BFrame.responseWithCallback(map);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}
