package cn.android.support.v7.lib.eep.kera.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * 手机如果同时连上了数据流量和wifi,以wifi的为主。即使wifi没有网，仍然连接的是wifi。只要wifi连着，不管有网没网。都是以wifi为主。不会去连数据流量。
 * 除非wifi断开。或者wifi连接失败(没有获得IP)。才会去连数据流量。
 * 网络链接状态类【需要wifi等权限】
 */
public class KNetWorkUtils {
    /**
     * 判断是否连上了网络。不能判断是否有网。比如你连上了一个wifi，但是该wifi是虚网，无法真正上网。
     * 只要数据流量打开或者随便连上一个wifi就会返回true，什么都没连，返回false
     *
     * @param activity
     * @return true 可能连上了数据流量，也可能连接上wifi。 false什么都没连。
     */
    public static boolean isNetworkAvailable(Context activity) {
        Context context = activity.getApplicationContext();
        // 获取手机所有链接管理对象(包括对wi-fi,net等链接的管理)
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) {
            return false;
        } else {
            // 获取NetWorkInfo对象
            NetworkInfo[] networkInfo = connectivityManager.getAllNetworkInfo();

            if (networkInfo != null && networkInfo.length > 0) {
                for (int i = 0; i < networkInfo.length; i++) {
                    // 判断当前网络状态是否为链接状态
                    if (networkInfo[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }

        }
        return false;
    }

    //判断wifi是否已连接【只能判断是否连接上wifi,不能判断是否有网】,如果wifi已经连接上了。即使数据流量连接上，上网也是已wifi为主，不会消耗流量。
    public static boolean isWifi(Context mContext) {
        ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
        if (activeNetInfo != null && activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI) {
            return true;
        }
        return false;
    }

    /**
     * 判断上网方式，是否为手机流量
     *
     * @param context
     * @return
     */
    public static boolean isTel(Context context) {
        if (isNetworkAvailable(context)) {//是否连接上网络
            if (isWifi(context)) {//是否连接上wifi
                return false;
            } else {
                return true;//手机流量
            }
        }
        return false;
    }

}
