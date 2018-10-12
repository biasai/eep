package cn.android.support.v7.lib.eep.kera.utils

import android.os.Build
import android.support.v7.view.menu.ListMenuItemView
import android.util.Log
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.util.*

object KUniQueUtils {
    //不需要任何权限。最保险。
    //获得独一无二的Psuedo ID【如：ffffffff-80ac-a8f1-ffff-ffff8e4712be】,等价于设备号ID【设备唯一标识】
    fun getUniquePsuedoID(): String {
        var serial: String? = null
        val m_szDevIDShort = "Psuedo ID:" +
                Build.BOARD.length % 10 + Build.BRAND.length % 10 +
                Build.CPU_ABI.length % 10 + Build.DEVICE.length % 10 +
                Build.DISPLAY.length % 10 + Build.HOST.length % 10 +
                Build.ID.length % 10 + Build.MANUFACTURER.length % 10 +
                Build.MODEL.length % 10 + Build.PRODUCT.length % 10 +
                Build.TAGS.length % 10 + Build.TYPE.length % 10 +
                Build.USER.length % 10 //13 位
        try {
            serial = Build::class.java.getField("SERIAL").get(null).toString()
            //API>=9 使用serial号
            return UUID(m_szDevIDShort.hashCode().toLong(), serial.hashCode().toLong()).toString()
        } catch (exception: Exception) {
            //serial需要一个初始化
            serial = "serial" // 随便一个初始化
        }
        //使用硬件信息拼凑出来的15位号码
        return UUID(m_szDevIDShort.hashCode().toLong(), serial!!.hashCode().toLong()).toString()
    }

    var letteres = mutableListOf<String>("0","1","2","3","4","5","6","7","8","9","a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z")

    //生成随机字符【格式(包含数字和小写字符)：7gc0y3】
    //确保每次生成都尽可能不一样（随机个数count越大越好。）
    fun getSecurityRandom(count: Int): String {
        if (count > 0) {
            var code = ""
            // 生成正随机数
            for (i in 0 until count) {
                // 以时间为种子(每次循环都重新实例化一个Random，增大随机概率。)
                var rand = Random((Math.random() * 100000000).toLong()+System.currentTimeMillis())
                var c = Math.abs(rand.nextInt() % 36).toString()
                c = letteres.get(c.toInt())
                code = code + c
            }
            return code
        } else {
            return "随机个数不能少于0"
        }
    }
}