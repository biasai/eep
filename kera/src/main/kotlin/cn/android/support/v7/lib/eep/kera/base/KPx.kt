package cn.android.support.v7.lib.eep.kera.base

import android.app.Activity
import android.app.Application
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.Paint
import android.util.DisplayMetrics
import android.view.View
import android.view.WindowManager
import android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN

/**
 * Created by 彭治铭 on 2018/7/21.
 */
open class KPx {
    var statusHeight = 0//状态栏高度
    var baseWidth = 750f//基准宽
    var baseHeight = 1334f//基准高
    var horizontalProportion: Float = 0.toFloat()//真实水平比例大小
    var verticalProportion: Float = 0.toFloat()//真实垂直比例大小
    var density: Float = 0.toFloat()//当前设备dpi密度值比例，即 dpi/160 的比值
    var ignorex: Boolean = false//是否忽悠比例缩放
    var ignorey: Boolean = false//是否忽悠比例缩放
    private var realWidth = 0f//真实屏幕宽(以竖屏为标准，宽度比高度小)
    private var realHeight = 0f//真实屏幕高

    /**
     * 获取当前Activity屏幕方向，true竖屏，false横屏
     */
    fun oritation(activity: Activity): Boolean {
        if (activity.getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE || activity.getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE) {
            return false
        }
        //ActivityInfo.SCREEN_ORIENTATION_PORTRAIT 竖屏
        return true
    }

    /**
     * fixme 获取屏幕宽，isVertical true以竖屏为标准。默认是。false以横屏为标准
     */
    fun realWidth(isVertical: Boolean = true): Float {
        if (isVertical) {
            return realWidth
        } else {
            return realHeight
        }
    }

    /**
     * fixme 获取屏幕高，isVertical true以竖屏为标准。默认是。false以横屏为标准
     */
    fun realHeight(isVertical: Boolean = true): Float {
        if (isVertical) {
            return realHeight
        } else {
            return realWidth
        }
    }


    init {
        init()
    }

    //初始化，基准宽或高，发生变化时(以竖屏为标准)，可以手动调用，重新初始化
    //fixme 注意：以竖屏为标准，宽度比高度小(高度大于宽度)
    fun init(baseWidth: Float = 750f, baseHeight: Float = 1334f) {
        this.baseWidth = baseWidth
        this.baseHeight = baseHeight
        //真实值
        var displayMetrics: DisplayMetrics? = context()?.resources?.displayMetrics
        realWidth = displayMetrics!!.widthPixels.toFloat()
        realHeight = displayMetrics.heightPixels.toFloat()
        density = displayMetrics.density
        if (realWidth > realHeight) {
            var w = realWidth
            realWidth = realHeight
            realHeight = w
        }
        horizontalProportion = realWidth / baseWidth
        verticalProportion = realHeight / baseHeight
        //获取状态栏的高度
        statusHeight()
        ignorex()
        ignorey()
    }

    private fun ignorex() {
        //防止比例为1的时候做多余的适配
        if (horizontalProportion >= 0.999 && horizontalProportion <= 1.001) { //750/720=1.04166 苹果/安卓
            ignorex = true
        } else {
            ignorex = false
        }
    }

    private fun ignorey() {
        //防止比例为1的时候做多余的适配
        if (verticalProportion >= 0.999 && verticalProportion <= 1.001) { //1334/1280=1.04218 苹果/安卓
            ignorey = true
        } else {
            ignorey = false
        }
    }

    //以x为标准适配位图
    //isRecycle 是否释放原图
    fun xBitmap(src: Bitmap, w: Int = 0, h: Int = 0, isRecycle: Boolean = true): Bitmap {
        if (src.width == w && src.height == h) {
            return src
        }
        var width = w
        var height = h
        //如果宽度和高度小于0。就以位置自身的宽和高进行适配
        if (width <= 0 || height <= 0) {
            //以水平X轴，为标准进行适配
            width = x(src.width)
            height = x(src.height)
        }
        if (width > 0 && height > 0 && src.width != width) {
            var bm=GeomeBitmap(src, width = width.toFloat(), height = height.toFloat(),isRecycle = isRecycle)
            return bm
        }
        return src
    }

    //以y为标准适配位图
    //isRecycle 是否释放原图
    fun yBitmap(src: Bitmap, w: Int = 0, h: Int = 0, isRecycle: Boolean = true): Bitmap {
        if (src.width == w && src.height == h) {
            return src
        }
        var width = w
        var height = h
        //如果宽度和高度小于0。就以位置自身的宽和高进行适配
        if (width <= 0 || height <= 0) {
            //以水平X轴，为标准进行适配
            width = y(src.width)
            height = y(src.height)
        }
        if (width > 0 && height > 0 && src.width != width) {
            var bm=GeomeBitmap(src, width = width.toFloat(), height = height.toFloat(),isRecycle = isRecycle)
            if (isRecycle) {
                src.recycle()//原有的位图释放掉
            }
            return bm
        }
        return src
    }

    //等比压缩，压缩之后，宽和高相等。
    //参数values是压缩后的宽度及高度。
    //计算比率时，千万要注意，一定要使用float类型。千万不要使用int类型。不然计算不出。
    //这个方法，图片不会变形[取中间的那一部分]
    //isRecycle 是否释放原图
    fun GeomeBitmap(src: Bitmap, value: Float, isRecycle: Boolean = true): Bitmap {
        if (src.width == value.toInt() && src.height == value.toInt()) {
            return src//防止重复压缩
        }
        var dst: Bitmap
        if (src.width == src.height) {
            dst = Bitmap.createScaledBitmap(src, value.toInt(), value.toInt(), true)
        } else {
            //以较小边长为计算标准
            //宽小于高
            if (src.width < src.height) {
                val p = src.width.toFloat() / value
                val heith = src.height.toFloat() / p
                dst = Bitmap.createScaledBitmap(src, value.toInt(), heith.toInt(), true)
                val y = (dst.height - dst.width) / 2
                dst = Bitmap.createBitmap(dst, 0, y, dst.width, dst.width)
            } else {
                //宽大于高，或等于高。
                //高小于宽
                val p = src.height.toFloat() / value
                val width = src.width.toFloat() / p
                dst = Bitmap.createScaledBitmap(src, width.toInt(), value.toInt(), true)
                val x = (dst.width - dst.height) / 2
                dst = Bitmap.createBitmap(dst, x, 0, dst.height, dst.height)
            }
        }
        if (isRecycle) {
            if (src != null && !src.isRecycled) {
                src.recycle()
            }
        }
        return dst
    }

    /**
     * 不要在适配器里。对图片进行压缩。适配器反反复复的执行。多次执行。会内存溢出的。切记。
     *
     *
     * 根据宽和宽的比率压缩Bitmap
     * 这个方法，图片不会变形（按比例缩放）[取中间的那一部分]
     *
     * @param src   原图
     * @param width 压缩后的宽
     * @param height 压缩后的高（实际的高，是根据宽和宽的比例算出来的。按比例计算出来的。）
     * @param isRecycle 是否释放原图
     * @return
     */
    fun GeomeBitmap(src: Bitmap, width: Float, height: Float, isRecycle: Boolean = true): Bitmap {
        if (src.width == width.toInt() && src.height == height.toInt()) {
            return src//防止重复压缩
        }
        if (width == height) {
            //宽和高相等,等比压缩
            return GeomeBitmap(src, width)
        } else {
            var sp = height / width//高的比率
            var dst: Bitmap? = null
            val dp = src.height.toFloat() / src.width.toFloat()
            val pp = Math.abs(sp - dp)
            if (pp < 0.01) {
                //fixme Bitmap.createScaledBitmap 如果缩放位图和原有位图大小差异在1%之内，使用的还是同一个位图对象。
                //fixme 大小差异超过1%左右，使用的就是新的位图，和原位图就没有关系了。
                //fixme 要求的宽和高与位图的宽和高，比例一致。
                dst = Bitmap.createScaledBitmap(src, width.toInt(), (width * sp).toInt(), true)
            } else {
                //fixme 要求的宽和高与位图的宽和高，比例不一致。按比例要求，居中截取。
                val p = src.width.toFloat() / width
                val heith = src.height.toFloat() / p
                dst = Bitmap.createScaledBitmap(src, width.toInt(), heith.toInt(), true)
                val height = (width * sp).toInt()
                //要求高，小于压缩后的高。对压缩后的高进行截取
                if (height < dst!!.height) {
                    val y = (dst.height - height) / 2
                    dst = Bitmap.createBitmap(dst, 0, y, dst.width, height)
                }
            }
            if (isRecycle) {
                if (src != null && !src.isRecycled) {
                    src.recycle()
                }
            }
            return dst
        }
    }

    /**
     * fixme 适配x值(默认全屏)，以竖屏为标准。
     */
    fun x(x: Int = baseWidth.toInt()): Int {
        return x(x.toFloat()).toInt()
    }

    //Int类型已经添加了默认参数，Float就不能添加默认参数了。不然无法识别
    fun x(x: Float): Float {
        if (ignorex) {
            return x
        }
        return x * horizontalProportion
    }

    /**
     * fixme 适配y值，始终以竖屏为标准。
     */
    fun y(y: Int = baseHeight.toInt()): Int {
        return y(y.toFloat()).toInt()
    }

    fun y(y: Float): Float {
        if (ignorey) {
            return y;
        }
        return y * verticalProportion
    }

    /**
     * fixme 设置文字大小。以X为标准
     */
    fun textSizeX(x: Float): Float {
        return pixelToDp(x(x))//textView.setTextSize单位是dp,且是float类型。设置文字大小。
    }

    fun textSizeX(x: Int): Float {
        return pixelToDp(x(x.toFloat()))
    }

    /**
     * fixme 设置文字大小。以Y为标准
     */
    fun textSizeY(y: Float): Float {
        return pixelToDp(y(y))//textView.setTextSize单位是dp,且是float类型。设置文字大小。
    }

    fun textSizeY(y: Int): Float {
        return pixelToDp(y(y.toFloat()))
    }

    //与屏幕边缘左边的距离
    fun left(view: View): Int {
        //获取现对于整个屏幕的位置。
        val location = IntArray(2)
        view.getLocationOnScreen(location)
        return location[0]
    }

    //与屏幕边缘右边的距离
    fun right(view: View): Int {
        val location = IntArray(2)
        view.getLocationOnScreen(location)
        return (realWidth - location[0] - view.width).toInt()
    }

    //与屏幕边缘上边的距离
    fun top(view: View): Int {
        val location = IntArray(2)
        view.getLocationOnScreen(location)
        return location[1]
    }

    //与屏幕边缘下边的距离
    fun bottom(view: View): Int {
        val location = IntArray(2)
        view.getLocationOnScreen(location)
        return (realHeight - location[1] - view.height).toInt()
    }

    //测量两个View之间的X坐标间距
    fun distanceX(view1: View, view2: View): Float {
        return view2.x - view1.x
    }

    //测量两个View之间的Y坐标间距
    fun distanceY(view1: View, view2: View): Float {
        return view2.y - view1.y
    }

    //获取文本居中Y坐标,height：以这个高度进行对其。即对其高度
    fun centerTextY(paint: Paint, height: Float): Float {
        var baseline = (height - (paint.descent() - paint.ascent())) / 2 - paint.ascent()
        return baseline
    }

    //获取文本居中X坐标，以文本居左为计算标准，即：paint.textAlign=Paint.Align.LEFT
    fun centerTextX(text: String, paint: Paint, width: Float): Float {
        val w = paint.measureText(text, 0, text.length)//测量文本的宽度
        var x = (width - w) / 2
        return x
    }

    //获取位图居中Y坐标
    fun centerBitmapY(bitmap: Bitmap, height: Float): Float {
        var y = (height - bitmap.height) / 2
        return y
    }

    //获取位图居中X坐标，width对其的宽度[即总宽度]
    fun centerBitmapX(bitmap: Bitmap, width: Float): Float {
        var x = (width - bitmap.width) / 2
        return x
    }

    //Dp转像素
    fun dpToPixel(dp: Float): Float {
        return dp * density//其中 density就是 dpi/160的比值。
    }

    //像素转Dp
    fun pixelToDp(px: Float): Float {
        return px / density
    }

    var id: Int = 0//id不能小于0，-1表示没有id
        get() = id()
    private var ids = 1000//记录id生成的个数，依次叠加，保证不重复。
    private var map = mutableMapOf<String, Int>()//保存id键值
    fun id(key: Int): Int {
        return id(key.toString())
    }

    //id生成器(xml系统布局id都是从20亿开始的。所以绝对不会和系统id重复。)
    //即能生成id,也能获取id
    fun id(key: String? = null): Int {
        //根据键值获取id
        //id不能小于0，-1表示没有id
        //constraintLayout id找不到时，就以父容器为主。(前提：id不能小于0)
        key?.let {
            map[it]?.let {
                return it//如果该键值的id已经存在，直接返回
            }
        }
        //如果id不存在，就重新创建id
        ids++
        //Log.e("test", "id:\t" + ids)
        key?.let {
            map.put(key, ids)
        }
        return ids
    }

    /**
     * 获得状态栏的高度，单位像素
     *
     * @return
     */
    public fun statusHeight(context: Application? = null): Int {
        if (statusHeight <= 0) {
            try {
                val clazz = Class.forName("com.android.internal.R\$dimen")
                val `object` = clazz.newInstance()
                val height = Integer.parseInt(clazz.getField("status_bar_height")
                        .get(`object`).toString())
                statusHeight = context(context)?.resources?.getDimensionPixelSize(height) ?: 0
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
        return statusHeight
    }

    /**
     * 判断当前Activity是否有状态栏
     * true 有状态栏，false没有状态栏
     */
    public fun isStatusBarVisible(activity: Activity): Boolean {
        if (activity.getWindow().getAttributes().flags and WindowManager.LayoutParams.FLAG_FULLSCREEN === 0) {
            return true//有状态栏
        } else {
            return false//没有有状态栏
        }
    }

    //通过反射获取ActivityThread【隐藏类】
    private fun getActivityThread(): Any? {
        try {
            val clz = Class.forName("android.app.ActivityThread")
            val method = clz.getDeclaredMethod("currentActivityThread")
            method.isAccessible = true
            return method.invoke(null)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    private var application: Application? = null

    //上下文
    //px.context(this.application) 或 px.context()
    fun context(context: Application? = null): Application? {
        if (application != null) {
            return application
        }
        context?.let {
            application = context
        }
        if (application == null) {
            //如果配置文件没有声明，也没有手动初始化。则通过反射自动初始化。【反射是最后的手段，效率不高】
            //通过反射，手动获取上下文。
            val activityThread = getActivityThread()
            if (null != activityThread) {
                try {
                    val getApplication = activityThread.javaClass.getDeclaredMethod("getApplication")
                    getApplication.isAccessible = true
                    application = getApplication?.invoke(activityThread) as Application ?: null
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
        }
        return application
    }

}