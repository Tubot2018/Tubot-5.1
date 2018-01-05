package com.tobot.tobot.base;

import android.content.Context;
import android.util.Log;

import com.tobot.tobot.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by YF-04 on 2017/12/20.
 */

public class MyTouchResponse {
    private static final String TAG = "MyTouchResponse";

    private Context mContext;
    private List<String> onceTouchList;
    private List<String> doubleTouchList;

    public MyTouchResponse(Context context){
        this.mContext=context;
    }

    private void initData() {
        Log.d(TAG, "initData: ");
        if (onceTouchList==null){
            onceTouchList=new ArrayList<>();
            String[]onceTouchArray=mContext.getResources().getStringArray(R.array.onceTouchHead);
            for (int i=0;i<onceTouchArray.length;i++){
                onceTouchList.add(onceTouchArray[i]);
            }
        }
        if (doubleTouchList==null){
            doubleTouchList=new ArrayList<>();
            String[]doubleTouchArray=mContext.getResources().getStringArray(R.array.doubleTouchHead);
            for (int i=0;i<doubleTouchArray.length;i++){
                doubleTouchList.add(doubleTouchArray[i]);
            }
        }
    }

    public String onceTouchHeadResponse(){
        Log.d(TAG, "onceTouchHeadResponse: ");
        if (onceTouchList==null){
            initData();
        }
        Random random=new Random();
        int index=random.nextInt(onceTouchList.size());
        String str=onceTouchList.get(index);
        Log.d(TAG, "str: "+str);
        return str;

    }

    public String doubleTouchHeadResponse(){
        Log.d(TAG, "doubleTouchHeadResponse: ");
        if (doubleTouchList==null){
            initData();
        }
        Random random=new Random();
        int index=random.nextInt(doubleTouchList.size());
        String str=doubleTouchList.get(index);
        Log.d(TAG, "str: "+str);
        return str;

    }

}
