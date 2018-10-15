package cn.android.support.v7.lib.eep.kera.base

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.view.ViewManager
import android.view.Window
import cn.android.support.v7.lib.eep.kera.R
import cn.android.support.v7.lib.eep.kera.common.KToast
import cn.android.support.v7.lib.eep.kera.utils.KPermissionUtils
import cn.android.support.v7.lib.eep.kera.utils.KPictureUtils
import cn.android.support.v7.lib.eep.kera.widget.*
import cn.android.support.v7.lib.eep.kera.widget.chart.KChart
import cn.android.support.v7.lib.eep.kera.widget.chart.KLineChart
import cn.android.support.v7.lib.eep.kera.widget.chart.KXAxis
import cn.android.support.v7.lib.eep.kera.widget.chart.KYAxis
import cn.android.support.v7.lib.eep.kera.widget.recycler.KFooterView
import cn.android.support.v7.lib.eep.kera.widget.viewpager.KDotsLvView
import cn.android.support.v7.lib.eep.kera.widget.viewpager.KDotsView
import cn.android.support.v7.lib.eep.kera.widget.viewpager.KNoScrollViewPager
import cn.android.support.v7.lib.eep.kera.widget.viewpager.KTabLayoutBar
import org.jetbrains.anko.AnkoViewDslMarker
import org.jetbrains.anko.act
import org.jetbrains.anko.custom.ankoView

/**
 * Created by 彭治铭 on 2018/6/24.
 */
open class KAppCompatActivity : AppCompatActivity() {
    var activity: Activity? = null
    open fun getActivity(): KAppCompatActivity {
        return this
    }

    override fun onDestroy() {
        activity = null//防止内存泄露
        super.onDestroy()
    }

    //触摸点击效果。
    open fun onPress(view: View?) {
        KView.onPress(view)
    }

    //右边滑动的阴影效果。子类可以自定义效果。
    open fun shadowSliding(): Int {
        return R.drawable.kera_drawable_left_shadow
    }

    //是否开启左滑移除效果。交给子类重写。true开启（默认），false不开启。
    open fun enableSliding(): Boolean {
        return true
    }

    open var isPortrait = true//是否竖屏。默认就是竖屏。
    open var isOrientation = true//是否固定竖屏横屏方向。true会固定方向。false就不会。

    override fun onCreate(savedInstanceState: Bundle?) {
        try {
            activity = this
            super.onCreate(savedInstanceState)
            //fixme 在8.0(api 26)系统的时候，Actvity透明和锁屏（横屏或竖屏）只能存在一个。这个Bug，8.1已经修复了。
            //fixme 这个Bug在 targetSdkVersion >= 27时，且系统是8.0才会出现 Only fullscreen activities can request orientation
            if (Build.VERSION.SDK_INT == 26 && getApplicationInfo().targetSdkVersion >= 26) {
                //这个情况会崩溃，不能横竖屏。是系统Bug
            } else if (isOrientation) {
                if (isPortrait) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)//竖屏
                } else {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)//横屏

                }
            }
            if (enableSliding()) {
                //开启左滑移除效果
                val slideLayout = KSlideLayout(this, shadowSliding())
                slideLayout.bindActivity(this)
            }
        } catch (e: Exception) {
            Log.e("test", "系统框架脑抽筋:\t" + e.message)
        }

        // 将当前Activity添加到栈中
        KActivityManager.getInstance().pushActivity(this)
        requestWindowFeature(Window.FEATURE_NO_TITLE)//无标题栏(setContentView()之前才有效)
        //设置状态栏透明
        KApplication.getInstance().setStatusBarTransparent(window)
    }

    //是否开启动画。true开启(默认开启)，false不开启。[子类可以重写]
    protected open fun isopenAnim(): Boolean {
        return true
    }

    open var enterAnim = R.anim.kera_right_in_without_alpha//进入动画[子类可重写]
    open var exitAnim = R.anim.kera_right_out_without_alpha//退出动画[子类可重写]

    //true 状态栏字体颜色为 黑色，false 状态栏字体颜色为白色。子类可以重写
    protected open fun isDarkMode(): Boolean {
        return KApplication.getInstance().darkmode
    }

    override fun onResume() {
        super.onResume()
        //设置状态栏字体颜色
        KApplication.getInstance().setStatusBarDrak(window, isDarkMode())
        if (isopenAnim()) {
            //启动动画(app首次启动也有效果。)
            overridePendingTransition(enterAnim, exitAnim)
            //overridePendingTransition(0, 0);这个可以关闭动画，系统默认动画也会关闭
        } else {
            overridePendingTransition(0, 0)//关闭动画。防止动画没有关闭。
        }
    }

    override fun onPause() {
        super.onPause()
        if (isopenAnim()) {
            //退出动画(app最后一个activity,关闭应用时，无效。即退出应用时无效)
            overridePendingTransition(enterAnim, exitAnim)
        } else {
            overridePendingTransition(0, 0)
        }
    }

    //true 程序按两次退出。false正常按键操作。[子类可以重写]
    protected open fun isExit(): Boolean {
        return false//默认不监听返回键，不退出
    }

    private var exitTime: Long = 0
    private var exitTime2: Long = 0
    var exitIntervalTime: Long = 2000//结束间隔时间
    //open var exitInfo = "再按一次退出"//退出提示信息[子类可以重写]
    open var exitInfo = "别点了，再点我就要走了"

    //退出时，提示语句。子类可重写。
    open fun onShowExit() {
        KToast.show(exitInfo)
    }

    //监听返回键
    override fun onBackPressed() {
        if (System.currentTimeMillis() - exitTime2 > exitIntervalTime) {
            exitTime2 = System.currentTimeMillis()
            //返回键第一次按下监听
            onBackPressed1?.let {
                it()
            }
        } else {
            //第二次按下监听
            onBackPressed2?.let {
                it()
            }
        }
        if (isExit()) {
            if (System.currentTimeMillis() - exitTime > exitIntervalTime) {
                onShowExit()
                exitTime = System.currentTimeMillis()
            } else {
                finish()
                KActivityManager.getInstance().finishAllActivity()
                KApplication.getInstance().exit()//退出应用（杀进程）
            }
        } else if (onBackPressed1 == null && onBackPressed2 == null) {
            super.onBackPressed()
        }
    }

    private var onBackPressed1: (() -> Unit)? = null
    //返回返回键第一次按下监听
    fun onBackPressed1(onBackPressed1: (() -> Unit)?) {
        this.onBackPressed1 = onBackPressed1
    }

    private var onBackPressed2: (() -> Unit)? = null
    //返回返回键第二次按下监听
    fun onBackPressed2(onBackPressed2: (() -> Unit)?) {
        this.onBackPressed2 = onBackPressed2
    }

    // 两次点击按钮之间的点击间隔不能少于1000毫秒（即1秒）
    var MIN_CLICK_DELAY_TIME = 1000
    var lastClickTime: Long = System.currentTimeMillis()//记录最后一次点击时间

    //判断是否快速点击，true是快速点击，false不是
    open fun isFastClick(): Boolean {
        var flag = false
        var curClickTime = System.currentTimeMillis()
        if ((curClickTime - lastClickTime) <= MIN_CLICK_DELAY_TIME) {
            flag = true//快速点击
        }
        lastClickTime = curClickTime
        return flag
    }

    //fixme 自定义点击事件，可以添加多个点击事情。互不影响
    open fun onClick(view: View?, onClick: () -> Unit) {
        //点击事件
        view?.setOnClickListener {
            //fixme 防止快速点击
            if (!isFastClick()) {
                onClick()//点击事件
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        KPermissionUtils.onRequestPermissionsResult(getActivity(), requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        KPictureUtils.onActivityResult(this, requestCode, resultCode, data)
    }

    //自定义控件
    //必须在AnkoComponent里面定义（只能在Activity中和Fragment中添加，才有效。）
    //ViewManager.随便取名
    inline fun ViewManager.kview(init: (@AnkoViewDslMarker KView).() -> Unit): KView {
        return ankoView({ ctx: Context -> KView(ctx) }, theme = 0) { init() }
    }

    inline fun ViewManager.kbutton(init: (@AnkoViewDslMarker KRadiusButton).() -> Unit): KRadiusButton {
        return ankoView({ ctx: Context -> KRadiusButton(ctx) }, theme = 0) { init() }
    }

    inline fun ViewManager.keditText(init: (@AnkoViewDslMarker KRadiusEditText).() -> Unit): KRadiusEditText {
        return ankoView({ ctx: Context -> KRadiusEditText(ctx, 0) }, theme = 0) { init() }
    }

    inline fun ViewManager.krelativeLayout(init: (@AnkoViewDslMarker KRadiusRelativeLayout).() -> Unit): KRadiusRelativeLayout {
        return ankoView({ ctx: Context -> KRadiusRelativeLayout(ctx) }, theme = 0) { init() }
    }

    inline fun ViewManager.ktextView(init: (@AnkoViewDslMarker KRadiusTextView).() -> Unit): KRadiusTextView {
        return ankoView({ ctx: Context -> KRadiusTextView(ctx) }, theme = 0) { init() }
    }

    //虚线
    inline fun ViewManager.kdashView(init: (@AnkoViewDslMarker KDashView).() -> Unit): KDashView {
        return ankoView({ ctx: Context -> KDashView(ctx) }, theme = 0) { init() }
    }

    inline fun ViewManager.kframeView(init: (@AnkoViewDslMarker KFrameView).() -> Unit): KFrameView {
        return ankoView({ ctx: Context -> KFrameView(ctx) }, theme = 0) { init() }
    }

    inline fun ViewManager.kgradientScrollView(init: (@AnkoViewDslMarker KGradientScrollView).() -> Unit): KGradientScrollView {
        return ankoView({ ctx: Context -> KGradientScrollView(ctx) }, theme = 0) { init() }
    }

    inline fun ViewManager.kgradientView(init: (@AnkoViewDslMarker KGradientView).() -> Unit): KGradientView {
        return ankoView({ ctx: Context -> KGradientView(ctx) }, theme = 0) { init() }
    }

    inline fun ViewManager.knumberProgressBar(init: (@AnkoViewDslMarker KNumberProgressBar).() -> Unit): KNumberProgressBar {
        return ankoView({ ctx: Context -> KNumberProgressBar(ctx) }, theme = 0) { init() }
    }

    inline fun ViewManager.kshadowRectView(init: (@AnkoViewDslMarker KShadowRectView).() -> Unit): KShadowRectView {
        return ankoView({ ctx: Context -> KShadowRectView(ctx) }, theme = 0) { init() }
    }

    inline fun ViewManager.ktriangleView(init: (@AnkoViewDslMarker KTriangleView).() -> Unit): KTriangleView {
        return ankoView({ ctx: Context -> KTriangleView(ctx) }, theme = 0) { init() }
    }

    //折线图
    inline fun ViewManager.klineChart(init: (@AnkoViewDslMarker KLineChart).() -> Unit): KLineChart {
        return ankoView({ ctx: Context -> KLineChart(ctx) }, theme = 0) { init() }
    }

    inline fun ViewManager.kxAxis(init: (@AnkoViewDslMarker KXAxis).() -> Unit): KXAxis {
        return ankoView({ ctx: Context -> KXAxis(ctx) }, theme = 0) { init() }
    }

    inline fun ViewManager.kyAxis(init: (@AnkoViewDslMarker KYAxis).() -> Unit): KYAxis {
        return ankoView({ ctx: Context -> KYAxis(ctx) }, theme = 0) { init() }
    }

    inline fun ViewManager.kfooterView(init: (@AnkoViewDslMarker KFooterView).() -> Unit): KFooterView {
        return ankoView({ ctx: Context -> KFooterView(ctx) }, theme = 0) { init() }
    }

    inline fun ViewManager.kdotsLvView(init: (@AnkoViewDslMarker KDotsLvView).() -> Unit): KDotsLvView {
        return ankoView({ ctx: Context -> KDotsLvView(ctx) }, theme = 0) { init() }
    }

    inline fun ViewManager.kdotsView(init: (@AnkoViewDslMarker KDotsView).() -> Unit): KDotsView {
        return ankoView({ ctx: Context -> KDotsView(ctx) }, theme = 0) { init() }
    }

    inline fun ViewManager.kviewPager(init: (@AnkoViewDslMarker KNoScrollViewPager).() -> Unit): KNoScrollViewPager {
        return ankoView({ ctx: Context -> KNoScrollViewPager(ctx) }, theme = 0) { init() }
    }

    inline fun ViewManager.ktabLayoutBar(init: (@AnkoViewDslMarker KTabLayoutBar).() -> Unit): KTabLayoutBar {
        return ankoView({ ctx: Context -> KTabLayoutBar(ctx) }, theme = 0) { init() }
    }

}