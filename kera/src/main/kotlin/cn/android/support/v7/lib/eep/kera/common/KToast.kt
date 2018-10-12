package cn.android.support.v7.lib.eep.kera.common

import android.graphics.Color
import android.graphics.Paint
import android.view.Gravity
import android.view.View
import android.widget.Toast
import cn.android.support.v7.lib.eep.kera.base.KApplication
import cn.android.support.v7.lib.eep.kera.widget.KRadiusTextView
import org.jetbrains.anko.*

/**
 * Created by 彭治铭 on 2018/6/24.
 */
object KToast {
    var toast: Toast? = null
    var textView: KRadiusTextView? = null
    fun view(): View? = with(kpx.context()?.baseContext) {
        this?.let {
            with(it) {
                UI {
                    verticalLayout {
                        textView = KRadiusTextView(this.context).apply {}.lparams {}
                        addView(textView)
                    }
                }?.view
            }
        }
    }

    var yOffset = kpx.y(160)//提示框的与屏幕底部的距离。

    //以下默认属性，可以全局修改。根据需求来改。
    var defaultColor = Color.parseColor("#ab313131")//默认背景颜色（浅黑色）
    //var defaultColor = Color.parseColor("#61A465")//浅绿色，效果不错。
    var defaultPdding = kpx.x(24)//默认内补丁
    var defaultRadius = kpx.x(480f)//默认圆角半径（尽可能的大，确保圆形效果）
    var defaultTextSize = kpx.textSizeX(32)//默认字体大小
    var defaultTextColor = Color.WHITE//默认字体颜色
    private fun default() {
        textView?.let {
            //默认样式
            it.backgroundColor = defaultColor
            it.padding = defaultPdding
            it.topPadding = defaultPdding / 3 * 2
            it.bottomPadding = defaultPdding / 3 * 2
            it.all_radius = defaultRadius
            it.setTextSize(defaultTextSize)
            it.setTextColor(defaultTextColor)
            it.draw { canvas, paint -> }//自定义画空
        }
    }

//    调用案例
//    Toast.show("提示信息")
//    Toast.show(exitInfo){
//        it.apply {
//            backgroundColor= Color.parseColor("#61A465")//根据需求自定义文本框样式,这里是浅绿色，效果不错。
//        }
//    }

    //显示成功文本,浅绿色背景
    open fun showSuccess(text: String?, init: ((textView: KRadiusTextView) -> Unit)? = null) {
        show(text) {
            it.apply {
                backgroundColor = Color.parseColor("#61A465")
                leftPadding = kpx.x(80)
                rightPadding = kpx.x(30)
                //画正确的勾勾
                draw { canvas, paint ->
                    paint.color = Color.WHITE
                    paint.strokeCap = Paint.Cap.ROUND
                    paint.style = Paint.Style.STROKE
                    paint.strokeWidth = kpx.x(4.5f)
                    var w = kpx.x(30f)
                    var startX = leftPadding.toFloat() / 9*4
                    var endX = startX + w / 3
                    var startY = centerY + w / 7
                    var endY = startY + w / 4
                    canvas.drawLine(startX, startY, endX, endY, paint)
                    var endX2 = endX + w / 3 * 2
                    var endY2 = endY - w / 3 * 2
                    canvas.drawLine(endX, endY, endX2, endY2, paint)
                }
            }
        }
        init?.let {
            textView?.let {
                init(it)//可根据需求自定义样式
            }
        }
    }

    //显示失败文本,浅红色背景
    open fun showError(text: String?, init: ((textView: KRadiusTextView) -> Unit)? = null) {
        show(text) {
            it.apply {
                backgroundColor = Color.parseColor("#DA2222")
                leftPadding = kpx.x(80)
                rightPadding = kpx.x(30)
                //画错误的叉叉
                draw { canvas, paint ->
                    paint.color = Color.WHITE
                    paint.strokeCap = Paint.Cap.ROUND
                    paint.style = Paint.Style.STROKE
                    paint.strokeWidth = kpx.x(4.5f)
                    var w = kpx.x(25f)
                    var startX = leftPadding.toFloat() / 2
                    var endX = startX + w
                    var startY = centerY - w / 2
                    var endY = centerY + w / 2
                    canvas.drawLine(startX, startY, endX, endY, paint)
                    canvas.drawLine(startX, endY, endX, startY, paint)
                }
            }
        }
        init?.let {
            textView?.let {
                init(it)//可根据需求自定义样式
            }
        }
    }

    //显示提示信息，浅蓝色背景
    open fun showInfo(text: String?, init: ((textView: KRadiusTextView) -> Unit)? = null) {
        show(text) {
            it.apply {
                backgroundColor = Color.parseColor("#525FB7")
                leftPadding = kpx.x(80)
                rightPadding = kpx.x(30)
                //画提示图标
                draw { canvas, paint ->
                    paint.color = Color.WHITE
                    paint.strokeCap = Paint.Cap.ROUND
                    paint.style = Paint.Style.STROKE
                    paint.strokeWidth = kpx.x(3.5f)
                    var w = kpx.x(35f)
                    var startX = leftPadding.toFloat() / 9*3
                    var startY = centerY - w / 2
                    var endY = centerY + w / 2
                    canvas.drawCircle(startX + w / 2, centerY, w / 2, paint)//圆
                    paint.style = Paint.Style.FILL
                    paint.strokeWidth = 0F
                    var r = kpx.x(5f) / 2
                    canvas.drawCircle(startX + w / 2, startY + r + kpx.x(5f), r, paint)//点
                    paint.style = Paint.Style.STROKE
                    paint.strokeWidth = kpx.x(3.5f)
                    canvas.drawLine(startX + w / 2, startY + r * 6, startX + w / 2, endY - r*3.5f, paint)//线
                }
            }
        }
        init?.let {
            textView?.let {
                init(it)//可根据需求自定义样式
            }
        }
    }

    //显示一般文本,浅黑色背景
    open fun show(text: String?, init: ((textView: KRadiusTextView) -> Unit)? = null) {
        text?.let {
            if (it.trim().equals("")) {
                return
            }
            default()//默认样式
            if (toast == null) {
                toast = Toast(KApplication.getInstance())
                toast?.setDuration(Toast.LENGTH_SHORT)// 显示时长，1000为1秒
                val view = view()
                toast?.setView(view)// 自定义view
                default()
            }
            init?.let {
                textView?.let {
                    init(it)//可根据需求自定义样式
                }
            }
            toast?.setGravity(Gravity.CENTER or Gravity.BOTTOM, 0, yOffset)// 显示位置
            textView?.setText(text)
            toast?.show()
        }
    }
}