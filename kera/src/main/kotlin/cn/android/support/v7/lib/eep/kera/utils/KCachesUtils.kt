package cn.android.support.v7.lib.eep.kera.utils

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import cn.android.support.v7.lib.eep.kera.base.KApplication
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.Serializable

/**
 * Created by 彭治铭 on 2018/10/25.
 */
object KCachesUtils {

    private fun getCache(): KCacheUtils {
        return KCacheUtils.getInstance()
    }

    //存储时间，亲测有效。单位秒。即1就代表一秒
    val TIME_MINUTES = 60//1分钟
    val TIME_HOUR = TIME_MINUTES * 60//1小时
    val TIME_DAY = TIME_HOUR * 24//一天
    val TIME_MONTH = TIME_DAY * 30//大约一个月
    val TIME_YEAR = TIME_MONTH * 12//大约一年

    //缓存目录
    fun getCacheDir(): File {
        //KCacheUtils.get(KApplication.getInstance().getFilesDir().getAbsoluteFile());//这个是之前用的。
        return KApplication.getInstance().cacheDir
    }

    //缓存目录的路径
    fun getCachePath(): String {
        return getCacheDir().absolutePath
    }

    //获取缓存目录大小
    //计算大小,单位根据文件大小情况返回。返回结果带有单位。
    fun getCacheSize(): String {
        var size = KFileUtils.getDirSize(getCacheDir())
        return KStringUtils.getDataSize(size) ?: "0KB"
    }

    //逗号分隔符
    private var comma = ","

    //字符串标志
    private var markString = "KCachesUtils_MarkString"
    //Int标志
    private var markInt = "KCachesUtils_MarkInt"
    //Float标志
    private var markFloat = "KCachesUtils_MarkFloat"
    //Double标志
    private var markDouble = "KCachesUtils_Double"
    //Long长整型
    private var markLong = "KCachesUtils_Long"
    //Serializable序列化对象
    private var markAny = "KCachesUtils_Any"
    //Bitmap位图
    private var markBitmap = "KCachesUtils_Bitmap"
    //Drawable对象
    private var markDrawable = "KCachesUtils_Drawable"
    //ByteArray字节数组
    private var markByteArray = "KCachesUtils_ByteArray"
    //JSONArray数组
    private var markJSONArray = "KCachesUtils_JSONArray"
    //json对象
    private var markJSONObject = "KCachesUtils_JSONObject"

    //清除所有缓存
    fun clear() {
        clearAll()
    }

    //清除所有缓存
    fun clearAll() {
        getCache().clear()
    }

    //清除所有字符串
    fun clearString() {
        removeMark(markString)
    }

    //清除所有Int
    fun clearInt() {
        removeMark(markInt)
    }

    //清除Float浮点型
    fun clearFloat() {
        removeMark(markFloat)
    }

    //清除所有Double型
    fun clearDouble() {
        removeMark(markDouble)
    }

    //清除所有长整型
    fun clearLong() {
        removeMark(markLong)
    }

    //清除所有可序列化对象，Serializable
    fun clearAny() {
        removeMark(markAny)
    }

    //清除所有位图
    fun clearBitmap() {
        removeMark(markBitmap)
    }

    //清除所有Drawable对象
    fun clearDrawable() {
        removeMark(markDrawable)
    }

    //清除所有ByteArray字节数组
    fun clearByteArray() {
        removeMark(markByteArray)
    }

    //清除所有JSONArray json数组
    fun clearJSONArray() {
        removeMark(markJSONArray)
    }

    //清除所有json对象
    fun clearJSONObject() {
        removeMark(markJSONObject)
    }

    //存储标志
    private fun putMark(key: String, marks: String) {
        var mark: String? = getCache().getAsString(marks)
        if (mark == null) {
            getCache().put(marks, key)
        } else if (mark.trim().length > 0 && !mark.contains(key)) {
            mark = mark + comma + key
            getCache().put(marks, mark)
        }
    }

    //移除标志
    private fun removeMark(key: String) {
        var mark: String? = getCache().getAsString(key)
        //KLoggerUtils.e("test","所有移除标志:\t"+mark)
        mark?.let {
            if (it.trim().length > 0) {
                var keys = it.split(comma)
                for (i in 0 until keys.size) {
                    getCache().remove(keys[i])
                    //KLoggerUtils.e("test","移除标志:\t"+keys[i])
                }
                getCache().remove(key)
            }
        }
    }


    //fixme========================================================================================= String字符串类型 1

    fun put(key: String, value: String?) {
        putString(key, value, null)
    }

    fun put(key: String, value: String?, saveTime: Int?) {
        putString(key, value, saveTime)
    }

    fun putString(key: String, value: String?) {
        putString(key, value, null)
    }

    /**
     * 保存 String数据 到 缓存中
     *
     * @param key      保存的key
     * @param value    保存的String数据
     * @param saveTime 保存的时间，单位：秒
     */
    fun putString(key: String, value: String?, saveTime: Int?) {
        if (value != null && value.length > 0 && key.trim().length > 0) {
            if (saveTime != null && saveTime > 0) {
                getCache().put(key, value, saveTime)//有存储时间限制
            } else {
                getCache().put(key, value)//fixme 没有时间限制。即永久保存。
            }
            putMark(key, markString)
        } else {
            KCacheUtils.getInstance().remove(key)
        }
    }

    //获取字符串
    fun getString(key: String): String? {
        if (key.trim().length > 0) {
            return getCache().getAsString(key)
        }
        return null
    }


    //fixme========================================================================================= Int整型 2

    fun put(key: String, value: Int?) {
        putInt(key, value, null)
    }

    fun put(key: String, value: Int?, saveTime: Int?) {
        putInt(key, value, saveTime)
    }

    fun putInt(key: String, value: Int?) {
        putInt(key, value, null)
    }

    /**
     * 保存 Int数据 到 缓存中
     *
     * @param key      保存的key
     * @param value    保存的数据
     * @param saveTime 保存的时间，单位：秒
     */
    fun putInt(key: String, value: Int?, saveTime: Int?) {
        if (value != null && key.trim().length > 0) {
            if (saveTime != null && saveTime > 0) {
                getCache().put(key, value, saveTime)//有存储时间限制
            } else {
                getCache().put(key, value)//fixme 没有时间限制。即永久保存。
            }
            putMark(key, markInt)
        } else {
            KCacheUtils.getInstance().remove(key)
        }
    }

    //获取Int
    fun getInt(key: String): Int? {
        if (key.trim().length > 0) {
            var obj: Any? = getCache().getAsObject(key)
            if (obj != null && obj is Int) {
                return obj as Int
            }
        }
        return null
    }

    //fixme========================================================================================= Float浮点型 3

    fun put(key: String, value: Float?) {
        putFloat(key, value, null)
    }

    fun put(key: String, value: Float?, saveTime: Int?) {
        putFloat(key, value, saveTime)
    }

    fun putFloat(key: String, value: Float?) {
        putFloat(key, value, null)
    }

    /**
     * 保存 Float数据 到 缓存中
     *
     * @param key      保存的key
     * @param value    保存的数据
     * @param saveTime 保存的时间，单位：秒
     */
    fun putFloat(key: String, value: Float?, saveTime: Int?) {
        if (value != null && key.trim().length > 0) {
            if (saveTime != null && saveTime > 0) {
                getCache().put(key, value, saveTime)//有存储时间限制
            } else {
                getCache().put(key, value)//fixme 没有时间限制。即永久保存。
            }
            putMark(key, markFloat)
        } else {
            KCacheUtils.getInstance().remove(key)
        }
    }

    //获取Float
    fun getFloat(key: String): Float? {
        if (key.trim().length > 0) {
            var obj: Any? = getCache().getAsObject(key)
            if (obj != null && obj is Float) {
                return obj as Float
            }
        }
        return null
    }

    //fixme========================================================================================= Double类型 4

    fun put(key: String, value: Double?) {
        putDouble(key, value, null)
    }

    fun put(key: String, value: Double?, saveTime: Int?) {
        putDouble(key, value, saveTime)
    }

    fun putDouble(key: String, value: Double?) {
        putDouble(key, value, null)
    }

    /**
     * 保存 数据 到 缓存中
     *
     * @param key      保存的key
     * @param value    保存的数据
     * @param saveTime 保存的时间，单位：秒
     */
    fun putDouble(key: String, value: Double?, saveTime: Int?) {
        if (value != null && key.trim().length > 0) {
            if (saveTime != null && saveTime > 0) {
                getCache().put(key, value, saveTime)//有存储时间限制
            } else {
                getCache().put(key, value)//fixme 没有时间限制。即永久保存。
            }
            putMark(key, markDouble)
        } else {
            KCacheUtils.getInstance().remove(key)
        }
    }

    //获取Double
    fun getDouble(key: String): Double? {
        if (key.trim().length > 0) {
            var obj: Any? = getCache().getAsObject(key)
            if (obj != null && obj is Double) {
                return obj as Double
            }
        }
        return null
    }

    //fixme========================================================================================= Long长整型 5

    fun put(key: String, value: Long?) {
        putLong(key, value, null)
    }

    fun put(key: String, value: Long?, saveTime: Int?) {
        putLong(key, value, saveTime)
    }

    fun putLong(key: String, value: Long?) {
        putLong(key, value, null)
    }

    /**
     * 保存 数据 到 缓存中
     *
     * @param key      保存的key
     * @param value    保存的数据
     * @param saveTime 保存的时间，单位：秒
     */
    fun putLong(key: String, value: Long?, saveTime: Int?) {
        if (value != null && key.trim().length > 0) {
            if (saveTime != null && saveTime > 0) {
                getCache().put(key, value, saveTime)//有存储时间限制
            } else {
                getCache().put(key, value)//fixme 没有时间限制。即永久保存。
            }
            putMark(key, markLong)
        } else {
            KCacheUtils.getInstance().remove(key)
        }
    }

    //获取Long
    fun getLong(key: String): Long? {
        if (key.trim().length > 0) {
            var obj: Any? = getCache().getAsObject(key)
            if (obj != null && obj is Long) {
                return obj as Long
            }
        }
        return null
    }

    //fixme========================================================================================= Serializable序列化对象 6

    fun put(key: String, value: Serializable?) {
        putAny(key, value, null)
    }

    fun put(key: String, value: Serializable?, saveTime: Int?) {
        putAny(key, value, saveTime)
    }

    fun putAny(key: String, value: Serializable?) {
        putAny(key, value, null)
    }

    /**
     * 保存 数据 到 缓存中
     *
     * @param key      保存的key
     * @param value    保存的数据
     * @param saveTime 保存的时间，单位：秒
     */
    fun putAny(key: String, value: Serializable?, saveTime: Int?) {
        if (value != null && key.trim().length > 0) {
            if (saveTime != null && saveTime > 0) {
                getCache().put(key, value, saveTime)//有存储时间限制
            } else {
                getCache().put(key, value)//fixme 没有时间限制。即永久保存。
            }
            putMark(key, markAny)
        } else {
            KCacheUtils.getInstance().remove(key)
        }
    }

    //获取可序列化Serializable对象
    fun getAny(key: String): Any? {
        if (key.trim().length > 0) {
            return getCache().getAsObject(key)
        }
        return null
    }

    //fixme========================================================================================= Bitmap位图 7

    fun put(key: String, value: Bitmap?) {
        putBitmap(key, value, null)
    }

    fun put(key: String, value: Bitmap?, saveTime: Int?) {
        putBitmap(key, value, saveTime)
    }

    fun putBitmap(key: String, value: Bitmap?) {
        putBitmap(key, value, null)
    }

    /**
     * 保存 数据 到 缓存中
     *
     * @param key      保存的key
     * @param value    保存的数据
     * @param saveTime 保存的时间，单位：秒
     */
    fun putBitmap(key: String, value: Bitmap?, saveTime: Int?) {
        if (value != null && key.trim().length > 0) {
            if (saveTime != null && saveTime > 0) {
                getCache().put(key, value, saveTime)//有存储时间限制
            } else {
                getCache().put(key, value)//fixme 没有时间限制。即永久保存。
            }
            putMark(key, markBitmap)
        } else {
            KCacheUtils.getInstance().remove(key)
        }
    }

    //获取位图
    fun getBitmap(key: String, isOptionsRGB_565: Boolean): Bitmap? {
        if (key.trim().length > 0) {
            return getCache().getAsBitmap(key, isOptionsRGB_565)
        }
        return null
    }

    fun getBitmap(key: String): Bitmap? {
        return getBitmap(key, false)
    }

    //fixme========================================================================================= Drawable对象 8

    fun put(key: String, value: Drawable?) {
        putDrawable(key, value, null)
    }

    fun put(key: String, value: Drawable?, saveTime: Int?) {
        putDrawable(key, value, saveTime)
    }

    fun putDrawable(key: String, value: Drawable?) {
        putDrawable(key, value, null)
    }

    /**
     * 保存 数据 到 缓存中
     *
     * @param key      保存的key
     * @param value    保存的数据
     * @param saveTime 保存的时间，单位：秒
     */
    fun putDrawable(key: String, value: Drawable?, saveTime: Int?) {
        if (value != null && key.trim().length > 0) {
            if (saveTime != null && saveTime > 0) {
                getCache().put(key, value, saveTime)//有存储时间限制
            } else {
                getCache().put(key, value)//fixme 没有时间限制。即永久保存。
            }
            putMark(key, markDrawable)
        } else {
            KCacheUtils.getInstance().remove(key)
        }
    }

    //获取位图
    fun getDrawable(key: String): Drawable? {
        if (key.trim().length > 0) {
            return getCache().getAsDrawable(key)
        }
        return null
    }

    //fixme========================================================================================= ByteArray字节数组 9

    fun put(key: String, value: ByteArray?) {
        putByteArray(key, value, null)
    }

    fun put(key: String, value: ByteArray?, saveTime: Int?) {
        putByteArray(key, value, saveTime)
    }

    fun putByteArray(key: String, value: ByteArray?) {
        putByteArray(key, value, null)
    }

    /**
     * 保存 数据 到 缓存中
     *
     * @param key      保存的key
     * @param value    保存的数据
     * @param saveTime 保存的时间，单位：秒
     */
    fun putByteArray(key: String, value: ByteArray?, saveTime: Int?) {
        if (value != null && key.trim().length > 0) {
            if (saveTime != null && saveTime > 0) {
                getCache().put(key, value, saveTime)//有存储时间限制
            } else {
                getCache().put(key, value)//fixme 没有时间限制。即永久保存。
            }
            putMark(key, markByteArray)
        } else {
            KCacheUtils.getInstance().remove(key)
        }
    }

    //获取字节数组
    fun getByteArray(key: String): ByteArray? {
        if (key.trim().length > 0) {
            return getCache().getAsBinary(key)
        }
        return null
    }

    //fixme========================================================================================= JSONArray json数组 10

    fun put(key: String, value: JSONArray?) {
        putJSONArray(key, value, null)
    }

    fun put(key: String, value: JSONArray?, saveTime: Int?) {
        putJSONArray(key, value, saveTime)
    }

    fun putJSONArray(key: String, value: JSONArray?) {
        putJSONArray(key, value, null)
    }

    /**
     * 保存 数据 到 缓存中
     *
     * @param key      保存的key
     * @param value    保存的数据
     * @param saveTime 保存的时间，单位：秒
     */
    fun putJSONArray(key: String, value: JSONArray?, saveTime: Int?) {
        if (value != null && key.trim().length > 0) {
            if (saveTime != null && saveTime > 0) {
                getCache().put(key, value, saveTime)//有存储时间限制
            } else {
                getCache().put(key, value)//fixme 没有时间限制。即永久保存。
            }
            putMark(key, markJSONArray)
        } else {
            KCacheUtils.getInstance().remove(key)
        }
    }

    //获取json数组
    fun getJSONArray(key: String): JSONArray? {
        if (key.trim().length > 0) {
            return getCache().getAsJSONArray(key)
        }
        return null
    }

    //fixme========================================================================================= JSONObject json对象 11

    fun put(key: String, value: JSONObject?) {
        putJSONObject(key, value, null)
    }

    fun put(key: String, value: JSONObject?, saveTime: Int?) {
        putJSONObject(key, value, saveTime)
    }

    fun putJSONObject(key: String, value: JSONObject?) {
        putJSONObject(key, value, null)
    }

    /**
     * 保存 数据 到 缓存中
     *
     * @param key      保存的key
     * @param value    保存的数据
     * @param saveTime 保存的时间，单位：秒
     */
    fun putJSONObject(key: String, value: JSONObject?, saveTime: Int?) {
        if (value != null && key.trim().length > 0) {
            if (saveTime != null && saveTime > 0) {
                getCache().put(key, value, saveTime)//有存储时间限制
            } else {
                getCache().put(key, value)//fixme 没有时间限制。即永久保存。
            }
            putMark(key, markJSONObject)
        } else {
            KCacheUtils.getInstance().remove(key)
        }
    }

    //获取json对象
    fun getJSONObject(key: String): JSONObject? {
        if (key.trim().length > 0) {
            return getCache().getAsJSONObject(key)
        }
        return null
    }
}