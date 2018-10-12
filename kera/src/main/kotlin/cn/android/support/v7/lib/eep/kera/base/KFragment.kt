package cn.android.support.v7.lib.eep.kera.base

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

/**
 * 继承本Fragment，主构造函数传入一个布局id或者一个View即可。然后就可以像Activity一样使用了。
 * Activity中加载说明：supportFragmentManager.beginTransaction().replace(px.id("frameLayoutID"),Myfragment()).commit()即可
 * Created by 彭治铭 on 2018/4/20.
 */
abstract open class KFragment(var layout: Int = 0, var content: View? = null) : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (layout <= 0) {
            content?.let {
                return it
            }
            content = onCreateView()//子类可以直接重写onCreateView来创建View
            content?.let {
                return it
            }
            return super.onCreateView(inflater, container, savedInstanceState)
        } else {
            //获取xml布局
            if (content == null) {
                content = inflater.inflate(layout, container, false)
            }
            return content
        }
    }

    //fixme 如果传入的布局和view都为空。则可重写以下方法,一般都是重写的该方法。
    open fun onCreateView(): View? {
        //return UI { }.view//使用Anko布局
        return null
    }

    //获取控件
    fun <T> findViewById(id: Int): T? {
        var view = content?.findViewById<View>(id)
        return view as? T
    }

    override fun onResume() {
        super.onResume()
        KApplication.getInstance().setStatusBarDrak(activity?.window, isDarkMode())
    }

    //true 状态栏字体颜色为 黑色，false 状态栏字体颜色为白色。子类可以重写
    protected open fun isDarkMode(): Boolean {
        return KApplication.getInstance().darkmode
    }

}