package cn.android.support.v7.lib.eep.kera.https

import android.app.Activity
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import android.view.View
import cn.android.support.v7.lib.eep.kera.utils.KAssetsUtils
import cn.android.support.v7.lib.eep.kera.utils.KProportionUtils
import kotlinx.coroutines.experimental.async
import cn.android.support.v7.lib.eep.kera.common.KProgressbar
import cn.android.support.v7.lib.eep.kera.common.kpx


//调用案例
//Bitmaps(imgurl,this).view(txt_hello).onStart {//fixme .view 加载到view上的位图。是根据服务器位图，新建的位图。不是原服务器位图。
//    Log.e("test","开始")
//}.onFailure {
//    Log.e("test","失败:\t"+it)
//}.onFinish {
//    Log.e("test","结束")
//}.showLoad(true).Get(){
//    Log.e("test","位图宽:\t"+it.width+"\t位图高:\t"+it.height)//fixme 有回调就返回服务器原图，没有回调。就释放掉服务器原图。记住：是服务器原图。
//    it.recycle()
//}

//支持圆形位图（加载在view上面的位图可以变成圆形的，但是保存的仍然是服务器原始图片。而不是圆形图片）
//Bitmaps(url,this).showLoad().circle(true).optionsRGB_565(false).strokeColor(Color.CYAN).strokeWidth(px.x(2f)).view(view).Get()
//Bitmaps(url,this).circle(true).view(view).Get()

//调用案例
//                    Bitmaps(url, this@MainActivity).circle(true).showLoad().strokeColor(Color.CYAN).view(view2).get()
//                    Bitmaps().url(url).showLoad(this@MainActivity).Get {
//                        view?.setBackgroundDrawable(BitmapDrawable(it))
//                    }

/**
 * 网络位图加载
 */
open class KBitmaps() {
    //fixme actiivty不为空，跳转到主线程。
    //fixme Activity不为空时，位图才会加载到View上
    open var url: String? = null

    fun url(url: String): KBitmaps {
        this.url = url
        return this
    }

    open var activity: Activity? = null
    //fixme activity不为空时，回调到主线程。且进度条也是在Activity不为空时才有效。
    fun activity(activity: Activity): KBitmaps {
        this.activity = activity
        return this
    }

    constructor(url: String? = null, activity: Activity? = null) : this() {
        this.url = url
        this.activity = activity
    }

    var view: View? = null

    //设置位图依附的控件，如果不为空。会自动将位图加载到控件上
    fun view(view: View?): KBitmaps {
        this.view = view
        return this
    }

    var circle: Boolean = false//加载在View上面的位图是否为圆形位图。默认不是。
    fun circle(circle: Boolean = true): KBitmaps {
        this.circle = circle
        if (circle) {
            optionsRGB_565 = false//圆型位图。必须使用Bitmap.Config.ARGB_8888(支持透明)，565不支持透明。会有黑色边框的。
        }
        return this
    }

    var strokeWidth: Float = kpx.x(2f)//圆的边框
    fun strokeWidth(strokeWidth: Float = this.strokeWidth): KBitmaps {
        this.strokeWidth = strokeWidth
        return this
    }

    var strokeColor: Int = Color.WHITE//圆边框的颜色
    fun strokeColor(strokeColor: Int = this.strokeColor): KBitmaps {
        this.strokeColor = strokeColor
        return this
    }

    var timeOut = 3000;//超时链接时间，单位毫秒,一般500毫秒足已。亲测100%有效。极少数设备可能脑抽无效。不用管它。
    fun timeOut(timeOut: Int = this.timeOut): KBitmaps {
        this.timeOut = timeOut
        return this
    }

    var repeat: Boolean = false//是否允许网络重复请求。默认不允许重复请求。
    fun repeat(isRepeat: Boolean = true): KBitmaps {
        this.repeat = isRepeat
        return this
    }

    var optionsRGB_565 = true//fixme 位图加载格式是否为 Bitmap.Config.RGB_565 ，省内存。默认为true
    fun optionsRGB_565(optionsRGB_565: Boolean = true): KBitmaps {
        this.optionsRGB_565 = optionsRGB_565
        if (circle) {
            this.optionsRGB_565 = false//圆型位图。必须使用Bitmap.Config.ARGB_8888(支持透明)，565不支持透明。会有黑色边框的。
        }
        return this
    }

    var cacle: Boolean = true//是否缓存位图，默认缓存
    fun cacle(isCache: Boolean = true): KBitmaps {
        cacle = isCache
        return this
    }

    var load: Boolean = false//是否显示进度条，默认不显示，fixme (Activity不能为空，Dialog需要Activity的支持)
    fun showLoad(isLoad: Boolean = true): KBitmaps {
        this.load = isLoad
        return this
    }

    fun showLoad(activity: Activity): KBitmaps {
        this.load = true
        if (this.activity == null) {
            this.activity = activity
        }
        return this
    }

    //进度条变量名，子类虽然可以重写，但是类型改不了。所以。进度条就不允许继承了。子类自己去定义自己的进度条。
    var progressbar: KProgressbar? = null//进度条(Activity不能为空，Dialog需要Activity的支持)

    //fixme 显示进度条[子类要更改进度条，可以重写这个]
    //重写的时候，注意屏蔽父类的方法，屏蔽 super.showProgress()
    open fun showProgress() {
        if (load) {
            if (progressbar == null && activity != null) {
                progressbar = KProgressbar(activity!!)
            }
            progressbar?.show()
        }
    }

    //fixme 关闭进度条[子类可以重写,重写的时候，记得对自己的进度条进行内存释放。]
    //重写的时候，注意屏蔽父类的方法，屏蔽 super.showProgress()
    open fun dismissProgress() {
        if (load) {
            progressbar?.let {
                progressbar?.dismiss()
                progressbar = null
            }
        }
    }

    //参数
    //header头部参数。Get，Post都行
    val headers: MutableMap<String, String> by lazy { mutableMapOf<String, String>() }
    //params属于 body子集。Get，Post都行
    val params: MutableMap<String, String> by lazy { mutableMapOf<String, String>() }


    //添加头部参数
    fun addHeader(key: String, value: String): KBitmaps {
        headers.put(key, value)
        return this
    }

    //添加头部参数(融合两个Map)
    fun addHeader(header: MutableMap<String, String>? = null): KBitmaps {
        header?.let {
            for ((key, value) in header.entries) {
                headers.put(key, value)
            }
        }
        return this
    }

    fun addParam(key: String, value: String): KBitmaps {
        params.put(key, value)
        return this
    }

    fun addParam(param: MutableMap<String, String>? = null): KBitmaps {
        param?.let {
            for ((key, value) in param.entries) {
                params.put(key, value)
            }
        }
        return this
    }


    //获取网络请求唯一标志(url+所有参数集合)
    fun getUrlUnique(): String {
        var stringBuffer = StringBuffer("网络位图唯一标志:\t")
        stringBuffer.append(url)
        if (headers?.size > 0) {
            for ((key, value) in headers.entries) {
                stringBuffer.append(key)
                stringBuffer.append(value)
            }
        }
        if (params?.size > 0) {
            for ((key, value) in params.entries) {
                stringBuffer.append(key)
                stringBuffer.append(value)
            }
        }
        //Log.e("test", "" + stringBuffer)
        return stringBuffer.toString()
    }


    //开始回调
    var start: (() -> Unit)? = null

    fun onStart(start: (() -> Unit)? = null): KBitmaps {
        this.start = start
        return this
    }

    //失败回调
    var failure: ((errStr: String?) -> Unit)? = null

    fun onFailure(failure: ((errStr: String?) -> Unit)? = null): KBitmaps {
        this.failure = failure
        return this
    }

    //结束回调，无论是成功还是失败都会调用(最后执行)
    var finish: (() -> Unit)? = null

    fun onFinish(finish: (() -> Unit)? = null): KBitmaps {
        this.finish = finish
        return this
    }


    //fixme 不需要成功回调，因为最后的Get方法。就等于了成功回调。
    //Get请求获取网络位图，参数设置完成之后，最后调用
    //返回的位图是服务器原始图片，缓存的也是服务器原始图片
    //fixme 回调为空时，会释放掉服务器原始位图
    //fixme 网络图片，一般就使用Get请求
    fun Get(callback: ((bitimap: Bitmap) -> Unit)? = null) {
        KHttp.GetNetBitmap(url, activity, this, requestCallBack = object : KBitmapCallback(this) {
            override fun onSuccess(bitmap: Bitmap) {
                view?.let {
                    //fixme 控件不为空。位图自动加载到
                    //这里新压缩了一个位图，和服务器原始位图已经没有关系了。即使释放到服务器原图，也不受影响
                    ViewBitmap(activity, it, bitmap, this@KBitmaps)
                }
                if (callback != null) {
                    callback(bitmap)
                } else {
                    //fixme 没有回调，就将位图释放掉,防止异常，在新开协程里释放。
                    async {
                        if (!bitmap.isRecycled) {
                            bitmap.recycle()
                        }
                        if (KAssetsUtils.getInstance() != null) {
                            KAssetsUtils.getInstance().recycleBitmap(getUrlUnique())//fixme 释放缓存。
                        }
                    }
                }
                //最后执行
                super.onSuccess(bitmap)
            }
        }, timeOut = timeOut)
    }

    //兼容大小写
    fun get(callback: ((bitimap: Bitmap) -> Unit)? = null) {
        Get(callback)
    }


    companion object {
        //获取控件位图
        fun getViewBitmap(view: View): Bitmap? {
            if (view.background is BitmapDrawable) {
                var bitmapDrawable = view.background as BitmapDrawable
                var bitmap = bitmapDrawable.bitmap
                //Log.e("test","控件位图:\twidth:\t"+bitmap.width+"\theight:\t"+bitmap.height)
                return bitmap
            }
            return null
        }

        //将bitmap压缩到和View同等大小，并且加载到View上。(Activity不为空时才有效)
        //这里新压缩了一个位图，和服务器原始位图已经没有关系了。即使释放到服务器原图，也不受影响
        fun ViewBitmap(activity: Activity?, view: View?, src: Bitmap?, bitmaps: KBitmaps? = null) {
            if (view != null && src != null) {
                var width = view.layoutParams.width
                if (width < view.width) {
                    width = view.width
                }
                var height = view.layoutParams.height
                if (height < view.height) {
                    height = view.height
                }
                //Log.e("test", "控件宽:\t" + width + "\t控件高:\t" + height)
                if (width > 0 && height > 0) {
                    val sp = height.toFloat() / width.toFloat()
                    //这一步，位图压缩是耗时操作。之后需要跳转主线程才能更新UI
                    var bitmap = KProportionUtils.getInstance().GeometricCompressionBitmap(src, width.toFloat(), sp)//对Bitmap根据控件大小，进行压缩。这个方法可以防止图片变形。
                    bitmaps?.let {
                        if (bitmaps.circle) {
                            view?.setLayerType(View.LAYER_TYPE_HARDWARE, null)//圆形位图，需要开启硬件加速
                            //圆形位图
                            var bitmaps2 = getCircleBitmap(it.strokeWidth, it.strokeColor, bitmap)
                            bitmap.recycle()//释放掉原有位图
                            bitmap = null
                            bitmap = bitmaps2
                        }
                    }
                    if (activity != null && !activity.isFinishing) {
                        activity.runOnUiThread {
                            //跳转UI主线程更新View
                            //view.setBackground(new BitmapDrawable(bm));
                            view.setBackgroundDrawable(BitmapDrawable(bitmap))
                        }
                    } else {
                        view.post {
                            view.setBackgroundDrawable(BitmapDrawable(bitmap))
                        }
                    }
                } else {
                    //控件大小不一定，就以图片的原始尺寸为标准进行加载（最大不会超过屏幕宽，大于屏幕会进行压缩）。
                    //fixme 如果要自定义，控件宽和高最好手动设置成0。
                    try {
                        var w = view.resources.displayMetrics.widthPixels
                        //var h = view.resources.displayMetrics.heightPixels
                        //Log.e("test", "屏幕宽:\t" + w + "\t屏幕高:\t" + h)
                        //Log.e("test", "位图宽:\t" + src.width + "\t位图高:\t" + src.height)
                        var bp: Bitmap
                        if (src.width <= w * 1.02) {
                            //fixme Bitmap.createScaledBitmap 如果缩放位图和原有位图大小差异在1%之内，使用的还是同一个位图对象。
                            //fixme 大小差异超过1%左右，使用的就是新的位图，和原位图就没有关系了。wrap_content等靠不住，不一定返回小于0
                            //Log.e("test", "小")
                            bp = KProportionUtils.getInstance().GeometricCompressionBitmap(src, src.width.toFloat() * 0.98f, src.height.toFloat() / src.width.toFloat())
                        } else {
                            //Log.e("test", "大")
                            bp = KProportionUtils.getInstance().GeometricCompressionBitmap(src, w.toFloat(), src.height.toFloat() / src.width.toFloat())
                        }

                        bitmaps?.let {
                            if (bitmaps.circle) {
                                view?.setLayerType(View.LAYER_TYPE_HARDWARE, null)//圆形位图，需要开启硬件加速
                                //圆形位图
                                var bitmaps2 = getCircleBitmap(it.strokeWidth, it.strokeColor, bp)
                                bp?.recycle()//释放掉原有位图
                                bp = bitmaps2
                            }
                        }

                        //match_parent 和 wrap_content 返回高度或宽度，小于等于0
                        //如果控件的宽或高，以图片的尺寸为标准
                        if (activity != null && !activity.isFinishing) {
                            activity.runOnUiThread {
                                //跳转UI主线程更新View
                                //view.setBackground(new BitmapDrawable(bm));
                                view.layoutParams.width = bp.width
                                view.layoutParams.height = bp.height
                                view.requestLayout()
                                view.setBackgroundDrawable(BitmapDrawable(bp))
                            }
                        } else {
                            view.post {
                                view.layoutParams.width = bp.width
                                view.layoutParams.height = bp.height
                                view.requestLayout()
                                view.setBackgroundDrawable(BitmapDrawable(bp))
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("test", "kotlin之Bitmaps背景图片设置异常:\t" + e.message)
                    }
                }
            }
        }

        //获取指定颜色的位图(将该位图，有像素的部分全都变成该颜色值)
        fun getColorBitmap(color: Int, bitmap: Bitmap): Bitmap {
            val outBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig())//不能对原位图直接进行操作。所以要新建一个位图
            var canvas = Canvas(outBitmap)
            var paint = Paint()
            paint.setAntiAlias(true)
            paint.setDither(true)
            paint.setColorFilter(LightingColorFilter(Color.TRANSPARENT, color))//去除原有位图的颜色，直接变成制定颜色的位图
            canvas.drawBitmap(bitmap, 0f, 0f, paint)
            return outBitmap
        }

        /**
         * 获取圆形位图（返回一个新的圆形位图）
         * 要求：一 Bitmap格式必须是Bitmap.Config.ARGB_8888（支持透明），二 view?.setLayerType(View.LAYER_TYPE_HARDWARE, null)需要开启硬件加速
         * strokeWidth 圆形位图，边框宽度
         * strokeColor 圆形位图，边框颜色
         */
        fun getCircleBitmap(strokeWidth: Float, strokeColor: Int, bitmap: Bitmap): Bitmap {
            var radius = bitmap.width
            if (bitmap.height < radius) {
                radius = bitmap.height//半径取位图较小的一边
            }
            val outBitmap = KProportionUtils.getInstance().GeometricCompressionBitmap(bitmap, radius.toFloat() + strokeWidth)//取位图中间的那一部分（新建一个位图）
            var canvas = Canvas(outBitmap)
            var paint = Paint()
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = radius.toFloat()
            paint.color = strokeColor
            paint.setAntiAlias(true)
            paint.setDither(true)
            paint.setXfermode(PorterDuffXfermode(PorterDuff.Mode.SRC_OUT))
            canvas.drawCircle(outBitmap.width / 2f, outBitmap.height / 2f, radius.toFloat() / 2 + paint.strokeWidth / 2, paint)
            paint.setXfermode(null)
            if (strokeWidth > 0) {
                paint.strokeWidth = strokeWidth
                canvas.drawCircle(outBitmap.width / 2f, outBitmap.height / 2f, radius.toFloat() / 2, paint)
            }
            return outBitmap
        }

    }

}