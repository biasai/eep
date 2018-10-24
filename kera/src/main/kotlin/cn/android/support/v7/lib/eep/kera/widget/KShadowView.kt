package cn.android.support.v7.lib.eep.kera.widget

import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.text.TextUtils
import android.util.AttributeSet
import android.view.*
import android.widget.TextView
import cn.android.support.v7.lib.eep.kera.R
import cn.android.support.v7.lib.eep.kera.base.KView
import cn.android.support.v7.lib.eep.kera.common.kpx
import cn.android.support.v7.lib.eep.kera.utils.KAssetsUtils
import cn.android.support.v7.lib.eep.kera.utils.KSelectorUtils
import org.jetbrains.anko.*

/**
 * 阴影圆角矩形(比KShadowRectView更好用。添加了颜色渐变)
 * shadow_color，bg_color，horizontalColors，verticalColors 添加了一般，触摸和选中三种状态。
 *
 * Created by 彭治铭 on 2018/7/1.
 */
open class KShadowView : TextView {

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
        KView.typeface?.let {
            if (KView.isGlobal) {
                typeface = it//fixme 设置全局自定义字体
            }
        }
        textSize = kpx.textSizeX(30f)
        textColor = Color.parseColor("#181818")
        hintTextColor = Color.parseColor("#9b9b9b")
        gravity = Gravity.CENTER//居中
        isClickable = true//局部点击能力
    }

    /**
     * fixme 更多（显示不全时）显示三个点...
     * fixme 【设置了显示更多，文本垂直居中就无效了。始终与顶部对齐,但是可以使用topPadding控制文本垂直位置。】
     * lines 显示的最大行数。
     */
    fun setMore(lines: Int = 1) {
        //能水平滚动较长的文本内容。不要用这个。圆角会没有效果的。就是这个搞的圆角没有效果。
        //setHorizontallyScrolling(true)
        //setSingleLine(true)//是否單行顯示。过时了。也会导致圆角没有效果。
        //fixme 上面两个属性导致圆角无效。不要使用。TextView,editText,button都会导致圆角无效。

        setMaxLines(lines);//fixme 显示最大行,这个也是关键。setMaxLines和setEllipsize同时设置，才会显示更多。
        //代码不换行，更多显示三个点...
        setEllipsize(TextUtils.TruncateAt.END)//fixme 这个才是关键，会显示更多
    }

    //fixme 清空原始背景
    fun clearOriBackground() {
        if (Build.VERSION.SDK_INT >= 16) {
            backgroundColor = Color.TRANSPARENT
            background = null
        } else {
            backgroundColor = Color.TRANSPARENT
            backgroundDrawable = null
        }
    }

    //清除背景
    open fun clearBackground() {
        clearOriBackground()
    }

    open fun background(mcolor: String) {
        setBackgroundColor(Color.parseColor(mcolor))
    }

    open fun background(resId: Int) {
        setBackgroundResource(resId)
    }

    open fun background(bitmap: Bitmap) {
        if (Build.VERSION.SDK_INT >= 16) {
            background = BitmapDrawable(bitmap)
        } else {
            backgroundDrawable = BitmapDrawable(bitmap)
        }
    }

    // 两次点击按钮之间的点击间隔不能少于1000毫秒（即1秒）
    var MIN_CLICK_DELAY_TIME = 1000
    var lastClickTime: Long = System.currentTimeMillis()//记录最后一次点击时间

    //判断是否快速点击，true是快速点击，false不是
    open fun isFastClick(): Boolean {
        var flag = false
        var curClickTime = System.currentTimeMillis()
        if ((curClickTime - lastClickTime) <= MIN_CLICK_DELAY_TIME) {
            flag = true//快速点击
        }
        lastClickTime = curClickTime
        return flag
    }

    private var onClick: (() -> Unit)? = null
    private var onClickes = mutableListOf<() -> Unit>()
    private var hasClick = false//判断是否已经添加了点击事情。
    //fixme 自定义点击事件，可以添加多个点击事情。互不影响
    open fun onClick(onClick: () -> Unit) {
        if (!hasClick) {
            isClickable = true//设置具备点击能力
            //点击事件
            setOnClickListener {
                //fixme 防止快速点击
                if (!isFastClick()) {
                    for (i in onClickes) {
                        i?.let {
                            it()//点击事件
                        }
                    }
                }
            }
            hasClick = true
        }
        onClickes.add(onClick)
    }

    var bindView: View? = null//状态绑定的View
        set(value) {
            field = value
            if (value != null) {
                if (value is KView) {
                    if (value.bindView == null) {
                        value.bindView = this//相互绑定
                    }
                } else if (value is KRadiusTextView) {
                    if (value.bindView == null) {
                        value.bindView = this//相互绑定
                    }
                } else if (value is KRadiusRelativeLayout) {
                    if (value.bindView == null) {
                        value.bindView = this//相互绑定
                    }
                }
            }
        }

    fun bindView(bindView: View?) {
        this.bindView = bindView
    }

    //状态同步
    fun bindSycn() {
        bindView?.let {
            it.isSelected = isSelected
            it.isPressed = isPressed
        }
    }

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        var b = super.dispatchTouchEvent(event)
        //防止点击事件冲突。所以。一定要放到super()后面。
        event?.let {
            when (it.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN, MotionEvent.ACTION_MOVE -> {
                    bindView?.isPressed = true//按下状态
                    isPressed = true
                    invalidate()
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                    bindView?.isPressed = false
                    isPressed = false
                    invalidate()
                }
                MotionEvent.ACTION_CANCEL -> {
                    //其他异常
                    bindView?.isPressed = false
                    invalidate()
                }
            }
        }
        return b
    }

    //字体颜色
    fun selectorTextColor(NormalColor: Int, PressColor: Int?, SelectColor: Int? = PressColor) {
        KSelectorUtils.selectorTextColor(this, NormalColor, PressColor, SelectColor)
    }

    fun selectorTextColor(NormalColor: String, PressColor: String?, SelectColor: String? = PressColor) {
        KSelectorUtils.selectorTextColor(this, NormalColor, PressColor, SelectColor)
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
    var bg_colorPress = Color.TRANSPARENT//按下
    var bg_colorSelect = Color.TRANSPARENT//选中

    var shadow_color = Color.BLACK//阴影颜色，会根据这个颜色值进行阴影渐变
    var shadow_colorPress = Color.TRANSPARENT//按下
    var shadow_colorSelect = Color.TRANSPARENT//选中
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
            //paint.color = bg_color
            if (isSelected && bg_colorSelect != Color.TRANSPARENT) {
                //选中
                paint.color = bg_colorSelect
            } else if (isPressed && bg_colorPress != Color.TRANSPARENT) {
                //按下
                paint.color = bg_colorPress
            } else if (bg_color2 != Color.TRANSPARENT) {
                //一般
                paint.color = bg_color
            }

            //水平渐变
//            horizontalColors?.let {
//                var shader = LinearGradient(left, 0f, right, 0f, it, null, Shader.TileMode.MIRROR)
//                paint.setShader(shader)
//            }
            if (isSelected && horizontalColorsSelect != null) {
                //选中状态
                var shader = LinearGradient(left, 0f, right, 0f, horizontalColorsSelect, null, Shader.TileMode.MIRROR)
                paint.setShader(shader)
            } else if (isPressed && horizontalColorsPress != null) {
                //按下状态
                var shader = LinearGradient(left, 0f, right, 0f, horizontalColorsPress, null, Shader.TileMode.MIRROR)
                paint.setShader(shader)
            } else if (horizontalColors != null) {
                //一般状态
                var shader = LinearGradient(left, 0f, right, 0f, horizontalColors, null, Shader.TileMode.MIRROR)
                paint.setShader(shader)
            }

            //fixme 垂直渐变 会覆盖水平渐变。[测试发现，渐变色对阴影也有效果，即渐变色的颜色会变成阴影的颜色。]

            //垂直渐变
//            verticalColors?.let {
//                var shader = LinearGradient(0f, top, 0f, bottom, it, null, Shader.TileMode.MIRROR)
//                paint.setShader(shader)
//            }
            if (isSelected && verticalColorsSelect != null) {
                //选中状态
                var shader = LinearGradient(0f, top, 0f, bottom, verticalColorsSelect, null, Shader.TileMode.MIRROR)
                paint.setShader(shader)
            } else if (isPressed && verticalColorsPress != null) {
                //按下状态
                var shader = LinearGradient(0f, top, 0f, bottom, verticalColorsPress, null, Shader.TileMode.MIRROR)
                paint.setShader(shader)
            } else if (verticalColors != null) {
                //一般状态
                var shader = LinearGradient(0f, top, 0f, bottom, verticalColors, null, Shader.TileMode.MIRROR)
                paint.setShader(shader)
            }

            //设置阴影
            //paint.setShadowLayer(shadow_radius, shadow_dx, shadow_dy, shadow_color)
            if (isSelected && shadow_colorSelect != Color.TRANSPARENT) {
                //选中状态
                paint.setShadowLayer(shadow_radius, shadow_dx, shadow_dy, shadow_colorSelect)
            } else if (isPressed && shadow_colorPress != Color.TRANSPARENT) {
                //按下状态
                paint.setShadowLayer(shadow_radius, shadow_dx, shadow_dy, shadow_colorPress)
            } else if (shadow_color != Color.TRANSPARENT) {
                //一般状态
                paint.setShadowLayer(shadow_radius, shadow_dx, shadow_dy, shadow_color)
            }
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
            if (isSelected && bg_color2Select != Color.TRANSPARENT) {
                //选中
                paint2.color = bg_color2Select
                canvas.drawPath(path, paint2)
            } else if (isPressed && bg_color2Press != Color.TRANSPARENT) {
                //按下
                paint2.color = bg_color2Press
                canvas.drawPath(path, paint2)
            } else if (bg_color2 != Color.TRANSPARENT) {
                //一般
                paint2.color = bg_color2
                canvas.drawPath(path, paint2)
            }

            //水平渐变
            if (isSelected && horizontalColors2Select != null) {
                //选中状态
                var shader = LinearGradient(left, 0f, right, 0f, horizontalColors2Select, null, Shader.TileMode.MIRROR)
                paint2.setShader(shader)
                canvas.drawPath(path, paint2)
            } else if (isPressed && horizontalColors2Press != null) {
                //按下状态
                var shader = LinearGradient(left, 0f, right, 0f, horizontalColors2Press, null, Shader.TileMode.MIRROR)
                paint2.setShader(shader)
                canvas.drawPath(path, paint2)
            } else if (horizontalColors2 != null) {
                //一般状态
                var shader = LinearGradient(left, 0f, right, 0f, horizontalColors2, null, Shader.TileMode.MIRROR)
                paint2.setShader(shader)
                canvas.drawPath(path, paint2)
            }

            //垂直渐变
            if (isSelected && verticalColors2Select != null) {
                //选中状态
                var shader = LinearGradient(0f, top, 0f, bottom, verticalColors2Select, null, Shader.TileMode.MIRROR)
                paint2.setShader(shader)
                canvas.drawPath(path, paint2)
            } else if (isPressed && verticalColors2Press != null) {
                //按下状态
                var shader = LinearGradient(0f, top, 0f, bottom, verticalColors2Press, null, Shader.TileMode.MIRROR)
                paint2.setShader(shader)
                canvas.drawPath(path, paint2)
            } else if (verticalColors2 != null) {
                //一般状态
                var shader = LinearGradient(0f, top, 0f, bottom, verticalColors2, null, Shader.TileMode.MIRROR)
                paint2.setShader(shader)
                canvas.drawPath(path, paint2)
            }

        }
        //画自定义背景(在super的后面，不然会遮挡文字)
        canvas?.let {
            drawAutoBg(it)//画自定义背景位图
        }
        super.draw(canvas)//fixme 防止字体被遮挡
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

    //fixme 水平渐变颜色数组值【均匀渐变】，按下
    var horizontalColorsPress: IntArray? = null

    open fun horizontalColorsPress(vararg color: Int) {
        horizontalColorsPress = color
    }

    open fun horizontalColorsPress(vararg color: String) {
        horizontalColorsPress = IntArray(color.size)
        horizontalColorsPress?.apply {
            if (color.size > 1) {
                for (i in 0..color.size - 1) {
                    this[i] = Color.parseColor(color[i])
                }
            } else {
                this[0] = Color.parseColor(color[0])
            }
        }

    }

    //fixme 水平渐变颜色数组值【均匀渐变】，选中
    var horizontalColorsSelect: IntArray? = null

    open fun horizontalColorsSelect(vararg color: Int) {
        horizontalColorsSelect = color
    }

    open fun horizontalColorsSelect(vararg color: String) {
        horizontalColorsSelect = IntArray(color.size)
        horizontalColorsSelect?.apply {
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

    //fixme 垂直渐变颜色数组值【均匀】,按下
    var verticalColorsPress: IntArray? = null

    open fun verticalColorsPress(vararg color: Int) {
        verticalColorsPress = color
    }

    //fixme 如：verticalColors("#00dedede","#dedede") 向上的阴影线
    open fun verticalColorsPress(vararg color: String) {
        verticalColorsPress = IntArray(color.size)
        verticalColorsPress?.apply {
            if (color.size > 1) {
                for (i in 0..color.size - 1) {
                    this[i] = Color.parseColor(color[i])
                }
            } else {
                this[0] = Color.parseColor(color[0])
            }
        }
    }

    //fixme 垂直渐变颜色数组值【均匀】,按下
    var verticalColorsSelect: IntArray? = null

    open fun verticalColorsSelect(vararg color: Int) {
        verticalColorsSelect = color
    }

    //fixme 如：verticalColors("#00dedede","#dedede") 向上的阴影线
    open fun verticalColorsSelect(vararg color: String) {
        verticalColorsSelect = IntArray(color.size)
        verticalColorsSelect?.apply {
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
    var bg_color2Press = Color.TRANSPARENT//矩形画布背景颜色,按下
    var bg_color2Select = Color.TRANSPARENT//矩形画布背景颜色,按下

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


    //fixme 水平渐变颜色数组值【均匀渐变,按下】
    var horizontalColors2Press: IntArray? = null

    open fun horizontalColors2Press(vararg color: Int) {
        horizontalColors2Press = color
    }

    open fun horizontalColors2Press(vararg color: String) {
        horizontalColors2Press = IntArray(color.size)
        horizontalColors2Press?.apply {
            if (color.size > 1) {
                for (i in 0..color.size - 1) {
                    this[i] = Color.parseColor(color[i])
                }
            } else {
                this[0] = Color.parseColor(color[0])
            }
        }

    }

    //fixme 水平渐变颜色数组值【均匀渐变,选中】
    var horizontalColors2Select: IntArray? = null

    open fun horizontalColors2Select(vararg color: Int) {
        horizontalColors2Select = color
    }

    open fun horizontalColors2Select(vararg color: String) {
        horizontalColors2Select = IntArray(color.size)
        horizontalColors2Select?.apply {
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

    //fixme 垂直渐变颜色数组值【均匀】按下
    var verticalColors2Press: IntArray? = null

    open fun verticalColors2Press(vararg color: Int) {
        verticalColors2Press = color
    }

    //fixme 如：verticalColors("#00dedede","#dedede") 向上的阴影线
    open fun verticalColors2Press(vararg color: String) {
        verticalColors2Press = IntArray(color.size)
        verticalColors2Press?.apply {
            if (color.size > 1) {
                for (i in 0..color.size - 1) {
                    this[i] = Color.parseColor(color[i])
                }
            } else {
                this[0] = Color.parseColor(color[0])
            }
        }
    }

    //fixme 垂直渐变颜色数组值【均匀】选中
    var verticalColors2Select: IntArray? = null

    open fun verticalColors2Select(vararg color: Int) {
        verticalColors2Select = color
    }

    //fixme 如：verticalColors("#00dedede","#dedede") 向上的阴影线
    open fun verticalColors2Select(vararg color: String) {
        verticalColors2Select = IntArray(color.size)
        verticalColors2Select?.apply {
            if (color.size > 1) {
                for (i in 0..color.size - 1) {
                    this[i] = Color.parseColor(color[i])
                }
            } else {
                this[0] = Color.parseColor(color[0])
            }
        }
    }

    /**
     * 重新自定义背景图片(为了适配正确，位图最好都放在nodpi文件夹里。)
     */
    private var autoDefaultBg: Bitmap? = null//fixme 默认图片

    fun autoDefaultBg(bitmap: Bitmap?) {
        this.autoDefaultBg = bitmap
        if (context != null && context is Activity) {
            context.runOnUiThread {
                invalidate()
            }
        }
    }

    fun autoDefaultBg(resId: Int, width: Int = 0, height: Int = 0, isRGB_565: Boolean = false) {
        autoDefaultBg = KAssetsUtils.getInstance().getBitmapFromAssets(null, resId, isRGB_565)
        autoDefaultBg?.let {
            autoDefaultBg = kpx.xBitmap(it, width, height)//自动适配
        }
    }

    fun autoDefaultBg(assetsPath: String, width: Int = 0, height: Int = 0, isRGB_565: Boolean = false) {
        autoDefaultBg = KAssetsUtils.getInstance().getBitmapFromAssets(assetsPath, 0, isRGB_565)
        autoDefaultBg?.let {
            autoDefaultBg = kpx.xBitmap(it, width, height)//自动适配
        }
    }

    private var autoPressBg: Bitmap? = null//fixme 按下图片
    fun autoPressBg(bitmap: Bitmap?) {
        this.autoPressBg = bitmap
        if (context != null && context is Activity) {
            context.runOnUiThread {
                invalidate()
            }
        }
    }

    fun autoPressBg(resId: Int, width: Int = 0, height: Int = 0, isRGB_565: Boolean = false) {
        autoPressBg = KAssetsUtils.getInstance().getBitmapFromAssets(null, resId, isRGB_565)
        autoPressBg?.let {
            autoPressBg = kpx.xBitmap(it, width, height)//自动适配
        }
        isClickable = true//具备点击能力
    }

    fun autoPressBg(assetsPath: String, width: Int = 0, height: Int = 0, isRGB_565: Boolean = false) {
        autoPressBg = KAssetsUtils.getInstance().getBitmapFromAssets(assetsPath, 0, isRGB_565)
        autoPressBg?.let {
            autoPressBg = kpx.xBitmap(it, width, height)//自动适配
        }
        isClickable = true//具备点击能力
    }

    private var autoSelectBg: Bitmap? = null//fixme 选中图片（优先级最高）
    fun autoSelectBg(bitmap: Bitmap?) {
        this.autoSelectBg = bitmap
        if (context != null && context is Activity) {
            context.runOnUiThread {
                invalidate()
            }
        }
    }

    fun autoSelectBg(resId: Int, width: Int = 0, height: Int = 0, isRGB_565: Boolean = false) {
        autoSelectBg = KAssetsUtils.getInstance().getBitmapFromAssets(null, resId, isRGB_565)
        autoSelectBg?.let {
            autoSelectBg = kpx.xBitmap(it, width, height)//自动适配
        }
        isClickable = true//具备点击能力
    }

    fun autoSelectBg(assetsPath: String, width: Int = 0, height: Int = 0, isRGB_565: Boolean = false) {
        autoSelectBg = KAssetsUtils.getInstance().getBitmapFromAssets(assetsPath, 0, isRGB_565)
        autoSelectBg?.let {
            autoSelectBg = kpx.xBitmap(it, width, height)//自动适配
        }
        isClickable = true//具备点击能力
    }

    //fixme 防止触摸状态和选中状态冲突，会出现一闪的情况。把触摸状态制空。
    //fixme autoBg(R.mipmap.p_second_gou_gay,null, R.mipmap.p_second_gou_blue)
    fun autoBg(default: Int, press: Int? = default, select: Int? = press, width: Int = 0, height: Int = 0, isRGB_565: Boolean = false) {
        autoDefaultBg(default, width, height, isRGB_565)
        if (press == default) {
            autoPressBg = autoDefaultBg
        } else {
            press?.apply {
                autoPressBg(this, width, height, isRGB_565)
                isClickable = true//具备点击能力
            }
        }
        if (press == select) {
            autoSelectBg = autoPressBg
        } else {
            select?.apply {
                autoSelectBg(this, width, height, isRGB_565)
                isClickable = true//具备点击能力
            }
        }
    }

    fun autoBg(default: String, press: String? = default, select: String? = press, width: Int = 0, height: Int = 0, isRGB_565: Boolean = false) {
        autoDefaultBg(default, width, height, isRGB_565)
        if (press == default || press.equals(default)) {
            autoPressBg = autoDefaultBg
        } else {
            press?.apply {
                autoPressBg(this, width, height, isRGB_565)
                isClickable = true//具备点击能力
            }
        }
        if (press == select || press.equals(select)) {
            autoSelectBg = autoPressBg
        } else {
            select?.apply {
                autoSelectBg(this, width, height, isRGB_565)
                isClickable = true//具备点击能力
            }
        }
    }

    //fixme 来自sd卡,普通
    fun autoDefaultBgFromFile(filePath: String, width: Int = 0, height: Int = 0, isRGB_565: Boolean = false) {
        autoDefaultBg = KAssetsUtils.getInstance().getBitmapFromFile(filePath, isRGB_565)
        autoDefaultBg?.let {
            autoDefaultBg = kpx.xBitmap(it, width, height)//自动适配
        }
        invalidate()
    }

    //fixme 来自sd卡,触摸
    fun autoPressBgFromFile(filePath: String, width: Int = 0, height: Int = 0, isRGB_565: Boolean = false) {
        autoPressBg = KAssetsUtils.getInstance().getBitmapFromFile(filePath, isRGB_565)
        autoPressBg?.let {
            autoPressBg = kpx.xBitmap(it, width, height)//自动适配
        }
        invalidate()
        isClickable = true//具备点击能力
    }

    //fixme 来自sd卡,选中
    fun autoSelectBgFromFile(filePath: String, width: Int = 0, height: Int = 0, isRGB_565: Boolean = false) {
        autoSelectBg =KAssetsUtils.getInstance().getBitmapFromFile(filePath, isRGB_565)
        autoSelectBg?.let {
            autoSelectBg = kpx.xBitmap(it, width, height)//自动适配
        }
        invalidate()
        isClickable = true//具备点击能力
    }

    //fixme 来自sd卡,普通，触摸，选中
    fun autoBgFromFile(default: String, press: String? = default, select: String? = press, width: Int = 0, height: Int = 0, isRGB_565: Boolean = false) {
        autoDefaultBgFromFile(default, width, height, isRGB_565)//fixme 普通
        if (press == default || press.equals(default)) {
            autoPressBg = autoDefaultBg
        } else {
            press?.apply {
                autoPressBgFromFile(this, width, height, isRGB_565)//fixme 触摸
                isClickable = true//具备点击能力
            }
        }
        if (press == select || press.equals(select)) {
            autoSelectBg = autoPressBg
        } else {
            select?.apply {
                autoSelectBgFromFile(this, width, height, isRGB_565)//fixme 选中
                isClickable = true//具备点击能力
            }
        }
    }

    var autoLeftPadding = 0f//左补丁(负数也有效哦)
    var autoTopPadding = 0f//上补丁
    var isAutoCenter = true//位图是否居中,默认居中（水平+垂直居中）
        set(value) {
            field = value
            if (field) {
                isAutoCenterHorizontal = false
                isAutoCenterVertical = false
            }
        }
    var isAutoCenterHorizontal = false//水平居中
        set(value) {
            field = value
            if (field) {
                isAutoCenter = false
                isAutoCenterVertical = false
            }
        }
    var isAutoCenterVertical = false//垂直居中
        set(value) {
            field = value
            if (field) {
                isAutoCenter = false
                isAutoCenterHorizontal = false
            }
        }


    //画自定义背景
    open fun drawAutoBg(canvas: Canvas) {
        if (w <= 0 || h <= 0) {
            return
        }
        var paint = KView.getPaint()
        //Log.e("test", "isSelected:\t" + isSelected + "\tisPress：\t" + isPressed)
        if (isSelected && autoSelectBg != null) {
            //选中状态图片,优先级最高
            autoSelectBg?.apply {
                if (!isRecycled) {
                    if (isAutoCenter) {
                        canvas.drawBitmap(this, kpx.centerBitmapX(this, w.toFloat()) + autoLeftPadding, kpx.centerBitmapY(this, h.toFloat()) + autoTopPadding, paint)
                    } else if (isAutoCenterHorizontal) {
                        canvas.drawBitmap(this, kpx.centerBitmapX(this, w.toFloat()) + autoLeftPadding, autoTopPadding, paint)
                    } else if (isAutoCenterVertical) {
                        canvas.drawBitmap(this, autoLeftPadding, kpx.centerBitmapY(this, h.toFloat()) + autoTopPadding, paint)
                    } else {
                        canvas.drawBitmap(this, autoLeftPadding, autoTopPadding, paint)
                    }
                }
            }
        } else {
            if (isPressed && autoPressBg != null) {
                //按下状态
                autoPressBg?.apply {
                    if (!isRecycled) {
                        if (isAutoCenter) {
                            canvas.drawBitmap(this, kpx.centerBitmapX(this, w.toFloat()) + autoLeftPadding, kpx.centerBitmapY(this, h.toFloat()) + autoTopPadding, paint)
                        } else if (isAutoCenterHorizontal) {
                            canvas.drawBitmap(this, kpx.centerBitmapX(this, w.toFloat()) + autoLeftPadding, autoTopPadding, paint)
                        } else if (isAutoCenterVertical) {
                            canvas.drawBitmap(this, autoLeftPadding, kpx.centerBitmapY(this, h.toFloat()) + autoTopPadding, paint)
                        } else {
                            canvas.drawBitmap(this, autoLeftPadding, autoTopPadding, paint)
                        }
                    }
                }
            } else {
                //普通状态
                autoDefaultBg?.apply {
                    if (!isRecycled) {
                        if (isAutoCenter) {
                            canvas.drawBitmap(this, kpx.centerBitmapX(this, w.toFloat()) + autoLeftPadding, kpx.centerBitmapY(this, h.toFloat()) + autoTopPadding, paint)
                        } else if (isAutoCenterHorizontal) {
                            canvas.drawBitmap(this, kpx.centerBitmapX(this, w.toFloat()) + autoLeftPadding, autoTopPadding, paint)
                        } else if (isAutoCenterVertical) {
                            canvas.drawBitmap(this, autoLeftPadding, kpx.centerBitmapY(this, h.toFloat()) + autoTopPadding, paint)
                        } else {
                            canvas.drawBitmap(this, autoLeftPadding, autoTopPadding, paint)
                        }
                    }
                }
            }
        }
    }

    //释放位图
    fun recycle() {
        autoDefaultBg?.apply {
            if (!isRecycled) {
                recycle()
            }
        }
        autoDefaultBg = null
        autoPressBg?.apply {
            if (!isRecycled) {
                recycle()
            }
        }
        autoPressBg = null
        autoSelectBg?.apply {
            if (!isRecycled) {
                recycle()
            }
        }
        autoSelectBg = null
        invalidate()
        System.gc()//提醒内存回收
    }

    //属性动画
    fun ofFloat(propertyName: String, repeatCount: Int, duration: Long, vararg value: Float, AnimatorUpdateListener: ((values: Float) -> Unit)? = null): ObjectAnimator {
        var objectAnimator = KView.ofFloat(this, propertyName, repeatCount, duration, *value, AnimatorUpdateListener = AnimatorUpdateListener)
        return objectAnimator
    }

    fun ofInt(propertyName: String, repeatCount: Int, duration: Long, vararg value: Int, AnimatorUpdateListener: ((values: Int) -> Unit)? = null): ObjectAnimator {
        var objectAnimator = KView.ofInt(this, propertyName, repeatCount, duration, *value, AnimatorUpdateListener = AnimatorUpdateListener)
        return objectAnimator
    }

}