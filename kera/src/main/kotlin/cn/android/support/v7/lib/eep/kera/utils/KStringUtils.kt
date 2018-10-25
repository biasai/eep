package cn.android.support.v7.lib.eep.kera.utils

import android.util.Log
import java.text.DecimalFormat
import java.util.*

/**
 * 字符串处理工具类
 * 之所以写这个工具，是因为NumberFormat的四舍五入有问题。不可靠。所以自己写。
 */
object KStringUtils {

    //去除字符串双引号
    fun removeMarks(result: String): String {
        var result = result
        result = result.replace("\"", "")
        return result
    }

    //如果数字小于10，则前面自动补上一个0
    fun addZero(s: String): String {
        val n = Integer.valueOf(s)
        return if (n < 10) {
            "0$s"
        } else s
    }

    /**
     * @param num 随机数的个数
     * @return 返回随机数
     */
    fun getRandom(num: Int): String {
        //产生一个100以内的整数：int x=(int)(Math.random()*100);
        return getRandom(num, (Math.random() * 100).toInt())
    }


    /**
     * @param num   随机数的个数
     * @param seeds 随机种子
     * @return
     */
    fun getRandom(num: Int, seeds: Int): String {
        var code = ""
        // 以时间为种子
        val rand = Random(System.currentTimeMillis() + seeds.toLong() + (Math.random() * 1000).toInt().toLong())
        // 生成真正随机数
        for (i in 0 until num) {
            code = code + Math.abs(rand.nextInt() % 10)
        }
        return code
    }


    /**
     * 将字符串转换为整形。精确到小数后两位（其后全部割舍，不采用四舍五入），
     * 转换后的整数是原有的100倍（因为 微信的1对应0.01）
     *
     * @param s
     * @return
     */
    fun toWeixinInt(s: String): String {
        var b: Double? = java.lang.Double.parseDouble(s) * 100
        val format = DecimalFormat("0.00 ")
        b = java.lang.Double.parseDouble(format.format(b))
        val i = b.toInt()
        return i.toString() + ""
    }

    fun doubleString(d: Double, num: Int = 2): String? {
        return doubleString(d, num, true, false)
    }

    fun doubleString(d: Double, num: Int, isKeep0: Boolean = true, isRounded: Boolean = false): String? {
        d.toString().trim().apply {
            return doubleString(this, num, isKeep0, isRounded)
        }
        return d.toString()
    }

    fun doubleString(str: String?, num: Int = 2): String? {
        return doubleString(str, num, true, false)
    }

    /**
     * str 字符串
     * num 小数点保留个数
     * isKeep0 小数点后末尾如果是0,是否保留0。true保留0，false不保留。默认保留。
     * isRounded 小数点后，最后保留的一位数，是否四舍五入。默认false不。
     */
    fun doubleString(str: String?, num: Int, isKeep0: Boolean = true, isRounded: Boolean = false): String? {
        str?.trim()?.let {
            if (it.contains(".")) {
                //有小数点
                var index = it.indexOf(".")//小数点下标
                var front = it.substring(0, index)//小数点前面的数
                var behind = it.substring(index)//小数点后面数（包含小数点）
                if (behind.length > 1 && num > 0) {
                    behind = behind.substring(1)//小数点后面数（不包含小数点）
                    if (behind.length > num) {
                        //小数个数大于保留个数(存在四舍五入)
                        if (isRounded) {
                            //四舍五入
                            var lastBehind = behind.substring(num, num + 1)//最后一个数。
                            behind = behind.substring(0, num)
                            if (lastBehind.toInt() > 4) {
                                var rounde = "0."
                                for (i in 1..num) {
                                    if (i == num) {
                                        rounde += "1"
                                    } else {
                                        rounde += "0"
                                    }
                                }
                                var double = (front + "." + behind).toDouble()
                                double += rounde.toDouble()//fixme 四舍五入进一
                                var str2 = double.toString()
                                index = str2.indexOf(".")//小数点下标
                                front = str2.substring(0, index)//小数点前面的数
                                behind = str2.substring(index)//小数点后面数（包含小数点）
                                if (behind.length > 1) {
                                    behind = behind.substring(1)//小数点后面数（不包含小数点）
                                } else {
                                    behind = ""
                                }
                                if (behind.length > num) {
                                    behind = behind.substring(0, num)
                                }
                            }
                        } else {
                            //保留原始数据
                            behind = behind.substring(0, num)
                        }
                    }
                    if (isKeep0) {
                        if (behind.length > 0) {
                            return front + "." + behind//fixme 保留0
                        } else {
                            return front
                        }
                    } else {
                        return removeZero(front, behind)//fixme 不保留0
                    }
                } else {
                    return front//fixme 小数点后面没有数字了，直接返回小数点前面的数
                }
            } else {
                return it//fixme 没有小数点的情况,返回原数据
            }
        }
        return str//fixme 为空的情况，返回空
    }

    /**
     * 去除小数点末尾的0 （包括末尾相连的0）。如：1.10 ->1.1 ; 1.00->1
     * s1 小数点前面的数，s2小数点后面的数
     */
    private fun removeZero(s1: String, s2: String): String {
        for (i in s2.length downTo 1) {
            if (s2.substring(i - 1, i) != "0") {
                return s1 + "." + s2.substring(0, i)
            }
        }
        return s1
    }


    /**
     * 计算大小,单位根据文件大小情况返回。返回结果带有单位。
     *
     * @param data 数据大小
     * @return
     */
    fun getDataSize(data: Double): String? {
        var data = data
        if (data < 1024 && data >= 0) {
            return doubleString(data,num = 2,isKeep0 = false) + "B"
        } else if (data >= 1024 && data < 1024 * 1024) {
            data = data / 1024
            return doubleString(data,num = 2,isKeep0 = false) + "KB"
        } else if (data >= 1024 * 1024) {
            data = data / 1024.0 / 1024.0
            return doubleString(data,num = 2,isKeep0 = false) + "MB"
        }
        return null
    }

    /**
     * @param data 单位MB。返回的结果不会带有MB两个字。返回格式 "0.00"
     * @return
     */
    fun getDataSizeMB(data: Double): Double? {
        var format = DecimalFormat("0.00")// 格式
        var data = data
        data = data / 1024.0 / 1024.0
        data = java.lang.Double.parseDouble(format.format(data))
        return data
    }

    /**
     * 计算百分比
     *
     * @param curent     当前数值
     * @param total 总数值
     * @param keep  保留小数个数。0不保留小数，1小数一位，2小数2位。最大支持小数后四位
     * @return 返回百分比字符串。自带%百分比符合。
     */
    fun getPercent(curent: Long, total: Long, keep: Int = 2): String {
        var result = ""// 接受百分比的值
        val x_double = curent * 1.0
        val tempresult = curent / total.toDouble()
        // NumberFormat nf = NumberFormat.getPercentInstance(); 注释掉的也是一种方法
        // nf.setMinimumFractionDigits( 2 ); 保留到小数点后几位
        var df1: DecimalFormat? = null
        if (keep <= 0) {
            df1 = DecimalFormat("0%")
        } else if (keep == 1) {
            df1 = DecimalFormat("0.0%")
        } else if (keep == 2) {
            df1 = DecimalFormat("0.00%") // ##.00%
        } else if (keep == 3) {
            df1 = DecimalFormat("0.000%") // ##.00%
        } else if (keep >= 4) {
            df1 = DecimalFormat("0.0000%") // ##.00%
        }
        // 百分比格式，后面不足2位的用0补齐
        // result=nf.format(tempresult);
        result = df1!!.format(tempresult)
        return result
    }

}