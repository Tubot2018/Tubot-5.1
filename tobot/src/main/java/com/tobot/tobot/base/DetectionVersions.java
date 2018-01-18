package com.tobot.tobot.base;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tobot.tobot.entity.VersionsEntity;
import com.tobot.tobot.utils.AppTools;
import com.tobot.tobot.utils.Transform;
import com.tobot.tobot.utils.okhttpblock.OkHttpUtils;
import com.tobot.tobot.utils.okhttpblock.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
                            Log.i(TAG,"获取的版本号:"+versionsEntity.getVersionName()+";当前版本号:"+ AppTools.getVersionName(context));
                            if (transition(String.valueOf(versionsEntity.getVersionName())) > transition(AppTools.getVersionName(context))){
                                new UpgradeManger(context,versionsEntity.getApk()).showDownloadDialog();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    private int transition(String var){
        Pattern p = Pattern.compile("[^0-9]");
        Matcher matcher = p.matcher(String.valueOf(var));
        return Integer.parseInt(matcher.replaceAll("").trim());
    }

}
