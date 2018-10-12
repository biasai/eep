package cn.android.support.v7.lib.eep.kera.utils

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.Settings

object KIntentUtils {

    //跳转到权限设置界面
    fun goSetting(activity: Activity) {
        val packageURI = Uri.parse("package:" + activity.getPackageName())
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageURI)
        activity.startActivity(intent)//跳转权限设置界面。基本上通用。小米是肯定行的。android6.0基本都可以。
    }

    /**
     * "021-80370889" 座机号码，格式就这样，系统可以识别出来，直接拔掉。(手机，座机都能识别)
     * 拨打电话（跳转到拨号界面，用户手动点击拨打,不需要要权限。）
     *
     * @param phoneNum 电话号码
     */
    fun goCallPhone(activity: Activity, phoneNum: String) {
        val intent = Intent(Intent.ACTION_DIAL)
        val data = Uri.parse("tel:$phoneNum")
        intent.data = data
        activity.startActivity(intent)
    }


    /**
     *  跳转系统浏览器
     *  其中url不能有空格。 .trim() 就可以了
     *  并且网址格式必须正确，必须有 http://  不然报错。
     */
    fun goBrowser(activity: Activity, url: String) {
        val intent = Intent()
        intent.action = "android.intent.action.VIEW"
        val content_url = Uri.parse(url.trim())
        intent.data = content_url
        activity.startActivity(intent)
    }

    //跳转到桌面
    fun goHome(activity: Activity) {
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)//防止报错
        activity.startActivity(intent)
    }

    //App重启
    fun goRest(activity: Activity) {
        var intent = activity.getPackageManager().getLaunchIntentForPackage(activity.getPackageName());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        activity.startActivity(intent);
    }

}