package com.tobot.tobot.db.model;

import android.content.Context;
import android.util.Log;

import com.tobot.tobot.utils.CommonRequestManager;

import org.litepal.tablemanager.Connector;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by YF-04 on 2018/2/1.
 */

public class MyInitData {
    private static final String TAG = "MyInitData";

    public static final String CUSTOM_SCENE_DIR="TuubaCustomScene";
    public static final String ROLE_PROJECT_FILE_NAME="customRoleProject";
    public static final String ROLE_QUESTION_FILE_NAME="customRoleQuestion";

    private volatile List<Map<String ,String>> list;
    private List<CustomRoleProject>customRoleProjectList;
    private List<CustomRoleQuestion>customRoleQuestionList;

    private CommonRequestManager manager;
    private Context mContext;

    public MyInitData(Context context){
        this.mContext=context;
        manager= CommonRequestManager.getInstanse(mContext);
    }

    public synchronized void initData(){
        //1.数据库是否有数据 ，如果有数据则不需要初始化数据库的数据
        //创建数据库
        Connector.getDatabase();

        //2.初始化 数据库中的数据
        //2.1 初始化 CustomRoleProject 表
        List<Map<String, String>> projectList = new ArrayList<>();
        try {
             projectList = initCustomRoleProject();
            Log.d(TAG, "projectList: " + projectList);
        } catch (Exception e) {
            Log.e(TAG, "初始化 CustomRoleProject 表 出现 Exception e:" + e.getMessage());
            e.printStackTrace();
        }

        for (int i=0;i<projectList.size();i++){
            
        }


        //2.2 初始化 CustomRoleQuestion 表
        try {
            List<Map<String, String>> questionList = initCustomRoleQuestion();
            Log.d(TAG, "questionList: " + questionList);
        } catch (Exception e) {
            Log.e(TAG, "初始化 CustomRoleQuestion 表 出现 Exception e:" + e.getMessage());
            e.printStackTrace();
        }

    }

    /**
     *
     * @return
     * @throws Exception
     */
    public synchronized List<Map<String,String>>  initCustomRoleQuestion() throws Exception {
        Log.d(TAG, "initBasicActionInfo(): ");
        String configFileName=CUSTOM_SCENE_DIR+ File.separator+ROLE_QUESTION_FILE_NAME;
        File file=manager.getSDcardFile(configFileName);
        Log.d(TAG, "file.getAbsolutePath(): "+file.getAbsolutePath());
        return initCustomRoleInfo(file);
    }

    /**
     *
     * @return
     * @throws Exception
     */
    public synchronized List<Map<String,String>>  initCustomRoleProject() throws Exception {
        Log.d(TAG, "initBasicActionInfo(): ");
        String configFileName=CUSTOM_SCENE_DIR+ File.separator+ROLE_PROJECT_FILE_NAME;
        File file=manager.getSDcardFile(configFileName);
        Log.d(TAG, "file.getAbsolutePath(): "+file.getAbsolutePath());
        return initCustomRoleInfo(file);
    }


    /**
     *
     * @param fileName
     * @return
     * @throws Exception
     */
    public synchronized List<Map<String,String>> initCustomRoleInfo(File fileName) throws Exception {
        if (fileName == null) {
            throw new Exception("Illegal fileName: fileName is null!");
        }
        if (!fileName.exists()) {
            throw new Exception("Illegal fileName:  fileName is not exist!");
        }
        if (list == null) {
            list =new ArrayList<>();
        }
        if (!list.isEmpty()) {
            list.clear();
        }

        FileInputStream is = null;
        BufferedReader br = null;
        String line = "";
        try {
            is = new FileInputStream(fileName);
            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                String[] temp = line.split("#,#");
                if (temp!=null && temp.length>0){
                    try {
                        Map<String,String>map=new HashMap<>();
                        for (int i=0;i<temp.length;i++){
                            String [] infoTemp=temp[i].split(":");
                            String key=infoTemp[0];
                            String value=infoTemp[1];
                            map.put(key,value);
                        }
                        list.add(map);
                    }catch (Exception e){
                        throw new Exception("There are some errors in your configuration file:" + fileName.getName());
                    }
                }else {
                    Log.e(TAG, "configure is nothing: "+fileName.getName() );
                }

            }
            br.close();
            is.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return list;
    }

}
