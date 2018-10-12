package cn.android.support.v7.lib.eep.kera.base

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.view.Window
import cn.android.support.v7.lib.eep.kera.R
import cn.android.support.v7.lib.eep.kera.common.KToast
import cn.android.support.v7.lib.eep.kera.utils.KPermissionUtils
import cn.android.support.v7.lib.eep.kera.utils.KPictureUtils

/**
 * Created by 彭治铭 on 2018/6/24.
 */
open class KAppCompatActivity : AppCompatActivity() {
    var activity: Activity? = null
    open fun getActivity(): KAppCompatActivity {
        return this
    }

    override fun onDestroy() {
        activity = null//防止内存泄露
        super.onDestroy()
    }

    //触摸点击效果。
    open fun onPress(view: View?) {
        KView.onPress(view)
    }

    //右边滑动的阴影效果。子类可以自定义效果。
    open fun shadowSliding(): Int {
        return R.drawable.kera_drawable_left_shadow
    }

    //是否开启左滑移除效果。交给子类重写。true开启（默认），false不开启。
    open fun enableSliding(): Boolean {
        return true
    }

    open var isPortrait = true//是否竖屏。默认就是竖屏。
    open var isOrientation = true//是否固定竖屏横屏方向。true会固定方向。false就不会。

    override fun onCreate(savedInstanceState: Bundle?) {
        try {
            activity = this
            super.onCreate(savedInstanceState)
            //fixme 在8.0(api 26)系统的时候，Actvity透明和锁屏（横屏或竖屏）只能存在一个。这个Bug，8.1已经修复了。
            //fixme 这个Bug在 targetSdkVersion >= 27时，且系统是8.0才会出现 Only fullscreen activities can request orientation
            if (Build.VERSION.SDK_INT == 26 && getApplicationInfo().targetSdkVersion >= 26) {
                //这个情况会崩溃，不能横竖屏。是系统Bug
            } else if (isOrientation) {
                if (isPortrait) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)//竖屏
                } else {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)//横屏

                }
            }
            if (enableSliding()) {
                //开启左滑移除效果
                val slideLayout = KSlideLayout(this, shadowSliding())
                slideLayout.bindActivity(this)
            }
        } catch (e: Exception) {
            Log.e("test", "系统框架脑抽筋:\t" + e.message)
        }

        // 将当前Activity添加到栈中
        KActivityManager.getInstance().pushActivity(this)
        requestWindowFeature(Window.FEATURE_NO_TITLE)//无标题栏(setContentView()之前才有效)
        //设置状态栏透明
        KApplication.getInstance().setStatusBarTransparent(window)
    }

    //是否开启动画。true开启(默认开启)，false不开启。[子类可以重写]
    protected open fun isopenAnim(): Boolean {
        return true
    }

    open var enterAnim = R.anim.kera_right_in_without_alpha//进入动画[子类可重写]
    open var exitAnim = R.anim.kera_right_out_without_alpha//退出动画[子类可重写]

    //true 状态栏字体颜色为 黑色，false 状态栏字体颜色为白色。子类可以重写
    protected open fun isDarkMode(): Boolean {
        return KApplication.getInstance().darkmode
    }

    override fun onResume() {
        super.onResume()
        //设置状态栏字体颜色
        KApplication.getInstance().setStatusBarDrak(window, isDarkMode())
        if (isopenAnim()) {
            //启动动画(app首次启动也有效果。)
            overridePendingTransition(enterAnim, exitAnim)
            //overridePendingTransition(0, 0);这个可以关闭动画，系统默认动画也会关闭
        } else {
            overridePendingTransition(0, 0)//关闭动画。防止动画没有关闭。
        }
    }

    override fun onPause() {
        super.onPause()
        if (isopenAnim()) {
            //退出动画(app最后一个activity,关闭应用时，无效。即退出应用时无效)
            overridePendingTransition(enterAnim, exitAnim)
        } else {
            overridePendingTransition(0, 0)
        }
    }

    //true 程序按两次退出。false正常按键操作。[子类可以重写]
    protected open fun isExit(): Boolean {
        return false//默认不监听返回键，不退出
    }

    private var exitTime: Long = 0
    //open var exitInfo = "再按一次退出"//退出提示信息[子类可以重写]
    open var exitInfo = "别点了，再点我就要走了"

    //监听返回键
    override fun onBackPressed() {
        if (isExit()) {
            if (System.currentTimeMillis() - exitTime > 2000) {
                KToast.show(exitInfo)
                exitTime = System.currentTimeMillis()
            } else {
                finish()
                KActivityManager.getInstance().finishAllActivity()
                KApplication.getInstance().exit()//退出应用（杀进程）
            }
        } else {
            super.onBackPressed()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        KPermissionUtils.onRequestPermissionsResult(getActivity(), requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        KPictureUtils.onActivityResult(this, requestCode, resultCode, data)
    }

}