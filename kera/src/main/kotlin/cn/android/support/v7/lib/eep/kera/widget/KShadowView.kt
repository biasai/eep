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
import cn.android.support.v7.lib.eep.kera.base.KView
import cn.android.support.v7.lib.eep.kera.common.kpx

/**
 * 阴影圆角矩形(比KShadowRectView更好用。添加了颜色渐变)
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

    var w: Int = 0//fixme 控件的真实宽度,需要手动设置(保证阴影完整显示出来，设置lparams阴影无法完整显示。)
        get() {
            if (field > 0) {
                return field
            }
            var w = width
            if (layoutParams != null && layoutParams.width > w) {
                w = layoutParams.width
            }
            return w
        }
        set(value) {
            field = value
            requestLayout2()
        }
    var h: Int = 0//fixme 控件的真实高度,需要手动设置
        get() {
            if (field > 0) {
                return field
            }
            var h = height
            if (layoutParams != null && layoutParams.height > h) {
                h = layoutParams.height
            }
            return h
        }
        set(value) {
            field = value
            requestLayout2()
        }


    fun requestLayout2() {
        if (w > 0 && h > 0) {
            requestLayout()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (w > 0 && h > 0) {
            setMeasuredDimension((w + shadow_radius * 2 + Math.abs(shadow_dx)).toInt(), (h + shadow_radius * 2 + Math.abs(shadow_dy)).toInt())//真实的宽和高要加上阴影的宽度。和偏移量。
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }

    }

    var all_radius: Float = 0F//默认，所有圆角的角度
    var left_top: Float = 0f//左上角
    var left_bottom: Float = 0f//左下角
    var right_top = 0f//右上角
    var right_bottom = 0f//右下角
    var bg_color = Color.WHITE//矩形画布背景颜色，不能为透明，不然什么也看不见（包括阴影），也就是说画布必须有一个背景色

    var shadow_color = Color.BLACK//阴影颜色，会根据这个颜色值进行阴影渐变
    var shadow_radius = kpx.x(15f)//阴影半径，决定了阴影的长度
        set(value) {
            field = value
            requestLayout2()
        }
    var shadow_dx = kpx.x(0f)//x偏移量（阴影左右方向），0 阴影居中，小于0，阴影偏左，大于0,阴影偏右
        set(value) {
            field = value
            requestLayout2()
        }
    var shadow_dy = kpx.x(0f)//y偏移量(阴影上下方法)，0 阴影居中，小于0，阴影偏上，大于0,阴影偏下
        set(value) {
            field = value
            requestLayout2()
        }

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
            var paint = KView.getPaint()
            paint.isDither = true
            paint.isAntiAlias = true
            paint.strokeWidth = 0f
            paint.style = Paint.Style.FILL

            //注意是减去偏移量。
            var left = 0f + shadow_radius - shadow_dx
            var top = 0f + shadow_radius - shadow_dy
            var right = w + left
            var bottom = h + top

            //一般背景颜色值
            paint.color = bg_color

            //水平渐变
            horizontalColors?.let {
                var shader = LinearGradient(left, 0f, right, 0f, it, null, Shader.TileMode.MIRROR)
                paint.setShader(shader)
            }

            //fixme 垂直渐变 会覆盖水平渐变。[测试发现，渐变色对阴影也有效果，即渐变色的颜色会变成阴影的颜色。]

            //垂直渐变
            verticalColors?.let {
                var shader = LinearGradient(0f, top, 0f, bottom, it, null, Shader.TileMode.MIRROR)
                paint.setShader(shader)
            }

            //设置阴影
            paint.setShadowLayer(shadow_radius, shadow_dx, shadow_dy, shadow_color)
            // 矩形弧度
            val radian = floatArrayOf(left_top!!, left_top!!, right_top, right_top, right_bottom, right_bottom, left_bottom, left_bottom)
            // 画矩形
            var rectF = RectF(left, top, right, bottom)
            var path = Path()
            path.addRoundRect(rectF, radian, Path.Direction.CW)
            canvas.drawPath(path, paint)

            //fixme 第二个矩形，没有阴影。
            var paint2 = KView.getPaint()
            paint2.isDither = true
            paint2.isAntiAlias = true
            paint2.strokeWidth = 0f
            paint2.style = Paint.Style.FILL
            //paint2.color = Color.WHITE
            if (bg_color2 != Color.TRANSPARENT) {
                paint2.color = bg_color2
                canvas.drawPath(path, paint2)
            }

            //水平渐变
            horizontalColors2?.let {
                var shader = LinearGradient(left, 0f, right, 0f, it, null, Shader.TileMode.MIRROR)
                paint2.setShader(shader)
                canvas.drawPath(path, paint2)
            }

            //垂直渐变
            verticalColors2?.let {
                var shader = LinearGradient(0f, top, 0f, bottom, it, null, Shader.TileMode.MIRROR)
                paint2.setShader(shader)
                canvas.drawPath(path, paint2)
            }

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

    //fixme 水平渐变颜色数组值【均匀渐变】，[测试发现，渐变色对阴影也有效果]
    var horizontalColors: IntArray? = null

    open fun horizontalColors(vararg color: Int) {
        horizontalColors = color
    }

    open fun horizontalColors(vararg color: String) {
        horizontalColors = IntArray(color.size)
        horizontalColors?.apply {
            if (color.size > 1) {
                for (i in 0..color.size - 1) {
                    this[i] = Color.parseColor(color[i])
                }
            } else {
                this[0] = Color.parseColor(color[0])
            }
        }

    }

    //fixme 垂直渐变颜色数组值【均匀】
    var verticalColors: IntArray? = null

    open fun verticalColors(vararg color: Int) {
        verticalColors = color
    }

    //fixme 如：verticalColors("#00dedede","#dedede") 向上的阴影线
    open fun verticalColors(vararg color: String) {
        verticalColors = IntArray(color.size)
        verticalColors?.apply {
            if (color.size > 1) {
                for (i in 0..color.size - 1) {
                    this[i] = Color.parseColor(color[i])
                }
            } else {
                this[0] = Color.parseColor(color[0])
            }
        }

    }


    //fixme 以下的颜色，是用来画第二个矩形的。这个矩形会重叠完全覆盖在第一个矩形上面。但是这个矩形没有阴影。

    var bg_color2 = Color.TRANSPARENT//矩形画布背景颜色

    //fixme 水平渐变颜色数组值【均匀渐变】
    var horizontalColors2: IntArray? = null

    open fun horizontalColors2(vararg color: Int) {
        horizontalColors2 = color
    }

    open fun horizontalColors2(vararg color: String) {
        horizontalColors2 = IntArray(color.size)
        horizontalColors2?.apply {
            if (color.size > 1) {
                for (i in 0..color.size - 1) {
                    this[i] = Color.parseColor(color[i])
                }
            } else {
                this[0] = Color.parseColor(color[0])
            }
        }

    }

    //fixme 垂直渐变颜色数组值【均匀】
    var verticalColors2: IntArray? = null

    open fun verticalColors2(vararg color: Int) {
        verticalColors2 = color
    }

    //fixme 如：verticalColors("#00dedede","#dedede") 向上的阴影线
    open fun verticalColors2(vararg color: String) {
        verticalColors2 = IntArray(color.size)
        verticalColors2?.apply {
            if (color.size > 1) {
                for (i in 0..color.size - 1) {
                    this[i] = Color.parseColor(color[i])
                }
            } else {
                this[0] = Color.parseColor(color[0])
            }
        }

    }

}