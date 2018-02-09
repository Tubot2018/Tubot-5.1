package com.tobot.tobot.control.demand;

import android.content.Context;

import com.tobot.tobot.R;
import com.tobot.tobot.presenter.BRealize.BFrame;
import com.tobot.tobot.presenter.BRealize.BaseTTSCallback;
import com.tobot.tobot.presenter.BRealize.InterruptTTSCallback;
import com.tobot.tobot.utils.CommonRequestManager;

import java.util.Map;

/**
 * Created by YF-04 on 2018/1/30.
 */

public class DemandSinology  implements DemandBehavior {

    private DemandModel demandModel;
    private CommonRequestManager manager;
    private Context context;

    public DemandSinology(Context context, DemandModel musicModel){
        this.context=context;
        this.manager= CommonRequestManager.getInstanse(context);
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
        String speech=manager.getString(R.string.beforeDemandSinology,songName);
        Map<String,String> map=null;
        try {
            map= BFrame.getString(speech);
        } catch (Exception e) {
            e.printStackTrace();
        }

        BaseTTSCallback baseTTSCallback=new BaseTTSCallback(){
            @Override
            public void onCompleted() {
                //开始播放国学
                try {
                    manager.playMusic(demandModel.getPlayUrl32(),null,null,null);
                } catch (Exception e) {
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
