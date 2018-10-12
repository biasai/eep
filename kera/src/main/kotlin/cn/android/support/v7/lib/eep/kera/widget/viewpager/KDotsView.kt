package cn.android.support.v7.lib.eep.kera.widget.viewpager

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import cn.android.support.v7.lib.eep.kera.utils.KProportionUtils

/**
 * 自定义小圆点
 */
open class KDotsView: View {

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {}

    var paint: Paint

    init {
        paint = Paint()
        paint.isDither = true
        paint.isAntiAlias = true
        paint.style=Paint.Style.FILL_AND_STROKE
    }

    //默认圆点颜色
    var defaultColor = Color.parseColor("#9FA1A0")

    fun defaultColor(defaultColor: Int = this.defaultColor): KDotsView {
        this.defaultColor = defaultColor
        return this
    }

    //选择圆点颜色
    var selectColor = Color.parseColor("#7160C6")

    fun selectColor(selectColor: Int = this.selectColor): KDotsView {
        this.selectColor = selectColor
        return this
    }

    var radius = KProportionUtils.getInstance().adapterInt(15 / 2)//圆点半径
    fun radius(radius: Int = this.radius): KDotsView {
        this.radius = radius
        return this
    }

    var offset = KProportionUtils.getInstance().adapterInt(30)//圆点之间的间隙
    fun offset(offset: Int = this.offset): KDotsView {
        this.offset = offset
        return this
    }

    var count = 3//圆点个数
    fun count(count: Int = this.count): KDotsView {
        this.count = count
        return this
    }

    private var selectPosition = 0//选中下标
    fun selectPosition(selectPosition: Int=this.selectPosition): KDotsView {
        this.selectPosition = selectPosition
        invalidate()
        return this
    }

    override fun draw(canvas: Canvas?) {
        super.draw(canvas)
        canvas?.run {
            var w=radius*count+offset*(count-1)//圆点+间隙总长度
            var x=(width-w)/2+radius//第一个圆点的x坐标
            var y=height/2
            for (i in 1..count) {
                if (i == selectPosition+1) {
                    paint.color = selectColor
                } else {
                    paint.color = defaultColor
                }
                drawCircle(x.toFloat(),y.toFloat(),radius.toFloat(),paint)
                x+=radius+offset+radius
            }
        }
        canvas?.let {
            draw?.let {
                var paint = Paint()
                paint.isAntiAlias = true
                paint.isDither = true
                paint.style=Paint.Style.FILL_AND_STROKE
                paint.strokeWidth=0f
                it(canvas, paint)
            }
        }
    }
    //自定义画布，根据需求。自主实现
    open var draw: ((canvas: Canvas, paint: Paint) -> Unit)? = null

    //自定义，重新绘图
    open fun draw(draw: ((canvas: Canvas, paint: Paint) -> Unit)? = null): KDotsView {
        this.draw = draw
        postInvalidate()//刷新
        return this
    }

    //画自己【onDraw在draw()的流程里面，即在它的前面执行】
    var onDraw: ((canvas: Canvas, paint: Paint) -> Unit)? = null

    //画自己
    fun onDraw_(onDraw: ((canvas: Canvas, paint: Paint) -> Unit)? = null): KDotsView {
        this.onDraw = onDraw
        postInvalidate()//刷新
        return this
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.let {
            onDraw?.let {
                var paint = Paint()
                paint.isAntiAlias = true
                paint.isDither = true
                paint.style=Paint.Style.FILL_AND_STROKE
                paint.strokeWidth=0f
                it(canvas, paint)
            }
        }
    }

}