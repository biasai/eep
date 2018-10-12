package cn.android.support.v7.lib.eep.kera.utils;

import cn.android.support.v7.lib.eep.kera.base.KProportion;


/**
 * UI适配工具。
 * 使用说明：
 * UtilProportion.getInstance().reflection( this);//通过反射来自动适配当前所有View控件(不太可靠)
 * 或(非常可靠)：
 * UtilProportion.getInstance().adapterTextView((TextView) findViewById(R.id.textView));//通过控件ID自主适配
 * 退出时建议调用：onDestroy()
 * Created by 彭治铭 on 2016/10/10.
 */

public class KProportionUtils extends KProportion {
    private static KProportionUtils proportionUtils;

    public static KProportionUtils getInstance() {
        if (proportionUtils == null) {
            proportionUtils = new KProportionUtils();
        }
        return proportionUtils;
    }

    private KProportionUtils() {
        //标准分辨率
        //垂直分辨率 1920x1080作为主流标准。比例为16:9 。或 1280x720【小,省内存,一般这个就足够了】。使用本类，所有单位都使用px像素为单位。
        //1334x750是苹果的主流屏
        init(750f, 1334f);
    }

    //自我销毁,退出应用时，建议调用
    public void onDestroy() {
        if (proportionUtils != null) {
            proportionUtils = null;
        }
    }

}
