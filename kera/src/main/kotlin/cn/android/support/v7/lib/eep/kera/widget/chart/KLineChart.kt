package cn.android.support.v7.lib.eep.kera.widget.chart

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import cn.android.support.v7.lib.eep.kera.common.kpx

/**
 * 折线图
 */
open class KLineChart : KChart {

    constructor(context: Context?) : super(context) {}
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {}

    var default = -1f//默认值。
    var strokeWidth: Float = kpx.x(1.5f)//边框的宽度
    var strokeColor: Int = Color.parseColor("#bcbec0")//边框颜色
    var strokeCorner: Float = 0f//边框圆角
    var strokeDashFloat: FloatArray? = null//虚线数组,先画实线再画虚线,以此循环
    var gradientY: Float = default//路径渐变的Y坐标
    var gradientStartColor = Color.TRANSPARENT//渐变开始颜色
    var gradientEndColor = Color.TRANSPARENT//渐变结束颜色
    //fixme 渐变颜色数组值【均匀渐变】，gradientColors优先
    var gradientColors: IntArray? = null

    open fun gradientColors(vararg color: Int) {
        gradientColors = color
    }

    open fun gradientColors(vararg color: String) {
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

    var ORIENTATION_VERTICAL = 0//垂直
    var ORIENTATION_HORIZONTAL = 1//水平
    var gradientOritation = ORIENTATION_VERTICAL//渐变颜色方向，默认垂直
    var segmentLength: Float = 0f//杂线的密度,数值越小，杂线越多越紧密。
    var deviation: Float = 0f//原始路径的偏差,数值越大，杂线越越长。即杂线的长度
    //fixme 0f~20f,20f最好等于实线+虚线的长度。这样不会卡顿。
    var strokeDashPhase = 0f//虚线偏移量,属性动画，0f,20f先左流动。20f,0f向右流动。总之以0f开始，或以0f结束。0开始是左流动。0结束是右流动。

    override fun draw3(canvas: Canvas, paint: Paint) {
        //画路径
        var path = Path()
        getXY { x, y, position, value, sub ->
            var offset = 0f
            when (sub) {
                sub_big -> {
                    offset = -strokeCorner / 2.35f
                }
                sub_low -> {
                    offset = strokeCorner / 2.35f
                }
            }
            if (position == startPosition) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y + offset)//fixme offset尽可能解决圆角时的偏移量。【圆角太大了，同样会有误差。圆角最好不要超过50,20~30是最好的】
            }
        }
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = strokeWidth
        paint.color = strokeColor
        var cornerPathEffect = CornerPathEffect(strokeCorner)
        var dashPathEffect: DashPathEffect? = null
        var discretePathEffect: DiscretePathEffect? = null
        if (segmentLength > 0 && deviation > 0) {
            discretePathEffect = DiscretePathEffect(segmentLength, deviation)
        }
        strokeDashFloat?.let {
            dashPathEffect = DashPathEffect(strokeDashFloat, strokeDashPhase)
        }
        if (strokeDashFloat != null) {
            var composePathEffect = ComposePathEffect(dashPathEffect, cornerPathEffect)
            if (discretePathEffect != null) {
                var composePathEffect2 = ComposePathEffect(composePathEffect, discretePathEffect)
                paint.setPathEffect(composePathEffect2)
            } else {
                paint.setPathEffect(composePathEffect)
            }
        } else {
            if (discretePathEffect != null) {
                var composePathEffect = ComposePathEffect(cornerPathEffect, discretePathEffect)
                paint.setPathEffect(composePathEffect)
            } else {
                paint.setPathEffect(cornerPathEffect)
            }
        }
        var measure = PathMeasure()
        measure.setPath(path, false)
        var segPath = Path()
        var length = measure.length * progress
        measure.getSegment(0f, length, segPath, true)
        canvas.drawPath(segPath, paint)//画折线图路径
        val coords = floatArrayOf(0f, 0f)
        measure.getPosTan(length, coords, null)
        if (gradientY > default && progress >= 0.001) {
            var x1 = 0f
            var x2 = 0f
            //画路径股票渐变样式
            getStartAndEndXY { startX, startY, endX, endY ->
                x1 = startX
                x2 = endX
                path.lineTo(endX, gradientY)
                path.lineTo(startX, gradientY)
                path.close()
                //fixme 同一个点画两次就不会有圆角。
                if (progress >= 0.999) {
                    segPath.lineTo(endX, endY)
                }
                segPath.lineTo(coords[0], gradientY)
                segPath.lineTo(coords[0], gradientY)
                segPath.lineTo(startX, gradientY)
                segPath.lineTo(startX, gradientY)
                segPath.lineTo(startX, startY)
                segPath.lineTo(startX, startY)
                segPath.close()
            }
            var y1 = 0f
            getMinAndMaxXY { minX, minY, maxX, maxY ->
                y1 = maxY
            }
            if (strokeDashFloat != null) {
                var composePathEffect = ComposePathEffect(cornerPathEffect, dashPathEffect)//fixme 这里圆角顺序要换一下，不然虚线圆角无效。
                paint.setPathEffect(composePathEffect)
            }
            if (gradientOritation == ORIENTATION_VERTICAL) {
                //垂直渐变
                var linearGradient: LinearGradient? = null
                if (gradientColors != null) {
                    linearGradient = LinearGradient(0f, y1, 0f, gradientY, gradientColors, null, Shader.TileMode.CLAMP)
                } else {
                    linearGradient = LinearGradient(0f, y1, 0f, gradientY, gradientStartColor, gradientEndColor, Shader.TileMode.CLAMP)
                }
                paint.setShader(linearGradient)
            } else {
                //水平渐变
                var linearGradient: LinearGradient? = null
                if (gradientColors != null) {
                    linearGradient = LinearGradient(x1, gradientY, x2, gradientY, gradientColors, null, Shader.TileMode.CLAMP)
                } else {
                    linearGradient = LinearGradient(x1, gradientY, x2, gradientY, gradientStartColor, gradientEndColor, Shader.TileMode.CLAMP)
                }
                paint.setShader(linearGradient)
            }
            paint.style = Paint.Style.FILL
            canvas.drawPath(segPath, paint)//画折线图渐变背景
        }
        paint.setPathEffect(null)
    }

}