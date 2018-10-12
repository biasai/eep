package cn.android.support.v7.lib.eep.kera.dialog

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.widget.TextView
import cn.android.support.v7.lib.eep.kera.R
import cn.android.support.v7.lib.eep.kera.base.KDialog
import cn.android.support.v7.lib.eep.kera.common.kpx
import org.jetbrains.anko.*

//            val alert: AlertDialog by lazy { AlertDialog(this) }
//            alert.little(false).title("温馨").mession("是否确认退出？").positive("确定"){
//                ToastUtils.showToastView("点击确定")
//            }.negative("NO"){
//                ToastUtils.showToastView("NO!!!")
//            }.isDismiss(false).show()
open class KTimiAlertDialog(activity: Activity, isStatus: Boolean = true, isTransparent: Boolean = true) : KDialog(activity,isStatus = isStatus,isTransparent = true) {

    override fun onCreateView(context: Context): View? {
        return context.UI {
            verticalLayout {
                gravity = Gravity.CENTER
                relativeLayout {
                    id = kpx.id("crown_alert_parent")
                    isClickable = true
                    //background=resources.getDrawable(R.drawable.crown_drawable_alert)
                    setBackgroundResource(R.drawable.kera_drawable_alert)
                    //标题
                    textView {
                        id = kpx.id("crown_txt_title")
                        textColor = Color.parseColor("#242424")
                        textSize = kpx.textSizeX(32)
                    }.lparams {
                        leftMargin = kpx.x(24)
                        topMargin = kpx.x(24)
                    }

                    //内容
                    textView {
                        id = kpx.id("crown_txt_mession")
                        textColor = Color.parseColor("#242424")
                        textSize = kpx.textSizeX(26)
                    }.lparams {
                        leftMargin = kpx.x(26)
                        centerVertically()
                    }

                    //取消
                    textView {
                        id = kpx.id("crown_txt_Negative")
                        textColor = Color.parseColor("#239F93")
                        textSize = kpx.textSizeX(26)
                        padding = kpx.x(24)
                    }.lparams {
                        alignParentBottom()
                        leftOf(kpx.id("crown_txt_Positive"))
                    }

                    //确定
                    textView {
                        id = kpx.id("crown_txt_Positive")
                        textColor = Color.parseColor("#239F93")
                        textSize = kpx.textSizeX(26)
                        padding = kpx.x(24)
                    }.lparams {
                        alignParentBottom()
                        alignParentRight()
                        leftOf(kpx.id("crown_txt_Positive"))
                    }

                }.lparams {
                    width = kpx.x(500)
                    height = kpx.y(300)
                }
            }
        }.view
    }

    var little = false//是否为小窗口，默认不是。
    open fun little(little: Boolean = true): KTimiAlertDialog {
        this.little = little
        return this
    }

    val container: View by lazy { findViewById<View>(kpx.id("crown_alert_parent")) }
    //标题栏文本
    var txt_title: String? = ""
    val title: TextView by lazy { findViewById<TextView>(kpx.id("crown_txt_title")) }
    open fun title(title: String? = null): KTimiAlertDialog {
        txt_title = title
        return this
    }

    //信息文本
    var txt_mession: String? = ""
    val mession: TextView by lazy { findViewById<TextView>(kpx.id("crown_txt_mession")) }
    open fun mession(mession: String? = null): KTimiAlertDialog {
        txt_mession = mession
        return this
    }

    val negative: TextView by lazy { findViewById<TextView>(kpx.id("crown_txt_Negative")) }
    //左边，取消按钮
    open fun negative(negative: String? = "取消", callback: (() -> Unit)? = null): KTimiAlertDialog {
        this.negative.setText(negative)
        this.negative.setOnClickListener {
            callback?.run {
                this()
            }
            dismiss()
        }
        return this
    }

    val positive: TextView by lazy { findViewById<TextView>(kpx.id("crown_txt_Positive")) }
    //右边，确定按钮
    open fun positive(postive: String? = "确定", callback: (() -> Unit)? = null): KTimiAlertDialog {
        this.positive.setText(postive)
        this.positive.setOnClickListener {
            callback?.run {
                this()
            }
            dismiss()
        }
        return this
    }

    init {
        //取消
        negative.setOnClickListener {
            dismiss()
        }
        //确定
        positive.setOnClickListener {
            dismiss()
        }
        isDismiss(false)//默认不消失
    }


    override fun listener() {
        container.layoutParams.width = kpx.x(500)
        if (little) {
            container.layoutParams.height = kpx.x(200)
        } else {
            container.layoutParams.height = kpx.x(300)
        }
        container.requestLayout()
        title.setText(txt_title)
        mession.setText(txt_mession)
    }

    override fun recycleView() {
    }


}