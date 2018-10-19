package cn.android.support.v7.lib.eep.kera.widget

import android.content.Context
import android.graphics.*
import android.os.Build
import android.support.annotation.RequiresApi
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import cn.android.support.v7.lib.eep.kera.R
import cn.android.support.v7.lib.eep.kera.common.kpx

/**
 * 阴影矩形
 * Created by 彭治铭 on 2018/7/1.
 */
open class KShadowView : View {

    constructor(viewGroup: ViewGroup) : super(viewGroup.context) {
        setLayerType(View.LAYER_TYPE_SOFTWARE, null)//必须关闭硬件加速，不支持
        viewGroup.addView(this)//直接添加进去,省去addView(view)
    }

    constructor(viewGroup: ViewGroup, HARDWARE: Boolean) : super(viewGroup.context) {
        if (HARDWARE) {
            setLayerType(View.LAYER_TYPE_HARDWARE, null)
        } else {
            setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        }
        viewGroup.addView(this)//直接添加进去,省去addView(view)
    }

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        val typedArray = context?.obtainStyledAttributes(attrs, R.styleable.RoundCornersRect)
        typedArray?.let {
            var all_radius = typedArray?.getDimension(R.styleable.RoundCornersRect_radian_all, 0f)
            left_top = typedArray?.getDimension(R.styleable.RoundCornersRect_radian_left_top, all_radius)
            left_bottom = typedArray?.getDimension(R.styleable.RoundCornersRect_radian_left_bottom, all_radius)
            right_top = typedArray?.getDimension(R.styleable.RoundCornersRect_radian_right_top, all_radius)
            right_bottom = typedArray?.getDimension(R.styleable.RoundCornersRect_radian_right_bottom, all_radius)
        }
    }

    init {
        setLayerType(View.LAYER_TYPE_SOFTWARE, null)//必须关闭硬件加速，不支持
    }


    var all_radius: Float = 0F//默认，所有圆角的角度
    var left_top: Float = 0f//左上角
    var left_bottom: Float = 0f//左下角
    var right_top = 0f//右上角
    var right_bottom = 0f//右下角
    var bg_color = Color.WHITE//矩形画布背景颜色，不能为透明，不然什么也看不见（包括阴影），也就是说画布必须有一个背景色

    var shadow_color = Color.BLACK//阴影颜色，会根据这个颜色值进行阴影渐变
    var shadow_radius = kpx.x(15f)//阴影半径，决定了阴影的长度
    var shadow_dx = kpx.x(0f)//x偏移量（阴影左右方向），0 阴影居中，小于0，阴影偏左，大于0,阴影偏右
    var shadow_dy = kpx.x(0f)//y偏移量(阴影上下方法)，0 阴影居中，小于0，阴影偏上，大于0,阴影偏下

    override fun draw(canvas: Canvas?) {
        super.draw(canvas)
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
            var paint = Paint()
            paint.isDither = true
            paint.isAntiAlias = true
            paint.strokeWidth = 100f
            paint.style = Paint.Style.STROKE
            paint.color = bg_color
            paint.setShadowLayer(shadow_radius, shadow_dx, shadow_dy, shadow_color)
            // 矩形弧度
            val radian = floatArrayOf(left_top!!, left_top!!, right_top, right_top, right_bottom, right_bottom, left_bottom, left_bottom)
            // 画矩形
            var p = 1.39f//防止阴影显示不全
            var dx = Math.abs(shadow_dx)
            var dy = Math.abs(shadow_dy)
            var rectF = RectF(0f + shadow_radius * p + dx, 0f + shadow_radius * p + dy, width.toFloat() - shadow_radius * p - dx, height.toFloat() - shadow_radius * p - dy)
            var path = Path()
            path.addRoundRect(rectF, radian, Path.Direction.CW)
            canvas.drawPath(path, paint)
        }
        canvas?.let {
            draw?.let {
                var paint = Paint()
                paint.isAntiAlias = true
                paint.isDither = true
                paint.style = Paint.Style.FILL_AND_STROKE
                paint.strokeWidth = 0f
                it(canvas, paint)
            }
        }
    }

    //自定义画布，根据需求。自主实现
    open var draw: ((canvas: Canvas, paint: Paint) -> Unit)? = null

    //自定义，重新绘图
    open fun draw(draw: ((canvas: Canvas, paint: Paint) -> Unit)? = null): KShadowView {
        this.draw = draw
        postInvalidate()//刷新
        return this
    }

    //画自己【onDraw在draw()的流程里面，即在它的前面执行】
    var onDraw: ((canvas: Canvas, paint: Paint) -> Unit)? = null

    //画自己
    fun onDraw_(onDraw: ((canvas: Canvas, paint: Paint) -> Unit)? = null): KShadowView {
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
                paint.style = Paint.Style.FILL_AND_STROKE
                paint.strokeWidth = 0f
                it(canvas, paint)
            }
        }
    }

}