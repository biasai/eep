package cn.android.support.v7.lib.eep.kera.utils

import cn.android.support.v7.lib.eep.kera.type.KTypeReference
import org.json.JSONArray
import org.json.JSONObject
import java.lang.reflect.Method


/**
 * Kotlin json解析类。data class类型，参数必须有默认值才行。即data类型有默认参数，就等价空构造函数。
 * fixme 有空构造函数 和 属性有set方法即可。
 * 单例模式。直接通过类名即可调用里面的方法。都是静态的。
 * Created by 彭治铭 on 2018/4/24.
 */
object KGsonUtils {

    //fixme 直接传入泛型即可。支持所有类型。泛型可以无限嵌套。[一个类里面只支持一个泛型，不支持同时有两个泛型。如：Model<T,T2>]
    //fixme [之前就是json格式不正确，才转换失败。][只要json格式正确，都能转换。亲测！]
    //fixme 兼容ArrayList和MutableList（两个都可以。）
    inline fun <reified T> parseAny(json: String?, vararg field: String): T {
        //Log.e("test", "json:\t" + json)
        var kjson = parseJson(json, *field)//解析指定字段里的json数据。
        //Log.e("test", "json2:\t" + kjson)
        var typeReference = object : KTypeReference<T>() {}
        return parseObject(kjson, typeReference.classes, 0) as T
    }

    fun parseObject(json: String?, classes: List<Class<*>>, index: Int): Any {
        var clazz = classes[index]//当前类型
        //KLoggerUtils.e("test", "当前类型:\t" + clazz + "\t" + clazz.name + "\t长度:\t" + classes.size + "\t" + classes)
        if (clazz.name.equals("java.lang.String")) {
            json?.let {
                return it
            }
            return ""
        }
        var clazzT: Class<*>? = null//当前类型里面的泛型
        if (classes.size > (index + 1)) {
            clazzT = classes[index + 1]
        }
        //Log.e("test", "当前类型:\t" + clazz + "\t泛型:\t" + clazzT + "\t下标：\t" + index)
        //必须有空构造函数，或者所有参数都有默认参数。说的是所有参数。不然无法实例化。
        var t: Any? = null
        var isMutableList=false
        if (clazz.name.equals("java.util.List") || clazz.name.equals("interface java.util.List")) {
            t = mutableListOf<Any>()
            isMutableList=true
        }else{
            t = clazz.newInstance()
        }
        //var t = clazz.newInstance()
        //判断json数据是否为空
        if (json == null || json.toString().trim().equals("") || json.toString().trim().equals("{}") || json.toString().trim().equals("[]")) {
            return t!!
        }
        if (clazz.name.equals("java.util.ArrayList") || clazz.name.equals("class java.util.ArrayList")||isMutableList) {
            //fixme 数组
            var jsonArray = JSONArray(json)
            var last = jsonArray.length()
            last -= 1//最后一个下标
            if (last < 0) {
                last = 0
            }
            var list = ArrayList<Any>()
            clazzT?.let {
                var position = index + 1
                for (i in 0..last) {
                    var m = parseObject(jsonArray.getString(i), classes, position)
                    m?.let {
                        list.add(it as Any)
                    }
                }
            }
            return list//直接返回数组
        } else {
            //fixme 非数组
            try {
                var jsonObject = JSONObject(json)
                clazz?.declaredFields?.forEach {
                    var value: String? = null
                    if (jsonObject.has(it.name)) {//判斷json數據是否存在該字段
                        value = jsonObject.getString(it.name)//获取json数据
                    }
                    if (value != null && !value.trim().equals("") && !value.trim().equals("null")) {
                        var type = it.genericType.toString().trim()//属性类型
                        var name = it.name.substring(0, 1).toUpperCase() + it.name.substring(1)//属性名称【首字目进行大写】。
                        var m: Method? = null
                        if (type == "boolean" || type.equals("class java.lang.Boolean")) {
                            var name2 = name
                            if (name2.contains("Is")) {
                                var index = name2.indexOf("Is")
                                if (index == 0 && name2.length > 2) {
                                    name2 = name2.substring(2)
                                    name2 = name2.substring(0, 1).toUpperCase() + name2.substring(1)
                                }
                            }
                            m = clazz.getMethod("set" + name2, it.type)
                        }
                        if (m == null) {
                            m = clazz.getMethod("set" + name, it.type)
                        }
                        //Log.e("test", "属性:\t" + it.name + "\t类型:\t" + it.genericType.toString() + "\ttype:\t" + type)
                        if (type == "class java.lang.String" || type == "class java.lang.Object") {//Object 就是Any,class类型是相同的。
                            m?.invoke(t, value)//String类型 Object类型
                        } else if (type == "int" || type.equals("class java.lang.Integer")) {
                            m?.invoke(t, value.toInt())//Int类型
                        } else if (type == "float" || type.equals("class java.lang.Float")) {
                            m?.invoke(t, value.toFloat())//Float类型
                        } else if (type == "double" || type.equals("class java.lang.Double")) {
                            m?.invoke(t, value.toDouble())//Double类型
                        } else if (type == "long" || type.equals("class java.lang.Long")) {
                            m?.invoke(t, value.toLong())//Long类型
                        } else if (type == "boolean" || type.equals("class java.lang.Boolean")) {
                            m?.invoke(t, value.toBoolean())//布尔类型。 "true".toBoolean() 只有true能够转换为true，其他所有值都只能转换为false
                        } else if (type == "short" || type.equals("class java.lang.Short")) {
                            m?.invoke(t, value.toShort())//Short类型
                        } else if (type == "byte" || type.equals("class java.lang.Byte")) {
                            var byte = value.toInt()//不能有小数点，不然转换异常。小数点无法正常转换成Int类型。可以有负号。负数能够正常转换。
                            if (byte > 127) {
                                byte = 127
                            } else if (byte < -128) {
                                byte = -128
                            }
                            m?.invoke(t, byte.toByte())//Byte类型 ,范围是：-128~127
                        } else if (type == "char" || type.equals("class java.lang.Character")) {
                            m?.invoke(t, value.toCharArray()[0])//Char类型。字符只有一个字符。即单个字符。
                        } else if (!type.equals("class java.util.HashMap") && !type.equals("class java.util.LinkedHashMap")) {//不支持Map
                            try {
                                //fixme 泛型标志固定一下。就用T，T1或者T2。不要用其他的。不然不好辨别。
                                if ((type.toString().trim().equals("T") || type.toString().trim().equals("T1") || type.toString().trim().equals("T2")) && clazzT != null) {
                                    //fixme 嵌套泛型。
                                    if (clazzT.name.equals("java.util.ArrayList")) {
                                        //fixme 嵌套泛型数组
                                        var jsonArray = JSONArray(value)
                                        var last = jsonArray.length()
                                        last -= 1//最后一个下标
                                        if (last < 0) {
                                            last = 0
                                        }
                                        var list = ArrayList<Any>()
                                        clazzT?.let {
                                            var position = index + 2//fixme 注意就这里数组要加2
                                            for (i in 0..last) {
                                                //Log.e("test", "嵌套数组循环:\t" + jsonArray.getString(i) + "\t下标:\t" + position)
                                                var m = parseObject(jsonArray.getString(i), classes, position)
                                                m?.let {
                                                    list.add(it as Any)
                                                }
                                            }
                                        }
                                        m?.invoke(t, list)
                                    } else {
                                        //fixme 嵌套泛型实体类
                                        var position = index + 1
                                        m?.invoke(t, parseObject(value, classes, position))
                                    }
                                } else {
                                    //fixme 嵌套具体实体类[普通实体类不支持数组]
                                    if (!type.equals("class java.util.ArrayList")) {
                                        var position = index + 1
                                        m?.invoke(t, parseObject(value, classes, position))
                                    }
                                }
                            } catch (e: Exception) {
                                KLoggerUtils.e("test", "kGsonUtils嵌套json解析异常:\t" + e.message)
                            }

                        }
                    }
                }
            } catch (e: Exception) {
                KLoggerUtils.e("test", "KGsonUtils转实体类异常:\t" + e.message)
            }
        }
        return t!!
    }

    //数据解析(解析之后，可以显示中文。)
    //根据字段解析数据(如果该字段不存在，就返回原有数据)
    fun parseJson(result: String?, vararg field: String): String? {
        if (result == null) {
            return null
        }
        var response = result
        if (field.size > 0) {
            //解析字段里的json数据
            for (i in field) {
                i?.let {
                    try {
                        var json = JSONObject(response)
                        if (json.has(it)) {
                            response = json.getString(it)
                        }
                    } catch (e: Exception) {
                        //Log.e("test","json异常:\t"+e.message)
                    }
                }
            }
        }
        //判断是否为合法JSON格式
        try {
            response?.let {
                if (it.contains("{") || it.contains("}") || it.contains("[") || it.contains("]")) {
                    JSONObject(response)//fixme 判断是否为JSONObject对象
                } else {
                    response = null//fixme 不包含{和[肯定不是json数据。
                }
            }
        } catch (e: Exception) {
            try {
                JSONArray(response)//fixme 判断是否为 JSONArray对象
            } catch (e: Exception) {
                response = null //fixme 即不能转JSONObject也不能转JSONArray。不合法则制空,防止异常报错。
            }
        }
        return response
    }

    //实体类转JSON数据
    inline fun <reified T : Any> parseJSON(t: T): JSONObject {
        val jsonObject = JSONObject()
        try {
            //遍历类 成员属性【只对当前类有效，父类无效，即只获取本类的属性】
            val fields = t::class.java.getDeclaredFields()
            for (i in fields.indices) {
                // 获取属性的名字
                var name = fields[i].getName()
                if (name.trim({ it <= ' ' }) == "\$change" || name.trim({ it <= ' ' }) == "serialVersionUID") {
                    continue
                }
                val jName = name
                // 将属性的首字符大写，方便构造get，set方法
                name = name.substring(0, 1).toUpperCase() + name.substring(1)
                // 获取属性的类型
                val type = fields[i].getGenericType().toString()
                //Log.e("test","type:\t"+type+"\tjName:\t"+jName);
                var m: Method? = null
                try {
                    if (type == "boolean" || type.equals("class java.lang.Boolean")) {
                        var name2 = name
                        if (name2.contains("Is")) {
                            var index = name2.indexOf("Is")
                            if (index == 0 && name2.length > 2) {
                                name2 = name2.substring(2)
                                name2 = name2.substring(0, 1).toUpperCase() + name2.substring(1)
                                m = t::class.java.getMethod("is$name2")
                                val obj = m!!.invoke(t) ?: continue
                            }
                        }
                        //Log.e("test","name：\t"+name+"\tname2:\t"+name2)
                        if (m == null) {
                            // 调用getter方法获取属性值
                            m = t::class.java.getMethod("get$name2")
                            val obj = m!!.invoke(t) ?: continue
                        }
                    } else {
                        // 调用getter方法获取属性值
                        m = t::class.java.getMethod("get$name")
                        val obj = m!!.invoke(t) ?: continue
                    }
                    //Log.e("test","obj:\t"+obj);
                } catch (e: Exception) {
                    KLoggerUtils.e("test", "get()异常:\t" + e.message)
                }
                // 如果type是类类型，则前面包含"class "，后面跟类名
                if (type == "class java.lang.String" || type.equals("class java.lang.String")) {
                    val value = m!!.invoke(t) as String
                    jsonObject.put(jName, value)
                } else if (type == "double" || type.equals("class java.lang.Double")) {
                    val value = m!!.invoke(t) as Double
                    jsonObject.put(jName, value)
                } else if (type == "class java.lang.Object" || type.equals("class java.lang.Object")) {
                    val value = m!!.invoke(t)
                    jsonObject.put(jName, value)
                } else if (type == "float" || type.equals("class java.lang.Float")) {
                    val value = m!!.invoke(t) as Float
                    jsonObject.put(jName, value.toDouble())
                } else if (type == "int" || type.equals("class java.lang.Integer")) {
                    val value = m!!.invoke(t) as Int
                    jsonObject.put(jName, value)
                } else if (type == "boolean" || type.equals("class java.lang.Boolean")) {
                    val value = m!!.invoke(t) as Boolean
                    jsonObject.put(jName, value)
                } else if (type == "long" || type.equals("class java.lang.Long")) {
                    val value = m!!.invoke(t) as Long
                    jsonObject.put(jName, value)
                } else if (type == "short" || type.equals("class java.lang.Short")) {
                    val value = m!!.invoke(t) as Short
                    jsonObject.put(jName, value)
                } else if (type == "byte" || type.equals("class java.lang.Byte")) {
                    val value = m!!.invoke(t) as Byte
                    jsonObject.put(jName, value)
                } else if (type == "char" || type.equals("class java.lang.Character")) {
                    val value = m!!.invoke(t) as Character
                    jsonObject.put(jName, value)
                } else {
                    val value = m!!.invoke(t)
                    if (value != null && value != "null" && value != "") {
                        jsonObject.put(jName, value)//Object类型,兼容基本类型
                    }
                }
            }
        } catch (e: Exception) {
            KLoggerUtils.e("test", "KGsonUtils实体类转JSON数据异常:\t" + e.message)
        }
        return jsonObject
    }

    //fixme MutableList转化为JSONArray,
    //fixme MutableList和ArrayList两个都可以。ArrayList可以转化成MutableList
    //fixme 即MutableList兼容ArrayList
    inline fun <reified T : Any> parseJSONArray(list: MutableList<T>): JSONArray {
        val jsonArray = JSONArray()
        try {
            for (i in list.indices) {
                val jsonObject = parseJSON(list[i])
                jsonArray.put(i, jsonObject)
            }
        } catch (e: Exception) {
            KLoggerUtils.e("test", "KGsonUtils List转换JSONArray异常:\t" + e.message)
        }
        return jsonArray
    }

}