package cn.android.support.v7.lib.eep.kera.base

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.support.v4.view.ViewPager
import android.view.*
import cn.android.support.v7.lib.eep.kera.R

//    子类初始化参考
//    init {
//        isDarkmode(true).isDismiss(false).isLocked(true) {
//            Log.e("ui", "返回键按下监听")
//        }
//    }

/**
 * @author 彭治铭
 */
//fixme isStatus 是否显示状态栏【true会显示状态栏，false不会显示状态栏】,默认有状态栏
//fixme isTransparent 背景是否透明,true透明，false背景会有遮罩层半透明的效果。默认背景透明
abstract class KDialog(open var act: Activity?, open var layoutId: Int = 0, open var isStatus: Boolean = true, open var isTransparent: Boolean = true, open var isInitUI: Boolean = false) {
    //fixme Activity不要使用全局变量。局部即可。防止内存泄露
    //fixme 不要使用单列模式，一个Activity就对应一个Dialog。（Dialog需要Activity的支持）
    var dialog: Dialog? = null

    //fixme 如果传入的xml布局为空。则可重写以下方法来创建视图View,一般都是重写的该方法。
    open fun onCreateView(context: Context): View? {
        //return UI { }.view//使用Anko布局
        return null
    }

    //fixme 手动设置状态栏字体颜色(true黑色(深色，默认)，false白色(浅色)。)[亲测有效]
    fun isDarkmode(isDarkmode: Boolean = KApplication.getInstance().darkmode): KDialog {
        KApplication.getInstance().setStatusBarDrak(dialog!!.window, isDarkmode)
        return this
    }

    //fixme 触摸最外层控件，弹窗是否消失。
    fun isDismiss(isDismiss: Boolean = true): KDialog {
        //触摸最外层控件，是否消失\
        dialog?.window.let {
            if (isDismiss) {
                getParentView(it)?.setOnTouchListener { view, motionEvent ->
                    if (motionEvent.action == MotionEvent.ACTION_DOWN) {
                        dismiss()
                    }
                    true
                }
            } else {
                getParentView(it)?.setOnTouchListener(null)
            }
        }
        return this
    }


    //fixme true屏蔽返回键，false不屏蔽返回键。
    //fixme 是否屏蔽返回键+监听返回键（只监听返回键按下。）
    fun isLocked(isLocked: Boolean = true, callback: (() -> Unit)? = null): KDialog {
        //屏蔽返回键,并且监听返回键（只监听返回键按下。）
        dialog?.setOnKeyListener { dialog, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_DOWN) {
                callback?.let {
                    it()//监听返回键(按下)
                }
                isLocked//true,已经处理(屏蔽)，false没有处理，系统会自行处理。
            } else {
                false//返回false，不屏蔽
                //返回键以外交给系统自行处理。不可以屏蔽，不然输入法键盘的按键可能无效。如删除键
            }
        }
        return this
    }

    //fixme 为Window设置动画【Dialog和PopuWindow动画都是Style文件】
    fun setWindowAnimations(styleId: Int) {
        dialog?.window?.setWindowAnimations(styleId);
    }

    init {
        //fixme 是否在父类中初始化视图，默认就是否，false。需要在子类中手动调用initUI()方法
        //fixme 如果为false,不在父类中初始化视图，则在子类的init方法中，必须手动调用initUI()方法。
        //fixme 之所以引用isInitUI参数，是因为子类的构造参数无法在父类中获取(父类无法获取子类的构造参数)。为了获取子类构造参数。则必须在子类中初始化。
        //fixme initUI()在子类中调用时，onCreateView()就可以获取子类构造参数了。
        if (isInitUI) {
            initUI()
        }
    }

    //初始化视图
    fun initUI() {
        //==========================================================================================开始

        //if (Build.VERSION.SDK_INT < 19) {//4.4以下全部全屏。因为控制不了状态栏的颜色。所以就不要状态栏。直接全屏}
        //如果Activity是全屏,弹窗也会是全屏。主题文件也没办法。
        val styleTheme: Int//布局居中

        //与Activity是否有状态栏无关。Dialog可以自己控制自己是否显示状态栏。即使Acitivy全屏，Dilog也可以有自己的状态栏。亲测
        if (isStatus && Build.VERSION.SDK_INT >= 19) {//是否有状态栏
            if (isTransparent) {//背景是否透明
                styleTheme = R.style.KTheme_Dialog_hasStatus_transparent
            } else {
                styleTheme = R.style.KTheme_Dialog_hasStatus
            }
        } else {
            if (isTransparent) {
                styleTheme = R.style.KTheme_Dialog_full_transparent
            } else {
                styleTheme = R.style.KTheme_Dialog_full
            }
        }
        //fixme Dialog主题，必须在构造函数中传入才有效，其他地方设置主题无效。只有构造函数中才有效。
        dialog = object : Dialog(act!!, styleTheme) {
            override fun onAttachedToWindow() {
                super.onAttachedToWindow()
                //附加到窗口,每次显示的时候都会调用
                listener()
                onListener?.let {
                    it()
                }//在listener的后面调用
            }

            override fun onDetachedFromWindow() {
                super.onDetachedFromWindow()
                onRecycleView?.let {
                    it()
                }//在recycleView的前面调用
                //从当前窗口移除。每次dismiss的时候都会调用
                recycleView()
            }
        }
        var window = dialog!!.window
        if (Build.VERSION.SDK_INT < 19) {
            //低版本显示窗体，必须在window.setContentView之前调用一次。其后就可随便调show()了。
            //高版本可在window.setContentView()之后调用。
            if (dialog != null) {
                act?.let {
                    if (!it.isFinishing) {
                        dialog!!.show()
                    }
                }
            }
        }
        //true(按其他区域会消失),按返回键还起作用。false(按对话框以外的地方不起作用,即不会消失)【现在的布局文件，基本都是全屏的。这个属性设置已经没有意义了。触屏消失，需要自己手动去实现。】
        //都以左上角为标准对齐。没有外区，全部都是Dialog区域。已经确保百分百全屏。所以这个方法已经没有意义
        //dialog.setCanceledOnTouchOutside(true);// 调用这个方法时，按对话框以外的地方不起作用。按返回键还起作用
        window = dialog!!.window
        if (Build.VERSION.SDK_INT <= 17) {
            //解决乐视黑屏问题
            window!!.setFormat(PixelFormat.RGBA_8888)
            window.setBackgroundDrawable(null)
        }
        //window.setContentView之前设置
        //window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);//全屏,无效。全屏必须到style主题文件里设置才有效
        window!!.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)//状态栏透明,4.4及以上有效
        window.decorView.fitsSystemWindows = false
        //fixme =====================================================================================布局
        if (layoutId > 0) {
            window.setContentView(layoutId)// xml布局
        } else {
            act?.apply {
                onCreateView(this)?.let {
                    window.setContentView(it)//一般为anko布局
                }
            }
        }
        if (Build.VERSION.SDK_INT >= 19) {
            if (dialog != null) {
                act?.let {
                    if (!it.isFinishing) {
                        dialog!!.show()//防止状态栏的设置无效。一定在设置之后再显示出来。
                    }
                }
            }
        }
        //为Window设置动画【Dialog和PopuWindow动画都是Style文件】
        //window.setWindowAnimations(R.style.CustomDialog);
        //popupWindow.setAnimationStyle(R.style.CustomDialog)

        //如果是windowIsFloating为false。则以左上角为标准。居中无效。并且触摸外区也不会消失。因为没有外区。整个屏幕都是Dialog区域。
        window.setGravity(Gravity.CENTER)//居中。

        //设置状态栏背景透明【亲测有效】
        try {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)   //去除半透明状态栏
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN  //一般配合fitsSystemWindows()使用, 或者在根部局加上属性android:fitsSystemWindows="true", 使根部局全屏显示
            if (Build.VERSION.SDK_INT >= 21) {
                window.statusBarColor = Color.TRANSPARENT
            }
            val decordView = window.decorView as ViewPager.DecorView     //获取DecorView实例
            val field = ViewPager.DecorView::class.java.getDeclaredField("mSemiTransparentStatusBarColor")  //获取特定的成员变量
            field.isAccessible = true   //设置对此属性的可访问性
            field.setInt(decordView, Color.TRANSPARENT)  //修改属性值
        } catch (e: Exception) {
            //Log.e("test", "状态栏异常:\t" + e.getMessage());
        }
        //==========================================================================================结束
    }

    //获取xml文件最外层控件。
    fun getParentView(window: Window?): ViewGroup? {
        val decorView = window!!.decorView//布局里面的最顶层控件，本质上是FrameLayout(帧布局)，FrameLayout.LayoutParams
        val contentView = decorView.findViewById<View>(android.R.id.content) as ViewGroup//我们的布局文件。就放在contentView里面。contentView本质上也是FrameLayout(帧布局)，FrameLayout.LayoutParams
        val parent = contentView.getChildAt(0)//这就是我们xml布局文件最外层的那个父容器控件。
        if (parent != null) {
            return parent as ViewGroup
        }
        return null
    }

    //返回Dimen Int类型
    fun getDimensionPixelSize(id: Int): Int {
        var dimen = dialog?.window?.decorView?.resources?.getDimensionPixelSize(id)
        if (dimen != null) {
            return dimen
        } else {
            return 0
        }
    }

    //返回Dimen Float类型
    fun getDimension(id: Int): Float {
        var dimen = dialog?.window?.decorView?.resources?.getDimension(id)
        if (dimen != null) {
            return dimen
        } else {
            return 0f
        }
    }

    //事件，弹窗每次显示时，都会调用
    protected abstract fun listener()

    private var onListener: (() -> Unit)? = null
    //显示监听
    fun onListener(onListener: () -> Unit): KDialog {
        this.onListener = onListener
        return this
    }

    //事件，弹窗每次消失时，都会调用
    protected abstract fun recycleView()

    var onRecycleView: (() -> Unit)? = null
    //消失监听
    fun onRecycleView(onRecycleView: () -> Unit): KDialog {
        this.onRecycleView = onRecycleView
        return this
    }

    //防止内存泄漏
    //在activity结束时记得手动调用一次
    fun recycles() {
        dialog?.dismiss()
        dialog?.cancel()
        dialog = null
        System.gc()
    }


    //获取控件
    fun <T : View?> findViewById(id: Int): T {
        return dialog!!.findViewById<T>(id)
    }

    //关闭弹窗
    fun dismiss() {
        if (dialog != null) {
            dialog!!.dismiss()
        }
        System.gc()//垃圾内存回收
    }

    //显示窗体
    fun show() {
        if (dialog != null && !dialog!!.isShowing) {
            //显示窗体，必须在window.setContentView之前调用一次。其后就可随便调show()了。
            dialog!!.show()
        }
    }

}
