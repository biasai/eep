package cn.android.support.v7.lib.eep.kera.dialog

import android.app.Activity
import android.view.View
import android.widget.TextView
import cn.android.support.v7.lib.eep.kera.R
import cn.android.support.v7.lib.eep.kera.base.KApplication
import cn.android.support.v7.lib.eep.kera.base.KDialog
import cn.android.support.v7.lib.eep.kera.widget.KNumberProgressBar
import cn.android.support.v7.lib.eep.kera.utils.KAppUtils
import cn.android.support.v7.lib.eep.kera.utils.KFileLoadUtils
import cn.android.support.v7.lib.eep.kera.utils.KProportionUtils
import java.io.File

/**
 * 版本更新
 * 使用说明：VersionUpdateDialog(this).setUrl(url).setSrcFileName("app名称带后缀（如果为null或""空，会自动获取网络上的名称）.apk")
 */
open class KVersionUpdateDialog(context: Activity, isStatus: Boolean = true, isTransparent: Boolean = true) : KDialog(context, R.layout.kera_dialog_version_update,isStatus,isTransparent) {
    //进度条
    val numprogressbar: KNumberProgressBar by lazy { findViewById<KNumberProgressBar>(R.id.numprogressbar) }
    //apk下载链接
    var url: String? = null
    //文件名，包括后缀。如果为null或""空，会自动获取网络上的名称。
    var srcFileName: String? = null

    open fun setUrl(url: String): KVersionUpdateDialog {
        this.url = url
        return this
    }

    open fun setSrcFileName(srcFileName: String): KVersionUpdateDialog {
        this.srcFileName = srcFileName
        return this
    }

    init {
        KProportionUtils.getInstance().adapterWindow(context, dialog?.window)//适配
        //取消
        findViewById<View>(R.id.crown_txt_cancel).setOnClickListener {
            dismiss()
        }
        //更新
        findViewById<View>(R.id.crown_txt_ok).setOnClickListener {
            findViewById<View>(R.id.crown_update)?.visibility = View.INVISIBLE//隐藏更新弹框
//            模拟下载进度
//            async {
//                for (i in 1..100) {
//                    delay(50)
//                    coroutineContext.run {
//                        numprogressbar.setProgress(i)
//                    }
//                }
//
//            }
            url?.let {
                KFileLoadUtils.getInstance(context).downLoad(url, srcFileName, object : KFileLoadUtils.RequestCallBack {
                    override fun onStart() {
                        //开始下载
                        context.runOnUiThread {
                            findViewById<View>(R.id.crown_progress)?.visibility = View.VISIBLE//显示进度条
                        }
                    }

                    override fun onFailure(isLoad: Boolean?, result: String?, file: File?) {
                        //下载失败
                        dismiss()
                        if (isLoad!!) {
                            //已经下载(进行安装)
                            file?.let {
                                KAppUtils.getInstance().installation(context, file)
                            }
                        }
                    }

                    override fun onSuccess(file: File?) {
                        //下载完成安装
                        dismiss()
                        KAppUtils.getInstance().installation(context, file)
                    }

                    override fun onLoad(current: Long, max: Long, bias: Int) {
                        //下载进度
                        numprogressbar.setProgress(bias)
                    }
                })
            }

        }
        findViewById<TextView>(R.id.crown_txt_version_name)?.setText("发现新版本：" + KApplication.getInstance().versionName)
        isDismiss(false)
    }

    override fun listener() {}

    /**
     * 更新版本号
     */
    open fun setUpdateVersion(version: String): KVersionUpdateDialog {
        findViewById<TextView>(R.id.crown_txt_version_name)?.setText("发现新版本：" + version)
        return this
    }

    /**
     * 更新内容
     */
    open fun setUpdateContent(content: String): KVersionUpdateDialog {
        findViewById<TextView>(R.id.crown_txt_version_content)?.setText(content)
        return this
    }

    override fun recycleView() {}
}