package cn.android.support.v7.lib.eep.kera.dialog

import android.app.Activity
import android.graphics.Color
import android.view.View
import cn.android.support.v7.lib.eep.kera.R
import cn.android.support.v7.lib.eep.kera.base.KDialog
import cn.android.support.v7.lib.eep.kera.bean.KDateChoose
import cn.android.support.v7.lib.eep.kera.common.kpx
import cn.android.support.v7.lib.eep.kera.utils.KProportionUtils
import cn.android.support.v7.lib.eep.kera.utils.KTimeUtils
import cn.android.support.v7.lib.eep.kera.view.KRollerView

/**
 * 日期选择器
 * Created by 彭治铭 on 2018/6/3.
 */
//使用说明
//var dateChoose = DateChoose()
//val dateChooseDialog:DateChooseDialog by lazy { DateChooseDialog(this, dateChoose).setCallBack { dateChoose = it }}
//dateChooseDialog.show()
open class KDateChooseDialog(context: Activity, var dateChoose: KDateChoose=KDateChoose(), isStatus: Boolean = true, isTransparent: Boolean = true) : KDialog(context, R.layout.kera_dialog_date_choose,isStatus,isTransparent,isInitUI = true) {
    val yyyy: KRollerView by lazy { findViewById<KRollerView>(R.id.crown_roller_yyyy) }
    val MM: KRollerView by lazy { findViewById<KRollerView>(R.id.crown_roller_MM) }
    val dd: KRollerView by lazy { findViewById<KRollerView>(R.id.crown_roller_dd) }

    init {
        KProportionUtils.getInstance().adapterWindow(context,dialog?.window)//适配
        dialog?.window?.setWindowAnimations(R.style.kera_window_bottom)//动画
        //取消
        findViewById<View>(R.id.crown_txt_cancel).setOnClickListener {
            dismiss()
        }
        //完成
        findViewById<View>(R.id.crown_txt_ok).setOnClickListener {
            dismiss()
        }
        //年
        var list_yyyy = ArrayList<String>()
        for (i in 2010..2030) {
            list_yyyy.add(i.toString())
        }
        yyyy.setLineColor(Color.TRANSPARENT).setItems(list_yyyy).setTextSize(kpx.x(40f)).setCount(5)
                .setDefaultTextColor(Color.parseColor("#444444")).setSelectTextColor(Color.parseColor("#444444"))
        //月
        var list_MM = ArrayList<String>()
        for (i in 1..12) {
            list_MM.add(i.toString())
        }
        MM.setLineColor(Color.TRANSPARENT).setItems(list_MM).setTextSize(kpx.x(40f)).setCount(5)
                .setDefaultTextColor(Color.parseColor("#444444")).setSelectTextColor(Color.parseColor("#444444"))
        MM.setItemSelectListener(object : KRollerView.ItemSelectListener {
            override fun onItemSelect(item: String?, position: Int) {
                //月份监听
                updateDays()
            }
        })
        //日
        dd.setLineColor(Color.TRANSPARENT).setTextSize(kpx.x(40f)).setCount(5)
                .setDefaultTextColor(Color.parseColor("#444444")).setSelectTextColor(Color.parseColor("#444444"))

        //fixme 设置数据滚轮循环效果
        yyyy.isCyclic = true
        MM.isCyclic = true
        dd.isCyclic = true

        isDismiss(true)
    }

    open fun updateDays() {
        //日，联动，更加月份而改变
        var list_dd = ArrayList<String>()
        val mDay = KTimeUtils.getInstance().getMonthDay(yyyy.currentItemValue + "-" + MM.currentItemValue)//天数
        for (i in 1..mDay) {
            list_dd.add(i.toString())
        }
        dd.setItems(list_dd)
    }

    override fun listener() {
        updateDays()
        //选中
        yyyy.setCurrentPostion(yyyy.getItemPostion(dateChoose.yyyy))
        MM.setCurrentPostion(MM.getItemPostion(dateChoose.MM))
        dd.setCurrentPostion(dd.getItemPostion(dateChoose.dd))
    }

    override fun recycleView() {
    }

    //日期返回回调
    open fun setCallBack(callbak: (dateChoose: KDateChoose) -> Unit): KDateChooseDialog {
        //完成
        findViewById<View>(R.id.crown_txt_ok).setOnClickListener {
            dateChoose.yyyy = yyyy.currentItemValue
            dateChoose.MM = MM.currentItemValue
            dateChoose.dd = dd.currentItemValue
            callbak(dateChoose)
            dismiss()
        }
        return this
    }

}