package cn.android.support.v7.lib.eep.kera.widget.chart

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import cn.android.support.v7.lib.eep.kera.base.KView
import cn.android.support.v7.lib.eep.kera.common.kpx
import android.graphics.PaintFlagsDrawFilter

/**
 * X轴，水平方向。从左到右。
 */
open class KXAxis : KView {
    //关闭硬件加速。不然在部分手机，如小米。线条与线条之间的连接处有锯齿。
    constructor(context: Context?) : super(context, false) {}

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        setLayerType(View.LAYER_TYPE_SOFTWARE, null)
    }

    override fun draw2(canvas: Canvas, paint: Paint) {
        super.draw2(canvas, paint)
    }

    var default = -1f//默认值。
    var strokeLength: Float = default//X轴实际长度
    var strokeWidth: Float = kpx.x(1.5f)//边框的宽度
    var strokeColor: Int = Color.parseColor("#bcbec0")//边框颜色
    var strokeDashFloat: FloatArray = floatArrayOf(0f, 0f)//虚线数组,先画实线再画虚线,以此循环
    //fixme 0f~20f,20f最好等于实线+虚线的长度。这样不会卡顿。
    var strokeDashPhase = 0f//虚线偏移量,属性动画，0f,20f先左流动。20f,0f向右流动。总之以0f开始，或以0f结束。0开始是左流动。0结束是右流动。
    //fixme 起点,终点X坐标[方向从左往右]
    var startX: Float = default
    var stopX: Float = default
    //起点结束Y坐标(y坐标就一个，如果要画倾斜的线，直接rotation=30f旋转整个控件即可，旋转控件不会有锯齿，直接画斜线会有锯齿。)
    var startAndStopY: Float = default
    var data: KData? = null//数据
    open fun data(data: KData?): KXAxis {
        this.data = data
        invalidate()
        return this
    }

    //原点
    var originX: Float = default
        set(value) {
            field = value
            startX = value
        }
    var originY: Float = default
        set(value) {
            field = value
            startAndStopY = value
        }

    /**
     * fixme 设置起点。X轴和Y轴，如果宽和高一样，起点也一样。那起点肯定就会重合。
     */
    open fun setOrigin(x: Float, y: Float) {
        this.originX = x;
        this.originY = y;
    }

    open fun setOrigin(yAxis: KYAxis) {
        setOrigin(yAxis.originX, yAxis.originY)
    }

    //终点
    var endX = default
        get() = stopX
        set(value) {
            field = value
            stopX = value
        }
    var endY = default
        get() = startAndStopY
        set(value) {
            field = value
            startAndStopY = value
        }

    open fun setEnd(x: Float, y: Float) {
        this.endX = x
        this.endY = y
    }

    var unit: Float = default//fixme 单位长度。总长度就是控件本身长度。优先级高于count 。
    /**
     * fixme 根据单位获取X坐标值。1就等于一个unit的长度。
     */
    open fun getUnitX(unit: Float): Float {
        return this.unit * unit + startX
    }

    open fun getUnitX(unit: Int): Float {
        return getUnitX(unit.toFloat())
    }

    var count: Int = default.toInt()//显示的个数
    var rulerStrokeWidth: Float = strokeWidth//单位线条的宽度
    var rulerStrokeColor: Int = strokeColor//单位线条的颜色
    var rulerStartY: Float = default//单位线条开始Y坐标
    var rulerStopY: Float = default//单位线条结束Y坐标
    //fixme 0f~20f,20f最好等于实线+虚线的长度。这样不会卡顿。
    var rulerDashFloat: FloatArray = floatArrayOf(0f, 0f)//虚线数组,先画实线再画虚线,以此循环
    var rulerDashPhase = 0f//虚线偏移量,属性动画，0f,20f先左流动。20f,0f向右流动。总之以0f开始，或以0f结束。0开始是左流动。0结束是右流动。

    var arrowLength: Float = default//X轴最右边箭头的长度。
    var arrowStrokeWidth: Float = default//箭头边框的宽度
    var arrowStrokeColor: Int = strokeColor//箭头线条的颜色

    var isHidden = false//是否隐藏。true隐藏之后，不会绘制轴，只做计算

    override fun onDraw2(canvas: Canvas, paint: Paint) {
        super.onDraw2(canvas, paint)
        paint.style = Paint.Style.STROKE
        //设置边框线帽，边框小了，看不出效果。
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeJoin = Paint.Join.ROUND
        if (startX <= default) {
            startX = strokeWidth
            originX = startX
        }
        if (stopX <= default) {
            if (strokeLength > 0) {
                stopX = startX + strokeLength
            } else {
                stopX = width.toFloat() - strokeWidth * 2
            }
        }
        if (startAndStopY <= default) {
            startAndStopY = height / 2f//默认垂直居中
            originY = startAndStopY
        }
        if (strokeLength <= default) {
            strokeLength = stopX - startX
        }
        if (unit <= default && count > 0) {
            unit = (strokeLength / count)
        }
        if (count <= default && unit > 0) {
            count = (strokeLength / unit).toInt()
        }
        if (count > 0) {
            unit = (strokeLength / (count + 1))
        }

        if (arrowLength <= default) {
            arrowLength = strokeWidth * 5//箭头的长度
        }
        if (isHidden) {
            return
        }
        if (unit > 0 && count > 0) {
            if (rulerStartY <= default) {
                rulerStartY = startAndStopY - strokeWidth / 2
            }
            if (rulerStopY <= default) {
                rulerStopY = rulerStartY - strokeWidth * 5//直尺的长度。
            }
            paint.strokeWidth = rulerStrokeWidth
            paint.color = rulerStrokeColor
            //直尺虚线，如果数组为0，则是实线。
            var dashPathEffect = DashPathEffect(rulerDashFloat, rulerDashPhase)
            paint.setPathEffect(dashPathEffect)
            //画X轴上的单位直尺
            for (i in 0..count) {
                var x = i * unit + startX
                if (i != 0) {//fixme 第一个不画
                    if (i == count) {
                        var p = 0f
                        if (arrowLength > 0) {
                            p = arrowLength + strokeWidth
                        }
                        if (x < (stopX - unit / 3 - p)) {//fixme 最后一个距离少于单位长度的三分之一，也不画。
                            canvas.drawLine(x, rulerStartY, x, rulerStopY, paint)
                        }
                    } else {
                        canvas.drawLine(x, rulerStartY, x, rulerStopY, paint)
                    }
                }
                //画X轴上的单位文字
                drawPoint?.let {
                    var value = data?.getX(i.toFloat())
                    //Log.e("test","值:\t"+value+"\t下标:\t"+i.toFloat())
                    it(canvas, getPaint(), x, startAndStopY, i, value)
                }
            }
            paint.setPathEffect(null)
        }
        canvas.drawFilter = PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)

        //画X轴
        paint.strokeWidth = strokeWidth
        paint.color = strokeColor
        paint.isDither = true
        paint.isAntiAlias = true
        var dashPathEffect = DashPathEffect(strokeDashFloat, strokeDashPhase)
        paint.setPathEffect(dashPathEffect)
        canvas.drawLine(startX, startAndStopY, stopX, startAndStopY, paint)
        paint.setPathEffect(null)
        //画原点
        drawOrigin?.let {
            it(canvas,getPaint(), startX, startAndStopY)
        }

        //画终点箭头
        if (arrowLength > 0) {
            if (arrowStrokeWidth <= default) {
                arrowStrokeWidth = strokeWidth
            }
            paint.strokeWidth = arrowStrokeWidth
            paint.color = arrowStrokeColor
            var path = Path()
            path.moveTo(stopX - arrowLength, startAndStopY - arrowLength)
            path.lineTo(stopX + strokeWidth / 2, startAndStopY)
            path.lineTo(stopX - arrowLength, startAndStopY + arrowLength)
            canvas.drawPath(path, paint)
        }

        //画终点
        drawEnd?.let {
            it(canvas, getPaint(),stopX, startAndStopY)
        }

    }

    //画单位文本，返回每个标尺单位的x,y坐标，以及当前下标。从0开始
    //循环从 0 到 count 都会调用。
    //position X轴上的下标
    //value,X轴上的数据可能会为空
    private var drawPoint: ((canvas: Canvas, paint: Paint, x: Float, y: Float, position: Int, value: String?) -> Unit)? = null

    open fun drawPoint(drawPoint: (canvas: Canvas, paint: Paint, x: Float, y: Float, position: Int, value: String?) -> Unit) {
        this.drawPoint = drawPoint
    }

    //画原点，返回原点坐标
    private var drawOrigin: ((canvas: Canvas, paint: Paint, x: Float, y: Float) -> Unit)? = null

    open fun drawOrigin(drawOrigin: (canvas: Canvas, paint: Paint, x: Float, y: Float) -> Unit) {
        this.drawOrigin = drawOrigin
    }

    //画终点，返回终点坐标
    private var drawEnd: ((canvas: Canvas, paint: Paint, x: Float, y: Float) -> Unit)? = null

    open fun drawEnd(drawEnd: (canvas: Canvas, paint: Paint, x: Float, y: Float) -> Unit) {
        this.drawEnd = drawEnd
    }

}