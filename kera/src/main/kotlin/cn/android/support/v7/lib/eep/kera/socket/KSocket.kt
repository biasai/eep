package cn.android.support.v7.lib.eep.kera.socket

import android.util.Log
import kotlinx.coroutines.experimental.async
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.InputStream
import java.net.InetSocketAddress
import java.net.Socket

/**
 *       Socket客户端
 * ip    ip地址,默认是本机ip,fixme 注意哦，记录的是对方的ip地址。客服端记录的是服务端的ip;服务端记录是客户端的ip
 * port  端口号
 */
open class KSocket(var ip: String? = KIpPort.HostIp4(), var port: Int? = KIpPort.Port()) {
    var socket: Socket? = null
    var conTimeout = 10000//连接超时时间
    var ipPort: String? = null
        get() {
            return KIpPort.IpPort(ip, port)
        }

    init {
        //fixme 默认读取时间无限，即一直等待。如果设置了时间，超时了就会报超时异常。最好不要设置时间。
        //soTimeout = 15000//设置read方法的超时时间。单位毫秒。1000等于1秒
    }

    //重写关闭链接，一旦关闭，socke就无法再使用，需要重新实例化。
    fun close() {
        socket?.apply {
            if (isInputShutdown) {
                shutdownInput()//关闭输入流
            }
            if (isOutputShutdown) {
                shutdownOutput()//关闭输出流
            }
            close()
            socket = null
        }
        isRead = false
    }

    //判断是否链接
    fun isConnect(): Boolean {
        socket?.apply {
            if (!isClosed() && isConnected()) {
                return true
            }
        }
        return false
    }

    //fixme 连接服务端[读写的时候，会自动连接。]
    fun connect(callback: ((state: KState) -> Unit)? = null) {
        if (socket == null) {
            isRead = false
            socket = Socket()
        }
        socket?.let {
            if (it.isClosed) {
                isRead = false
                socket = Socket()//fixme 如果已经关闭，则需要重新实例化。因为一旦关闭就无法再使用。
            }
        }
        if (ip != null && port != null) {
            if (isConnect()) {
                callback?.let {
                    it(KState(true))//fixme 连接成功
                }
            } else {
                async {
                    socket?.apply {
                        try {
                            var socketAddress = InetSocketAddress(ip, this@KSocket.port!!)//fixme 注意了，socket自己也有port属性。之前就是port错误，才导致一直连接不上
                            connect(socketAddress, conTimeout)//fixme 这一步，链接的过程中，是阻塞线程的。设置连接超时时间
                            callback?.let {
                                it(KState(true))//连接成功
                            }
                            onConnectSuccess?.let {
                                it()//连接成功全局回调
                            }
                            readUTF()//fixme 服务器读取监听，不能调用多次。只能调用一次。因为一个流不能多个一起操作。
                        } catch (e: Exception) {
                            close()//异常断开
                            callback?.let {
                                it(KState(false, e.message))//fixme 连接失败
                            }
                            onConnectFail?.let {
                                it(e.message)
                            }
                        }
                    }
                }
            }
        } else {
            close()//异常断开
            callback?.let {
                it(KState(false, "ip地址为空"))//fixme 连接失败
            }
            onConnectFail?.let {
                it("ip地址为空")
            }
        }
    }

    var isRead = false//是否调用了读取。
    //读取对方信息(监听状态，需要等待对方写入),不要多次调用。不能对同一个流多次操作。
    private fun readUTF() {
        if (isConnect()&&!isRead) {
            isRead = true
            kotlin.run {
                async {
                    while (isConnect()) {
                        //无限循环读取
                        try {
                            var input: InputStream? = socket?.getInputStream()
                            input?.let {
                                var inputStream = DataInputStream(input)
                                var text = inputStream.readUTF()//fixme 读要等待写的完成，不然一直处于等待状态。
                                READ_LIST.forEach {
                                    it?.let {
                                        it(text)
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            //Log.e("test","读取异常:\t"+e.message)
                            close()//异常断开
                            onDisConnect?.let {
                                it(e.message)//异常断开
                            }
                        }
                    }
                }
            }
        }
    }

    private var READ_LIST = arrayListOf<((text: String) -> Unit)?>()
    //读取监听(这个可多次调用)，直接返回服务器信息
    fun readUTF(callback: (text: String) -> Unit) {
        if (!isConnect()) {
            connect()//fixme 主动连接
        } else if (!isRead) {
            //判断是否调用了读取。这个只能调用一次。
            readUTF()
        }
        READ_LIST.add(callback)
    }


    private var onDisConnect: ((text: String?) -> Unit)? = null
    //fixme 服务器连接断开。（服务器已经连接成功后，再断开。才会监听。）
    fun onDisConnect(onDisConnect: (text: String?) -> Unit) {
        this.onDisConnect = onDisConnect
    }

    private var onConnectSuccess: (() -> Unit)? = null
    //fixme 服务器连接成功回调
    fun onConnectSuccess(onConnectSuccess: () -> Unit) {
        this.onConnectSuccess = onConnectSuccess
    }

    private var onConnectFail: ((text: String?) -> Unit)? = null
    //fixme 服务器连接失败回调
    fun onConnectFail(onConnectFail: (text: String?) -> Unit) {
        this.onConnectFail = onConnectFail
    }

    //向对方写入信息（可以多次调用）
    fun writeUTF(text: String?, callback: ((state: KState) -> Unit)? = null) {
        text?.let {
            if (it.length > 0) {
                async {
                    try {
                        if (!isConnect()) {
                            //fixme 主动连接
                            connect {
                                if (it.isSuccess) {
                                    var out = DataOutputStream(socket?.outputStream)
                                    out.writeUTF(text)
                                    out.flush()//清除缓冲区
                                    callback?.let {
                                        it(KState(true, text))//fixme 发送成功记录一下发送的文本
                                    }
                                } else {
                                    callback?.apply {
                                        this(it)
                                    }
                                }
                            }
                        } else {
                            var out = DataOutputStream(socket?.outputStream)
                            out.writeUTF(text)
                            out.flush()//清除缓冲区
                            callback?.let {
                                it(KState(true, text))//fixme 发送成功记录一下发送的文本
                            }
                        }
                    } catch (e: Exception) {
                        close()//异常断开
                        callback?.let {
                            it(KState(false, e.message))
                        }
                    }
                }
            }
        }
    }

}