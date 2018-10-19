package cn.android.support.v7.lib.eep.kera.utils

import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.*
import android.os.Build
import android.view.View
import android.widget.CheckBox
import android.widget.RadioButton
import android.widget.TextView
import cn.android.support.v7.lib.eep.kera.common.kpx

/**
 * fixme 用代码来实现selector 选择器 需要手动设置View的isSelected才会有选中效果。
 * fixme Chekbox和RadioButton 选中按钮自动设置了isSelected,所以不需要手动设置。
 *
 * fixme isSelected=true true 选中，false 未选中。可以通过代码设置
 * fixme isSelectable 所有的View都具备select选中能力
 *
 */
object KSelectorUtils {

    fun selectorRippleDrawable(view: View, NormalColor: String?, PressColor: String?, all_radius: Float, isRipple: Boolean = true) {
        selectorRippleDrawable(view, Color.parseColor(NormalColor), Color.parseColor(PressColor), Color.parseColor(PressColor), left_top = all_radius, right_top = all_radius, right_bottom = all_radius, left_bottom = all_radius, isRipple = isRipple)
    }

    /**
     * 波纹点击效果
     * all_radius fixme 圆角,注意，不能小于或等于0.必须大于0，不然波纹没有效果。
     */
    fun selectorRippleDrawable(view: View, NormalColor: Int?, PressColor: Int?, all_radius: Float, isRipple: Boolean = true) {
        selectorRippleDrawable(view, NormalColor, PressColor, PressColor, left_top = all_radius, right_top = all_radius, right_bottom = all_radius, left_bottom = all_radius, isRipple = isRipple)
    }

    fun selectorRippleDrawable(view: View, NormalColor: String?, PressColor: String?, SelectColor: String? = PressColor, strokeWidth: Int = 0, strokeColor: Int = Color.TRANSPARENT, all_radius: Float = 1f, left_top: Float = 1f, right_top: Float = 1f, right_bottom: Float = 1f, left_bottom: Float = 1f, isRipple: Boolean = true) {
        selectorRippleDrawable(view, Color.parseColor(NormalColor), Color.parseColor(PressColor), Color.parseColor(SelectColor), strokeWidth, strokeColor, all_radius, left_top, right_top, right_bottom, left_bottom, isRipple)
    }

    /**
     * 波纹点击效果
     * NormalColor 正常背景颜色值
     * PressColor  按下正常背景颜色值 ,也可以理解为波纹点击颜色
     * SelectColor 选中(默认和按下相同)背景颜色值
     * isRipple 是否显示波浪效果
     */
    fun selectorRippleDrawable(view: View, NormalColor: Int?, PressColor: Int?, SelectColor: Int? = PressColor, strokeWidth: Int = 0, strokeColor: Int = Color.TRANSPARENT, all_radius: Float = 0f, left_top: Float = 0f, right_top: Float = 0f, right_bottom: Float = 0f, left_bottom: Float = 0f, isRipple: Boolean = true) {
        var normalGradientDrawable = GradientDrawable()
        normalGradientDrawable?.apply {
            //fixme 圆角,注意，不能小于或等于0.必须大于0，不然波纹没有效果。
            var all_radius2 = all_radius
            if (all_radius2 < 1) {
                all_radius2 = 1f
            }
            var left_top2 = left_top
            if (left_top2 < 1) {
                left_top2 = all_radius2
            }
            var left_bottom2 = left_bottom
            if (left_bottom2 < 1) {
                left_bottom2 = all_radius2
            }
            var right_top2 = right_top
            if (right_top2 < 1) {
                right_top2 = all_radius2
            }
            var right_bottom2 = right_bottom
            if (right_bottom2 < 1) {
                right_bottom2 = all_radius2
            }
            //cornerRadius=all_radius2
            cornerRadii = floatArrayOf(left_top2, left_top2, right_top2, right_top2, right_bottom2, right_bottom2, left_bottom2, left_bottom2)
            //边框大小和边框颜色
            //setStroke(strokeWidth.toInt(), strokeColor)
            NormalColor?.let {
                setColor(NormalColor)
            }
        }
        var selectGradientDrawable = GradientDrawable()
        selectGradientDrawable?.apply {
            //fixme 圆角,注意，不能小于或等于0.必须大于0，不然波纹没有效果。
            var all_radius2 = all_radius
            if (all_radius2 < 1) {
                all_radius2 = 1f
            }
            var left_top2 = left_top
            if (left_top2 < 1) {
                left_top2 = all_radius2
            }
            var left_bottom2 = left_bottom
            if (left_bottom2 < 1) {
                left_bottom2 = all_radius2
            }
            var right_top2 = right_top
            if (right_top2 < 1) {
                right_top2 = all_radius2
            }
            var right_bottom2 = right_bottom
            if (right_bottom2 < 1) {
                right_bottom2 = all_radius2
            }
            //cornerRadius=all_radius2
            cornerRadii = floatArrayOf(left_top2, left_top2, right_top2, right_top2, right_bottom2, right_bottom2, left_bottom2, left_bottom2)
            //边框大小和边框颜色
            //setStroke(strokeWidth.toInt(), strokeColor)
            SelectColor?.let {
                setColor(SelectColor)
            }
        }
        if (Build.VERSION.SDK_INT >= 21 && isRipple && NormalColor != null && SelectColor != null) {//5.0以上才支持波纹效果
            //普通状态
            var rippleDrawable = RippleDrawable(
                    ColorStateList.valueOf(SelectColor),//波纹颜色
                    normalGradientDrawable,//控制波纹范围
                    null
            )
            //选中状态，是选中。不是触摸
            var rippleDrawable2 = RippleDrawable(
                    ColorStateList.valueOf(NormalColor!!),//波纹颜色
                    selectGradientDrawable,//控制波纹范围
                    null
            )
            view.isClickable = true//具体点击能力,必不可少
            //防止与触摸状态冲突。去掉触摸状态。只要普通状态，和选中状态。放心两中状态都有波纹效果
            selectorDrawable(view, rippleDrawable, null, rippleDrawable2)
//                view.background = rippleDrawable
//                以下这个方法不要用，就直接用背景即可,图片就使用下面的。颜色就使用背景
//                if (view is CheckBox) {//多选框
//                    view.buttonDrawable = rippleDrawable
//                } else if (view is RadioButton) {//单选框
//                    view.buttonDrawable = rippleDrawable
//                } else {//一般View
//                    view.setBackgroundDrawable(rippleDrawable)
//                }
        } else {
            normalGradientDrawable?.apply {
                //fixme 此时不需要波浪效果，所以角度可以为0
                var all_radius2 = all_radius
                if (all_radius2 <= 0) {
                    all_radius2 = 0f
                }
                var left_top2 = left_top
                if (left_top2 <= 0) {
                    left_top2 = all_radius2
                }
                var left_bottom2 = left_bottom
                if (left_bottom2 <= 0) {
                    left_bottom2 = all_radius2
                }
                var right_top2 = right_top
                if (right_top2 <= 0) {
                    right_top2 = all_radius2
                }
                var right_bottom2 = right_bottom
                if (right_bottom2 <= 0) {
                    right_bottom2 = all_radius2
                }
                cornerRadii = floatArrayOf(left_top2, left_top2, right_top2, right_top2, right_bottom2, right_bottom2, left_bottom2, left_bottom2)
            }
            selectGradientDrawable?.apply {
                //fixme 此时不需要波浪效果，所以角度可以为0
                var all_radius2 = all_radius
                if (all_radius2 <= 0) {
                    all_radius2 = 0f
                }
                var left_top2 = left_top
                if (left_top2 <= 0) {
                    left_top2 = all_radius2
                }
                var left_bottom2 = left_bottom
                if (left_bottom2 <= 0) {
                    left_bottom2 = all_radius2
                }
                var right_top2 = right_top
                if (right_top2 <= 0) {
                    right_top2 = all_radius2
                }
                var right_bottom2 = right_bottom
                if (right_bottom2 <= 0) {
                    right_bottom2 = all_radius2
                }
                //cornerRadius=all_radius2
                cornerRadii = floatArrayOf(left_top2, left_top2, right_top2, right_top2, right_bottom2, right_bottom2, left_bottom2, left_bottom2)
            }
            //点击一般效果，5.0以下不支持波纹
            selectorDrawable(view, normalGradientDrawable, selectGradientDrawable, selectGradientDrawable)
        }
    }

    fun selectorDrawable(view: View, drawableNormal: Drawable?, drawablePress: Drawable?, drawableSelect: Drawable? = drawablePress) {
        val drawable = StateListDrawable()
        //fixme - 表示fasle
        view.isClickable = true//具体点击能力
        //按下
        drawablePress?.let {
            drawable.addState(intArrayOf(android.R.attr.state_pressed), drawablePress)
        }
        //选中
        drawableSelect?.let {
            drawable.addState(intArrayOf(android.R.attr.state_checked), drawableSelect)
        }
        drawableSelect?.let {
            drawable.addState(intArrayOf(android.R.attr.state_selected), drawableSelect)
        }
        //未选中 + 未按下 (也就是一般状态)
        drawableNormal?.let {
            drawable.addState(intArrayOf(-android.R.attr.state_checked), drawableNormal)
        }
        if (view is CheckBox) {//多选框
            view.buttonDrawable = drawable
        } else if (view is RadioButton) {//单选框
            view.buttonDrawable = drawable
        } else {//一般View
            view.setBackgroundDrawable(drawable)
        }
    }

    /**
     * NormalID 默认背景图片id
     * PressID 按下背景图片id
     * SelectID 选中(默认和按下相同)时背景图片id
     */
    fun selectorDrawable(view: View, NormalID: Int?, PressID: Int?, SelectID: Int? = PressID) {
        val drawable = StateListDrawable()
        var drawableNormal: Drawable? = null
        NormalID?.let {
            drawableNormal = kpx.context()?.resources?.getDrawable(NormalID)
        }
        var drawablePress: Drawable? = null
        PressID?.let {
            drawablePress = kpx.context()?.resources?.getDrawable(PressID)
        }
        var drawableSelect: Drawable? = null
        SelectID?.let {
            drawableSelect = kpx.context()?.resources?.getDrawable(SelectID)
        }
        //fixme - 表示fasle
        view.isClickable = true//具体点击能力
        //按下
        drawablePress?.let {
            drawable.addState(intArrayOf(android.R.attr.state_pressed), drawablePress)
        }
        //选中
        drawableSelect?.let {
            drawable.addState(intArrayOf(android.R.attr.state_checked), drawableSelect)
        }
        drawableSelect?.let {
            drawable.addState(intArrayOf(android.R.attr.state_selected), drawableSelect)
        }
        //未选中 + 未按下 (也就是一般状态)
        drawableNormal?.let {
            drawable.addState(intArrayOf(-android.R.attr.state_checked), drawableNormal)
        }
        if (view is CheckBox) {//多选框
            view.buttonDrawable = drawable
        } else if (view is RadioButton) {//单选框
            view.buttonDrawable = drawable
        } else {//一般View
            view.setBackgroundDrawable(drawable)
        }
    }

    /**
     * NormalBtmap 默认背景位图
     * PressBitmap 按下时背景位图
     * SelectBitmap 选中(默认和按下相同)时背景位图
     */
    fun selectorDrawable(view: View, NormalBtmap: Bitmap?, PressBitmap: Bitmap?, SelectBitmap: Bitmap? = PressBitmap) {
        val drawable = StateListDrawable()
        var drawableNormal: BitmapDrawable? = null
        NormalBtmap?.let {
            drawableNormal = BitmapDrawable(NormalBtmap)
        }
        var drawablePress: BitmapDrawable? = null
        PressBitmap?.let {
            drawablePress = BitmapDrawable(PressBitmap)
        }
        var drawableSelect: BitmapDrawable? = null
        SelectBitmap?.let {
            drawableSelect = BitmapDrawable(SelectBitmap)
        }
        //fixme - 表示fasle
        view.isClickable = true//具体点击能力
        //按下
        drawablePress?.let {
            drawable.addState(intArrayOf(android.R.attr.state_pressed), drawablePress)
        }
        //选中
        drawableSelect?.let {
            drawable.addState(intArrayOf(android.R.attr.state_checked), drawableSelect)
            drawable.addState(intArrayOf(android.R.attr.state_selected), drawableSelect)
        }
        //未选中 + 未按下 (也就是一般状态)
        drawableNormal?.let {
            drawable.addState(intArrayOf(-android.R.attr.state_checked), drawableNormal)
        }
        if (view is CheckBox) {//多选框
            view.buttonDrawable = drawable
        } else if (view is RadioButton) {//单选框
            view.buttonDrawable = drawable
        } else {//一般View
            view.setBackgroundDrawable(drawable)
        }
    }

    /**
     * NormalColor 正常背景颜色值
     * PressColor  按下正常背景颜色值
     * SelectColor 选中(默认和按下相同)背景颜色值
     */
    fun selectorColor(view: View, NormalColor: Int?, PressColor: Int?, SelectColor: Int? = PressColor) {
        val drawable = StateListDrawable()
        var drawableNormal: ColorDrawable? = null
        NormalColor?.let {
            drawableNormal = ColorDrawable(NormalColor)
        }
        var drawablePress: ColorDrawable? = null
        PressColor?.let {
            drawablePress = ColorDrawable(PressColor)
        }
        var drawableSelect: ColorDrawable? = null
        SelectColor?.let {
            drawableSelect = ColorDrawable(SelectColor)
        }
        //fixme - 表示fasle
        view.isClickable = true//具体点击能力
        //按下
        drawablePress?.let {
            drawable.addState(intArrayOf(android.R.attr.state_pressed), drawablePress)
        }
        //选中
        drawableSelect?.let {
            drawable.addState(intArrayOf(android.R.attr.state_checked), drawableSelect)
            drawable.addState(intArrayOf(android.R.attr.state_selected), drawableSelect)
        }
        //未选中 + 未按下 (也就是一般状态)
        drawableNormal?.let {
            drawable.addState(intArrayOf(-android.R.attr.state_checked), drawableNormal)
        }
        view.setBackgroundDrawable(drawable)
//        以下这个方法不要用，就直接用背景即可,图片就使用下面的。颜色就使用背景
//        if (view is CheckBox) {//多选框
//            view.buttonDrawable = drawable
//        } else if (view is RadioButton) {//单选框
//            view.buttonDrawable = drawable
//        } else {//一般View
//            view.setBackgroundDrawable(drawable)
//        }
    }

    /**
     * NormalColor 正常背景颜色值
     * PressColor  按下背景颜色值
     * SelectColor 选中(默认和按下时相同)背景颜色值
     */
    fun selectorColor(view: View, NormalColor: String?, PressColor: String?, SelectColor: String? = PressColor) {
        val drawable = StateListDrawable()
        var drawableNormal: ColorDrawable? = null
        NormalColor?.let {
            drawableNormal = ColorDrawable(Color.parseColor(NormalColor))
        }
        var drawablePress: ColorDrawable? = null
        PressColor?.let {
            drawablePress = ColorDrawable(Color.parseColor(PressColor))
        }
        var drawableSelect: ColorDrawable? = null
        SelectColor?.let {
            drawableSelect = ColorDrawable(Color.parseColor(SelectColor))
        }
        //fixme - 表示fasle
        view.isClickable = true//具体点击能力
        //按下
        drawable.addState(intArrayOf(android.R.attr.state_pressed), drawablePress)
        //选中
        drawable.addState(intArrayOf(android.R.attr.state_checked), drawableSelect)
        drawable.addState(intArrayOf(android.R.attr.state_selected), drawableSelect)
        //未选中 + 未按下 (也就是一般状态)
        drawable.addState(intArrayOf(-android.R.attr.state_checked), drawableNormal)
        view.setBackgroundDrawable(drawable)
//        以下这个方法不要用，就直接用背景即可,图片就使用下面的。颜色就使用背景
//        if (view is CheckBox) {//多选框
//            view.buttonDrawable = drawable
//        } else if (view is RadioButton) {//单选框
//            view.buttonDrawable = drawable
//        } else {//一般View
//            view.setBackgroundDrawable(drawable)
//        }
    }

    /**
     * NormalColor 正常字体颜色值
     * PressColor  按下时字体颜色值
     * SelectColor 选中(默认和按下相同)字体颜色值
     */
    fun selectorTextColor(view: View, NormalColor: Int, PressColor: Int?, SelectColor: Int? = PressColor) {
        if (PressColor != null && SelectColor != null) {
            val colors = intArrayOf(PressColor, SelectColor, SelectColor, NormalColor)
            val states = arrayOfNulls<IntArray>(4)
            //fixme 以下顺序很重要。特别是最后一个，普通效果。必须放在最后一个，不然可能没有效果。
            states[0] = intArrayOf(android.R.attr.state_pressed)//按下
            states[1] = intArrayOf(android.R.attr.state_checked)//选中
            states[2] = intArrayOf(android.R.attr.state_selected)//选中
            states[3] = intArrayOf(-android.R.attr.state_checked)//未选中，未按下，普通一般效果
            val colorStateList = ColorStateList(states, colors)
            view.isClickable = true//具体点击能力
            if (view is TextView) {
                view.setTextColor(colorStateList)
            }
        } else if (PressColor == null) {
            //触摸状态为空（防止与选中状态冲突）
            var mSelectColor = SelectColor
            if (mSelectColor == null) {
                mSelectColor = NormalColor
            }
            val colors = intArrayOf(mSelectColor, mSelectColor, NormalColor)
            val states = arrayOfNulls<IntArray>(3)
            //fixme 以下顺序很重要。特别是最后一个，普通效果。必须放在最后一个，不然可能没有效果。
            states[0] = intArrayOf(android.R.attr.state_checked)//选中
            states[1] = intArrayOf(android.R.attr.state_selected)//选中
            states[2] = intArrayOf(-android.R.attr.state_checked)//未选中，未按下，普通一般效果
            val colorStateList = ColorStateList(states, colors)
            view.isClickable = true//具体点击能力
            if (view is TextView) {
                view.setTextColor(colorStateList)
            }
        }

    }

    /**
     * NormalColor 正常字体颜色值
     * PressColor  按下时颜色值
     * SelectColor 选中(默认和按下时相同)  字体颜色值
     */
    fun selectorTextColor(view: View, NormalColor: String, PressColor: String?, SelectColor: String? = PressColor) {
        if (PressColor != null && SelectColor != null) {
            selectorTextColor(view, Color.parseColor(NormalColor), Color.parseColor(PressColor), Color.parseColor(SelectColor))
        } else if (PressColor == null) {
            var mSelectColor = SelectColor
            if (mSelectColor == null) {
                mSelectColor = NormalColor
            }
            selectorTextColor(view, Color.parseColor(NormalColor), null, Color.parseColor(mSelectColor))
        }

    }

}