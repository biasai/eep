package cn.android.support.v7.lib.eep.kera.widget

import android.content.Context
import android.graphics.*
import android.util.AttributeSet

/**
 * 三角形
 * Created by 彭治铭 on 2018/8/26.
 */
open class KTriangleView : KGradientView {
    constructor(context: Context) : super(context) {}
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {}

    var ORIENTATION_LEFT = 0//左
    var ORIENTATION_TOP = 1//上
    var ORIENTATION_RIGHT = 2//右
    var ORIENTATION_BOTTOM = 3//下
    var triangleOrientation = ORIENTATION_LEFT//三角形起点位置。默认左边。
    var triangleWidth = 0f//fixme 三角形边框宽度, 大于0有边框三角形空心，小于或等于0 实心。默认实心。
    var triangleCorner: Float = 0f//三角形圆角
    override fun draw2(canvas: Canvas, paint: Paint) {
        super.draw2(canvas, paint)
        paint.setShader(null)
        paint.setXfermode(PorterDuffXfermode(PorterDuff.Mode.DST_IN))//fixme 取下层交集，三角形的颜色，由背景色，或渐变色决定。
        if (triangleWidth > 0) {
            //有边框，空心
            paint.setStyle(Paint.Style.STROKE)
        } else {
            //边框为0，则实心
            paint.setStyle(Paint.Style.FILL_AND_STROKE)//可以画实心三角形
            triangleWidth = 0f
        }
        if (triangleCorner > 0) {
            var cornerPathEffect = CornerPathEffect(triangleCorner)
            paint.setPathEffect(cornerPathEffect)
        } else {
            paint.setPathEffect(null)
        }
        paint.setStrokeWidth(triangleWidth)
        paint.strokeCap = Paint.Cap.BUTT
        paint.strokeJoin = Paint.Join.MITER
        var path = Path()
        var offset = triangleWidth//偏移量。防止三角形无法完整显示出来。不要除以2
        when (triangleOrientation) {
            ORIENTATION_LEFT -> {
                path.moveTo(0f + offset, h / 2f)
                path.lineTo(w.toFloat() - offset, 0f + offset)
                path.lineTo(w.toFloat() - offset, h.toFloat() - offset)
                path.close()
            }
            ORIENTATION_TOP -> {
                path.moveTo(w / 2f, 0f + offset)
                path.lineTo(0f + offset, h.toFloat() - offset)
                path.lineTo(w.toFloat() - offset, h.toFloat() - offset)
                path.close()
            }
            ORIENTATION_RIGHT -> {
                path.moveTo(w.toFloat() - offset, h / 2f)
                path.lineTo(0f + offset, 0f + offset)
                path.lineTo(0f + offset, h.toFloat() - offset)
                path.close()
            }
            ORIENTATION_BOTTOM -> {
                path.moveTo(w / 2f, h.toFloat() - offset)
                path.lineTo(0f + offset, 0f + offset)
                path.lineTo(w.toFloat() - offset, 0f + offset)
                path.close()
            }
        }
        canvas.drawPath(path, paint)
        paint.setXfermode(null)
    }
}