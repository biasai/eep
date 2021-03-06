package cn.android.support.v7.lib.eep.kera.base

import android.app.Activity
import android.support.constraint.ConstraintLayout
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cn.android.support.v7.lib.eep.kera.R

//BaseAlertDialog(this).setContentView(UI {  verticalLayout {
//    gravity= Gravity.CENTER
//    textView {
//        text = "我是弹窗口"
//    }
//} }.view).builder()

/**
 * 使用說明：BaseAlertDialog(this).setContentView(R.layout.dialog_photo).builder()
 * Created by 彭治铭 on 2018/4/27.
 */
open class KAlertDialog(activity: Activity, isStatus: Boolean = true, isTransparent: Boolean = true) : KDialog(activity, R.layout.kera_dialog_base_alert,isStatus,isTransparent) {


    var view: View? = null //传入的布局
    var gravity: Int = Gravity.CENTER //位置
    fun Gravity(gravity: Int): KAlertDialog {
        this.gravity = gravity
        return this
    }

    fun setContentView(id: Int): KAlertDialog {
        val mInflater = LayoutInflater.from(dialog?.context)
        view = mInflater.inflate(id, getParentView(dialog?.window), false)
        return this
    }

    fun setContentView(view: View): KAlertDialog {
        this.view = view
        return this@KAlertDialog
    }

    //布局的宽度和高度(0代表了默认全屏)
    private var width = 0
    private var height = 0

    fun getContentWidth(): Int {
        return width
    }

    fun setContentHeight(height: Int): KAlertDialog {
        this.height = height
        return this@KAlertDialog
    }

    fun builder(): KAlertDialog {
        view?.let {
            it?.layoutParams?.let {
                if (width == 0) {
                    width = it?.width!!
                }
                if (height == 0) {
                    height = it?.height!!
                }
            }

            if (width == ViewGroup.LayoutParams.MATCH_PARENT) {
                width = 0
            }
            if (height == ViewGroup.LayoutParams.MATCH_PARENT) {
                height = 0
            }
            var cstParams: ConstraintLayout.LayoutParams = ConstraintLayout.LayoutParams(width!!, height!!)
            var parentView = getParentView(dialog?.window)
            when (gravity) {
            //左，上，右，下。都统一居中。
                Gravity.LEFT -> {
                    cstParams.leftToLeft = parentView!!.left

                    cstParams.topToTop = parentView.top
                    cstParams.bottomToBottom = parentView.bottom
                }
                Gravity.TOP -> {
                    cstParams.topToTop = parentView!!.top

                    cstParams.leftToLeft = parentView.left
                    cstParams.rightToRight = parentView.right
                }
                Gravity.RIGHT -> {
                    cstParams.rightToRight = parentView!!.right

                    cstParams.topToTop = parentView.top
                    cstParams.bottomToBottom = parentView.bottom
                }
                Gravity.BOTTOM -> {
                    cstParams.bottomToBottom = parentView!!.bottom

                    cstParams.leftToLeft = parentView.left
                    cstParams.rightToRight = parentView.right
                }
                else -> {
                    cstParams.leftToLeft = parentView!!.left
                    cstParams.rightToRight = parentView.right
                    cstParams.topToTop = parentView.top
                    cstParams.bottomToBottom = parentView.bottom
                }
            }
            it?.layoutParams = cstParams
            it?.requestLayout()
            parentView?.addView(it)
            it?.isClickable = true//添加点击能力，防止点击样式无效
        }
        return this@KAlertDialog
    }

    open override fun listener() {}

    open override fun recycleView() {}

}