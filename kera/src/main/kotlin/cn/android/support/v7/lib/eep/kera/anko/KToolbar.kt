package cn.android.support.v7.lib.eep.kera.anko

import android.app.Activity
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import cn.android.support.v7.lib.eep.kera.R
import cn.android.support.v7.lib.eep.kera.common.kpx
import cn.android.support.v7.lib.eep.kera.widget.KGradientView
import cn.android.support.v7.lib.eep.kera.widget.KRadiusRelativeLayout
import cn.android.support.v7.lib.eep.kera.widget.KRadiusTextView
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick

object KToolbar {
    //fixme 返回键，暂时设定3种类型。需要用户自定义取实现。type类型，分别对应1,2,3 。0是默认返回键。null是没有返回键。
    var backResource1: Int? = null//全局静态返回键图标，交给用户取实现
    var backWidth1 = 0//返回键宽度
    var backHeight1 = 0//返回键高度

    var backResource2: Int? = null//全局静态返回键图标，交给用户取实现
    var backWidth2 = 0//返回键宽度
    var backHeight2 = 0//返回键高度

    var backResource3: Int? = null//全局静态返回键图标，交给用户取实现
    var backWidth3 = 0//返回键宽度
    var backHeight3 = 0//返回键高度

    //与左边或右边的间距
    var offset = kpx.x(24)
    //标题栏高度
    var height = kpx.y(88)
    //标题栏，字体大小
    var textSize = kpx.textSizeY(32)
    //默认字体颜色
    var textColor = Color.WHITE

    //标题栏
    class Bar {

        var backResource: Int? = null//全局静态返回键图标，交给用户取实现
        var backWidth: Int? = null//返回键宽度
        var backHeight: Int? = null//返回键高度

        //最外层容器
        var contentView: KRadiusRelativeLayout? = null
        //背景渐变控件
        var bgGradientView: KGradientView? = null
        //左边返回键
        var leftTextView: KRadiusTextView? = null
        //左边返回键,在返回键的下面。作用是增大返回键的触摸范围。
        var leftTextView2: KRadiusTextView? = null
        //中间标题
        var centerTextView: KRadiusTextView? = null
        //右边文本(也可以放图片)
        var rightTextView: KRadiusTextView? = null
        //底部阴影分割线
        var bottomShadowView: KGradientView? = null

        //是否显示阴影分割线，true 显示，false不显示。默认不显示
        var isShadow: Boolean = false
            set(value) {
                field = value
                if (value) {
                    bottomShadowView?.visibility = View.VISIBLE
                } else {
                    bottomShadowView?.visibility = View.INVISIBLE
                }
            }

        //与左边或右边的间距
        var offset = KToolbar.offset
        //标题栏高度
        var height = KToolbar.height

        //标题栏标题
        var title: String? = null
            set(value) {
                field = value
                centerTextView?.text = value
            }
        //默认字体大小
        var textSize = KToolbar.textSize
            set(value) {
                field = value
                leftTextView?.setTextSize(value)
                centerTextView?.setTextSize(value)
                rightTextView?.setTextSize(value)
            }
        //默认字体颜色
        var textColor = KToolbar.textColor
            set(value) {
                field = value
                leftTextView?.setTextColor(value)
                centerTextView?.setTextColor(value)
                rightTextView?.setTextColor(value)
            }
    }

    /**
     * 获取标题栏
     * title 标题（默认空）
     * backResource 返回键图片
     * backWidth,backHeight返回键宽度和高度
     */
    fun bar(activity: Activity, title: String? = null, backResource: Int?, backWidth: Int?, backHeight: Int?): Bar {
        var topBar = Bar()
        topBar.title = title
        topBar.backResource = backResource
        topBar.backWidth = backWidth
        topBar.backHeight = backHeight
        var contentView = view(activity, topBar)
        topBar.contentView = contentView as KRadiusRelativeLayout
        topBar.bgGradientView = contentView.findViewById(kpx.id("bgGradientView"))
        topBar.leftTextView = contentView.findViewById(kpx.id("txt_left"))
        topBar.leftTextView2 = contentView.findViewById(kpx.id("txt_left2"))
        topBar.centerTextView = contentView.findViewById(kpx.id("txt_center"))
        topBar.rightTextView = contentView.findViewById(kpx.id("txt_right"))
        topBar.bottomShadowView = contentView.findViewById(kpx.id("shadow_view_bottom"))
        return topBar
    }

    /**
     * fixme type类型，分别对应1,2,3 。0是默认返回键。null是没有返回键。
     */
    fun bar(activity: Activity, title: String? = null, type: Int? = null): Bar {
        var backResource0: Int? = null
        var backWidth0: Int? = null
        var backHeight0: Int? = null
        when (type) {
            0 -> {
                //默认返回键
                backResource0 = R.mipmap.kera_back_white
                backWidth0 = kpx.y(24)
                backHeight0 = kpx.x(41)
            }
            1 -> {
                backResource0 = backResource1
                backWidth0 = backWidth1
                backHeight0 = backHeight1
            }
            2 -> {
                backResource0 = backResource2
                backWidth0 = backWidth2
                backHeight0 = backHeight2
            }
            3 -> {
                backResource0 = backResource3
                backWidth0 = backWidth3
                backHeight0 = backHeight3
            }
        }
        return bar(activity, title, backResource0, backWidth0, backHeight0)
    }

    //fixme 注意，必须是Context上下文。Activity不行。引用时，会报错。
    private fun view(activity: Activity, topBar: Bar): View = with(activity.baseContext) {
        var status = kpx.statusHeight
        if (!kpx.isStatusBarVisible(activity)) {
            //没有状态栏,即全屏
            status = 0
        }
        var roundRelativeLayout = KRadiusRelativeLayout(activity).apply {
            //背景渐变控件
            var bgView = KGradientView(activity).apply {
                id = kpx.id("bgGradientView")
                var layoutParams = ViewGroup.LayoutParams(matchParent, topBar.height + status)
                setLayoutParams(layoutParams)
            }
            addView(bgView)
            relativeLayout {
                var layoutParams = ViewGroup.LayoutParams(matchParent, topBar.height)
                var marginLayoutParams = ViewGroup.MarginLayoutParams(layoutParams)
                marginLayoutParams.topMargin = status
                layoutParams = RelativeLayout.LayoutParams(marginLayoutParams)
                setLayoutParams(layoutParams)
                //fixme 返回键,增大返回键触摸范围
                var left2 = KRadiusTextView(activity).apply {
                    id = kpx.id("txt_left2")
                    topBar.backResource?.let {
                        onClick {
                            activity.finish()//返回键默认退出当前Activity
                        }
                    }
                    backgroundColor = Color.TRANSPARENT
                }.lparams {
                    height = matchParent
                    width = kpx.x(100)
                    centerVertically()
                }
                addView(left2)
                //返回键
                var left = KRadiusTextView(activity).apply {
                    id = kpx.id("txt_left")
                    textSize = topBar.textSize
                    setTextColor(topBar.textColor)
                    topBar.backResource?.let {
                        setBackgroundResource(topBar.backResource!!)
                        onClick {
                            activity.finish()//返回键默认退出当前Activity
                        }
                    }
                }.lparams {
                    if (topBar.backWidth != null) {
                        width = topBar.backWidth!!
                    } else {
                        width = 0
                    }
                    if (topBar.backHeight != null) {
                        height = topBar.backHeight!!
                    } else {
                        height = 0
                    }
                    leftMargin = topBar.offset
                    centerVertically()
                }
                addView(left)
                //标题栏
                var center = KRadiusTextView(activity).apply {
                    id = kpx.id("txt_center")
                    textSize = topBar.textSize
                    setTextColor(topBar.textColor)
                    text = topBar.title
                }.lparams {
                    centerInParent()
                }
                addView(center)
                //右边文字或图片
                var right = KRadiusTextView(activity).apply {
                    id = kpx.id("txt_right")
                    textSize = topBar.textSize
                    setTextColor(topBar.textColor)
                }.lparams {
                    centerVertically()
                    alignParentRight()
                    rightMargin = topBar.offset
                }
                addView(right)
                //底部阴影分割线
                var bottom = KGradientView(activity).apply {
                    id = kpx.id("shadow_view_bottom")
                    //background = resources.getDrawable(R.drawable.crown_shadow_line_up_to_down)//阴影线，方向从上往下
                    //backgroundResource = R.drawable.crown_shadow_line_up_to_down
                    verticalColors("#dedede","#44dedede")
                    if (topBar.isShadow) {
                        visibility = View.VISIBLE
                    } else {
                        visibility = View.INVISIBLE
                    }
                }.lparams {
                    width = matchParent
                    height = kpx.x(2)//高度为2,效果最好。
                    alignParentBottom()
                }
                addView(bottom)
            }
        }
        return roundRelativeLayout
    }
}