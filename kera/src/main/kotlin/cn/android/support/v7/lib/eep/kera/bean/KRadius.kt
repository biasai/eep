package cn.android.support.v7.lib.eep.kera.bean

import android.graphics.*
import cn.android.support.v7.lib.eep.kera.base.KView
import cn.android.support.v7.lib.eep.kera.utils.KLoggerUtils

/**
 * 圆角实体类
 */
open class KRadius {

    //绘制矩形的起点。
    var x: Int = 0
    var y: Int = 0

    //绘制矩形的宽和高
    var w: Int = 0
    var h: Int = 0

    var all_radius: Float = 0F//默认，所有圆角的角度
    var left_top: Float = 0f//左上角
    var left_bottom: Float = 0f//左下角
    var right_top = 0f//右上角
    var right_bottom = 0f//右下角

    var strokeWidth = 0f//边框宽度
    var strokeColor = Color.TRANSPARENT//边框颜色

    //fixme 边框颜色渐变
    var strokeGradientStartColor = Color.TRANSPARENT//渐变开始颜色
    var strokeGradientEndColor = Color.TRANSPARENT//渐变结束颜色
    //fixme 渐变颜色数组值【均匀渐变】，gradientColors优先
    var strokeGradientColors: IntArray? = null
    var ORIENTATION_VERTICAL = 0//垂直
    var ORIENTATION_HORIZONTAL = 1//水平
    var strokeGradientOritation = ORIENTATION_HORIZONTAL//渐变颜色方向，默认水平

    //画边框，圆角
    fun drawRadius(canvas: Canvas?) {
        //KLoggerUtils.e("test","宽:\t"+w+"\t高:\t"+h+"\t边框宽度：\t"+strokeWidth+"\t边框颜色:\t"+strokeColor)
        canvas?.let {
            if (left_top <= 0) {
                left_top = all_radius
            }
            if (left_bottom <= 0) {
                left_bottom = all_radius
            }
            if (right_top <= 0) {
                right_top = all_radius
            }
            if (right_bottom <= 0) {
                right_bottom = all_radius
            }
            //利用内补丁画圆角。只对负补丁有效(防止和正补丁冲突，所以取负)
            var paint = KView.getPaint()
            paint.strokeCap = Paint.Cap.BUTT
            paint.strokeJoin = Paint.Join.MITER
            paint.isDither = true
            paint.isAntiAlias = true
            paint.style = Paint.Style.FILL
            paint.strokeWidth = 0f
            paint.setXfermode(PorterDuffXfermode(PorterDuff.Mode.DST_IN))//取下面的交集
            // 矩形弧度
            val radian = floatArrayOf(left_top!!, left_top!!, right_top, right_top, right_bottom, right_bottom, left_bottom, left_bottom)
            //fixme  画矩形
            var rectF = RectF(0f + x, 0f + y, w.toFloat() + x, h.toFloat() + y)
            var path = Path()
            path.addRoundRect(rectF, radian, Path.Direction.CW)
            if (left_top > 0 || left_bottom > 0 || right_top > 0 || right_bottom > 0 || all_radius > 0) {
                canvas.drawPath(path, paint)
            }
            if (strokeWidth > 0) {
                paint.strokeWidth = strokeWidth
                if (strokeColor != Color.TRANSPARENT) {
                    //fixme 边框大于0时，边框颜色不能为透明。不然无法显示出来。
                    //0是透明，颜色值是有负数的。
                    paint.color = strokeColor
                }
                //fixme 画矩形边框
                rectF = RectF(0f + strokeWidth / 2F + x, 0f + strokeWidth / 2F + y, w.toFloat() - strokeWidth / 2F + x, h.toFloat() - strokeWidth / 2F + y)
                path.reset()
                //fixme 路径填充矩形
                path.addRoundRect(rectF, radian, Path.Direction.CW)
                paint.style = Paint.Style.FILL
                paint.setXfermode(PorterDuffXfermode(PorterDuff.Mode.DST_IN))//取下面的交集
                paint.style = Paint.Style.FILL_AND_STROKE
                canvas.drawPath(path, paint)

                //画边框
                paint.style = Paint.Style.STROKE
                paint.setXfermode(null)//正常
                //边框颜色渐变，渐变颜色优先等级大于正常颜色。
                var linearGradient: LinearGradient? = null
                if (strokeGradientColors != null) {
                    if (strokeGradientOritation == ORIENTATION_HORIZONTAL) {
                        //fixme 水平渐变
                        linearGradient = LinearGradient(0f + x, 0f, w.toFloat() + x, h / 2f, strokeGradientColors, null, Shader.TileMode.CLAMP)
                    } else {
                        //fixme 垂直渐变
                        linearGradient = LinearGradient(0f, 0f + y, 0f, h.toFloat() + y, strokeGradientColors, null, Shader.TileMode.CLAMP)
                    }
                } else {
                    if (!(strokeGradientStartColor == Color.TRANSPARENT && strokeGradientEndColor == Color.TRANSPARENT)) {
                        if (strokeGradientOritation == ORIENTATION_HORIZONTAL) {
                            //fixme 水平渐变
                            linearGradient = LinearGradient(0f + x, 0f, w.toFloat() + x, h / 2f, strokeGradientStartColor, strokeGradientEndColor, Shader.TileMode.CLAMP)
                        } else {
                            linearGradient = LinearGradient(0f, 0f, 0f, h.toFloat(), strokeGradientStartColor, strokeGradientEndColor, Shader.TileMode.CLAMP)
                        }
                    }
                }
                linearGradient?.let {
                    paint.setShader(linearGradient)
                }
                canvas.drawPath(path, paint)
            }
        }
    }

}