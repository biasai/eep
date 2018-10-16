package com.example.administrator.eep

import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.RelativeLayout
import cn.android.support.v7.lib.eep.kera.base.KAppCompatActivity
import cn.android.support.v7.lib.eep.kera.base.KView
import cn.android.support.v7.lib.eep.kera.common.KToast
import cn.android.support.v7.lib.eep.kera.common.kpx
import cn.android.support.v7.lib.eep.kera.utils.KSelectorUtils
import cn.android.support.v7.lib.eep.kera.widget.KRadiusButton
import cn.android.support.v7.lib.eep.kera.widget.KRadiusEditText
import cn.android.support.v7.lib.eep.kera.widget.KRadiusRelativeLayout
import cn.android.support.v7.lib.eep.kera.widget.KRadiusTextView
import org.jetbrains.anko.act
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.verticalLayout


class MainActivity : KAppCompatActivity() {


    override fun onBackPressed() {
        super.onBackPressed()
    }

//    override fun isExit(): Boolean {
//        return true
//    }
//
//    override fun onShowExit() {
//        //super.onShowExit()
//        KToast.show("再按一次退出")
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        onBackPressed1 {
//            KToast.show("第一次")
//        }

//        onBackPressed2 {
//            KToast.show("第二次")
//        }

        //setContentView(R.layout.activity_main)
        verticalLayout {
            krelativeLayout {

            }.lparams {

            }
            //backgroundColor = Color.parseColor("#1F4943")
            backgroundColor = Color.WHITE
            onPress(this)
            gravity = Gravity.CENTER_HORIZONTAL
//           var kview= KRadiusTextView(this).apply {
//                left_top = kpx.x(50f)
//                left_bottom = kpx.x(100f)
//                right_top = kpx.x(50f)
//                right_bottom = kpx.x(80f)
//                //strokeWidth = kpx.x(5f)
//                //strokeColor = Color.RED
//                backgroundColor = Color.CYAN
//                //strokeGradientColors(Color.RED, Color.BLUE, Color.GREEN)
//                //strokeGradientOritation=ORIENTATION_VERTICAL
//                //onPress()
//                onClick {
//                    KToast.showSuccess("成功")
//                    Log.e("test","点击")
//                }
//            }.lparams {
//                width = kpx.x(300)
//                height = kpx.x(500)
//                topMargin = kpx.x(200)
//            }
            //addView(kview)
            kbutton {
                //                left_top = kpx.x(50f)
//                left_bottom = kpx.x(100f)
//                right_top = kpx.x(50f)
//                right_bottom = kpx.x(80f)
                //strokeWidth = kpx.x(5f)
                //strokeColor = Color.RED
                //backgroundColor = Color.CYAN
                //strokeGradientColors(Color.RED, Color.BLUE, Color.GREEN)
                //strokeGradientOritation=ORIENTATION_VERTICAL
                //onPress()
                clearOriBackground()
                autoDefaultBg(R.mipmap.tab_1_1)
                isAutoCenterHorizontal = true
                autoTopPadding = kpx.x(13f)
                onClick {
                    KToast.showSuccess("成功")
                    Log.e("test", "点击")
                }
                isAutoWH = false
            }.lparams {
                width = kpx.x(65)
                height = kpx.x(98)
                topMargin = kpx.x(200)
            }
        }
    }
}
