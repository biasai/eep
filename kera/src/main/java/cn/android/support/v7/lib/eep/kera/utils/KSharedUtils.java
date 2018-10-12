package cn.android.support.v7.lib.eep.kera.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;
import android.util.Log;

import java.io.File;

import cn.android.support.v7.lib.eep.kera.base.KApplication;
import cn.android.support.v7.lib.eep.kera.base.KListenner;
import cn.android.support.v7.lib.eep.kera.common.KToast;


/**
 * 安装原生分享
 * Created by 彭治铭 on 2018/3/13.
 */

public class KSharedUtils {
    private static KSharedUtils sharedUtils;

    public static KSharedUtils getInstance() {
        if (sharedUtils == null) {
            sharedUtils = new KSharedUtils();
        }
        return sharedUtils;
    }

    String title = "独乐乐不如众乐乐";//标题
    String packageQQ = "com.tencent.mobileqq";//QQ
    String packageWX = "com.tencent.mm";//微信
    String packageWB = "com.sina.weibo";//新浪微博

    /**
     * 判断是否安装腾讯、新浪等指定的分享应用
     *
     * @param packageName 应用的包名
     */
    public boolean checkPackage(String packageName) {
        try {
            KApplication.getInstance().getPackageManager().getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            //Log.e("test", "包名存在:\t" + packageName);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            //Log.e("test", "不存在存在:\t" + packageName);
            return false;
        }
    }

    /**
     * 分享功能【多个选择会弹出系统选择框，如果只有一个则不会弹出，直接跳转该应用，如ComponentName】
     *
     * @param activity 上下文
     * @param title    标题(即分享弹出框的标题)
     * @param msgTitle 分享主题【空间里的主题，就普通分享一个好友，是没有显示的。】
     * @param msgText  消息内容
     * @param imgPath  图片路径，不分享图片则传null
     * @param Package  分享应用包名
     */
    public void sharedMsg(Activity activity, String title, String msgTitle, String msgText, String imgPath, String Package) {
        try {
            Intent intent = new Intent(Intent.ACTION_SEND);
            if (Package != null && !Package.trim().equals("")) {
                intent.setPackage(Package);//通过指定包名，去筛选要分享的应用，如果不写，那么有能够分享的应用都会显示出来
            }
            if (imgPath == null || imgPath.equals("")) {
                intent.setType("text/plain"); // 纯文本
            } else {
                File f = new File(imgPath);
                if (f != null && f.exists() && f.isFile()) {
                    intent.setType("image/png");

                    Uri fileUri=null;
                    if (Build.VERSION.SDK_INT >= 23) {//7.0及以上版本(版本号24),为了兼容6.0(版本号23)，防止6.0也可能会有这个问题。
                        //getPackageName()和${applicationId}显示的都是当前应用的包名。无论是在library还是moudle中，都是一样的。都显示的是当前应用moudle的。与类库无关。请放心使用。
                        fileUri = FileProvider.getUriForFile(activity, activity.getPackageName() + ".provider", //与android:authorities="${applicationId}.provider"对应上
                                f);
                    } else {
                        fileUri = Uri.fromFile(f);
                    }
                    //share.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
                    intent.putExtra(Intent.EXTRA_STREAM, fileUri);
                    //以下两个addFlags必不可少。【以防万一出错】
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

//                    Uri u = Uri.fromFile(f);
//                    intent.putExtra(Intent.EXTRA_STREAM, u);
                    intent.putExtra(Intent.EXTRA_STREAM, fileUri);

                }
            }
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra(Intent.EXTRA_SUBJECT, msgTitle);
            intent.putExtra(Intent.EXTRA_TEXT, msgText);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            activity.startActivity(Intent.createChooser(intent, title));//添加了Task，必须使用Acitivity,不然报错。

        } catch (Exception e) {
            Log.e("test", "分享异常:\t" + e.getMessage());
        }
    }

    /**
     * QQ文本分享
     *
     * @param activity
     * @param txtConten 分享内容
     * @param listenner 回调，判断是否安装了该应用，是否能够分享。true【会回调】 安装了该应用，可以分享，false【不会回调】没有安装该应用，不能分享
     */
    public void sharedMsgQQ(Activity activity, String txtConten, KListenner<Boolean> listenner) {
        if (checkPackage(packageQQ)) {
            if (listenner != null) {
                listenner.onResult(true);//返回true时，才会回调。即成功才会回调。
            }
            sharedMsg(activity, title, "", txtConten, null, packageQQ);
        } else {
            //ToastUtils.show(activity, "请先安装QQ");
            KToast.INSTANCE.show("请先安装QQ",null);
        }
    }

    /**
     * 微信文本分享
     *
     * @param activity
     * @param txtConten
     */
    public void sharedMsgWX(Activity activity, String txtConten, KListenner<Boolean> listenner) {
        if (checkPackage(packageWX)) {
            if (listenner != null) {
                listenner.onResult(true);
            }
            sharedMsg(activity, title, "", txtConten, null, packageWX);
        } else {
            //ToastUtils.show(activity, "请先安装微信");
            KToast.INSTANCE.show("请先安装微信",null);
        }
    }

    /**
     * 新浪微博文本分享
     *
     * @param activity
     * @param txtConten
     */
    public void sharedMsgWB(Activity activity, String txtConten, KListenner<Boolean> listenner) {
        if (checkPackage(packageWB)) {
            if (listenner != null) {
                listenner.onResult(true);
            }
            sharedMsg(activity, title, "", txtConten, null, packageWB);
        } else {
            //ToastUtils.show(activity, "请先安装微博");
            KToast.INSTANCE.show("请先安装微博",null);
        }
    }

    /**
     * 短信分享
     *
     * @param activity
     * @param smstext  短信分享内容
     * @return
     */
    public static Boolean sendSms(Activity activity, String smstext) {
        Uri smsToUri = Uri.parse("smsto:");
        Intent mIntent = new Intent(Intent.ACTION_SENDTO, smsToUri);
        mIntent.putExtra("sms_body", smstext);
        activity.startActivity(mIntent);
        return null;
    }

    /**
     * 邮件分享
     *
     * @param activity
     * @param title    邮件的标题
     * @param text     邮件的内容
     * @return
     */
    public static void sendMail(Activity activity, String title, String text) {
        // 调用系统发邮件
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        // 设置文本格式
        emailIntent.setType("text/plain");
        // 设置对方邮件地址
        emailIntent.putExtra(Intent.EXTRA_EMAIL, "");
        // 设置标题内容
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, title);
        // 设置邮件文本内容
        emailIntent.putExtra(Intent.EXTRA_TEXT, text);
        activity.startActivity(Intent.createChooser(emailIntent, "Choose Email Client"));
    }


    /**
     * 其他【所有能够分享的应用】，文本分享
     *
     * @param activity
     * @param txtConten
     */
    public void sharedMsgOther(Activity activity, String txtConten) {
        sharedMsg(activity, title, "", txtConten, null, null);
    }

    // 調用系統方法分享文件
    public static void shareFile(Context context, File file) {
        if (null != file && file.exists()) {
            Intent share = new Intent(Intent.ACTION_SEND);

            Uri fileUri=null;
            if (Build.VERSION.SDK_INT >= 23) {//7.0及以上版本(版本号24),为了兼容6.0(版本号23)，防止6.0也可能会有这个问题。
                //getPackageName()和${applicationId}显示的都是当前应用的包名。无论是在library还是moudle中，都是一样的。都显示的是当前应用moudle的。与类库无关。请放心使用。
                fileUri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", //与android:authorities="${applicationId}.provider"对应上
                        file);
            } else {
                fileUri = Uri.fromFile(file);
            }
            //share.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
            share.putExtra(Intent.EXTRA_STREAM, fileUri);
            //以下两个addFlags必不可少。【以防万一出错】
            share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            share.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

            share.setType(getMimeType(file.getAbsolutePath()));//此处可发送多种文件
            share.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            context.startActivity(Intent.createChooser(share, "分享文件"));
        } else {
            //ToastUtils.showToastView("分享文件不存在");
            KToast.INSTANCE.show("分享文件不存在",null);
        }
    }

    // 根据文件后缀名获得对应的MIME类型。
    public static String getMimeType(String filePath) {
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        String mime = "*/*";
        if (filePath != null) {
            try {
                mmr.setDataSource(filePath);
                mime = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE);
            } catch (IllegalStateException e) {
                return mime;
            } catch (IllegalArgumentException e) {
                return mime;
            } catch (RuntimeException e) {
                return mime;
            }
        }
        return mime;
    }

}
