package com.tobot.tobot.base;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tobot.tobot.db.bean.AnswerDBManager;
import com.tobot.tobot.db.model.Answer;
import com.tobot.tobot.entity.VersionsEntity;
import com.tobot.tobot.utils.AppTools;
import com.tobot.tobot.utils.SHA1;
import com.tobot.tobot.utils.TobotUtils;
import com.tobot.tobot.utils.Transform;
import com.tobot.tobot.utils.okhttpblock.OkHttpUtils;
import com.tobot.tobot.utils.okhttpblock.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import okhttp3.Call;

/**
 * Created by Javen on 2018/1/9.
 */

public class DetectionVersions {

    private String TAG = "Javen DetectionVersions";
    private String uuid;
    private Context context;

    public DetectionVersions(Context context){
        this.context = context;
        getVersions();
    }

    private void getVersions(){
        uuid = Transform.getGuid();
        OkHttpUtils.get()
                .url(Constants.VERSIONS )
//                .addParams("nonce", uuid)//伪随机数
//                .addParams("sign", SHA1.gen(Constants.identifying + uuid))//签名
//                .addParams("robotId", TobotUtils.getDeviceId(Constants.DeviceId, Constants.Path))//机器人设备ID
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        Log.i("Javen","获取版本信息失败:"+e);
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        Log.i("Javen","获取版本信息成功:"+response);
                        try {
                            JSONObject jsonObject = new JSONObject (response);
                            VersionsEntity versionsEntity = new Gson().fromJson(
                                    jsonObject.getString("data"),
                                    new TypeToken<VersionsEntity>() {
                                    }.getType());
                            Log.i(TAG,"版本号:"+versionsEntity.getVersionCode());
                            if (versionsEntity.getVersionCode() > AppTools.getVersionCode(context)){
                                new UpgradeManger(context,versionsEntity.getApk());
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

}
