package cn.android.support.v7.lib.eep.kera.socket

/**
 * state 状态，true成功，false失败
 * text 原因，一般是失败的原因。成功一般为空
 */
open class KState(var isSuccess: Boolean, var text: String? = null) {}