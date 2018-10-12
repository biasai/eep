package cn.android.support.v7.lib.eep.kera.widget

import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.os.Build
import android.text.*
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.Gravity
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.animation.TranslateAnimation
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import cn.android.support.v7.lib.eep.kera.R
import cn.android.support.v7.lib.eep.kera.base.KView
import cn.android.support.v7.lib.eep.kera.bean.KRadius
import cn.android.support.v7.lib.eep.kera.common.kpx
import cn.android.support.v7.lib.eep.kera.helper.KAsteriskPasswordTransformationMethod
import cn.android.support.v7.lib.eep.kera.helper.KLimitInputTextWatcher
import cn.android.support.v7.lib.eep.kera.https.KBitmaps
import cn.android.support.v7.lib.eep.kera.utils.KStringUtils
import cn.android.support.v7.lib.eep.kera.utils.KTimerUtils
import cn.android.support.v7.lib.eep.kera.utils.KSelectorUtils
import cn.android.support.v7.lib.eep.kera.utils.KAssetsUtils
import cn.android.support.v7.lib.eep.kera.utils.KRegexUtils
import org.jetbrains.anko.*

/**
 * 自定义圆角文本宽
 * Created by 彭治铭 on 2018/5/20.
 */
open class KRadiusEditText : EditText {

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

    //触摸点击效果。
    open fun onPress() {
        KView.onPress(this)
    }

    var bindView: View? = null//状态绑定的View
        set(value) {
            field = value
            if (value != null) {
                if (value is KView) {
                    if (value.bindView == null) {
                        value.bindView = this//相互绑定
                    }
                } else if (value is KRadiusEditText) {
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

    //底部横线
    var lineStrokeWidth = kpx.x(3f)//至少3像素才有圆角的效果。
    var lineStrokeColor = Color.parseColor("#2f5dd9")
    var lineProgress = 0f//底线的进度条
    var isLineAnime = true//是否开启线条动画(显示和隐藏)

    var hasFocus: Boolean = false
    //是否聚焦
    fun hasFocus(hasFocus: Boolean) {
        if (lineStrokeWidth <= 0 || !isLineAnime || lineStrokeColor == Color.TRANSPARENT) {
            return
        }
        this.hasFocus = hasFocus
        if (hasFocus) {
            //聚焦状态
            ofFloat("lineProgress", 0, 350, lineProgress, 1f)
        } else {
            //失去焦点
            ofFloat("lineProgress", 0, 350, lineProgress, 0f)
        }
    }

    private var onFocusChange: ((v: android.view.View, hasFocus: Boolean) -> Unit)? = null
    //重写聚焦事件，防止冲突
    fun onFocusChange(onFocusChange: (v: android.view.View, hasFocus: Boolean) -> Unit) {
        this.onFocusChange = onFocusChange
    }

    var default = -100f
    var leftLeftOffset = default//左边图片与左边的间距
    var leftTopOffset = default//左边图片与上面的间距
    //左边默认图片
    var leftDefault: Bitmap? = null

    fun leftDefault(bitmap: Bitmap?) {
        this.leftDefault = bitmap
    }

    fun leftDefault(res: Int, w: Int = 0, h: Int = 0) {
        leftDefault(KAssetsUtils.getInstance().getBitmapFromAssets(null, res, false), w, h)
    }

    fun leftDefault(bitmap: Bitmap?, w: Int = 0, h: Int = 0) {
        this.leftDefault = bitmap
        leftDefault?.let {
            var width = w
            var height = h
            if (width == 0 || height == 0) {
                width = kpx.x(it.width)
                height = kpx.x(it.height)
            }
            if (it.width != width) {
                var bitmap = Bitmap.createScaledBitmap(it, width, height, true)
                it.recycle()
                leftDefault = bitmap
            }
        }
    }


    //左边选中(触摸，聚焦，选中)图片
    var leftSelect: Bitmap? = null

    fun leftSelect(bitmap: Bitmap?) {
        this.leftSelect = bitmap
    }

    fun leftSelect(res: Int, w: Int = 0, h: Int = 0) {
        leftSelect(KAssetsUtils.getInstance().getBitmapFromAssets(null, res, false), w, h)
    }

    fun leftSelect(bitmap: Bitmap?, w: Int = 0, h: Int = 0) {
        this.leftSelect = bitmap
        leftSelect?.let {
            var width = w
            var height = h
            if (width == 0 || height == 0) {
                width = kpx.x(it.width)
                height = kpx.x(it.height)
            }
            if (it.width != width) {
                var bitmap = Bitmap.createScaledBitmap(it, width, height, true)
                it.recycle()
                leftSelect = bitmap
            }
        }
    }

    //同时设置默认图片和选中图片
    fun left(default: Int, select: Int) {
        leftDefault(default)
        leftSelect(select)
    }

    fun left(default: Bitmap?, select: Bitmap?) {
        leftDefault(default)
        leftSelect(select)
    }

    //正确位图位图与右边的距离
    var correctRightOffSet = default
    //与顶部的距离
    var correctTopOffSet = default
    //正确打勾勾的图片
    var correct: Bitmap? = null

    fun correct(bitmap: Bitmap?) {
        this.correct = bitmap
    }

    fun correct(res: Int, w: Int = 0, h: Int = 0) {
        correct(KAssetsUtils.getInstance().getBitmapFromAssets(null, res, false), w, h)
    }

    fun correct(bitmap: Bitmap?, w: Int = 0, h: Int = 0) {
        this.correct = bitmap
        correct?.let {
            var width = w
            var height = h
            if (width == 0 || height == 0) {
                width = kpx.x(it.width)
                height = kpx.x(it.height)
            }
            if (it.width != width) {
                var bitmap = Bitmap.createScaledBitmap(it, width, height, true)
                it.recycle()
                correct = bitmap
            }
        }
    }

    var correctProgress = 0f
    //成功，即输入框内容正确时手动调用
    open fun onSuccess(isSuccess: Boolean) {
        if (correct == null && isSuccess) {
            correct = KAssetsUtils.getInstance().getBitmapFromAssets(null, R.mipmap.kera_correct, false)
            if (correct != null && correct!!.width != kpx.x(41)) {
                var correct2 = Bitmap.createScaledBitmap(correct, kpx.x(41), kpx.x(29), true)
                correct!!.recycle()
                correct = correct2
            }
        }
        if (isSuccess) {
            //正确，显示成功图标
            if (correctProgress < 1) {
                onError(false)//正确的显示了，错误的就不能显示。即防止错误和成功同时显示。图片先隐藏。再显示。所以放在前面。
                ofFloat("correctProgress", 0, 300, correctProgress, 1f)
            }
        } else {
            //错误，不显示
            if (correctProgress > 0) {
                ofFloat("correctProgress", 0, 300, correctProgress, 0f)
            }
        }
    }

    //画成功的界面
    open fun drawSuccess(canvas: Canvas, paint: Paint) {
        correct?.let {
            if (correctRightOffSet <= default) {
                correctRightOffSet = lineStrokeWidth * 2
            }
            if (correctTopOffSet <= default) {
                correctTopOffSet = centerY - it.height / 2
            }
            var endX = w - it.width - correctRightOffSet
            var y = correctTopOffSet
            canvas.drawBitmap(correct, endX, y, paint)
            var x = endX + correctProgress * it.width
            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
            paint.style = Paint.Style.FILL
            canvas.drawRect(x, y, x + it.width, centerY + it.height / 2 * 1.1f, paint)
        }
    }

    //正确位图位图与右边的距离
    var errorRightOffSet = default
    //与顶部的距离
    var errorTopOffSet = default
    var error: Bitmap? = null
    fun error(bitmap: Bitmap?) {
        this.error = bitmap
    }

    fun error(res: Int, w: Int = 0, h: Int = 0) {
        error(KAssetsUtils.getInstance().getBitmapFromAssets(null, res, false), w, h)
    }

    fun error(bitmap: Bitmap?, w: Int = 0, h: Int = 0) {
        this.error = bitmap
        error?.let {
            var width = w
            var height = h
            if (width == 0 || height == 0) {
                width = kpx.x(it.width)
                height = kpx.x(it.height)
            }
            if (it.width != width) {
                var bitmap = Bitmap.createScaledBitmap(it, width, height, true)
                it.recycle()
                error = bitmap
            }
        }
    }

    var errorProgress = 0f
    //错误，即输入框内容错误时手动调用
    fun onError(isError: Boolean) {
        if (error == null && isError) {
            error = KAssetsUtils.getInstance().getBitmapFromAssets(null, R.mipmap.kera_error, false)
            if (error != null && error!!.width != kpx.x(40)) {
                var error2 = Bitmap.createScaledBitmap(error, kpx.x(40), kpx.x(40), true)
                error!!.recycle()
                error = error2
            }
        }
        if (isError) {
            //错误,显示错误图标
            if (errorProgress < 1) {
                onSuccess(false)//错误的图片显示了，正确的就不能显示。
                ofFloat("errorProgress", 0, 300, errorProgress, 1f)
            }
        } else {
            //没有错误，不显示
            if (errorProgress > 0) {
                ofFloat("errorProgress", 0, 300, errorProgress, 0f)
            }
        }
    }

    //画错误的界面
    open fun drawError(canvas: Canvas, paint: Paint) {
        error?.let {
            if (errorRightOffSet <= default) {
                errorRightOffSet = lineStrokeWidth * 2
            }
            if (errorTopOffSet <= default) {
                errorTopOffSet = centerY - it.height / 2
            }
            var endX = w - it.width - errorRightOffSet
            var y = errorTopOffSet
            canvas.drawBitmap(error, endX, y, paint)
            var x = endX + errorProgress * it.width
            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
            paint.style = Paint.Style.FILL
            canvas.drawRect(x, y, x + it.width, centerY + it.height / 2 * 1.1f, paint)
        }
    }

    //小数类型,保留指定小数个数
    var num: Int = 2//小数点后最多保留几位
    var isDecimal = false//是否为小数类型。
    //小数类型
    fun decimal(num: Int = 2) {
        this.num = num
        isDecimal = true//小数类型。
        //小数类型，以下两个必须同时设置才有效。
        inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        //最多保留多少位小数。
        addTextWatcher {
            if (it.contains(".")) {
                var s = it.substring(it.indexOf("."))
                if (s.length >= 2) {
                    s = s.substring(1)
                    if (s.length > this.num) {
                        setText(KStringUtils.doubleString(it, this.num))//保留指定小数个数
                        setSelection(length())//光标
                    }
                }
            }
        }
    }

    //账号类型(和密码类型一样，不能输入中文和空格。并且是可见的。)
    fun account() {
        password()
        showPassword()
    }

    //密码类型（默认就是显示不见的）
    fun password() {
        addTextChangedListener(KLimitInputTextWatcher(this))//默认的筛选条件(正则:不能输入中文和空格)
        inputType = EditorInfo.TYPE_CLASS_TEXT or EditorInfo.TYPE_TEXT_VARIATION_PASSWORD
        filters = arrayOf<InputFilter>(InputFilter.LengthFilter(21)) //最大输入长度
    }

    //手机号类型
    fun tel() {
        inputType = InputType.TYPE_CLASS_NUMBER
        filters = arrayOf<InputFilter>(InputFilter.LengthFilter(11)) //最大输入长度
    }

    //数字验证码类型
    fun code() {
        inputType = InputType.TYPE_CLASS_NUMBER
        filters = arrayOf<InputFilter>(InputFilter.LengthFilter(6))//有四位，也有六位的。一般都是六位。
    }

    //身份证类型
    fun idCard() {
        inputType = InputType.TYPE_CLASS_NUMBER
        filters = arrayOf<InputFilter>(InputFilter.LengthFilter(21)) //最大输入长度，公民身份证一般为18位
    }

    //银行卡类型
    fun bankNo() {
        inputType = InputType.TYPE_CLASS_NUMBER
        filters = arrayOf<InputFilter>(InputFilter.LengthFilter(21)) //最大输入长度，银行卡一般为16-19位
    }

    //邮箱类型
    fun email() {
        addTextChangedListener(KLimitInputTextWatcher(this, "[^A-Za-z0-9.@]"))
        inputType = EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        filters = arrayOf<InputFilter>(InputFilter.LengthFilter(21)) //最大输入长度，网易的是6-18个字符
    }

    //内容清除
    fun clear() {
        setText(null)
    }

    //是否显示密码
    var isShowPassword = false
        set(value) {
            if (field != value) {
                field = value
                if (field) {
                    showPassword2()
                } else {
                    hiddenPassword2()
                }
            }
        }

    //显示密码，隐藏密码交替切换
    fun togglePassword() {
        isShowPassword = !isShowPassword
    }

    //显示密码
    fun showPassword() {
        isShowPassword = true
    }

    private fun showPassword2() {
        setTransformationMethod(HideReturnsTransformationMethod.getInstance())//密码显示
        setSelection(length())//光标位置从1开始不是0.设置光标位置为最后一个。默认是第一个。
    }

    //隐藏密码
    fun hiddenPassword() {
        isShowPassword = false
    }

    private fun hiddenPassword2() {
        try {
            if (passWordChar == null) {
                setTransformationMethod(PasswordTransformationMethod.getInstance())//密码隐藏
            } else {
                passWordChar?.let {
                    transformationMethod = KAsteriskPasswordTransformationMethod(it)//自定义密码符号
                }
            }
            setSelection(length())
        } catch (e: Exception) {
        }

    }

    //设置光标宽度和颜色
    fun setCursorColor(color: Int) {
        try {
            var fCursorDrawableRes = TextView::class.java.getDeclaredField("mCursorDrawableRes")//获取这个字段
            fCursorDrawableRes.setAccessible(true)//代表这个字段、方法等等可以被访问
            var mCursorDrawableRes = fCursorDrawableRes.getInt(this)

            var fEditor = TextView::class.java.getDeclaredField("mEditor");
            fEditor.setAccessible(true);
            var editor = fEditor.get(this)

            var clazz = editor::class.java
            var fCursorDrawable = clazz.getDeclaredField("mCursorDrawable");
            fCursorDrawable.setAccessible(true);

            val drawables = arrayOfNulls<Drawable>(2)
            drawables[0] = getContext().getResources().getDrawable(mCursorDrawableRes);
            drawables[1] = getContext().getResources().getDrawable(mCursorDrawableRes);
            drawables[0]?.setColorFilter(color, PorterDuff.Mode.SRC_IN);//SRC_IN 上下层都显示。下层居上显示。
            drawables[1]?.setColorFilter(color, PorterDuff.Mode.SRC_IN);
            fCursorDrawable.set(editor, drawables);
        } catch (e: Exception) {
            //Log.e("test", "光标颜色设置异常")
        }
    }

    private var passWordChar: Char? = null
    //自定义密码符合
    //密码符号和字体颜色是一样的。
    fun setPasswordTransformation(c: Char? = null) {
        try {
            this.passWordChar = c
            if (c != null) {
                transformationMethod = KAsteriskPasswordTransformationMethod(c)//自定义密码符号，必须每次都要重新实例化，不然很容易奔溃。
            } else {
                setTransformationMethod(PasswordTransformationMethod.getInstance())//原始密码符号，是点。原始的是中间点。而且比较大。这个是中间小点·(原始的是中间大点)
            }
        } catch (e: Exception) {
        }
    }

    //文本监听
    fun addTextWatcher(watcher: (text: String) -> Unit) {
        addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                s?.let {
                    //watcher(it.toString())//""空字符串也会监听返回。
                    var mText = text.toString()//it靠不住，text获取的才是实时的正确数据。
                    if (isDecimal) {
                        //小数类型
                        if (mText.trim().equals(".")) {
                            //首个字符不能为点。
                            setText(null)
                        } else {
                            //1.默认就等于1.0 。double可以转换1.(点的后面没有数，默认就是0)
                            watcher(mText)
                        }
                    } else {
                        watcher(mText)
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    //文本空两格。首行缩进两个汉字的宽度
    fun indentation() {
        setText("\u3000\u3000")//缩进两个汉字的宽度,这里因为写入内容，所以hint属性就无效了。
        //能够监听回车键，删除键。像字符键，数字键等。这个方法无法监听到。
        setOnKeyListener(object : View.OnKeyListener {
            override fun onKey(v: View?, keyCode: Int, event: KeyEvent?): Boolean {
                //Log.e("test","按键:\t"+keyCode)
                event?.let {
                    if (keyCode == KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_DOWN) {//监听删除键。保留最后两个字符。
                        if (text.length == 2 || selectionStart <= 2) {//光标位置是从一开始的。1开始。不是0
                            return true
                        }
                    }
                    return false
                }
                return false
            }
        })
    }

    //监听输入框右下角完成按钮
    fun addDone(callbak: (() -> Unit)? = null) {
        imeOptions = EditorInfo.IME_ACTION_DONE//设置成完成类型
        callbak?.let {
            val onEditorActionListener = OnEditorActionListener { textView, actionId, keyEvent ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {//确定/完成
                    it()
                    true
                } else false
            }
            setOnEditorActionListener(onEditorActionListener)
        }
    }

    //监听输入框右下角搜索按钮
    fun addSearch(callbak: (() -> Unit)? = null) {
        imeOptions = EditorInfo.IME_ACTION_SEARCH//设置成搜索类型
        callbak?.let {
            val onEditorActionListener = OnEditorActionListener { textView, actionId, keyEvent ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {//搜索
                    it()
                    true
                } else false
            }
            setOnEditorActionListener(onEditorActionListener)
        }
    }

    //监听输入框右下角发送按钮
    fun addSend(callbak: (() -> Unit)? = null) {
        imeOptions = EditorInfo.IME_ACTION_SEND//设置成发送类型
        callbak?.let {
            val onEditorActionListener = OnEditorActionListener { textView, actionId, keyEvent ->
                if (actionId == EditorInfo.IME_ACTION_SEND) {//发送
                    it()
                    true
                } else false
            }
            setOnEditorActionListener(onEditorActionListener)
        }
    }

    //监听输入框右下角下一步按钮
    fun addNext(callbak: (() -> Unit)? = null) {
        imeOptions = EditorInfo.IME_ACTION_NEXT//设置下一步类型
        callbak?.let {
            val onEditorActionListener = OnEditorActionListener { textView, actionId, keyEvent ->
                if (actionId == EditorInfo.IME_ACTION_NEXT) {//下一步
                    it()
                    true
                } else false
            }
            setOnEditorActionListener(onEditorActionListener)
        }
    }

    //是否可以长按复制黏贴
    fun isCocy(isCocy: Boolean = false) {
        if (isCocy) {
            //可以长按复制黏贴
            setLongClickable(true)
        } else {
            //取消长按事件。禁止复制黏贴
            setLongClickable(false)
        }
    }

    //fixme 正则表达式

    //是否为手机号
    fun isTel(): Boolean {
        return KRegexUtils.getInstance().isMobileNO(this.text.toString().trim())
    }

    //是否为邮箱
    fun isEmail(): Boolean {
        return KRegexUtils.getInstance().isEmail(this.text.toString().trim())
    }

    //是否为身份证
    fun isIdCard(): Boolean {
        return KRegexUtils.getInstance().isIdCard(this.text.toString().trim())
    }

    //是否为银行卡号
    fun isBankNo(): Boolean {
        return KRegexUtils.getInstance().isBankCard(this.text.toString().trim())
    }

    init {
        setLayerType(View.LAYER_TYPE_HARDWARE, null)//开启硬件加速
        KView.typeface?.let {
            if (KView.isGlobal) {
                typeface = it//fixme 设置全局自定义字体
            }
        }
        //聚焦
        setOnFocusChangeListener { v, hasFocus ->
            hasFocus(hasFocus)
            //防止聚焦事件冲突
            onFocusChange?.let {
                it(v, hasFocus)
            }
        }
        if (Build.VERSION.SDK_INT >= 16) {
            background = null
        }
        textSize = kpx.textSizeX(32f)
        //密码符号的颜色和字体颜色是一致的。
        textColor = Color.parseColor("#333333")
        hintTextColor = Color.parseColor("#9b9b9b")
        leftPadding = kpx.x(60)//左边内补丁
        gravity = Gravity.CENTER_VERTICAL or Gravity.LEFT//左靠齐，垂直居中
        setCursorColor(lineStrokeColor)
        //默认就是文本类型。在此限制一下文本的长度。
        filters = arrayOf<InputFilter>(InputFilter.LengthFilter(18)) //最大输入长度
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

    /**
     * fixme 设置最大输入个数。即最大文字个数。
     * setMaxLines(lines) 设置行数
     */
    fun setMaxLength(num: Int) {
        filters = arrayOf<InputFilter>(InputFilter.LengthFilter(num)) //最大输入长度，网易的是6-18个字符
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

    //这个背景图片，会铺满整个控件
    private var autoUrlBg: Bitmap? = null//fixme 自定义网络背景图片,对图片是否为空，是否释放，做了判断。防止奔溃。比原生的背景图片更安全。

    fun autoUrlBg(bitmap: Bitmap?) {
        this.autoUrlBg = bitmap
        if (context != null && context is Activity) {
            context.runOnUiThread {
                invalidate()
            }
        }
    }

    fun autoUrlBg(resId: Int, isRGB_565: Boolean = false) {
        this.autoUrlBg = KAssetsUtils.getInstance().getBitmapFromAssets(null, resId, isRGB_565)
        if (context != null && context is Activity) {
            context.runOnUiThread {
                invalidate()
            }
        }
    }

    fun autoUrlAssetsBg(assetsPath: String, isRGB_565: Boolean = false) {
        this.autoUrlBg = KAssetsUtils.getInstance().getBitmapFromAssets(assetsPath, 0, isRGB_565)
        if (context != null && context is Activity) {
            context.runOnUiThread {
                invalidate()
            }
        }
    }

    /**
     * url 网络图片地址
     * isLoad 是否显示进度条，默认不显示
     * isRepeat 是否允许重复加载，默认允许
     */
    fun autoUrlBg(url: String?, isLoad: Boolean = false,isRepeat:Boolean=true) {
        if (isLoad && context != null && context is Activity) {
            KBitmaps(url).optionsRGB_565(false).showLoad(context as Activity).repeat(isRepeat).get() {
                autoUrlBg = it
                if (context != null && context is Activity) {
                    context.runOnUiThread {
                        invalidate()
                    }
                }
            }
        } else {
            KBitmaps(url).optionsRGB_565(false).showLoad(false).repeat(isRepeat).get() {
                autoUrlBg = it
                if (context != null && context is Activity) {
                    context.runOnUiThread {
                        invalidate()
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
                invalidate()
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
        }
    }

    fun autoDefaultBg(assetsPath: String, width: Int = 0, height: Int = 0, isRGB_565: Boolean = false) {
        autoDefaultBg = KAssetsUtils.getInstance().getBitmapFromAssets(assetsPath, 0, isRGB_565)
        autoDefaultBg?.let {
            autoDefaultBg = kpx.xBitmap(it, width, height)//自动适配
        }
        if (isAutoWH) {
            requestLayout()
        }
    }

    private var autoPressBg: Bitmap? = null//fixme 按下图片
    fun autoPressBg(bitmap: Bitmap?) {
        this.autoPressBg = bitmap
        if (context != null && context is Activity) {
            context.runOnUiThread {
                invalidate()
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
        }
        isClickable = true//具备点击能力
    }

    private var autoSelectBg: Bitmap? = null//fixme 选中图片（优先级最高）
    fun autoSelectBg(bitmap: Bitmap?) {
        this.autoSelectBg = bitmap
        if (context != null && context is Activity) {
            context.runOnUiThread {
                invalidate()
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
        }
        if (w > 0 && h > 0) {
            //取自定义位图宽度和高度最大的那个。
            setMeasuredDimension(w, h)
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }
    }

    var autoLeftPadding = 0f//左补丁(负数也有效哦)
    var autoTopPadding = 0f//上补丁
    var isAutoCenter = true//位图是否居中,默认居中
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
                    autoUrlBg = kpx.xBitmap(this, w, h)//位图和控件拉伸到一样大小
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
        //Log.e("test", "isSelected:\t" + isSelected + "\tisPress：\t" + isPressed)
        if (isSelected && autoSelectBg != null) {
            //选中状态图片,优先级最高
            autoSelectBg?.apply {
                if (!isRecycled) {
                    if (isAutoCenter) {
                        canvas.drawBitmap(this, kpx.centerBitmapX(this, w.toFloat()), kpx.centerBitmapY(this, h.toFloat()), paint)
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
                            canvas.drawBitmap(this, kpx.centerBitmapX(this, w.toFloat()), kpx.centerBitmapY(this, h.toFloat()), paint)
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
                            canvas.drawBitmap(this, kpx.centerBitmapX(this, w.toFloat()), kpx.centerBitmapY(this, h.toFloat()), paint)
                        } else {
                            canvas.drawBitmap(this, autoLeftPadding, autoTopPadding, paint)
                        }
                    }
                }
            }
        }
    }

    //释放位图
    fun recycle() {
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
            drawAutoBg(it)
        }
        super.draw(canvas)

        if (!afterDrawRadius) {
            drawRadius(canvas)
        }
        canvas?.let {
            if (!isLineAnime) {
                //线条动画关闭，直接显示出线条。
                lineProgress = 1f
            }
            if (lineProgress > 0.06 && lineStrokeWidth > 0 && lineStrokeColor != Color.TRANSPARENT) {//0.06效果是最好的。
                var paint = KView.getPaint()
                paint.style = Paint.Style.FILL_AND_STROKE
                paint.strokeCap = Paint.Cap.ROUND
                paint.color = lineStrokeColor
                paint.strokeWidth = 0f
                //画底部横线线
                var starX = lineStrokeWidth
                var endX = starX + (w - lineStrokeWidth * 2) * lineProgress
                var y = h - lineStrokeWidth / 2
                canvas.drawRoundRect(RectF(starX, y - lineStrokeWidth, endX, y), lineStrokeWidth, lineStrokeWidth, paint)
            }
            //画左边图片
            var bitmap = leftDefault
            //聚焦，选中，触摸
            if (isFocused || isSelected || isPressed) {
                bitmap = leftSelect
            }
            bitmap?.let {
                if (leftLeftOffset <= default) {
                    leftLeftOffset = lineStrokeWidth * 2
                }
                if (leftTopOffset <= default) {
                    leftTopOffset = centerY - it.height / 2
                }
                canvas.drawBitmap(it, leftLeftOffset, leftTopOffset, KView.getPaint())
            }

            //画成功时状态
            if (correctProgress > 0) {
                var paint = KView.getPaint()
                drawSuccess(canvas, paint)
            }
            //画失败时状态
            if (errorProgress > 0) {
                var paint = KView.getPaint()
                drawError(canvas, paint)
            }
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

    var kradius= KRadius()
    //画边框，圆角
    fun drawRadius(canvas: Canvas?) {
        this.let {
            kradius.apply {
                x=0
                y=0
                w=it.w
                h=it.h
                all_radius=it.all_radius
                left_top=it.left_top
                left_bottom=it.left_bottom
                right_top=it.right_top
                right_bottom=it.right_bottom
                strokeWidth=it.strokeWidth
                strokeColor=it.strokeColor
                strokeGradientStartColor=it.strokeGradientStartColor
                strokeGradientEndColor=it.strokeGradientEndColor
                strokeGradientColors=it.strokeGradientColors
                strokeGradientOritation=it.strokeGradientOritation
                drawRadius(canvas)
            }
        }
    }

    //自定义画布，根据需求。自主实现
    open var draw: ((canvas: Canvas, paint: Paint) -> Unit)? = null

    //自定义，重新绘图
    open fun draw(draw: ((canvas: Canvas, paint: Paint) -> Unit)? = null): KRadiusEditText {
        this.draw = draw
        postInvalidate()//刷新
        return this
    }

    //画自己【onDraw在draw()的流程里面，即在它的前面执行】
    var onDraw: ((canvas: Canvas, paint: Paint) -> Unit)? = null

    //画自己
    fun onDraw_(onDraw: ((canvas: Canvas, paint: Paint) -> Unit)? = null): KRadiusEditText {
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
            if (layoutParams.width > w) {
                w = layoutParams.width
            }
            return w
        }

    var h: Int = 0//获取控件的真实高度
        get() {
            var h = height
            if (layoutParams.height > h) {
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
        if (Build.VERSION.SDK_INT <= 19) {
            //fixme 防止按钮圆角不正确，必须对每个圆角都使用GradientDrawable控制。
            KSelectorUtils.selectorRippleDrawable(this, NormalColor, PressColor, PressColor, all_radius = this.all_radius, left_top = this.left_top, right_top = this.right_top, right_bottom = this.right_bottom, left_bottom = this.left_bottom, isRipple = false)

        } else {
            KSelectorUtils.selectorColor(this, NormalColor, PressColor, SelectColor)
        }

    }

    //fixme 颜色,调用之前一定要先设置圆角的属性。不然圆角不正确
    fun selectorColor(NormalColor: String?, PressColor: String?, SelectColor: String? = PressColor) {
        if (Build.VERSION.SDK_INT <= 19) {
            KSelectorUtils.selectorRippleDrawable(this, NormalColor, PressColor, PressColor, all_radius = this.all_radius, left_top = this.left_top, right_top = this.right_top, right_bottom = this.right_bottom, left_bottom = this.left_bottom, isRipple = false)

        } else {
            KSelectorUtils.selectorColor(this, NormalColor, PressColor, SelectColor)
        }

    }

    //字体颜色
    fun selectorTextColor(NormalColor: Int, PressColor: Int, SelectColor: Int = PressColor) {
        KSelectorUtils.selectorTextColor(this, NormalColor, PressColor, SelectColor)
    }

    fun selectorTextColor(NormalColor: String, PressColor: String, SelectColor: String = PressColor) {
        KSelectorUtils.selectorTextColor(this, NormalColor, PressColor, SelectColor)
    }

    //fixme 防止和以下方法冲突，all_radius不要设置默认值
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

}