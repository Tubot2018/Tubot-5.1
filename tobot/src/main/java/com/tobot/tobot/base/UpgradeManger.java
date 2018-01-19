package com.tobot.tobot.base;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.tobot.tobot.R;
import com.tobot.tobot.presenter.BRealize.BFrame;
import com.tobot.tobot.utils.AppTools;
import com.tobot.tobot.utils.TobotUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;


/**
 * 版本更新下载类
 * @author zwen
 */
public class UpgradeManger {
	
	private static String TAG = "Javen UpgradeManger";
	private Context mContext;
	private static final int DOWN_UPDATE = 1;
	private static final int DOWN_OVER = 2;
	private String apkUrl;
	private int progress;
	private Thread downLoadThread;
	private boolean interceptFlag = false, netWork;
	private ProgressDialog updateDialog;
	public static boolean upgrade;


	/* 下载包安装路径 */
	private String savePath() {
		String str = mContext.getString(R.string.app_name);
		return Environment.getExternalStorageDirectory() + "/" + str + "/";
	}

	private String getSaveName() {
		String str = mContext.getString(R.string.app_name);
		return savePath() + str + ".apk";
	}

	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case DOWN_UPDATE:
					updateDialog.setProgress(progress);
					if (!AppTools.netWorkAvailable(mContext)) {
						netWork = false;
						Toast.makeText(mContext, "网络不可用,请检查网络", Toast.LENGTH_SHORT).show();
					}
					break;
				case DOWN_OVER:
					upgrade = true;
					updateDialog.dismiss();
					BFrame.TTS(mContext.getResources().getString(R.string.Upgrade_prompt_initiative));
					StartOtherApplications();
//					installApk();
					break;
				default:
					break;
			}
		};
	};

	
	public UpgradeManger(Context context, String apkUrl) {
		this.mContext = context;
		this.apkUrl = apkUrl;
		netWork = true;
		updateDialog = new ProgressDialog(context);
		updateDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		updateDialog.setCanceledOnTouchOutside(false);// 设置点击屏幕Dialog不消失
		if (!AppTools.netWorkAvailable(context)) {
			updateDialog.setButton("取消", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					interceptFlag = true;
				}
			});
		}
		File thedir = new File(savePath());
		if (!thedir.exists() && !thedir.mkdirs()) {

		}
	}

	public void showDownloadDialog() {
		updateDialog.show();
		downloadApk();
	}

	private Runnable mdownApkRunnable = new Runnable() {
		@Override
		public void run() {
			try {
				URL url = new URL(apkUrl);
				Log.i(TAG,"apk地址: " + apkUrl);
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.connect();
				int length = conn.getContentLength();
				InputStream is = conn.getInputStream();

				File file = new File(savePath());
				if (!file.exists()) {
					file.mkdir();
				}
				String apkFile = getSaveName();
				File ApkFile = new File(apkFile);
				FileOutputStream fos = new FileOutputStream(ApkFile);
				int count = 0;
				byte buf[] = new byte[1024];
				do {
					int numread = is.read(buf);
					count += numread;
					progress = (int) (((float) count / length) * 100);
					// 更新进度
					mHandler.sendEmptyMessage(DOWN_UPDATE);

					if (numread <= 0) {
						// 下载完成通知安装
						mHandler.sendEmptyMessage(DOWN_OVER);
						break;
					}
					fos.write(buf, 0, numread);
				} while (!interceptFlag);// 点击取消就停止下载.
				fos.close();
				is.close();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	};

	/**
	 * 下载apk
	 */
	private void downloadApk() {
		downLoadThread = new Thread(mdownApkRunnable);
		downLoadThread.start();
	}

	/**
	 * 安装apk
	 */
	private void installApk() {
		File apkfile = new File(getSaveName());
		if (!apkfile.exists()) {
			return;
		}
		BFrame.TTS(mContext.getResources().getString(R.string.Upgrade_prompt));
		//adb命令安装
		Log.i(TAG,"apk保存路径:"+apkfile.toString());
		List<String> list = new LinkedList<>();
		list.add("adb shell");
		list.add("cd " + savePath());
		list.add("adb install -r tobot.apk");
		try {
			TobotUtils.doCmds(list);
			upgrade = false;
		} catch (Exception e) {
			e.printStackTrace();
		}

		//Android安装
//		Intent intent = new Intent(Intent.ACTION_VIEW);
//		intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//		intent.setClassName("com.android.packageinstaller", "com.android.packageinstaller.PackageInstallerActivity");
//		intent.setDataAndType(Uri.parse("file://" + apkfile.toString()), "application/vnd.android.package-archive");
//		mContext.startActivity(intent);
	}

	private void StartOtherApplications() {
//		final Intent intent = mContext.getPackageManager().getLaunchIntentForPackage("com.robot.restart");
//		new Thread(new Runnable() {
//			@Override
//			public void run() {
//				if (intent != null) {
//					Log.i(TAG, "已启动应用");
//					mContext.startActivity(intent);
//				} else {
		//创建一个意图（装载广播事件）
		Intent broadcast = new Intent();
		broadcast.setAction("adb.restart.start");
		//发送无序广播
		mContext.sendBroadcast(broadcast);
		Log.i(TAG, "没有要启动的应用");
//				}
//			}
//		}).start();
		installApk();
	}


}
