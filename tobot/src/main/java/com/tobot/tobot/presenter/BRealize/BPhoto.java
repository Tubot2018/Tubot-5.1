package com.tobot.tobot.presenter.BRealize;

import android.content.Context;
import android.graphics.Point;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.ViewGroup;

import com.tobot.tobot.MainActivity;
import com.tobot.tobot.R;
import com.tobot.tobot.base.Constants;
import com.tobot.tobot.presenter.ICommon.ICommonInterface;
import com.tobot.tobot.presenter.IPort.ILocal;
import com.tobot.tobot.utils.SHA1;
import com.tobot.tobot.utils.TobotUtils;
import com.tobot.tobot.utils.Transform;
import com.tobot.tobot.utils.okhttpblock.OkHttpUtils;
import com.tobot.tobot.utils.okhttpblock.callback.StringCallback;
import com.tobot.tobot.utils.photograph.CameraInterface;
import com.tobot.tobot.utils.photograph.CameraSurfaceView;
import com.tobot.tobot.utils.photograph.DisplayUtil;
import com.turing123.robotframe.RobotFrameManager;
import com.turing123.robotframe.localcommand.LocalCommand;
import com.turing123.robotframe.localcommand.LocalCommandCenter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;

/**
 * Created by Javen on 2017/8/9.
 */

public class BPhoto implements ILocal,CameraInterface.CamOpenOverCallback {

    private CameraSurfaceView surfaceView;
    private static BPhoto mBPhoto;
    private static Context mContext;
    private ICommonInterface mISceneV;
    private LocalCommandCenter localCommandCenter;
    private LocalCommand localCommand;
    private RobotFrameManager mRobotFrameManager;
    private MainActivity mainActivity;
    private String phty;
    float previewRate = -1f;
    private String sn = "";//流水号
    private String uuid;

    public static synchronized BPhoto instance(ICommonInterface mISceneV) {
        if (mBPhoto == null) {
            mBPhoto = new BPhoto(mISceneV);
        }
        return mBPhoto;
    }

    private BPhoto(ICommonInterface mISceneV){
        this.mISceneV = mISceneV;
        this.mContext = (Context) mISceneV;
        this.mainActivity = (MainActivity) mISceneV;
        disposeLocal();
//        Thread openThread = new Thread(){//开启预览
//            @Override
//            public void run() {
//                renderScreen();
//            }
//        };
//        openThread.start();
        initViewParams();//初始化屏幕
    }


    @Override
    public void disposeLocal() {
        //1. 获取LocalCommandCenter 对象
        localCommandCenter = LocalCommandCenter.getInstance(mContext);
        //2. 定义本地命令的名字
        String name = "photo";
        //3. 定义匹配该本地命令的关键词，包含这些关键词的识别结果将交由该本地命令处理。
        List<String> keyWords = getPhotoKeyWords();
        //4. 定义本地命令对象
        localCommand = new LocalCommand(name, keyWords) {
            //4.1. 在process 函数中实现该命令的具体动作。
            @Override
            protected void process(String name, String s) {
                //4.1.1. 本示例中，当喊关键词中配置的词时将使机器人进入拍照
                carryThrough("");
                //5. 命令执行完成后需明确告诉框架，命令处理结束，否则无法继续进行主对话流程。
                this.localCommandComplete.onComplete();
            }

            //4.2. 执行命令前的处理
            @Override
            public void beforeCommandProcess(String s) {

            }

            //4.3. 执行命令后的处理
            @Override
            public void afterCommandProcess() {

            }
        };
        //5. 将定义好的local command 加入 LocalCommandCenter中。
        localCommandCenter.add(localCommand);
    }

    @Override
    public void renderScreen() {
        try {
            CameraInterface.getInstance().doOpenCamera(BPhoto.this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void carryThrough(String var) {
        this.sn = var;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    renderScreen();
                    Photograph();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }). start();
    }

    private void initViewParams(){
        surfaceView = (CameraSurfaceView)mainActivity.findViewById(R.id.camera_surfaceview);
        ViewGroup.LayoutParams params = surfaceView.getLayoutParams();
        Point p = DisplayUtil.getScreenMetrics(mContext);
        params.width = p.x;
        params.height = p.y;
        previewRate = DisplayUtil.getScreenRate(mContext); //默认全屏的比例预览
        surfaceView.setLayoutParams(params);
    }

    @Override
    public void cameraHasOpened() {
        SurfaceHolder holder = surfaceView.getSurfaceHolder();
        CameraInterface.getInstance().doStartPreview(holder, previewRate);//预览
    }

    public void Photograph() throws Exception{//拍照
        CameraInterface.getInstance().doTakePicture();
    }


    public static List<String> getPhotoKeyWords() {
        List<String> keyWords = new ArrayList<>();
        String[] array = mContext.getResources().getStringArray(R.array.photograph_keyWords_array);
        for (int i = 0; i < array.length; i++) {
            keyWords.add(array[i]);
        }
        return keyWords;
    }

    public void upload(String path) {
        uuid = Transform.getGuid();
        OkHttpUtils.post()
                .url(Constants.IMAGE_UPLOAD + uuid + "/" + SHA1.gen(Constants.identifying + uuid))
                .addParams("nonce", uuid)//伪随机数
                .addParams("sign", SHA1.gen(Constants.identifying + uuid))//签名
                .addParams("robotId", TobotUtils.getDeviceId(Constants.DeviceId, Constants.Path))//机器人设备ID
                .addParams("sn", sn)
                .addFile("data", path, new File(path))
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        Log.i("Javen", "照片发送失败:" + call.toString());
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        Log.i("Javen", "照片发送成功:" + response);
                        try {
                            BFrame.response(R.string.photograph_success_response);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
    }


}




//        public void photograph(){
//        Log.i("Javen","执行拍照");
//        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        File out = new File(getPhotopath());
//        intent.putExtra("return-data", out);
////        Log.i("Javen","路径"+Uri.fromFile(out));
//        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(out));
////        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
////        intent.putExtra("noFaceDetection", true);
//        mISceneV.getImgpath(Uri.fromFile(out));
//        mainActivity.startActivityForResult(intent, Constants.FOR_RESULT);
//    }
//
//
//    /**
//     * 获取原图片存储路径
//     * @return
//     */
//    private String getPhotopath() {
//        // 文件夹路径
//        String thepath = Environment.getExternalStorageDirectory() + "/";
//        thepath += mContent.getString(R.string.app_name) + "/";
//        Calendar ca = Calendar.getInstance();
//        int year = ca.get(Calendar.YEAR);// 获取年份
//        int month = ca.get(Calendar.MONTH) + 1;// 获取月份
//        int day = ca.get(Calendar.DATE);// 获取日
//        int hour = ca.get(Calendar.HOUR_OF_DAY);// 小时
//        int minute = ca.get(Calendar.MINUTE);// 分
//        int second = ca.get(Calendar.SECOND);// 秒
//        int milliSecond = ca.get(Calendar.MILLISECOND);
//        String Suffix = "png";
//        // 照片全路径
//        String fileName = thepath + year + "-" + (month < 10 ? "0" : "") + month + "-" + (day < 10 ? "0" : "") + day
//                + "_" + (hour < 10 ? "0" : "") + hour + "-" + (minute < 10 ? "0" : "") + minute + "-"
//                + (second < 10 ? "0" : "") + second + "_" + (milliSecond < 10 ? "00" : (milliSecond < 100 ? "0" : ""))
//                + milliSecond + (Suffix == null ? "" : ("." + Suffix));
//        File file = new File(thepath);
//        if (!file.exists()) {
//            file.mkdirs();// 创建文件夹
//        }
//        return fileName;
//    }
