package cn.android.support.v7.lib.eep.kera.base

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewManager
import cn.android.support.v7.lib.eep.kera.widget.*
import cn.android.support.v7.lib.eep.kera.widget.chart.KLineChart
import cn.android.support.v7.lib.eep.kera.widget.chart.KXAxis
import cn.android.support.v7.lib.eep.kera.widget.chart.KYAxis
import cn.android.support.v7.lib.eep.kera.widget.recycler.KFooterView
import cn.android.support.v7.lib.eep.kera.widget.viewpager.KDotsLvView
import cn.android.support.v7.lib.eep.kera.widget.viewpager.KDotsView
import cn.android.support.v7.lib.eep.kera.widget.viewpager.KNoScrollViewPager
import cn.android.support.v7.lib.eep.kera.widget.viewpager.KTabLayoutBar
import org.jetbrains.anko.AnkoViewDslMarker
import org.jetbrains.anko.custom.ankoView

/**
 * 继承本Fragment，主构造函数传入一个布局id或者一个View即可。然后就可以像Activity一样使用了。
 * Activity中加载说明：supportFragmentManager.beginTransaction().replace(px.id("frameLayoutID"),Myfragment()).commit()即可
 * Created by 彭治铭 on 2018/4/20.
 */
abstract open class KFragment(var layout: Int = 0, var content: View? = null) : Fragment() {

    //触摸点击效果。isRipple是否具备波浪效果
    open fun onPress(view: View?, isRipple: Boolean = true) {
        KView.onPress(view,isRipple)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (layout <= 0) {
            content?.let {
                return it
            }
            content = onCreateView()//子类可以直接重写onCreateView来创建View
            content?.let {
                return it
            }
            return super.onCreateView(inflater, container, savedInstanceState)
        } else {
            //获取xml布局
            if (content == null) {
                content = inflater.inflate(layout, container, false)
            }
            return content
        }
    }

    //fixme 如果传入的布局和view都为空。则可重写以下方法,一般都是重写的该方法。
    open fun onCreateView(): View? {
        //return UI { }.view//使用Anko布局
        return null
    }

    //获取控件
    fun <T> findViewById(id: Int): T? {
        var view = content?.findViewById<View>(id)
        return view as? T
    }

    override fun onResume() {
        super.onResume()
        KApplication.getInstance().setStatusBarDrak(activity?.window, isDarkMode())
    }

    //true 状态栏字体颜色为 黑色，false 状态栏字体颜色为白色。子类可以重写
    protected open fun isDarkMode(): Boolean {
        return KApplication.getInstance().darkmode
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

    inline fun ViewManager.kviewPager(init: (@AnkoViewDslMarker KNoScrollViewPager).() -> Unit): KNoScrollViewPager {
        return ankoView({ ctx: Context -> KNoScrollViewPager(ctx) }, theme = 0) { init() }
    }

    inline fun ViewManager.kscrollView(init: (@AnkoViewDslMarker KGradientScrollView).() -> Unit): KGradientScrollView {
        return ankoView({ ctx: Context -> KGradientScrollView(ctx) }, theme = 0) { init() }
    }

    inline fun ViewManager.kgradientView(init: (@AnkoViewDslMarker KGradientView).() -> Unit): KGradientView {
        return ankoView({ ctx: Context -> KGradientView(ctx) }, theme = 0) { init() }
    }

    //虚线
    inline fun ViewManager.kdashView(init: (@AnkoViewDslMarker KDashView).() -> Unit): KDashView {
        return ankoView({ ctx: Context -> KDashView(ctx) }, theme = 0) { init() }
    }

    inline fun ViewManager.kframeView(init: (@AnkoViewDslMarker KFrameView).() -> Unit): KFrameView {
        return ankoView({ ctx: Context -> KFrameView(ctx) }, theme = 0) { init() }
    }

    inline fun ViewManager.knumberProgressBar(init: (@AnkoViewDslMarker KNumberProgressBar).() -> Unit): KNumberProgressBar {
        return ankoView({ ctx: Context -> KNumberProgressBar(ctx) }, theme = 0) { init() }
    }

    inline fun ViewManager.kshadowRectView(init: (@AnkoViewDslMarker KShadowRectView).() -> Unit): KShadowRectView {
        return ankoView({ ctx: Context -> KShadowRectView(ctx) }, theme = 0) { init() }
    }

    //阴影
    inline fun ViewManager.kshadowView(init: (@AnkoViewDslMarker KShadowView).() -> Unit): KShadowView {
        return ankoView({ ctx: Context -> KShadowView(ctx) }, theme = 0) { init() }
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

    inline fun ViewManager.ktabLayoutBar(init: (@AnkoViewDslMarker KTabLayoutBar).() -> Unit): KTabLayoutBar {
        return ankoView({ ctx: Context -> KTabLayoutBar(ctx) }, theme = 0) { init() }
    }

}