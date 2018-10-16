package cn.android.support.v7.lib.eep.kera.dialog

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.RelativeLayout
import cn.android.support.v7.lib.eep.kera.R
import cn.android.support.v7.lib.eep.kera.base.KDialog
import cn.android.support.v7.lib.eep.kera.common.kpx
import cn.android.support.v7.lib.eep.kera.view.KRollerView
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick

/**
 * 滚轮弹框
 */
open class KRollerDialog(activity: Activity, isStatus: Boolean = true, isTransparent: Boolean = true) : KDialog(activity, isStatus = isStatus, isTransparent = isTransparent,isInitUI = true) {
    var contenView: View? = null//包裹滚轮的容器，可以控制背景样式。
    var txtRelativeLayout: RelativeLayout? = null//文本容器
    var rollerView: KRollerView? = null//底部滚轮

    var cancel: Button? = null//取消按钮
    var ok: Button? = null//完成按钮
    var info: Button? = null//提示信息
    var line1: View? = null//第一条线条,透明。
    var line2: View? = null//第二条线条

    init {
        setWindowAnimations(R.style.kera_window_bottom)
        isDismiss(true)
    }

    override fun onCreateView(context: Context): View? {
        return context.UI {
            verticalLayout {
                gravity = Gravity.BOTTOM
                contenView = verticalLayout {
                    isClickable = true
                    backgroundColor = Color.parseColor("#FAFAFA")//背景色

                    //第一条线条，透明
                    line1 = view {
                        backgroundColor = Color.TRANSPARENT
                    }.lparams {
                        width = matchParent
                        height = kpx.x(2)
                    }

                    //文本
                    txtRelativeLayout = relativeLayout {
                        cancel = button {
                            text = "取消"
                            textSize = kpx.textSizeX(38)
                            textColor = Color.parseColor("#4B97F0")
                            if (Build.VERSION.SDK_INT>=16){
                                background = null
                            }
                            onClick {
                                dismiss()
                            }
                        }.lparams {
                            centerVertically()
                        }

                        info = button {
                            text = "请选择"
                            textSize = kpx.textSizeX(38)
                            textColor = Color.parseColor("#888888")
                            if (Build.VERSION.SDK_INT>=16){
                                background = null
                            }
                        }.lparams {
                            centerInParent()
                        }

                        ok = button {
                            text = "完成"
                            textSize = kpx.textSizeX(38)
                            textColor = Color.parseColor("#4B97F0")
                            if (Build.VERSION.SDK_INT>=16){
                                background = null
                            }
                            onClick {
                                callback?.apply {
                                    rollerView?.let {
                                        this(it.currentItemValue, it.currentItemPosition)
                                    }
                                }
                                dismiss()
                            }
                        }.lparams {
                            centerVertically()
                            alignParentRight()
                        }

                    }.lparams {
                        width = matchParent
                        height = kpx.x(100)
                    }

                    //第二条线条
                    line2 = view {
                        backgroundColor = Color.parseColor("#88888888")
                    }.lparams {
                        width = matchParent
                        height = kpx.x(2)
                    }

                    //滚轮
                    rollerView = KRollerView(context).apply {

                        isCurved = true//fixme 设置卷尺效果
                        isCyclic = true//fixme 是否循环显示。
                        setCount(7)//当前显示的item可见个数。

                        setLineColor(Color.parseColor("#88888888"))//中间两条线条的颜色
                        setLineWidth(0)//线条的长度，0 就是全屏
                        setStrokeWidth(kpx.x(2))//线条边框的宽度

                        setTextSize(kpx.x(36f))//字体大小,这个不是文本框，单位就是像素。
                        setSelectTextColor(Color.parseColor("#444444"))//选中字体颜色
                        setDefaultTextColor(Color.parseColor("#888888"))//默认字体颜色

                    }.lparams {
                        width = matchParent
                        height = kpx.x(330)
                    }
                    addView(rollerView)
                }.lparams {
                    width = matchParent
                    height = wrapContent
                }
            }
        }.view
    }

    //设置数据
    open fun setItems(items: ArrayList<String>): KRollerDialog {
        rollerView?.setItems(items)
//        //监听
//        rollerView?.setItemSelectListener { item, position ->
//            //返回数据和下标
//        }
        return this
    }

    //选中指定下标
    open fun setCurrentPostion(position: Int): KRollerDialog {
        rollerView?.setCurrentPostion(position)
        return this
    }


    var callback: ((item: String, position: Int) -> Unit)? = null
    //完成按钮，监听回调
    open fun setItemSelectListener(callback: (item: String, position: Int) -> Unit) {
        this.callback = callback
    }

    override fun listener() {

    }

    override fun recycleView() {

    }
}