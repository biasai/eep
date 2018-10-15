package cn.android.support.v7.lib.eep.kera.common

import android.app.Activity
import android.content.Context
import android.view.Gravity
import android.view.View
import cn.android.support.v7.lib.eep.kera.base.KDialog
import cn.android.support.v7.lib.eep.kera.view.KProgressCircleView
import org.jetbrains.anko.UI
import org.jetbrains.anko.verticalLayout

/**
 * 进度条
 * Created by 彭治铭 on 2018/6/24.
 */
open class KProgressbar(activity: Activity, isStatus: Boolean = true, isTransparent: Boolean = false) : KDialog(activity, isStatus = isStatus, isTransparent = isTransparent,isInitUI = true) {

    override fun onCreateView(context: Context): View? {
        return context.UI {
            verticalLayout {
                gravity = Gravity.CENTER
                var progressView = KProgressCircleView(this.context)
                addView(progressView)
            }
        }.view
    }

    init {
        isDismiss(false)//触摸不消失
        isLocked(true)//屏蔽返回键
    }

    open override fun listener() {}

    open override fun recycleView() {}
}