package cn.android.support.v7.lib.eep.kera.widget

import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.graphics.RectF
import android.graphics.drawable.*
import android.os.Build
import android.text.ClipboardManager
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.TranslateAnimation
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import cn.android.support.v7.lib.eep.kera.R
import cn.android.support.v7.lib.eep.kera.base.KView
import cn.android.support.v7.lib.eep.kera.bean.KRadius
import cn.android.support.v7.lib.eep.kera.common.kpx
import cn.android.support.v7.lib.eep.kera.https.bit.KBitmaps
import cn.android.support.v7.lib.eep.kera.utils.KTimerUtils
import cn.android.support.v7.lib.eep.kera.utils.KSelectorUtils
import cn.android.support.v7.lib.eep.kera.utils.KAssetsUtils
import cn.android.support.v7.lib.eep.kera.utils.KRegexUtils
import kotlinx.coroutines.experimental.async
import org.jetbrains.anko.*
import java.util.regex.Pattern

//            使用案例
//            var button=RoundButton(activity!!).apply {
//                selectorColor(Color.WHITE,Color.RED)
//                text="按钮"
//                tel(tel)
//                code(code)
//                password(pass)
//                confirmPassword(pass2)
//                addEditText(edit)
//                fixme onError会把按钮初始化为不可用，即：isEnabled=false
//                onError { error, eidt ->
//                    //错误信息，只有数据正确时才会处罚点击事件
//                    Toast.show(error)
//                }
//                onClick {
//                    Toast.show("点击事件")
//                }
//            }
//            addView(button)

/**
 * 自定义圆角按钮（自带数据校验）
 * 自带触摸阴影效果，支持波纹点击效果
 * 具备背景颜色渐变
 * Created by 彭治铭 on 2018/5/20.
 */
open class KRadiusButton : Button {

    constructor(viewGroup: ViewGroup) : super(viewGroup.context) {
        setLayerType(View.LAYER_TYPE_HARDWARE, null)//默认就开启硬件加速，不然圆角无效果
        viewGroup.addView(this)//直接添加进去,省去addView(view)
    }

    constructor(viewGroup: ViewGroup, HARDWARE: Boolean) : super(viewGroup.context) {
        if (HARDWARE) {
            setLayerType(View.LAYER_TYPE_HARDWARE, null)
        } else {
            setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        }
        viewGroup.addView(this)//直接添加进去,省去addView(view)
    }

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        val typedArray = context?.obtainStyledAttributes(attrs, R.styleable.RoundCornersRect)
        typedArray?.let {
            var all_radius = typedArray?.getDimension(R.styleable.RoundCornersRect_radian_all, 0f)
            left_top = typedArray?.getDimension(R.styleable.RoundCornersRect_radian_left_top, all_radius)
            left_bottom = typedArray?.getDimension(R.styleable.RoundCornersRect_radian_left_bottom, all_radius)
            right_top = typedArray?.getDimension(R.styleable.RoundCornersRect_radian_right_top, all_radius)
            right_bottom = typedArray?.getDimension(R.styleable.RoundCornersRect_radian_right_bottom, all_radius)
        }
    }

    //清除背景
    open fun clearBackground(){
        clearOriBackground()
    }

    open fun background(mcolor: String) {
        setBackgroundColor(Color.parseColor(mcolor))
    }

    open fun background(resId: Int) {
        setBackgroundResource(resId)
    }

    open fun background(bitmap: Bitmap) {
        if (Build.VERSION.SDK_INT >= 16) {
            background = BitmapDrawable(bitmap)
        } else {
            backgroundDrawable = BitmapDrawable(bitmap)
        }
    }

    /**
     * 复制文本
     * fixme isSelectable 所有的View都具备select选中能力,即文本框可以复制粘贴。
     * copyText 为要复制的文本内容。如果为空。则复制文本控件的文本。
     */
    fun copyText(copyText: String? = null) {
        if (context != null && context is Activity) {
            (context as Activity).apply {
                var cm: ClipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                if (copyText != null && copyText.length > 0) {
                    cm.setText(copyText)//复制指定文本
                } else {
                    cm.setText(getText())//复制控件文本
                }
            }
        }
    }

    //fixme 必须在all_radius等圆角属性设置完成之后再调用。
    //触摸点击效果。默认具备波浪效果
    open fun onPress(isRipple: Boolean = true) {
        if (isRipple) {
            //波浪效果
            selectorRippleDrawable(Color.WHITE, Color.parseColor("#E4E4E4"))
        } else {
            //普通点击效果
            selectorDrawable(Color.WHITE, Color.parseColor("#E4E4E4"))
        }
    }

    //手机号
    private var tel: EditText? = null
    var telError: String = "手机号格式不正确"

    fun tel(tel: EditText?, telError: String? = this.telError) {
        this.tel = tel
        telError?.let {
            this.telError = it
        }
        tel?.apply {
            addTextChanged(this)
        }
    }

    //手机号2(可能会有两个手机号,如：新手机号和旧手机号)
    private var tel2: EditText? = null
    var tel2Error: String = "手机号格式不正确"

    fun tel2(tel2: EditText?, tel2Error: String? = this.tel2Error) {
        this.tel2 = tel2
        tel2Error?.let {
            this.tel2Error = it
        }
        tel2?.apply {
            addTextChanged(this)
        }
    }

    //判断是否为空
    fun isEmpty(text: String?): Boolean {
        text?.let {
            if (it.trim().length > 0) {
                return false//不为空
            }
        }
        return true//空
    }

    //密码正则表达式
    //正则表达式一 ，复杂（大小写字母、数字、特殊符号 四选三）,并且长度不能小于八位
    var passwordPatten: String? = "^(?![a-zA-Z]+$)(?![A-Z0-9]+$)(?![A-Z\\W_]+$)(?![a-z0-9]+$)(?![a-z\\W_]+$)(?![0-9\\W_]+$)[a-zA-Z0-9\\W_]{8,}$"
    //正则表达式二，简单
    //var passwordPatten2: String? = "^[0-9A-Za-z]{6,20}\$"//由6-20字母和数字组成
    var passwordPatten2: String? = "^.{6,20}\$"//由6-20位任意字符组成 .匹配除 "/n" 之外的任何单个字符

    //fixme 判断密码是否正确
    fun isPasswordCorrect(password: String): Boolean {
        var patten = passwordPatten//优先使用正则表达式一
        if (isEmpty(patten)) {
            patten = passwordPatten2//其次使用正则表达式二
        }
        if (isEmpty(patten)) {
            return true//如果两个正则都为空，则不进行判断，直接判断为正确
        }
        val p = Pattern.compile(patten)
        val m = p.matcher(password)
        return m.matches()
    }

    //密码
    private var password: EditText? = null
    var passwordError: String = "密码格式不正确"

    fun password(password: EditText?, passwordError: String? = this.passwordError) {
        this.password = password
        passwordError?.let {
            this.passwordError = it
        }
        password?.apply {
            addTextChanged(this)
        }
    }

    //fixme 只对password和password2作对比判断，password3是另外的，不做对比判断
    //再次确认重复密码，和第一个密码作比较
    private var password2: EditText? = null
    var password2Error: String = "两次输入的密码不一致"

    fun password2(password2: EditText?, password2Error: String? = this.password2Error) {
        this.password2 = password2
        password2Error?.let {
            this.password2Error = it
        }
        password2?.apply {
            addTextChanged(this)
        }
    }

    //防止要输入第三个密码，如：旧密码
    private var password3: EditText? = null
    var password3Error: String = "密码格式不正确"

    fun password3(password3: EditText?, password3Error: String? = this.password3Error) {
        this.password3 = password3
        password3Error?.let {
            this.password3Error = it
        }
        password3?.apply {
            addTextChanged(this)
        }
    }

    //验证码
    private var code: EditText? = null
    var codeError: String = "请先获取验证码"
    fun code(code: EditText?, codeError: String? = this.codeError) {
        this.code = code
        codeError?.let {
            this.codeError = it
        }
        code?.apply {
            addTextChanged(this)
        }
    }

    //fixme 正确的验证码，获取验证码后，需要手动的赋值。会和验证码文本框的值做比较
    //fixme 如果code2为空字符"",表示不需要判断验证码是否正确。本地不判断，交给服务器取判断。
    //fixme 为null,表示用户没有点击获取验证码。提示用户获取验证码。
    //fixme 如果不为null也不为空字符串"",则会判断验证是否正确。

    //fixme null 提醒用户获取验证码（用户还没获取验证码）
    //fixme ""空字符，用户已经获取验证码，不会判断验证码是否正确。
    var code2: String? = null
    var code2Error: String = "验证码不正确"
    fun code2(code2: String?, code2Error: String? = this.code2Error) {
        this.code2 = code2
        code2Error?.let {
            this.code2Error = it
        }
    }

    //身份证号
    private var idCard: EditText? = null
    var idCardError: String = "身份证号格式不正确"
    fun idCard(idCard: EditText?, idCardError: String? = this.idCardError) {
        this.idCard = idCard
        idCardError?.let {
            this.idCardError = it
        }
        idCard?.apply {
            addTextChanged(this)
        }
    }

    //银行卡号
    private var bankNo: EditText? = null
    var bankNoError: String = "银行卡号格式不正确"
    fun bankNo(bankNo: EditText?, bankNoError: String? = this.bankNoError) {
        this.bankNo = bankNo
        bankNoError?.let {
            this.bankNoError = it
        }
        bankNo?.apply {
            addTextChanged(this)
        }
    }

    //邮箱
    private var email: EditText? = null
    var emailError: String = "邮箱格式不正确"

    fun email(email: EditText?, emailError: String? = this.emailError) {
        this.email = email
        emailError?.let {
            this.emailError = it
        }
        email?.apply {
            addTextChanged(this)
        }
    }

    //fixme 其它普通的文本输入框集合。主要监听不能为空。
    private var editTextList = mutableListOf<EditText>()

    //普通文本框
    fun addEditText(editText: EditText?) {
        editText?.apply {
            addTextChanged(this)
            editTextList.add(this)
        }
    }

    //fixme 其它普通的文本输入框集合,对TextVeiw文本也进行监听。主要监听不能为空。
    private var textViewtList = mutableListOf<TextView>()

    //普通文本框
    fun addTextView(textView: TextView?) {
        textView?.apply {
            addTextChanged(this)
            textViewtList.add(this)
        }
    }

    //同意，即协议。必须同意了，才能触发点击事件。
    private var agreeViewList = mutableListOf<KView>()

    //普通协议，isSelected选中即表示同意。
    fun addAgreeView(view: KView?) {
        view?.apply {
            this.addSelected {
                isEnable()//选中状态发生改变时，判断是否可用。
            }
            agreeViewList.add(this)
        }
    }

    //文本监听[主要监听是否为空]
    private fun addTextChanged(editText: TextView?) {
        editText?.apply {
            addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(p0: Editable?) {
                    isEnable()
                }

                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            })
        }
    }

    //判断按钮是否可用
    private var isEnable = true

    private fun isEnable() {
        isEnable = true
        isEmpty(tel)
        isEmpty(tel2)
        isEmpty(password)
        isEmpty(password2)
        isEmpty(password3)
        isEmpty(code)
        isEmpty(email)
        isEmpty(idCard)
        isEmpty(bankNo)
        //普通文本框集合，主要就是判断是否为空。
        editTextList.forEach() {
            isEmpty(it)
        }
        textViewtList.forEach() {
            isEmpty(it)
        }
        //同意控件，主要判断是否选中。选中就表示同意
        agreeViewList.forEach {
            if (!it.isSelected) {
                isEnable = false//没有选中，就不能用。
            }
        }
        //可用状态和选中状态绑定
        this.isEnabled = isEnable
        isSelected = isEnable
    }

    //判断文本是否为空
    private fun isEmpty(editText: TextView?) {
        editText?.apply {
            if (this.text.toString().trim().length <= 0) {
                isEnable = false//空,不可用
            }
        }
    }

    var errorEditText: EditText? = null
    //fixme 错误信息回调函数，交给调用者去实现。返回校验错误信息
    var onError: ((error: String, edit: KRadiusEditText?) -> Unit)? = null

    //fixme 返回错误信息，和错误文本框。该方法在点击事件的前面。
    //fixme 只有数据正确了才会触发点击事件。
    fun onError(onError: (error: String, edit: KRadiusEditText?) -> Unit) {
        isEnabled = false//默认不可用，只有所有数据不为空的情况下才可用。
        isSelected = false
        this.onError = onError
    }

    //fixme 点击事件，onError()没有错误返回时，才会触发
    fun onClick(onClick: () -> Unit) {
        setOnClickListener {
            //fixme 防止快速点击
            if (!isFastClick()) {
                var isRegular = true//判断数据是否正确，默认正确。
                onError?.let {
                    var error: String? = onRegular()
                    error?.apply {
                        isRegular = false//数据错误。
                        if (errorEditText != null) {
                            if (errorEditText is KRadiusEditText) {
                                it(this, errorEditText as KRadiusEditText)
                            } else {
                                it(this, null)
                            }
                        } else {
                            it(this, null)
                        }
                    }
                }
                if (isRegular) {
                    onClick()//fixme 如果数据校验正确，才会触发点击事件
                }
            }
        }
    }

    //fixme 正则判断，返回错误的信息，如果正确则返回为空。
    open fun onRegular(): String? {
        errorEditText = null
        tel?.apply {
            if (!KRegexUtils.getInstance().isMobileNO(this.text.toString().trim())) {
                errorEditText = this
                return telError//"手机号格式不正确"
            } else if (this is KRadiusEditText) {
                this.onError(false)//正确，就不显示错误图片。
            }
        }

        password3?.apply {
            if (!isPasswordCorrect(this.text.toString())) {
                errorEditText = this
                return password3Error//密码格式不正确（旧密码，一般旧密码都在前，所以先判断）
            } else if (this is KRadiusEditText) {
                this.onError(false)//正确，就不显示错误图片。
            }
        }

        password?.apply {
            if (!isPasswordCorrect(this.text.toString())) {
                errorEditText = this
                return passwordError//密码格式不正确
            } else if (this is KRadiusEditText) {
                this.onError(false)//正确，就不显示错误图片。
            }
        }

        //fixme 只对password和password2作对比判断，password3是另外的，不做对比判断
        if (password != null && password2 != null) {
            var p1 = password?.text.toString()
            var p2 = password2?.text.toString()
            if ((!p1.equals(p2))) {
                errorEditText = password2
                return password2Error//"两次输入的密码不一致"
            } else if (this is KRadiusEditText) {
                this.onError(false)
            }
        }
        tel2?.apply {
            if (!KRegexUtils.getInstance().isMobileNO(this.text.toString().trim())) {
                errorEditText = this
                return tel2Error//"手机号格式不正确"（新手机号）
            } else if (this is KRadiusEditText) {
                this.onError(false)//正确，就不显示错误图片。
            }
        }
        email?.apply {
            if (!KRegexUtils.getInstance().isEmail(this.text.toString().trim())) {
                errorEditText = this
                return emailError//"邮箱格式不正确"
            } else if (this is KRadiusEditText) {
                this.onError(false)
            }
        }

        idCard?.apply {
            if (!KRegexUtils.getInstance().isIdCard(this.text.toString().trim())) {
                errorEditText = this
                return idCardError//"身份证号格式不正确"
            } else if (this is KRadiusEditText) {
                this.onError(false)
            }
        }

        bankNo?.apply {
            if (!KRegexUtils.getInstance().isBankCard(this.text.toString().trim())) {
                errorEditText = this
                return bankNoError//"银行卡号格式不正确"
            } else if (this is KRadiusEditText) {
                this.onError(false)
            }
        }
        //fixme 验证码最后判断
        code?.apply {
            //fixme code2是真实验证码，需要手动赋值。
            //fixme 如果code2为空字符"",表示不需要判断验证码是否正确。
            //fixme 为null,表示用户没有点击获取验证码。
            if (code2 == null || code2?.trim().equals("")) {
                if (code2 == null) {
                    return codeError//"请先获取验证码"
                }
            } else {
                //真实验证码如果不为空，就进行判断
                code2?.let {
                    if (!this.text.toString().trim().equals(it.trim())) {
                        errorEditText = this
                        return code2Error//"验证码不正确"
                    } else if (this is KRadiusEditText) {
                        this.onError(false)
                    }
                }
            }

        }
        return null
    }


    var bindView: View? = null//状态绑定的View
        set(value) {
            field = value
            if (value != null) {
                if (value is KView) {
                    if (value.bindView == null) {
                        value.bindView = this//相互绑定
                    }
                } else if (value is KRadiusButton) {
                    if (value.bindView == null) {
                        value.bindView = this//相互绑定
                    }
                } else if (value is KRadiusRelativeLayout) {
                    if (value.bindView == null) {
                        value.bindView = this//相互绑定
                    }
                }
            }
        }

    fun bindView(bindView: View?) {
        this.bindView = bindView
    }

    //状态同步
    fun bindSycn() {
        bindView?.let {
            it.isSelected = isSelected
            it.isPressed = isPressed
        }
    }

    //fixme selectorDrawable(R.mipmap.p_dont_agree,null, R.mipmap.p_agree)
    //fixme 注意，如果要用选中状态，触摸状态最好设置为null空。不会有卡顿冲突。
    //重写选中状态。isSelected=true。选中状态。一定要手动调用。
    override fun setSelected(selected: Boolean) {
        super.setSelected(selected)
        bindView?.let {
            if (it.isSelected != isSelected) {
                it?.isSelected = isSelected//选中状态
            }
        }
        onSelectChangedList.forEach {
            it?.let {
                it(selected)//选中监听
            }
        }
    }

    //fixme 监听选中状态。防止多个监听事件冲突，所以添加事件数组。
    private var onSelectChanged: ((selected: Boolean) -> Unit)? = null
    private var onSelectChangedList = mutableListOf<((selected: Boolean) -> Unit)?>()
    fun addSelected(onSelectChanged: ((selected: Boolean) -> Unit)) {
        onSelectChanged.let {
            onSelectChangedList?.add(it)
        }
    }

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        var b = super.dispatchTouchEvent(event)
        //防止点击事件冲突。所以。一定要放到super()后面。
        event?.let {
            when (it.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN, MotionEvent.ACTION_MOVE -> {
                    bindView?.isPressed = true//按下状态
                    isPressed = true
                    invalidate()
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                    bindView?.isPressed = false
                    isPressed = false
                    invalidate()
                }
                MotionEvent.ACTION_CANCEL -> {
                    //其他异常
                    bindView?.isPressed = false
                    invalidate()
                }
            }
        }
        return b
    }

    var all_radius: Float = 0F//默认，所有圆角的角度

    var left_top: Float = 0f//左上角

    var left_bottom: Float = 0f//左下角

    var right_top = 0f//右上角

    var right_bottom = 0f//右下角

    var strokeWidth = 0f//边框宽度

    var strokeColor = Color.TRANSPARENT//边框颜色


    //fixme 边框颜色渐变
    var strokeGradientStartColor = Color.TRANSPARENT//渐变开始颜色
    var strokeGradientEndColor = Color.TRANSPARENT//渐变结束颜色
    //fixme 渐变颜色数组值【均匀渐变】，gradientColors优先
    var strokeGradientColors: IntArray? = null
    var ORIENTATION_VERTICAL = 0//垂直
    var ORIENTATION_HORIZONTAL = 1//水平
    var strokeGradientOritation = ORIENTATION_HORIZONTAL//渐变颜色方向，默认水平

    fun strokeGradientColors(vararg color: Int) {
        strokeGradientColors = color
    }

    fun strokeGradientColors(vararg color: String) {
        strokeGradientColors = IntArray(color.size)
        strokeGradientColors?.apply {
            if (color.size > 1) {
                for (i in 0..color.size - 1) {
                    this[i] = Color.parseColor(color[i])
                }
            } else {
                this[0] = Color.parseColor(color[0])
            }
        }
    }

    // 两次点击按钮之间的点击间隔不能少于1000毫秒（即1秒）
    var MIN_CLICK_DELAY_TIME = 1000
    var lastClickTime: Long = System.currentTimeMillis()//记录最后一次点击时间

    //判断是否快速点击，true是快速点击，false不是
    open fun isFastClick(): Boolean {
        var flag = false
        var curClickTime = System.currentTimeMillis()
        if ((curClickTime - lastClickTime) <= MIN_CLICK_DELAY_TIME) {
            flag = true//快速点击
        }
        lastClickTime = curClickTime
        return flag
    }

    //fixme 初始化=================================================================================
    init {
        setLayerType(View.LAYER_TYPE_HARDWARE, null)//开启硬件加速
        KView.typeface?.let {
            if (KView.isGlobal) {
                typeface = it//fixme 设置全局自定义字体
            }
        }
        textSize = kpx.textSizeX(30f)
        textColor = Color.WHITE
        gravity = Gravity.CENTER
        padding = 0//内补丁设置为0。防止他脑抽
    }

    /**
     * fixme 更多（显示不全时）显示三个点...
     * fixme 【设置了显示更多，文本垂直居中就无效了。始终与顶部对齐,但是可以使用topPadding控制文本垂直位置。】
     * lines 显示的最大行数。
     */
    fun setMore(lines: Int = 1) {
        //能水平滚动较长的文本内容。不要用这个。圆角会没有效果的。就是这个搞的圆角没有效果。
        //setHorizontallyScrolling(true)
        //setSingleLine(true)//是否單行顯示。过时了。也会导致圆角没有效果。
        //fixme 上面两个属性导致圆角无效。不要使用。TextView,editText,button都会导致圆角无效。

        setMaxLines(lines);//fixme 显示最大行,这个也是关键。setMaxLines和setEllipsize同时设置，才会显示更多。
        //代码不换行，更多显示三个点...
        setEllipsize(TextUtils.TruncateAt.END)//fixme 这个才是关键，会显示更多
    }

    //fixme 清空原始背景
    fun clearOriBackground() {
        if (Build.VERSION.SDK_INT >= 16) {
            backgroundColor = Color.TRANSPARENT
            background = null
        } else {
            backgroundColor = Color.TRANSPARENT
            backgroundDrawable = null
        }
    }

    //这个背景图片，会铺满整个控件。不会对位图进行适配。只会对图片矩阵（拉伸）处理。就和背景图片一样
    private var autoMatrixBg: Bitmap? = null

    //设置控件的高度和高度
    // matchParent:	-1 wrapContent:	-2
    open fun layoutParams(width: Int, height: Int) {
        layoutParams?.apply {
            //设置宽和高
            this.width = width
            this.height = height
            requestLayout()
        }
    }

    //记录autoMatrixBg拉伸之后的宽度和高度
    var autoMatrixBgWidth: Int = 0
    var autoMatrixBgHeight: Int = 0
    //设置矩阵的宽和高，不是图片。对图片继续拉伸处理
    //fixme 设置矩阵拉伸后的宽度和高度,参数Int是实际拉伸后的宽度和高度
    open fun autoMatrixBgScale(width: Int = this.w, height: Int = this.h) {
        autoMatrixBg?.let {
            if (!it.isRecycled){
                autoMatrixBgWidth = width
                autoMatrixBgHeight = height
                invalidate()
            }
        }
    }

    //fixme 设置矩阵拉伸后的比率。1是原图的比率。,参数Float是实际拉伸后的比率。实际宽度和高度。会自行计算
    open fun autoMatrixBgScale(sx: Float = 1f, sy: Float = 1f) {
        autoMatrixBg?.apply {
            if (!isRecycled) {
                autoMatrixBgWidth = (width * sx).toInt()
                autoMatrixBgHeight = (height * sy).toInt()
                invalidate()
            }
        }
    }

    open fun drawAutoMatrixBg(canvas: Canvas, paint: Paint) {
        autoMatrixBg?.apply {
            if (!isRecycled) {
                paint.isAntiAlias = true
                paint.isDither = true
                if (autoMatrixBgWidth <= 0) {
                    autoMatrixBgWidth = width
                }
                if (autoMatrixBgHeight <= 0) {
                    autoMatrixBgHeight = height
                }
                var offsetLeft = (autoMatrixBgWidth - width) / 2
                var offsetTop = (autoMatrixBgHeight - height) / 2
                if (isAutoCenter) {
                    //canvas.drawBitmap(this, kpx.centerBitmapX(this, w.toFloat()) + autoLeftPadding, kpx.centerBitmapY(this, h.toFloat()) + autoTopPadding, paint)
                    var left = kpx.centerBitmapX(this, w.toFloat()) + autoLeftPadding - offsetLeft
                    var top = kpx.centerBitmapY(this, h.toFloat()) + autoTopPadding - offsetTop
                    var right = left + autoMatrixBgWidth
                    var bottom = top + autoMatrixBgHeight
                    canvas.drawBitmap(this, null, RectF(left, top, right, bottom), paint)
                } else if (isAutoCenterHorizontal) {
                    //canvas.drawBitmap(this, kpx.centerBitmapX(this, w.toFloat()) + autoLeftPadding, autoTopPadding, paint)
                    var left = kpx.centerBitmapX(this, w.toFloat()) + autoLeftPadding - offsetLeft
                    var top = autoTopPadding - offsetTop
                    var right = left + autoMatrixBgWidth
                    var bottom = top + autoMatrixBgHeight
                    canvas.drawBitmap(this, null, RectF(left, top, right, bottom), paint)
                } else if (isAutoCenterVertical) {
                    //canvas.drawBitmap(this, autoLeftPadding, kpx.centerBitmapY(this, h.toFloat()) + autoTopPadding, paint)
                    var left = autoLeftPadding - offsetLeft
                    var top = kpx.centerBitmapY(this, h.toFloat()) + autoTopPadding - offsetTop
                    var right = left + autoMatrixBgWidth
                    var bottom = top + autoMatrixBgHeight
                    canvas.drawBitmap(this, null, RectF(left, top, right, bottom), paint)
                } else {
                    //canvas.drawBitmap(this, autoLeftPadding, autoTopPadding, paint)
                    var left = autoLeftPadding - offsetLeft
                    var top = autoTopPadding - offsetTop
                    var right = left + autoMatrixBgWidth
                    var bottom = top + autoMatrixBgHeight
                    canvas.drawBitmap(this, null, RectF(left, top, right, bottom), paint)
                }
            }
        }
    }

    fun autoMatrixBg(bitmap: Bitmap?) {
        this.autoMatrixBg = bitmap
        if (context != null && context is Activity) {
            context.runOnUiThread {
                if (isAutoWH) {
                    requestLayout()
                } else {
                    invalidate()
                }
            }
        }
    }

    fun autoMatrixBg(resId: Int, width: Int=0, height: Int=0, isRGB_565: Boolean = false) {
        this.autoMatrixBg = KAssetsUtils.getInstance().getBitmapFromAssets(null, resId, isRGB_565)
        if (width>=0&&height>=0){
            autoMatrixBg?.let {
                if (!it.isRecycled){
                    autoMatrixBg=kpx.xBitmap(it,width,height,true)
                }
            }
        }
        if (context != null && context is Activity) {
            context.runOnUiThread {
                if (isAutoWH) {
                    requestLayout()
                } else {
                    invalidate()
                }
            }
        }
    }

    fun autoMatrixBgFromAssets(assetsPath: String, width: Int=0, height: Int=0, isRGB_565: Boolean = false) {
        this.autoMatrixBg = KAssetsUtils.getInstance().getBitmapFromAssets(assetsPath, 0, isRGB_565)
        if (width>=0&&height>=0){
            autoMatrixBg?.let {
                if (!it.isRecycled){
                    autoMatrixBg=kpx.xBitmap(it,width,height,true)
                }
            }
        }
        if (context != null && context is Activity) {
            context.runOnUiThread {
                if (isAutoWH) {
                    requestLayout()
                } else {
                    invalidate()
                }
            }
        }
    }
    /**
     * @param url 网络地址
     * @param width 位图的宽度,默认0是服务器原图的尺寸（之后不会对位图进行适配，只会进行拉伸处理）。
     * @param height 位图的高度
     * @param isLoad 是否显示进度条
     * @param isRepeat 网络是否允许重复加载
     */
    fun autoMatrixBgFromUrl(url: String?, width: Int=0, height: Int=0, isLoad: Boolean = false, isRepeat: Boolean = false,finish:((bitmap:Bitmap)->Unit)?=null) {
        //Log.e("test", "宽度和高度:\t" + width + "\t" + height)
        if (isLoad && context != null && context is Activity) {
            KBitmaps(url).optionsRGB_565(false).showLoad(context as Activity).repeat(isRepeat).width(width).height(height).get() {
                autoMatrixBg = it
                if (context != null && context is Activity) {
                    context.runOnUiThread {
                        if (isAutoWH) {
                            requestLayout()
                        } else {
                            invalidate()
                        }
                        finish?.let {
                            autoMatrixBg?.apply {
                                if (!isRecycled){
                                    it(this)
                                }
                            }
                        }
                    }
                }
            }
        } else {
            KBitmaps(url).optionsRGB_565(false).showLoad(false).repeat(isRepeat).width(width).height(height).get() {
                //Log.e("test", "成功:\t" + it.width)
                autoMatrixBg = it
                if (context != null && context is Activity) {
                    context.runOnUiThread {
                        if (isAutoWH) {
                            requestLayout()
                        } else {
                            invalidate()
                        }
                        finish?.let {
                            autoMatrixBg?.apply {
                                if (!isRecycled){
                                    it(this)
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    //这个背景图片，会铺满整个控件
    private var autoUrlBg: Bitmap? = null//fixme 自定义网络背景图片,对图片是否为空，是否释放，做了判断。防止奔溃。比原生的背景图片更安全。

    fun autoUrlBg(bitmap: Bitmap?) {
        this.autoUrlBg = bitmap
        if (context != null && context is Activity) {
            context.runOnUiThread {
                if (isAutoWH) {
                    requestLayout()
                } else {
                    invalidate()
                }
            }
        }
    }

    fun autoUrlBg(resId: Int, width: Int=0, height: Int=0,isRGB_565: Boolean = false) {
        this.autoUrlBg = KAssetsUtils.getInstance().getBitmapFromAssets(null, resId, isRGB_565)
        if (width>=0&&height>=0){
            autoUrlBg?.let {
                if (!it.isRecycled){
                    autoUrlBg=kpx.xBitmap(it,width,height,true)
                }
            }
        }
        if (context != null && context is Activity) {
            context.runOnUiThread {
                if (isAutoWH) {
                    requestLayout()
                } else {
                    invalidate()
                }
            }
        }
    }

    fun autoUrlBgFromAssets(assetsPath: String,width: Int=0, height: Int=0, isRGB_565: Boolean = false) {
        this.autoUrlBg = KAssetsUtils.getInstance().getBitmapFromAssets(assetsPath, 0, isRGB_565)
        if (width>=0&&height>=0){
            autoUrlBg?.let {
                if (!it.isRecycled){
                    autoUrlBg=kpx.xBitmap(it,width,height,true)
                }
            }
        }
        if (context != null && context is Activity) {
            context.runOnUiThread {
                invalidate()
            }
        }
    }


    //fixme 防止无法获取宽和高，所以延迟100毫秒，这样就能获取控件的宽度和高度了。
    fun autoUrlBgDelay(url: String?, delay: Long = 100) {
        if (w <= 0 || h <= 0) {
            //无法获取宽度和高度，就延迟再获取
            async {
                kotlinx.coroutines.experimental.delay(delay)
                autoUrlBg(url)
            }
        } else {
            autoUrlBg(url)
        }
    }

    /**
     * url 网络图片地址
     * isLoad 是否显示进度条，默认不显示
     * isRepeat 是否允许重复加载（网络重复请求）
     * fixme width,height位图的宽和高(最好手动设置一下，或者延迟一下，不能无法获取宽和高)
     */
    fun autoUrlBg(url: String?, isLoad: Boolean = false, isRepeat: Boolean = false, width: Int = this.w, height: Int = this.h,finish:((bitmap:Bitmap)->Unit)?=null) {
        if (isLoad && context != null && context is Activity) {
            KBitmaps(url).optionsRGB_565(false).showLoad(context as Activity).repeat(isRepeat).width(width).height(height).get() {
                autoUrlBg = it
                if (context != null && context is Activity) {
                    context.runOnUiThread {
                        if (isAutoWH) {
                            requestLayout()
                        } else {
                            invalidate()
                        }
                        finish?.let {
                            autoUrlBg?.apply {
                                if (!isRecycled){
                                    it(this)
                                }
                            }
                        }
                    }
                }
            }
        } else {
            KBitmaps(url).optionsRGB_565(false).showLoad(false).repeat(isRepeat).width(width).height(height).get() {
                //Log.e("test", "成功:\t" + it.width)
                autoUrlBg = it
                if (context != null && context is Activity) {
                    context.runOnUiThread {
                        if (isAutoWH) {
                            requestLayout()
                        } else {
                            invalidate()
                        }
                        finish?.let {
                            autoUrlBg?.apply {
                                if (!isRecycled){
                                    it(this)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 重新自定义背景图片(为了适配正确，位图最好都放在nodpi文件夹里。)
     */
    private var autoDefaultBg: Bitmap? = null//fixme 默认图片

    fun autoDefaultBg(bitmap: Bitmap?) {
        this.autoDefaultBg = bitmap
        if (context != null && context is Activity) {
            context.runOnUiThread {
                if (isAutoWH) {
                    requestLayout()
                } else {
                    invalidate()
                }
            }
        }
    }

    fun autoDefaultBg(resId: Int, width: Int = 0, height: Int = 0, isRGB_565: Boolean = false) {
        autoDefaultBg = KAssetsUtils.getInstance().getBitmapFromAssets(null, resId, isRGB_565)
        autoDefaultBg?.let {
            autoDefaultBg = kpx.xBitmap(it, width, height)//自动适配
        }
        if (isAutoWH) {
            requestLayout()
        }else{
            invalidate()
        }
    }

    fun autoDefaultBg(assetsPath: String, width: Int = 0, height: Int = 0, isRGB_565: Boolean = false) {
        autoDefaultBg = KAssetsUtils.getInstance().getBitmapFromAssets(assetsPath, 0, isRGB_565)
        autoDefaultBg?.let {
            autoDefaultBg = kpx.xBitmap(it, width, height)//自动适配
        }
        if (isAutoWH) {
            requestLayout()
        }else{
            invalidate()
        }
    }

    private var autoPressBg: Bitmap? = null//fixme 按下图片
    fun autoPressBg(bitmap: Bitmap?) {
        this.autoPressBg = bitmap
        if (context != null && context is Activity) {
            context.runOnUiThread {
                if (isAutoWH) {
                    requestLayout()
                } else {
                    invalidate()
                }
            }
        }
    }

    fun autoPressBg(resId: Int, width: Int = 0, height: Int = 0, isRGB_565: Boolean = false) {
        autoPressBg = KAssetsUtils.getInstance().getBitmapFromAssets(null, resId, isRGB_565)
        autoPressBg?.let {
            autoPressBg = kpx.xBitmap(it, width, height)//自动适配
        }
        if (isAutoWH) {
            requestLayout()
        }else{
            invalidate()
        }
        isClickable = true//具备点击能力
    }

    fun autoPressBg(assetsPath: String, width: Int = 0, height: Int = 0, isRGB_565: Boolean = false) {
        autoPressBg = KAssetsUtils.getInstance().getBitmapFromAssets(assetsPath, 0, isRGB_565)
        autoPressBg?.let {
            autoPressBg = kpx.xBitmap(it, width, height)//自动适配
        }
        if (isAutoWH) {
            requestLayout()
        }else{
            invalidate()
        }
        isClickable = true//具备点击能力
    }

    private var autoSelectBg: Bitmap? = null//fixme 选中图片（优先级最高）
    fun autoSelectBg(bitmap: Bitmap?) {
        this.autoSelectBg = bitmap
        if (context != null && context is Activity) {
            context.runOnUiThread {
                if (isAutoWH) {
                    requestLayout()
                } else {
                    invalidate()
                }
            }
        }
    }

    fun autoSelectBg(resId: Int, width: Int = 0, height: Int = 0, isRGB_565: Boolean = false) {
        autoSelectBg = KAssetsUtils.getInstance().getBitmapFromAssets(null, resId, isRGB_565)
        autoSelectBg?.let {
            autoSelectBg = kpx.xBitmap(it, width, height)//自动适配
        }
        if (isAutoWH) {
            requestLayout()
        }else{
            invalidate()
        }
        isClickable = true//具备点击能力
    }

    fun autoSelectBg(assetsPath: String, width: Int = 0, height: Int = 0, isRGB_565: Boolean = false) {
        autoSelectBg = KAssetsUtils.getInstance().getBitmapFromAssets(assetsPath, 0, isRGB_565)
        autoSelectBg?.let {
            autoSelectBg = kpx.xBitmap(it, width, height)//自动适配
        }
        if (isAutoWH) {
            requestLayout()
        }else{
            invalidate()
        }
        isClickable = true//具备点击能力
    }

    //fixme 防止触摸状态和选中状态冲突，会出现一闪的情况。把触摸状态制空。
    //fixme autoBg(R.mipmap.p_second_gou_gay,null, R.mipmap.p_second_gou_blue)
    fun autoBg(default: Int, press: Int? = default, select: Int? = press, width: Int = 0, height: Int = 0, isRGB_565: Boolean = false) {
        autoDefaultBg(default, width, height, isRGB_565)
        if (press == default) {
            autoPressBg = autoDefaultBg
        } else {
            press?.apply {
                autoPressBg(this, width, height, isRGB_565)
                isClickable = true//具备点击能力
            }
        }
        if (press == select) {
            autoSelectBg = autoPressBg
        } else {
            select?.apply {
                autoSelectBg(this, width, height, isRGB_565)
                isClickable = true//具备点击能力
            }
        }
    }

    fun autoBg(default: String, press: String? = default, select: String? = press, width: Int = 0, height: Int = 0, isRGB_565: Boolean = false) {
        autoDefaultBg(default, width, height, isRGB_565)
        if (press == default || press.equals(default)) {
            autoPressBg = autoDefaultBg
        } else {
            press?.apply {
                autoPressBg(this, width, height, isRGB_565)
                isClickable = true//具备点击能力
            }
        }
        if (press == select || press.equals(select)) {
            autoSelectBg = autoPressBg
        } else {
            select?.apply {
                autoSelectBg(this, width, height, isRGB_565)
                isClickable = true//具备点击能力
            }
        }
    }

    //fixme 来自sd卡,普通
    fun autoDefaultBgFromFile(filePath: String, width: Int = 0, height: Int = 0, isRGB_565: Boolean = false) {
        autoDefaultBg = KAssetsUtils.getInstance().getBitmapFromFile(filePath, isRGB_565)
        autoDefaultBg?.let {
            autoDefaultBg = kpx.xBitmap(it, width, height)//自动适配
        }
        if (isAutoWH) {
            requestLayout()
        }else{
            invalidate()
        }
    }

    //fixme 来自sd卡,触摸
    fun autoPressBgFromFile(filePath: String, width: Int = 0, height: Int = 0, isRGB_565: Boolean = false) {
        autoPressBg = KAssetsUtils.getInstance().getBitmapFromFile(filePath, isRGB_565)
        autoPressBg?.let {
            autoPressBg = kpx.xBitmap(it, width, height)//自动适配
        }
        if (isAutoWH) {
            requestLayout()
        }else{
            invalidate()
        }
        isClickable = true//具备点击能力
    }

    //fixme 来自sd卡,选中
    fun autoSelectBgFromFile(filePath: String, width: Int = 0, height: Int = 0, isRGB_565: Boolean = false) {
        autoSelectBg =KAssetsUtils.getInstance().getBitmapFromFile(filePath, isRGB_565)
        autoSelectBg?.let {
            autoSelectBg = kpx.xBitmap(it, width, height)//自动适配
        }
        if (isAutoWH) {
            requestLayout()
        }else{
            invalidate()
        }
        isClickable = true//具备点击能力
    }

    //fixme 来自sd卡,普通，触摸，选中
    fun autoBgFromFile(default: String, press: String? = default, select: String? = press, width: Int = 0, height: Int = 0, isRGB_565: Boolean = false) {
        autoDefaultBgFromFile(default, width, height, isRGB_565)//fixme 普通
        if (press == default || press.equals(default)) {
            autoPressBg = autoDefaultBg
        } else {
            press?.apply {
                autoPressBgFromFile(this, width, height, isRGB_565)//fixme 触摸
                isClickable = true//具备点击能力
            }
        }
        if (press == select || press.equals(select)) {
            autoSelectBg = autoPressBg
        } else {
            select?.apply {
                autoSelectBgFromFile(this, width, height, isRGB_565)//fixme 选中
                isClickable = true//具备点击能力
            }
        }
    }

    var isAutoWH = true//fixme 控件的宽度和高度是否为自定义位图的宽度和高度。默认是
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var w = 0
        var h = 0
        if (isAutoWH) {
            autoDefaultBg?.apply {
                if (!isRecycled) {
                    if (width > w) {
                        w = width
                    }
                    if (height > h) {
                        h = height
                    }
                }
            }
            autoPressBg?.apply {
                if (!isRecycled) {
                    if (width > w) {
                        w = width
                    }
                    if (height > h) {
                        h = height
                    }
                }
            }
            autoSelectBg?.apply {
                if (!isRecycled) {
                    if (width > w) {
                        w = width
                    }
                    if (height > h) {
                        h = height
                    }
                }
            }
            autoUrlBg?.apply {
                if (!isRecycled) {
                    if (width > w) {
                        w = width
                    }
                    if (height > h) {
                        h = height
                    }
                }
            }
            autoMatrixBg?.apply {
                if (!isRecycled) {
                    if (width > w) {
                        w = width
                    }
                    if (height > h) {
                        h = height
                    }
                }
            }
        }
        if (w > 0 && h > 0) {
            this.w=w
            this.h=h
            layoutParams.width=w
            layoutParams.height=h
            //取自定义位图宽度和高度最大的那个。
            setMeasuredDimension(w, h)
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }
    }

    var autoLeftPadding = 0f//左补丁(负数也有效哦)
    var autoTopPadding = 0f//上补丁
    var isAutoCenter = true//位图是否居中,默认居中（水平+垂直居中）
        set(value) {
            field = value
            if (field) {
                isAutoCenterHorizontal = false
                isAutoCenterVertical = false
            }
        }
    var isAutoCenterHorizontal = false//水平居中
        set(value) {
            field = value
            if (field) {
                isAutoCenter = false
                isAutoCenterVertical = false
            }
        }
    var isAutoCenterVertical = false//垂直居中
        set(value) {
            field = value
            if (field) {
                isAutoCenter = false
                isAutoCenterHorizontal = false
            }
        }
    open var isRecycleAutoUrlBg:Boolean=true//图片适配时，是否释放原位图。
    //画自定义背景
    open fun drawAutoBg(canvas: Canvas) {
        if (w <= 0 || h <= 0) {
            return
        }
        var paint = KView.getPaint()
        //网络背景位图（铺满整个背景控件）
        autoUrlBg?.apply {
            if (!isRecycled) {
                if (width != w || height != h) {
                    autoUrlBg = kpx.xBitmap(this, w, h, isRecycle = isRecycleAutoUrlBg)//位图和控件拉伸到一样大小
                    autoUrlBg?.apply {
                        if (!isRecycled) {
                            canvas.drawBitmap(this, 0f, 0f, paint)
                        }
                    }
                } else {
                    canvas.drawBitmap(this, 0f, 0f, paint)
                }
            }
        }
        //拉伸图片
        drawAutoMatrixBg(canvas,paint)
        //Log.e("test", "isSelected:\t" + isSelected + "\tisPress：\t" + isPressed)
        if (isSelected && autoSelectBg != null) {
            //选中状态图片,优先级最高
            autoSelectBg?.apply {
                if (!isRecycled) {
                    if (isAutoCenter) {
                        canvas.drawBitmap(this, kpx.centerBitmapX(this, w.toFloat()) + autoLeftPadding, kpx.centerBitmapY(this, h.toFloat()) + autoTopPadding, paint)
                    } else if (isAutoCenterHorizontal) {
                        canvas.drawBitmap(this, kpx.centerBitmapX(this, w.toFloat()) + autoLeftPadding, autoTopPadding, paint)
                    } else if (isAutoCenterVertical) {
                        canvas.drawBitmap(this, autoLeftPadding, kpx.centerBitmapY(this, h.toFloat()) + autoTopPadding, paint)
                    } else {
                        canvas.drawBitmap(this, autoLeftPadding, autoTopPadding, paint)
                    }
                }
            }
        } else {
            if (isPressed && autoPressBg != null) {
                //按下状态
                autoPressBg?.apply {
                    if (!isRecycled) {
                        if (isAutoCenter) {
                            canvas.drawBitmap(this, kpx.centerBitmapX(this, w.toFloat()) + autoLeftPadding, kpx.centerBitmapY(this, h.toFloat()) + autoTopPadding, paint)
                        } else if (isAutoCenterHorizontal) {
                            canvas.drawBitmap(this, kpx.centerBitmapX(this, w.toFloat()) + autoLeftPadding, autoTopPadding, paint)
                        } else if (isAutoCenterVertical) {
                            canvas.drawBitmap(this, autoLeftPadding, kpx.centerBitmapY(this, h.toFloat()) + autoTopPadding, paint)
                        } else {
                            canvas.drawBitmap(this, autoLeftPadding, autoTopPadding, paint)
                        }
                    }
                }
            } else {
                //普通状态
                autoDefaultBg?.apply {
                    if (!isRecycled) {
                        if (isAutoCenter) {
                            canvas.drawBitmap(this, kpx.centerBitmapX(this, w.toFloat()) + autoLeftPadding, kpx.centerBitmapY(this, h.toFloat()) + autoTopPadding, paint)
                        } else if (isAutoCenterHorizontal) {
                            canvas.drawBitmap(this, kpx.centerBitmapX(this, w.toFloat()) + autoLeftPadding, autoTopPadding, paint)
                        } else if (isAutoCenterVertical) {
                            canvas.drawBitmap(this, autoLeftPadding, kpx.centerBitmapY(this, h.toFloat()) + autoTopPadding, paint)
                        } else {
                            canvas.drawBitmap(this, autoLeftPadding, autoTopPadding, paint)
                        }
                    }
                }
            }
        }
    }

    //释放位图
    fun recycleAutoBg() {
        autoDefaultBg?.apply {
            if (!isRecycled) {
                recycle()
            }
        }
        autoDefaultBg = null
        autoPressBg?.apply {
            if (!isRecycled) {
                recycle()
            }
        }
        autoPressBg = null
        autoSelectBg?.apply {
            if (!isRecycled) {
                recycle()
            }
        }
        autoSelectBg = null
        autoUrlBg?.apply {
            if (!isRecycled) {
                recycle()
            }
        }
        autoUrlBg = null
        autoMatrixBg?.apply {
            if (!isRecycled) {
                recycle()
            }
        }
        autoMatrixBg = null
        invalidate()
        System.gc()//提醒内存回收
    }


    //fixme 什么都不做，交给子类去实现绘图
    //fixme 之所以会有这个方法。是为了保证自定义的 draw和onDraw的执行顺序。始终是在最后。
    protected open fun draw2(canvas: Canvas, paint: Paint) {}

    var afterDrawRadius = true//fixme 圆角边框是否最后画。默认最后画。不管是先画，还是后面。总之都在背景上面。背景最底层。
    override fun draw(canvas: Canvas?) {
        if (Build.VERSION.SDK_INT <= 19 && (left_top > 0 || left_bottom > 0 || right_top > 0 || right_bottom > 0 || all_radius > 0)) {//19是4.4系统。这个系统已经很少了。基本上也快淘汰了。
            //防止4.4及以下的系统。背景出现透明黑框。
            //只能解决。父容器有背景颜色的时候。如果没有背景色。那就没有办法了。
            var color = KView.getParentColor(this)
            canvas?.drawColor(color)//必不可少，不能为透明色。
            canvas?.saveLayerAlpha(RectF(0f, 0f, w.toFloat(), h.toFloat()), 255, Canvas.ALL_SAVE_FLAG)//必不可少，解决透明黑框。
        }

        //画自定义背景(在super的后面，不然会遮挡文字)
        canvas?.let {
            drawGradent(canvas)//fixme 画渐变色,在背景的后面。
            drawAutoBg(it)//画自定义背景位图
        }
        super.draw(canvas)

        if (!afterDrawRadius) {
            drawRadius(canvas)
        }
        canvas?.let {
            draw2(it, KView.getPaint())
            draw?.let {
                it(canvas, KView.getPaint())
            }
            //画水平进度
            drawHorizontalProgress?.let {
                it(canvas, getPaint(), w * horizontalProgress / 100f)
            }
            //画垂直进度
            drawVerticalProgress?.let {
                it(canvas, getPaint(), h - h * verticalProgress / 100f)//方向从下往上。
            }
        }
        if (afterDrawRadius) {
            drawRadius(canvas)
        }
    }

    var kradius = KRadius()
    //画边框，圆角
    fun drawRadius(canvas: Canvas?) {
        this.let {
            kradius.apply {
                x = 0
                y = 0
                w = it.w
                h = it.h
                all_radius = it.all_radius
                left_top = it.left_top
                left_bottom = it.left_bottom
                right_top = it.right_top
                right_bottom = it.right_bottom
                strokeWidth = it.strokeWidth
                strokeColor = it.strokeColor
                strokeGradientStartColor = it.strokeGradientStartColor
                strokeGradientEndColor = it.strokeGradientEndColor
                strokeGradientColors = it.strokeGradientColors
                strokeGradientOritation = it.strokeGradientOritation
                drawRadius(canvas)
            }
        }
    }

    //自定义画布，根据需求。自主实现
    open var draw: ((canvas: Canvas, paint: Paint) -> Unit)? = null

    //自定义，重新绘图
    open fun draw(draw: ((canvas: Canvas, paint: Paint) -> Unit)? = null): KRadiusButton {
        this.draw = draw
        postInvalidate()//刷新
        return this
    }

    //画自己【onDraw在draw()的流程里面，即在它的前面执行】
    var onDraw: ((canvas: Canvas, paint: Paint) -> Unit)? = null

    //画自己
    fun onDraw_(onDraw: ((canvas: Canvas, paint: Paint) -> Unit)? = null): KRadiusButton {
        this.onDraw = onDraw
        postInvalidate()//刷新
        return this
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.let {
            onDraw?.let {
                var paint = Paint()
                paint.isAntiAlias = true
                paint.isDither = true
                paint.style = Paint.Style.FILL_AND_STROKE
                paint.strokeWidth = 0f
                it(canvas, paint)
            }
        }
    }

    var w: Int = 0//获取控件的真实宽度
        get() {
            var w = width
            if (layoutParams != null && layoutParams.width > w) {
                w = layoutParams.width
            }
            return w
        }

    var h: Int = 0//获取控件的真实高度
        get() {
            var h = height
            if (layoutParams != null && layoutParams.height > h) {
                h = layoutParams.height
            }
            return h
        }

    //获取文本居中Y坐标
    fun getCenterTextY(paint: Paint): Float {
        var baseline = (h - (paint.descent() - paint.ascent())) / 2 - paint.ascent()
        return baseline
    }

    /**
     * 获取文本实际居中Y坐标。
     */
    fun getTextY(paint: Paint, y: Float): Float {
        var centerY = getCenterTextY(paint)
        var sub = h / 2 - centerY
        var y2 = y - sub
        return y2
    }

    /**
     * 获取文本的高度
     */
    fun getTextHeight(paint: Paint): Float {
        return paint.descent() - paint.ascent()
    }

    var centerX = 0f
        get() = centerX()

    fun centerX(): Float {
        return w / 2f
    }

    var centerY = 0f
        get() = centerY()

    fun centerY(): Float {
        return h / 2f
    }

    //根据宽度，获取该宽度居中值
    fun centerX(width: Float): Float {
        return (w - width) / 2
    }

    //根据高度，获取该高度居中值
    fun centerY(height: Float): Float {
        return (h - height) / 2
    }

    fun selectorDrawable(NormalDrawable: Drawable?, PressDrawable: Drawable?, SelectDrawable: Drawable? = PressDrawable) {
        KSelectorUtils.selectorDrawable(this, NormalDrawable, PressDrawable, SelectDrawable)
    }

    /**
     * NormalID 默认背景图片id
     * PressID 按下背景图片id
     * SelectID 选中(默认和按下相同)时背景图片id,即选中时状态。需要isSelected=true才有效。
     */
    fun selectorDrawable(NormalID: Int?, PressID: Int?, SelectID: Int? = PressID) {
        KSelectorUtils.selectorDrawable(this, NormalID, PressID, SelectID)
    }

    //图片
    fun selectorDrawable(NormalBtmap: Bitmap?, PressBitmap: Bitmap?, SelectBitmap: Bitmap? = PressBitmap) {
        KSelectorUtils.selectorDrawable(this, NormalBtmap, PressBitmap, SelectBitmap)
    }


    //fixme 颜色,调用之前一定要先设置圆角的属性。不然圆角不正确
    fun selectorColor(NormalColor: Int?, PressColor: Int?, SelectColor: Int? = PressColor) {
//        SelectorUtils.selectorColor(this, NormalColor, PressColor, SelectColor)
        //fixme 防止按钮圆角不正确，必须对每个圆角都使用GradientDrawable控制。
        KSelectorUtils.selectorRippleDrawable(this, NormalColor, PressColor, PressColor, all_radius = this.all_radius, left_top = this.left_top, right_top = this.right_top, right_bottom = this.right_bottom, left_bottom = this.left_bottom, isRipple = false)
    }

    //fixme 颜色,调用之前一定要先设置圆角的属性。不然圆角不正确
    fun selectorColor(NormalColor: String?, PressColor: String?, SelectColor: String? = PressColor) {
//        SelectorUtils.selectorColor(this, NormalColor, PressColor, SelectColor)
        KSelectorUtils.selectorRippleDrawable(this, NormalColor, PressColor, PressColor, all_radius = this.all_radius, left_top = this.left_top, right_top = this.right_top, right_bottom = this.right_bottom, left_bottom = this.left_bottom, isRipple = false)

    }

    //字体颜色
    fun selectorTextColor(NormalColor: Int, PressColor: Int?, SelectColor: Int? = PressColor) {
        KSelectorUtils.selectorTextColor(this, NormalColor, PressColor, SelectColor)
    }

    fun selectorTextColor(NormalColor: String, PressColor: String?, SelectColor: String? = PressColor) {
        KSelectorUtils.selectorTextColor(this, NormalColor, PressColor, SelectColor)
    }

    //fixme 防止和以下方法冲突，all_radius不要设置默认值
    //fixme 调用之前一定要先设置圆角的属性。不然圆角不正确
    fun selectorRippleDrawable(NormalColor: String?, PressColor: String?, all_radius: Float) {
        KSelectorUtils.selectorRippleDrawable(this, Color.parseColor(NormalColor), Color.parseColor(PressColor), Color.parseColor(PressColor), left_top = all_radius, right_top = all_radius, right_bottom = all_radius, left_bottom = all_radius)
    }

    /**
     * 波纹点击效果
     * all_radius 圆角
     */
    fun selectorRippleDrawable(NormalColor: Int?, PressColor: Int?, all_radius: Float) {
        KSelectorUtils.selectorRippleDrawable(this, NormalColor, PressColor, PressColor, left_top = all_radius, right_top = all_radius, right_bottom = all_radius, left_bottom = all_radius)
    }

    fun selectorRippleDrawable(NormalColor: String?, PressColor: String?, SelectColor: String? = PressColor, strokeWidth: Int = 0, strokeColor: Int = Color.TRANSPARENT, all_radius: Float = this.all_radius, left_top: Float = this.left_top, right_top: Float = this.right_top, right_bottom: Float = this.right_bottom, left_bottom: Float = this.left_bottom) {
        KSelectorUtils.selectorRippleDrawable(this, Color.parseColor(NormalColor), Color.parseColor(PressColor), Color.parseColor(SelectColor), strokeWidth, strokeColor, all_radius, left_top, right_top, right_bottom, left_bottom)
    }

    /**
     * 波纹点击效果
     * NormalColor 正常背景颜色值
     * PressColor  按下正常背景颜色值 ,也可以理解为波纹点击颜色
     * SelectColor 选中(默认和按下相同)背景颜色值
     */
    fun selectorRippleDrawable(NormalColor: Int?, PressColor: Int?, SelectColor: Int? = PressColor, strokeWidth: Int = 0, strokeColor: Int = Color.TRANSPARENT, all_radius: Float = this.all_radius, left_top: Float = this.left_top, right_top: Float = this.right_top, right_bottom: Float = this.right_bottom, left_bottom: Float = this.left_bottom) {
        //Log.e("test","all:\t"+all_radius+"\tleft:\t"+left_top+"\tright:\t"+right_top+"\t"+right_bottom)
        KSelectorUtils.selectorRippleDrawable(this, NormalColor, PressColor, SelectColor, strokeWidth, strokeColor, all_radius, left_top, right_top, right_bottom, left_bottom)
    }

    //属性动画集合
    var objectAnimates = arrayListOf<ObjectAnimator?>()

    //停止所有属性动画
    fun stopAllObjAnim() {
        for (i in 0 until objectAnimates.size) {
            objectAnimates[i]?.let {
                it.end()
            }
        }
        objectAnimates.clear()//清除所有动画
    }

    //属性动画
    fun ofFloat(propertyName: String, repeatCount: Int, duration: Long, vararg value: Float, AnimatorUpdateListener: ((values: Float) -> Unit)? = null): ObjectAnimator {
        var objectAnimator = KView.ofFloat(this, propertyName, repeatCount, duration, *value, AnimatorUpdateListener = AnimatorUpdateListener)
        objectAnimates.add(objectAnimator)
        return objectAnimator
    }

    fun ofInt(propertyName: String, repeatCount: Int, duration: Long, vararg value: Int, AnimatorUpdateListener: ((values: Int) -> Unit)? = null): ObjectAnimator {
        var objectAnimator = KView.ofInt(this, propertyName, repeatCount, duration, *value, AnimatorUpdateListener = AnimatorUpdateListener)
        objectAnimates.add(objectAnimator)
        return objectAnimator
    }

    var realHeight = -1//保存控件的实际高度
    var isShowHeight = true//true展开状态，false关闭状态
        get() {
            if (realHeight > 0) {
                if (h > realHeight / 2) {
                    return true//展开状态
                } else {
                    return false//关闭状态
                }
            } else {
                if (h > 0) {
                    return true//展开状态
                } else {
                    return false//关闭状态
                }
            }
        }

    //要显示的高度（控制高度的变化）
    fun showHeight(mHeight: Int, duration: Long = 300) {
        if (realHeight < 0 && h > 0) {
            realHeight = h//保存实际原有高度
        }
        if (realHeight > 0) {
            //属性动画，随便搞个属性即可。不存在也没关系。仅仅需要这个属性值的变化过程
            ofInt("mmmShowHeight", 0, duration, h, mHeight) {
                layoutParams.apply {
                    //设置宽和高
                    height = it
                }
                requestLayout()
            }
        }
    }

    //高度变化，0->h 或者 h->0 自主判断
    fun showToggleHeight(duration: Long = 300) {
        if (realHeight < 0 && h > 0) {
            realHeight = h//保存实际原有高度
        }
        if (realHeight > 0) {
            if (isShowHeight) {
                //显示状态 改为 关闭状态，高度设置为0
                showHeight(0, duration)
            } else {
                //关闭状态 改为 显示状态，高度设置为原有高度
                showHeight(realHeight, duration)
            }
        }
    }

    var realWidth = -1//保存控件的实际宽度
    var isShowWidth = true//true展开状态，false关闭状态
        get() {
            if (realWidth > 0) {
                if (w > realWidth / 2) {
                    return true//展开状态
                } else {
                    return false//关闭状态
                }
            } else {
                if (w > 0) {
                    return true//展开状态
                } else {
                    return false//关闭状态
                }
            }
        }

    //要显示的宽度（控制宽度的变化）
    fun showWidth(mWidth: Int, duration: Long = 300) {
        if (realWidth < 0 && w > 0) {
            realWidth = w//保存实际原有宽度
        }
        if (realWidth > 0) {
            //属性动画，随便搞个属性即可。不存在也没关系。仅仅需要这个属性值的变化过程
            ofInt("mmmShowWidth", 0, duration, w, mWidth) {
                layoutParams.apply {
                    //设置宽和高
                    width = it
                }
                requestLayout()
            }
        }
    }

    //宽度变化，0->h 或者 h->0 自主判断
    fun showToggleWidth(duration: Long = 300) {
        if (realWidth < 0 && w > 0) {
            realWidth = w//保存实际原有宽度
        }
        if (realWidth > 0) {
            if (isShowWidth) {
                //显示状态 改为 关闭状态，宽度设置为0
                showWidth(0, duration)
            } else {
                //关闭状态 改为 显示状态，宽度设置为原有宽度
                showWidth(realWidth, duration)
            }
        }
    }

    //透明动画,透明度 0f(完全透明)到1f(完全不透明)
    fun alpha(repeatCount: Int, duration: Long, vararg value: Float, AnimatorUpdateListener: ((values: Float) -> Unit)? = null): ObjectAnimator {
        return ofFloat("alpha", repeatCount, duration, *value, AnimatorUpdateListener = AnimatorUpdateListener)
    }

    /**
     * 封装位置移动动画
     * toX,toY相对于父容器的移动的目标坐标点。
     * durationMillis 动画时间，单位毫秒。
     * end 回调，动画结束后，返回当前的位置坐标。[位置会实际发生改变]
     * fixme 注意，如果有多个控件同时开启动画，移动的时候可能会卡顿和抖动现象。多个控件最好不要同时进行动画，太耗性能了。
     */
    fun translateAnimation(toX: Float, toY: Float, durationMillis: Long = 300, end: ((x: Float, y: Float) -> Unit)? = null): TranslateAnimation {
        return KView.translateAnimation(this, toX, toY, durationMillis, end)
    }

    var objectAnimatorScaleX: ObjectAnimator? = null
    var objectAnimatorScaleY: ObjectAnimator? = null
    //缩放动画(因为有两个属性。就不添加监听了)
    //pivotX,pivotY 变换基准点，默认居中
    fun scale(repeatCount: Int, duration: Long, vararg value: Float, pivotX: Float = w / 2f, pivotY: Float = h / 2f) {
        endScale()
        this.pivotX = pivotX
        this.pivotY = pivotY
        //支持多个属性，同时变化，放心会同时变化的。
        objectAnimatorScaleX = ofFloat("scaleX", repeatCount, duration, *value)
        objectAnimatorScaleY = ofFloat("scaleY", repeatCount, duration, *value)
    }

    //暂停缩放（属性会保持当前的状态）
    fun pauseScale() {
        objectAnimatorScaleX?.let {
            if (Build.VERSION.SDK_INT >= 19) {
                it.pause()
            } else {
                it.end()
            }
        }
        objectAnimatorScaleY?.let {
            if (Build.VERSION.SDK_INT >= 19) {
                it.pause()
            } else {
                it.end()
            }
        }
    }

    //继续缩放
    fun resumeScale() {
        objectAnimatorScaleX?.let {
            if (Build.VERSION.SDK_INT >= 19) {
                it.resume()
            } else {
                it.start()//动画会重新开始
            }
        }
        objectAnimatorScaleY?.let {
            if (Build.VERSION.SDK_INT >= 19) {
                it.resume()
            } else {
                it.start()//动画会重新开始
            }
        }
    }

    //fixme 停止缩放,属性会恢复到原始状态。动画也会结束。
    fun endScale() {
        objectAnimatorScaleX?.let {
            it.end()//fixme 一旦调用了end()属性动画也就结束了，并且属性也会恢复到原始状态。
            objectAnimatorScaleX = null
        }
        objectAnimatorScaleY?.let {
            it.end()
            objectAnimatorScaleY = null
        }
    }

    var objectAnimatorRotation: ObjectAnimator? = null
    //旋转动画
    //pivotX,pivotY 变换基准点，默认居中
    fun rotation(repeatCount: Int, duration: Long, vararg value: Float, AnimatorUpdateListener: ((values: Float) -> Unit)? = null, pivotX: Float = w / 2f, pivotY: Float = h / 2f): ObjectAnimator {
        endRotation()
        this.pivotX = pivotX
        this.pivotY = pivotY
        objectAnimatorRotation = ofFloat("rotation", repeatCount, duration, *value, AnimatorUpdateListener = AnimatorUpdateListener)
        return objectAnimatorRotation!!
    }

    //暂停旋转（属性会保持当前的状态）
    fun pauseRotation() {
        objectAnimatorRotation?.let {
            if (Build.VERSION.SDK_INT >= 19) {
                it.pause()
            } else {
                it.end()
            }
        }
        objectAnimatorRotation?.let {
            if (Build.VERSION.SDK_INT >= 19) {
                it.pause()
            } else {
                it.end()
            }
        }
    }

    //继续旋转
    fun resumeRotation() {
        objectAnimatorRotation?.let {
            if (Build.VERSION.SDK_INT >= 19) {
                it.resume()
            } else {
                it.start()//动画会重新开始
            }
        }
        objectAnimatorRotation?.let {
            if (Build.VERSION.SDK_INT >= 19) {
                it.resume()
            } else {
                it.start()//动画会重新开始
            }
        }
    }

    //fixme 停止旋转,属性会恢复到原始状态。动画也会结束。
    fun endRotation() {
        objectAnimatorRotation?.let {
            it.end()//fixme 一旦调用了end()属性动画也就结束了，并且属性也会恢复到原始状态。
            objectAnimatorRotation = null
        }
        objectAnimatorRotation?.let {
            it.end()
            objectAnimatorRotation = null
        }
    }

    var kTimer: KTimerUtils.KTimer? = null
    //定时刷新
    fun refresh(count: Long = 60, unit: Long = 1000, firstUnit: Long = 0, callback: (num: Long) -> Unit): KTimerUtils.KTimer? {
        endRefresh();
        kTimer = KTimerUtils.refreshView(this, count, unit, firstUnit, callback)
        return kTimer
    }

    //暂停
    fun pauseRefresh() {
        kTimer?.let {
            it.pause()
        }
    }

    //判断是否暂停
    fun isPauseRefresh(): Boolean {
        var pause = false
        kTimer?.let {
            pause = it.isPause()
        }
        return pause
    }

    //继续
    fun resumeRefresh() {
        kTimer?.let {
            it.resume()
        }
    }

    //定时器停止
    fun endRefresh() {
        kTimer?.let {
            //一个View就添加一个定时器，防止泄露。
            it.pause()
            it.end()//如果定时器不为空，那一定要先停止之前的定时器。
            kTimer = null
        }
    }

    //水平进度(范围 0F~ 100F),从左往右
    var horizontalProgress = 0f

    fun horizontalProgress(repeatCount: Int, duration: Long, vararg value: Float, AnimatorUpdateListener: ((values: Float) -> Unit)? = null): ObjectAnimator {
        return ofFloat("horizontalProgress", repeatCount, duration, *value, AnimatorUpdateListener = AnimatorUpdateListener)
    }

    //返回当前水平移动坐标X,
    var drawHorizontalProgress: ((canvas: Canvas, paint: Paint, x: Float) -> Unit)? = null

    fun drawHorizontalProgress(drawHorizontalProgress: ((canvas: Canvas, paint: Paint, x: Float) -> Unit)) {
        this.drawHorizontalProgress = drawHorizontalProgress
    }

    //fixme 垂直进度(范围 0F~ 100F)注意：方向从下往上。0是最底下，100是最顶部。
    var verticalProgress = 0f

    fun verticalProgress(repeatCount: Int, duration: Long, vararg value: Float, AnimatorUpdateListener: ((values: Float) -> Unit)? = null): ObjectAnimator {
        return ofFloat("verticalProgress", repeatCount, duration, *value, AnimatorUpdateListener = AnimatorUpdateListener)
    }

    //返回当前垂直移动坐标Y
    var drawVerticalProgress: ((canvas: Canvas, paint: Paint, y: Float) -> Unit)? = null

    fun drawVerticalProgress(drawVerticalProgress: ((canvas: Canvas, paint: Paint, y: Float) -> Unit)) {
        this.drawVerticalProgress = drawVerticalProgress
    }


    //fixme 水平渐变颜色数组值【均匀渐变】
    var horizontalColors: IntArray? = null

    open fun horizontalColors(vararg color: Int) {
        horizontalColors = color
    }

    open fun horizontalColors(vararg color: String) {
        horizontalColors = IntArray(color.size)
        horizontalColors?.apply {
            if (color.size > 1) {
                for (i in 0..color.size - 1) {
                    this[i] = Color.parseColor(color[i])
                }
            } else {
                this[0] = Color.parseColor(color[0])
            }
        }

    }

    //fixme 垂直渐变颜色数组值【均匀】
    var verticalColors: IntArray? = null

    open fun verticalColors(vararg color: Int) {
        verticalColors = color
    }

    //fixme 如：verticalColors("#00dedede","#dedede") 向上的阴影线
    open fun verticalColors(vararg color: String) {
        verticalColors = IntArray(color.size)
        verticalColors?.apply {
            if (color.size > 1) {
                for (i in 0..color.size - 1) {
                    this[i] = Color.parseColor(color[i])
                }
            } else {
                this[0] = Color.parseColor(color[0])
            }
        }

    }

    var top_color = Color.TRANSPARENT//fixme 上半部分颜色
    open fun top_color(top_color: Int) {
        this.top_color = top_color
    }

    open fun top_color(top_color: String) {
        this.top_color = Color.parseColor(top_color)
    }

    var bottom_color = Color.TRANSPARENT//fixme 下半部分颜色
    open fun bottom_color(bottom_color: Int) {
        this.bottom_color = bottom_color
    }

    open fun bottom_color(bottom_color: String) {
        this.bottom_color = Color.parseColor(bottom_color)
    }

    var left_color = Color.TRANSPARENT//fixme 左半部分颜色
    open fun left_color(left_color: Int) {
        this.left_color = left_color
    }

    open fun left_color(left_color: String) {
        this.left_color = Color.parseColor(left_color)
    }

    var right_color = Color.TRANSPARENT//fixme 右半部分颜色
    open fun right_color(right_color: Int) {
        this.right_color = right_color
    }

    open fun right_color(right_color: String) {
        this.right_color = Color.parseColor(right_color)
    }

    var left_top_color = Color.TRANSPARENT//fixme 左上角部分颜色
    open fun left_top_color(left_top_color: Int) {
        this.left_top_color = left_top_color
    }

    open fun left_top_color(left_top_color: String) {
        this.left_top_color = Color.parseColor(left_top_color)
    }

    var right_top_color = Color.TRANSPARENT//fixme 右上角部分颜色
    open fun right_top_color(right_top_color: Int) {
        this.right_top_color = right_top_color
    }

    open fun right_top_color(right_top_color: String) {
        this.right_top_color = Color.parseColor(right_top_color)
    }

    var left_bottom_color = Color.TRANSPARENT//fixme 左下角部分颜色
    open fun left_bottom_color(left_bottom_color: Int) {
        this.left_bottom_color = left_bottom_color
    }

    open fun left_bottom_color(left_bottom_color: String) {
        this.left_bottom_color = Color.parseColor(left_bottom_color)
    }

    var right_bottom_color = Color.TRANSPARENT//fixme 右下角部分颜色
    open fun right_bottom_color(right_bottom_color: Int) {
        this.right_bottom_color = right_bottom_color
    }

    open fun right_bottom_color(right_bottom_color: String) {
        this.right_bottom_color = Color.parseColor(right_bottom_color)
    }


    //画渐变变色
    fun drawGradent(canvas: Canvas) {
        canvas.apply {
            var paint = KView.getPaint()
            paint.isAntiAlias = true
            paint.isDither = true
            paint.style = Paint.Style.FILL_AND_STROKE

            //上半部分颜色
            if (top_color != Color.TRANSPARENT) {
                paint.color = top_color
                drawRect(RectF(0f, 0f, width.toFloat(), height / 2f), paint)
            }

            //下半部分颜色
            if (bottom_color != Color.TRANSPARENT) {
                paint.color = bottom_color
                drawRect(RectF(0f, height / 2f, width.toFloat(), height.toFloat()), paint)
            }


            //左半部分颜色
            if (left_color != Color.TRANSPARENT) {
                paint.color = left_color
                drawRect(RectF(0f, 0f, width.toFloat() / 2, height.toFloat()), paint)
            }

            //右半部分颜色
            if (right_color != Color.TRANSPARENT) {
                paint.color = right_color
                drawRect(RectF(width / 2f, 0f, width.toFloat(), height.toFloat()), paint)
            }

            //左上角部分颜色
            if (left_top_color != Color.TRANSPARENT) {
                paint.color = left_top_color
                drawRect(RectF(0f, 0f, width.toFloat() / 2, height.toFloat() / 2), paint)
            }

            //右上角部分颜色
            if (right_top_color != Color.TRANSPARENT) {
                paint.color = right_top_color
                drawRect(RectF(width / 2f, 0f, width.toFloat(), height.toFloat() / 2), paint)
            }

            //左下角部分颜色
            if (left_bottom_color != Color.TRANSPARENT) {
                paint.color = left_bottom_color
                drawRect(RectF(0f, height / 2f, width.toFloat() / 2, height.toFloat()), paint)
            }

            //右下角部分颜色
            if (right_bottom_color != Color.TRANSPARENT) {
                paint.color = right_bottom_color
                drawRect(RectF(width / 2f, height / 2f, width.toFloat(), height.toFloat()), paint)
            }

            //水平渐变
            horizontalColors?.let {
                var shader = LinearGradient(0f, 0f, width.toFloat(), 0f, it, null, Shader.TileMode.MIRROR)
                paint.setShader(shader)
                drawPaint(paint)
            }

            //fixme 水平渐变 和 垂直渐变 效果会叠加。垂直覆盖在水平的上面。

            //垂直渐变
            verticalColors?.let {
                var shader = LinearGradient(0f, 0f, 0f, height.toFloat(), it, null, Shader.TileMode.MIRROR)
                paint.setShader(shader)
                drawPaint(paint)
            }
        }
    }

}