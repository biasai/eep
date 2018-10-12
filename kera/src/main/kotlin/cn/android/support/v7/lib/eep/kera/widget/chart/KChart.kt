package cn.android.support.v7.lib.eep.kera.widget.chart

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import cn.android.support.v7.lib.eep.kera.base.KView

/**
 * 宽高，位置。必须和X轴，Y轴一致。
 */
open abstract class KChart : KView {
    //关闭硬件加速。不然在部分手机，如小米。线条与线条之间的连接处有锯齿。
    constructor(context: Context?) : super(context, false) {}

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        setLayerType(View.LAYER_TYPE_SOFTWARE, null)
    }

    var xAxis: KXAxis? = null
        set(value) {
            field = value
            data?.let {
                field?.data(data)
            }
        }
    var yAxis: KYAxis? = null
        set(value) {
            field = value
            data?.let {
                field?.data(data)
            }
        }
    //设置数据时，X轴，Y轴的数据同时绑定。
    var data: KData? = null
        set(value) {
            field = value
            field?.let {
                xAxis?.let {
                    it.data(field)
                }
                yAxis?.let {
                    it.data(field)
                }
            }
        }
    //获取数据开始下标。
    var startPosition: Float = 0f
        get() = startPosition()

    open fun startPosition(): Float {
        return data!!.positionStart
    }

    //获取数据结束下标。
    var endPosition: Float = 0f
        get() = endPosition()

    open fun endPosition(): Float {
        return data!!.positionEnd
    }

    //X轴开始下标到结束下标的长度
    var length: Float = 0f
        get() {
            return xAxis!!.getUnitX(endPosition) - xAxis!!.getUnitX(startPosition)
        }

    open fun setAxis(xAxis: KXAxis, yAxis: KYAxis) {
        this.xAxis = xAxis
        this.yAxis = yAxis
    }

    open fun data(data: KData) {
        this.data = data
    }

    open fun setData(data: KData): KChart {
        this.data = data
        invalidate()
        return this
    }

    //设置X轴，Y轴，和数据
    open fun set(xAxis: KXAxis, yAxis: KYAxis, data: KData) {
        setAxis(xAxis, yAxis)
        data(data)
    }

    var progress = 1f//路径动画进度，范围【0~1】

    var sub_big = 1//数据比两侧大
    var sub_low = -1//数据比两侧小
    var sub_left = 2//左侧数据大小（右侧数据小）
    var sub_right = 3//右侧数据大（左侧数据小）

    /**
     * 绘制数据点文本案例
     */
    open fun drawPointText(canvas: Canvas, paint: Paint, x: Float, y: Float, text: String, sub: Int) {
        when (sub) {
            sub_big -> {
                canvas.drawText(text, x, y - paint.textSize, paint)
            }
            sub_low -> {
                canvas.drawText(text, x, y + paint.textSize * 1.8f, paint)
            }
            sub_left -> {
                paint.textAlign = Paint.Align.LEFT
                canvas.drawText(text, x + paint.textSize, getTextY(paint, y), paint)
            }
            sub_right -> {
                paint.textAlign = Paint.Align.RIGHT
                canvas.drawText(text, x - paint.textSize, getTextY(paint, y), paint)
            }
        }
    }


    /**
     * position X轴的坐标
     * value Y轴数据
     */
    open fun getStu(position: Float, value: Float): Int {
        var sub = sub_big
        data?.let {
            //var value = it.getY(position)!! * it.unitY//获取y轴数据
            if (position == startPosition && endPosition > startPosition) {
                //第一个
                var value2 = it.getY(position + 1)!! * it.unitY
                if (value < value2) {
                    sub = sub_right
                } else {
                    sub = sub_left
                }
            } else if (position == endPosition && endPosition > startPosition) {
                //最后一个
                var value2 = it.getY(position - 1)!! * it.unitY
                if (value < value2) {
                    sub = sub_left
                } else {
                    sub = sub_right
                }
            } else if (position > startPosition && position < endPosition) {
                //中间数据
                var value2 = it.getY(position - 1)!! * it.unitY//获取y轴数据
                var p = it.getY(position + 1)
                p?.let {
                    var value3 = it * data!!.unitY//获取y轴数据
                    if (value < value2 && value < value3) {
                        sub = sub_low
                    } else if (value < value2 && value > value3) {
                        sub = sub_left
                    } else if (value > value2 && value < value3) {
                        sub = sub_right
                    }
                }

            }
        }
        return sub
    }

    //画原点，返回X轴，Y轴的交叉点。,一般都是X轴和Y轴的原点
    private var drawOrigin: ((canvas: Canvas, paint: Paint, x: Float, y: Float) -> Unit)? = null

    open fun drawOrigin(drawOrigin: (canvas: Canvas, paint: Paint, x: Float, y: Float) -> Unit) {
        this.drawOrigin = drawOrigin
    }

    override fun draw2(canvas: Canvas, paint: Paint) {
        super.draw2(canvas, paint)
        if (xAxis != null && yAxis != null) {
            data?.let {
                draw3(canvas, paint)//交给子类取绘制
                getXY { x, y, position, value, sub ->
                    var progressLength = length * progress + xAxis!!.getUnitX(startPosition)//当前进度的长度
                    var currentLength = xAxis!!.getUnitX(position)//当前坐标点的长度
                    //只绘制当前进度的点。
                    if (progressLength >= currentLength) {
                        drawPoint?.let {
                            it(canvas, getPaint(), x, y, position, value!!, sub)//fixme 最后绘制数据点。
                        }
                    }
                }
                //最后画X轴,Y轴交叉点。
                drawOrigin?.let {
                    it(canvas, getPaint(), yAxis!!.startAndStopX, xAxis!!.startAndStopY)
                }
            }
        }
    }

    /**
     * 根据数据，循环获取所有x轴，y轴对应的坐标。
     * position X轴下标
     * value Y轴数据
     */
    open fun getXY(xy: (x: Float, y: Float, position: Float, value: Float, sub: Int) -> Unit) {
        data?.let {
            it.getXY { xPosition, yPosition ->
                var x = xAxis!!.getUnitX(xPosition)
                var y = yAxis!!.getUnitY(yPosition)
                var value = it.getY(xPosition)!! * it.unitY//获取y轴数据
                var sub = getStu(xPosition, value)
                xy(x, y, xPosition, value, sub)
            }
        }
    }

    /**
     * 获取第一个和最后一个坐标
     */
    open fun getStartAndEndXY(xy: (startX: Float, startY: Float, endX: Float, endY: Float) -> Unit) {
        data?.let {
            var startX = 0f
            var startY = 0f
            var endX = 0f
            var endY = 0f
            it.getXY { xPosition, yPosition ->
                if (xPosition == startPosition) {
                    startX = xAxis!!.getUnitX(xPosition)
                    startY = yAxis!!.getUnitY(yPosition)
                } else if (xPosition == endPosition) {
                    endX = xAxis!!.getUnitX(xPosition)
                    endY = yAxis!!.getUnitY(yPosition)
                }
            }
            xy(startX, startY, endX, endY)
        }
    }

    /**
     * 获取最高点和最低点
     */
    open fun getMinAndMaxXY(xy: (minX: Float, minY: Float, maxX: Float, maxY: Float) -> Unit) {
        data?.let {
            var minX = 0f
            var minY = 0f
            var maxX = 0f
            var maxY = 0f
            it.getXY { xPosition, yPosition ->
                var x = xAxis!!.getUnitX(xPosition)
                var y = yAxis!!.getUnitY(yPosition)
                if (xPosition == startPosition) {
                    minX = x
                    minY = y
                    maxX = x
                    maxY = y
                } else {
                    //求最大值(方向，从下往上。越往上，坐标越小，实际越大。)
                    if (y < maxY) {
                        maxY = y
                        maxX = x
                    }
                    //求最小值
                    if (y > minY) {
                        minY = y
                        minX = x
                    }
                }
            }
            xy(minX, minY, maxX, maxY)
        }
    }

    abstract fun draw3(canvas: Canvas, paint: Paint)
    var drawPoint: ((canvas: Canvas, paint: Paint, x: Float, y: Float, position: Float, value: Float, sub: Int) -> Unit)? = null
    //循环绘制出每个数据点。交给用户自主去实现。
    //position是X轴坐标，value是Y轴数据
    //sub 就两个值 1,比周围两边的数据大。-1比周围两边的数据小。
    open fun drawPoint(drawPoint: (canvas: Canvas, paint: Paint, x: Float, y: Float, position: Float, value: Float, sub: Int) -> Unit) {
        this.drawPoint = drawPoint
    }

}