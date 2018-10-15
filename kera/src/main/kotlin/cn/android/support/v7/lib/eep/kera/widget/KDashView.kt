package cn.android.support.v7.lib.eep.kera.widget

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import cn.android.support.v7.lib.eep.kera.base.KView
import cn.android.support.v7.lib.eep.kera.common.kpx

/**
 * 实现虚线。支持水平和垂直[都是居中]
 * 如果宽大于高，虚线是水平的。如果高大于宽，虚线是垂直的。
 */
open class KDashView : KView {

    constructor(viewGroup: ViewGroup) : super(viewGroup.context) {
        setLayerType(View.LAYER_TYPE_SOFTWARE, null)//虚线必须关闭硬件加速
        viewGroup.addView(this)//直接添加进去,省去addView(view)
    }


    //关闭硬件加速。不然在部分手机，如小米。线条与线条之间的连接处有锯齿。
    constructor(context: Context?) : super(context, false) {}

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        setLayerType(View.LAYER_TYPE_SOFTWARE, null)//虚线必须关闭硬件加速
    }

    var strokeWidth: Float = kpx.x(0.5f)//边框的宽度
    var strokeColor: Int = Color.WHITE//边框颜色
    var strokeDashFloat: FloatArray = floatArrayOf(kpx.x(15f), kpx.x(10f))//虚线数组,先画实线再画虚线,以此循环
    //fixme 0f~20f,20f最好等于实线+虚线的长度。这样不会卡顿。
    var strokeDashPhase = 0f//虚线偏移量,属性动画，0f,20f先左流动。20f,0f向右流动。总之以0f开始，或以0f结束。0开始是左流动。0结束是右流动。

    var gradientStartColor = Color.WHITE//渐变开始颜色
    var gradientEndColor = Color.WHITE//渐变结束颜色
    //fixme 渐变颜色数组值【均匀渐变】，gradientColors优先
    var gradientColors: IntArray? = null

    fun gradientColors(vararg color: Int) {
        gradientColors = color
    }

    fun gradientColors(vararg color: String) {
        gradientColors = IntArray(color.size)
        gradientColors?.apply {
            if (color.size > 1) {
                for (i in 0..color.size - 1) {
                    this[i] = Color.parseColor(color[i])
                }
            } else {
                this[0] = Color.parseColor(color[0])
            }
        }
    }


    override fun draw2(canvas: Canvas, paint: Paint) {
        super.draw2(canvas, paint)
        var dashPathEffect = DashPathEffect(strokeDashFloat, strokeDashPhase)
        paint.setPathEffect(dashPathEffect)
        paint.color = strokeColor
        paint.strokeWidth = strokeWidth
        paint.style = Paint.Style.STROKE
        if (w >= h) {
            //水平虚线
            var linearGradient: LinearGradient? = null
            if (gradientColors != null) {
                linearGradient = LinearGradient(0f, h/2f, w.toFloat(), h/2f, gradientColors, null, Shader.TileMode.CLAMP)
            } else {
                linearGradient = LinearGradient(0f, h/2f, w.toFloat(), h/2f, gradientStartColor, gradientEndColor, Shader.TileMode.CLAMP)
            }
            paint.setShader(linearGradient)
            canvas.drawLine(0f, h / 2f, w.toFloat(), h / 2f, paint)
        } else {
            //垂直虚线
            var linearGradient: LinearGradient? = null
            if (gradientColors != null) {
                linearGradient = LinearGradient(w / 2f, 0f, w / 2f, h.toFloat(), gradientColors, null, Shader.TileMode.CLAMP)
            } else {
                linearGradient = LinearGradient(w / 2f, 0f, w / 2f, h.toFloat(), gradientStartColor, gradientEndColor, Shader.TileMode.CLAMP)
            }
            paint.setShader(linearGradient)
            canvas.drawLine(w / 2f, 0f, w / 2f, h.toFloat(), paint)
        }

    }

}