package cn.android.support.v7.lib.eep.kera.https.ok

import cn.android.support.v7.lib.eep.kera.https.KHttp
import cn.android.support.v7.lib.eep.kera.utils.KCachesUtils

/**
 * Created by 彭治铭 on 2018/6/6.
 */
//最后的可变参数，对应json解析的字段
abstract class KGenericsCallback(var https: KHttps? = null) {

    //开始
    open fun onStart() {
        https?.start?.let {
            it()
            //https?.start!!()
        }
        //fixme 显示进度条
        if (https?.load ?: false) {
            https?.showProgressbar()
        }
    }

    //成功
    open fun onSuccess(response: String) {
        var result = https?.onPostResponse(response) ?: ""//对服务器返回数据，在解析之前，优先做处理。如解密等
        https?.success?.let {
            it(result)
        }
        https?.let {
            if (it.cacle ?: false) {
                KHttp.getUrlUnique(it)?.let {
                    //缓存数据
                    KCachesUtils.put(it, result)
                }
            }
        }
        result?.let {
            onResponse(it)
        }
        //最后执行
        onFinish()
    }

    //失败【基本可以断定是网络异常】
    open fun onFailure(errStr: String?) {
        https?.failure?.let {
            it(errStr)
        }
        var response: String? = null
        https?.let {
            if (it.cacle ?: false) {
                KHttp.getUrlUnique(it)?.let {
                    //读取缓存数据
                    response = KCachesUtils.getString(it)
                }
            }
        }
        response?.let {
            onResponse(it)
        }
        //最后执行
        onFinish()
    }

    //返回的数据为服务器原始数据，或缓存数据。如果断网，且缓存数据为空，则返回空。
    open fun onResponse(response: String?) {

    }

    //结束，无论是成功还是失败都会调用。且最后执行
    open fun onFinish() {
        //fixme 关闭进度条
        if (https?.load ?: false) {
            https?.dismissProgressbar()
        }

        //结束回调（在进度条关闭之后，再回调。防止进度条和activity同时关闭。）
        https?.finish?.let {
            it()
        }

        //fixme 去除网络请求标志(网络请求结束)
        https?.let {
            KHttp.map.remove(KHttp.getUrlUnique(it))
        }

        https?.activity = null
        https?.headers?.clear()
        https?.params?.clear()
        https?.files?.clear()
        https?.body.let {
            https?.body = null
        }
        https?.url = null
        https = null
    }
}
