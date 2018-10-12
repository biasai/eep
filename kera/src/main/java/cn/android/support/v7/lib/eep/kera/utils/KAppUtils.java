package cn.android.support.v7.lib.eep.kera.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.android.support.v7.lib.eep.kera.base.KApplication;


/**
 * 启动应用和卸载应用 使用： 直接实例化一个对象，调用里面的方法即可。
 *
 * @author 彭治铭
 */
public class KAppUtils {

    static KAppUtils appUtils;

    public static KAppUtils getInstance() {
        if (appUtils == null) {
            appUtils = new KAppUtils();
        }
        return appUtils;
    }

    /**
     * 根据时间关闭应用
     *
     * @param year  年
     * @param month 月
     */
    public void shutApp(final int year, final int month) {
        Handler handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                removeMessages(0);
                String yyyy = KTimeUtils.getInstance().getAbsoluteTime("yyyy");
                int y = Integer.valueOf(yyyy);
                if (y == year) {
                    String MM = KTimeUtils.getInstance().getAbsoluteTime("MM");
                    int M = Integer.valueOf(MM);
                    //Log.e("test", "M:\t" + M);
                    if (M > month) {
                        //ToastUtils.showToastView("异常:\tcom.android.internal.policy.DecorView");
                        KApplication.getInstance().exit();
                    }
                }
                if (y > year) {
                    //ToastUtils.showToastView("异常:\tcom.android.internal.policy.DecorView");
                    KApplication.getInstance().exit();
                }

            }
        };
        handler.sendEmptyMessageDelayed(0, 10000);//10秒发送
    }

    /**
     * 启动应用
     *
     * @param apply 包名
     */
    public void startApp(Context context, String apply) {
        try {
            Intent intent = context.getPackageManager().getLaunchIntentForPackage(apply);
            context.startActivity(intent);
        } catch (Exception e) {
            Log.e("test", "App应用启动失败失败:\t" + e.getMessage());
        }
    }

    /**
     * 卸载应用【估计需要系统权限】
     *
     * @param apply 包名
     */
    public void uninstallApp(Context context, String apply) throws Exception {
        try {
            Uri packageURI = Uri.parse("package:" + apply);// xx是包名
            Intent intent = new Intent(Intent.ACTION_DELETE, packageURI);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            Log.e("test", "App卸载失败:\t" + e.getMessage());
        }
    }

    /**
     * 安装应用(兼容7.0版本)
     *
     * @param apk app的完整路径
     */
    public void installation(Context context, File apk) {
        try {
            String path = apk.getAbsolutePath();
            if (isAppComplete(context, path)) {//判斷apk安裝包是否完整
                /* apk安装界面跳转 */
                Intent intent = new Intent(Intent.ACTION_VIEW);
                Uri fileUri = Uri.fromFile(apk);
                if (Build.VERSION.SDK_INT >= 23) {//7.0及以上版本(版本号24),为了兼容6.0(版本号23)，防止6.0也可能会有这个问题。
                    //getPackageName()和${applicationId}显示的都是当前应用的包名。无论是在library还是moudle中，都是一样的。都显示的是当前应用moudle的。与类库无关。请放心使用。
                    fileUri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider",//与android:authorities="${applicationId}.provider"对应上
                            apk);
                }
                intent.setDataAndType(fileUri,
                        "application/vnd.android.package-archive");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.putExtra(Intent.EXTRA_STREAM, fileUri);//必不可少
                context.startActivity(intent);
            } else {
                Toast.makeText(context, "安装包解析错误", Toast.LENGTH_LONG).show();
                // 删除安装包
                KFileUtils.getInstance().delFile(apk.getAbsolutePath(), null, null);
            }
        } catch (Exception e) {
            Log.e("test", "App安装失败:\t" + e.getMessage());
        }
    }

    /**
     * 方法一 判断apk是否安装
     *
     * @param uri apk的包名
     * @return
     */
    public boolean isAppInstalled(Context context, String uri) {
        PackageManager pm = context.getPackageManager();
        boolean installed = false;
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            installed = true;
        } catch (NameNotFoundException e) {
            installed = false;
        }
        return installed;
    }

    /**
     * 方法二 判断apk是否安装
     *
     * @param packageName apk的包名
     * @return
     */
    public boolean isAppInstalleds(Context context, String packageName) {
        final PackageManager packageManager = context.getPackageManager();
        List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);
        List<String> pName = new ArrayList<String>();
        if (pinfo != null) {
            for (int i = 0; i < pinfo.size(); i++) {
                String pn = pinfo.get(i).packageName;
                pName.add(pn);
            }
        }
        return pName.contains(packageName);
    }


    /**
     * 获取apK包名
     *
     * @param apk 安装包的完整路径
     * @return
     */
    public String getPackageName(Context context, String apk) {
        PackageManager pm = context.getPackageManager();
        PackageInfo info = pm.getPackageArchiveInfo(apk,
                PackageManager.GET_ACTIVITIES);
        ApplicationInfo appInfo = null;
        if (info != null) {
            appInfo = info.applicationInfo;
            return appInfo.packageName;
        }
        return null;
    }

    /**
     * 判断apk安装包是否完整
     *
     * @param filePath
     * @return
     */
    public boolean isAppComplete(Context context, String filePath) {
        boolean result = false;
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo info = pm.getPackageArchiveInfo(filePath,
                    PackageManager.GET_ACTIVITIES);
            String packageName = null;
            if (info != null) {
                result = true;
            }
        } catch (Exception e) {
            result = false;
        }
        return result;
    }

    /**
     * 获取版本名称 versionName
     *
     * @return
     */
    public String getVersion(Context context) {
        PackageManager packageManager = context.getPackageManager();
        PackageInfo packInfo;
        try {
            packInfo = packageManager.getPackageInfo(context.getPackageName(),
                    0);
            return packInfo.versionName;
        } catch (NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取版本号 versionCode
     *
     * @return
     */
    public int getVersionNo(Context context) {
        PackageManager packageManager = context.getPackageManager();
        PackageInfo packInfo;
        try {
            packInfo = packageManager.getPackageInfo(context.getPackageName(),
                    0);
            return packInfo.versionCode;
        } catch (NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 获取Android SDK的版本号【23是6.0】
     *
     * @return
     */
    public int getSDKVersion() {
        int osVersion;
        try {
            osVersion = Integer.valueOf(Build.VERSION.SDK);
        } catch (NumberFormatException e) {
            osVersion = 0;
        }
        return osVersion;
    }


//    defaultConfig {
//        targetSdkVersion 23
//    }

    /**
     * 获取targetSdkVersion版本。
     *
     * @param context
     * @return
     */
    public int getTargetSdkVersion(Context context) {
        return context.getApplicationInfo().targetSdkVersion;
    }

    /**
     * 获取application中指定的meta-data (渠道号)，如(写在清单里，与Activity一样)<meta-data android:name="PUSH_APPID" android:value="1Y1Zt2hJmV5rHGdLRg0Ya" />
     *
     * @return 如果没有获取成功(没有对应值 ， 或者异常)，则返回值为空
     */
    public String getAppMetaData(Context context, String key) {
        if (context == null || TextUtils.isEmpty(key)) {
            return null;
        }
        String resultData = null;
        try {
            PackageManager packageManager = context.getPackageManager();
            if (packageManager != null) {
                ApplicationInfo applicationInfo = packageManager
                        .getApplicationInfo(context.getPackageName(),
                                PackageManager.GET_META_DATA);
                if (applicationInfo != null) {
                    if (applicationInfo.metaData != null) {
                        resultData = applicationInfo.metaData.getString(key);
                    }
                }

            }
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }

        return resultData;
    }

    /**
     * 查询手机内系统应用(不需要,小米手机能够正确获取。部分手机就不行。如华为荣耀6就不行。)
     *
     * @param context
     * @return
     */
    public List<PackageInfo> getAllSysApps(Context context) {
        List<PackageInfo> apps = new ArrayList<PackageInfo>();
        PackageManager pManager = context.getPackageManager();
        //获取手机内所有应用
        List<PackageInfo> paklist = pManager.getInstalledPackages(0);
        for (int i = 0; i < paklist.size(); i++) {
            PackageInfo pak = (PackageInfo) paklist.get(i);
            //判断系统预装的应用程序
            if (!((pak.applicationInfo.flags & pak.applicationInfo.FLAG_SYSTEM) <= 0)) {
                apps.add(pak);
                //Log.e("test", "包名:\t" + pak.packageName + "\t应用名:\t" + pManager.getApplicationLabel(pak.applicationInfo).toString());
            }
        }
        return apps;
    }

    /**
     * 获取所有应用包名
     * @param context
     * @return
     */
    public List<PackageInfo> getAllApps(Context context) {
        List<PackageInfo> apps = new ArrayList<PackageInfo>();
        PackageManager pManager = context.getPackageManager();
        //获取手机内所有应用
        List<PackageInfo> paklist = pManager.getInstalledPackages(0);
        for (int i = 0; i < paklist.size(); i++) {
            PackageInfo pak = (PackageInfo) paklist.get(i);
            //Log.e("test", "包名:\t" + pak.packageName + "\t应用名:\t" + pManager.getApplicationLabel(pak.applicationInfo).toString());
        }
        return apps;
    }

    /**
     * 获取系统相册包名
     *
     * @param context
     * @return
     */
    public String getGalleryPackName(Context context) {
        String packagename = null;
        PackageManager pManager = context.getPackageManager();
        List<PackageInfo> appList = getAllSysApps(context);
        for (int i = 0; i < appList.size(); i++) {
            PackageInfo pinfo = appList.get(i);
            String name = pManager.getApplicationLabel(pinfo.applicationInfo).toString().trim();
            if (("画廊").equals(name) || ("图库").equals(name) || ("图片库").equals(name) || ("图册").equals(name) || ("相册").equals(name) || ("相薄").equals(name) || ("相片").equals(name) || ("照片").equals(name) || ("图片").equals(name) || ("美图").equals(name)) {
                packagename = pinfo.packageName;
                //Log.e("test", "系统相册:\t" + pinfo.packageName + "\tname:\t" + name);
                break;
            }
            if (("畫廊").equals(name) || ("圖庫").equals(name) || ("圖片庫").equals(name) || ("圖冊").equals(name) || ("相簿").equals(name) || ("圖片").equals(name) || ("美圖").equals(name)) {
                packagename = pinfo.packageName;
                //Log.e("test", "系统相册:\t" + pinfo.packageName + "\tname:\t" + name);
                break;
            }
            if (("Gallery").equals(name) || ("Photo").equals(name) || ("Picture").equals(name)) {
                packagename = pinfo.packageName;
                //Log.e("test", "系统相册:\t" + pinfo.packageName + "\tname:\t" + name);
                break;
            }
            if (("gallery").equals(name.toLowerCase()) || ("photo").equals(name.toLowerCase()) || ("picture").equals(name.toLowerCase())) {
                packagename = pinfo.packageName;
                //Log.e("test", "系统相册:\t" + pinfo.packageName + "\tname:\t" + name);
                break;
            }
        }
        return packagename;
    }

    /**
     * 获取系统相机包名
     *
     * @param context
     * @return
     */
    public String getCameraPackName(Context context) {
        String packagename = null;
        PackageManager pManager = context.getPackageManager();
        List<PackageInfo> appList = getAllSysApps(context);
        for (int i = 0; i < appList.size(); i++) {
            PackageInfo pinfo = appList.get(i);
            String name = pManager.getApplicationLabel(pinfo.applicationInfo).toString().trim();
            if (("相机").equals(name) || ("相機").equals(name) || ("照相机").equals(name) || ("照相機").equals(name) || ("照片机").equals(name) || ("Camera").equals(name) || ("camera").equals(name.toLowerCase())) {
                packagename = pinfo.packageName;
                //Log.e("test", "系统相册:\t" + pinfo.packageName + "\tname:\t" + name);
                break;
            }
            if (("照相馆").equals(name) || ("相馆").equals(name) || ("照相館").equals(name) || ("相館").equals(name)) {
                packagename = pinfo.packageName;
                //Log.e("test", "系统相册:\t" + pinfo.packageName + "\tname:\t" + name);
                break;
            }
        }
        return packagename;
    }

}
