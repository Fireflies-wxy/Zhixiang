package com.bnrc.bnrcbus.network;

/**
 * Created by GWW on 2016/12/16.
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;


import com.bnrc.bnrcbus.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class UpdateFromBaidu
{
    /* 下载中 */
    private static final int DOWNLOAD = 1;
    /* 下载结束 */
    private static final int DOWNLOAD_FINISH = 2;
    private static final String TAG = "UpdateFromBaidu";

    private String downloadUrl;

    /* 下载保存路径 */
    private String localSavePath;

    /* 记录进度条数量 */
    private int progress;

    /* 是否取消更新 */
    private boolean cancelUpdate = false;

    private Context mContext;

    /* 更新进度条 */
    private ProgressBar mProgress;
    private Dialog mDownloadDialog;

    /*获取临时路径*/
    private String getDiskCacheDir(Context context) {
        String cachePath = null;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            Log.i(TAG, "getDiskCacheDir: out");
            cachePath = context.getExternalCacheDir().getPath();
        } else {
            Log.i(TAG, "getDiskCacheDir: in");
            cachePath = context.getCacheDir().getPath();
        }
        return cachePath;
    }

    private Handler mHandler = new Handler()
    {
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                // 正在下载
                case DOWNLOAD:
                    // 设置进度条位置
                    mProgress.setProgress(progress);
                    break;
                case DOWNLOAD_FINISH:
                    // 安装文件
                    installApk();
                    break;
                default:
                    break;
            }
        };
    };

    public UpdateFromBaidu(Context context)
    {
        this.mContext = context;
    }

    /**
     * 显示软件更新对话框
     */
    public void showNoticeDialog(boolean forceUpdate, String updatedDetail, String downloadUrl, String localSavePath)
    {
        this.downloadUrl = downloadUrl;
        this.localSavePath = this.getDiskCacheDir(mContext);

        // 构造对话框
        AlertDialog.Builder builder = new Builder(mContext);
        builder.setTitle("软件更新");
        builder.setMessage(updatedDetail);

        // 更新
        if (forceUpdate)
            showDownloadDialog();
        else {
            builder.setPositiveButton("更新", new OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    dialog.dismiss();
                    // 显示下载对话框
                    showDownloadDialog();
                }
            });
                    // 稍后更新
            builder.setNegativeButton("稍后更新", new OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    dialog.dismiss();

                    dialog.dismiss();
                    ((Activity)mContext).finish();
                    System.exit(0);
                }
            });
            Dialog noticeDialog = builder.create();
            noticeDialog.show();
        }

    }

    /**
     * 显示软件下载对话框
     */
    private void showDownloadDialog()
    {
        // 构造软件下载对话框
        AlertDialog.Builder builder = new Builder(mContext);
        builder.setTitle("正在下载更新");

        // 给下载对话框增加进度条
        final LayoutInflater inflater = LayoutInflater.from(mContext);
        View v = inflater.inflate(R.layout.progress, null);
        mProgress = (ProgressBar) v.findViewById(R.id.progress);
        builder.setView(v);
        // 取消更新
        builder.setNegativeButton("取消", new OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();
                // 设置取消状态
                cancelUpdate = true;
            }
        });
        mDownloadDialog = builder.create();
        mDownloadDialog.setCanceledOnTouchOutside(false);
        mDownloadDialog.show();

        // 启动新线程下载软件
        new downloadApkThread().start();
    }

    /**
     * 下载文件线程
     */
    private class downloadApkThread extends Thread
    {
        @Override
        public void run()
        {
            try
            {
                // 判断SD卡是否存在，并且是否具有读写权限
                if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
                {
                    URL url = new URL(downloadUrl);
                    // 创建连接
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.connect();
                    // 获取文件大小
                    int length = conn.getContentLength();
                    // 创建输入流
                    InputStream is = conn.getInputStream();

                    File file = new File(localSavePath);
                    // 判断文件目录是否存在
                    if (!file.exists())
                    {
                        Log.i(TAG, "run: path not exists");
                        file.mkdirs();
                    }
                    File apkFile = new File(localSavePath, "update.apk");
                    FileOutputStream fos = new FileOutputStream(apkFile);
                    int count = 0;
                    // 缓存
                    byte buf[] = new byte[1024];
                    // 写入到文件中
                    do
                    {
                        int numread = is.read(buf);
                        count += numread;
                        // 计算进度条位置
                        progress = (int) (((float) count / length) * 100);
                        // 更新进度
                        mHandler.sendEmptyMessage(DOWNLOAD);
                        if (numread <= 0)
                        {
                            // 下载完成
                            Log.d(TAG, "run: finish download");
                            mHandler.sendEmptyMessage(DOWNLOAD_FINISH);
                            break;
                        }
                        // 写入文件
                        fos.write(buf, 0, numread);
                    } while (!cancelUpdate);// 点击取消就停止下载.
                    fos.close();
                    is.close();
                }
            } catch (MalformedURLException e)
            {
                e.printStackTrace();
            } catch (IOException e)
            {
                e.printStackTrace();
            }
            // 取消下载对话框显示
            mDownloadDialog.dismiss();
        }
    };

    /**
     * 安装APK文件
     */
    private void installApk()
    {
        File apkfile = new File(localSavePath, "update.apk");
        if (!apkfile.exists())
        {
            Log.d(TAG, "installApk: file not exist");
            return;
        }
        // 通过Intent安装APK文件
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setDataAndType(Uri.parse("file://" + apkfile.toString()), "application/vnd.android.package-archive");
        Log.d(TAG, "installApk: before install"+localSavePath+"  "+apkfile.toString());
        mContext.startActivity(i);
    }
}
