package cn.android.support.v7.lib.eep.kera.widget.recycler

import android.graphics.Canvas
import android.graphics.Paint
import android.support.v7.widget.RecyclerView

/**
 * 悬浮置顶ItemView
 */
abstract class KHoverItemDecoration() : RecyclerView.ItemDecoration() {
    //fixme 指定需要悬浮的下标数组[注意 下标顺序，从小到大进行排序]
    var positiones: MutableList<Int>? = null
    //悬浮itemView的宽度
    var w: Int = 0
    //悬浮的itemView的高度
    var h: Int = 0
    //创建悬浮的itemView【使用画布画笔进行绘图，保证了效率。】
    var itemView: ((canvas: Canvas, paint: Paint, position: Int, y: Float) -> Unit)? = null

    open fun itemView(itemView: (canvas: Canvas, paint: Paint, position: Int, y: Float) -> Unit) {
        this.itemView = itemView
    }

    //在整个RecyClerView上方进行绘图[显示在上面。]。
    //没有点击事件，只是复制item视图悬停在顶部，不会遮挡点击事件，会触发下面itme的点击事件
    override fun onDrawOver(c: Canvas?, parent: RecyclerView?, state: RecyclerView.State?) {
        super.onDrawOver(c, parent, state)
        positiones?.let {
            itemView?.let {
                parent?.let {
                    state?.let {
                        c?.apply {
                            //var itemCount = state.itemCount//item的数据总个数
                            var childCount = parent.childCount//RecyclerView当前绘制子View的个数。[即当前显示的item个数，不显示的没有绘制。]
                            //var paddingLeft = parent.paddingLeft//RecyclerView左边内补丁
                            //var paddingRight = parent.paddingRight//右边内补丁
                            if (childCount > 1) {
                                var view = parent.getChildAt(1)//获取当前显示的第二个itemView
                                if (w <= 0) {
                                    w = parent.getChildAt(0).width//悬浮宽度，默认就是第一个item宽度
                                }
                                if (h <= 0) {
                                    h = parent.getChildAt(0).height//悬浮高度，默认就是第一个item的高度
                                }
                                var origPosition = parent.getChildAdapterPosition(view)//获取当前显示第二个View的下标。
                                var position = origPosition
                                position = position - 1
                                var currentPosition = position
                                for (i in 0 until positiones!!.size) {
                                    var it = positiones!![i]
                                    if (position >= it) {
                                        currentPosition = it
                                    } else {
                                        break
                                    }
                                }
                                if (position < 0) {
                                    currentPosition = 0
                                }
                                position = currentPosition
                                var y = view.y
                                if (y < h && y >= 0f) {
                                    if (positiones!!.contains(origPosition)) {
                                        y = y - h//会移动
                                    } else {
                                        y = 0f
                                    }
                                } else {
                                    y = 0f//固定
                                }
                                var paint = Paint()
                                paint.isDither = true
                                paint.isAntiAlias = true
                                itemView!!(this, paint, position, y)
                            }
                        }
                    }
                }
            }
        }

    }
}