package cn.android.support.v7.lib.eep.kera.popu

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.View.OVER_SCROLL_NEVER
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.RelativeLayout
import cn.android.support.v7.lib.eep.kera.R
import cn.android.support.v7.lib.eep.kera.common.kpx
import cn.android.support.v7.lib.eep.kera.widget.KRadiusRelativeLayout
import cn.android.support.v7.lib.eep.kera.utils.KPopuWindowUtils
import org.jetbrains.anko.*
import org.jetbrains.anko.recyclerview.v7.recyclerView
import org.jetbrains.anko.sdk25.coroutines.onClick

//        //数据
//        var list = mutableListOf<String>()
//        var sp = SpinnerPop(this, list)
//        //创建recyclerView内部item视图,参数为下标position
//        sp.onCreateView {
//            UI { }.view //注意最好手动配置item的宽度和高度。
//        }
//        //视图刷新[业务逻辑都在这处理]，返回 视图itemView和下标postion
//        sp.onBindView { itemView, position -> }
//        //显示
//        sp.showAsDropDown(this@apply,50,-50)

//fixme 以下视图，根据需求可随意扩展。上下左右，四个容器只要不为空，就都会显示出来。centerView在四个容器的中间。
//fixme 记得手动配置各个容器的宽和高。默认都是wrapContent，如果没有内容。就等于是空，是不会显示出来的。

//                            sp.topView?.apply {//上方容器}
//                            sp.leftView?.apply {//左边容器}
//                            sp.rightView?.apply {//右边容器}
//                            sp.bottomView?.apply {//底部容器}
//                            sp.centerView?.apply {//中间容器，recyclerView的外框容器 }
//                            sp.containerView?.apply { //最外层容器，囊括以上所有布局。}

//传入的list和原有list是同一个对象，已经绑定。只要不重新赋值=，就会一直相互影响。
//styleAnime动画文件
open class KSpinnerPop(var context: Context, var list: MutableList<*>, var styleAnime: Int = R.style.kera_window_alpha_scale_drop) {
    var pop: PopupWindow? = null
    var recyclerView: RecyclerView? = null

    //fixme 最外层容器。
    var containerView: KRadiusRelativeLayout? = null

    //fixme 左边容器，recyclerView正左边
    var leftView: KRadiusRelativeLayout? = null

    //fixme 上方容器，recyclerView正上面
    var topView: KRadiusRelativeLayout? = null

    //fixme 中间容器，recyclerView的外框容器,即可以当作外层控件使用。随意更改样式
    //fixme 上下左右，四个容器只要不为空，就都会显示出来。centerView在四个容器的中间。
    var centerView: KRadiusRelativeLayout? = null

    //fixme 下方容器，recyclerView正下面
    var bottomView: KRadiusRelativeLayout? = null

    //fixme 左边容器，recyclerView正右边
    var rightView: KRadiusRelativeLayout? = null

    //fixme 创建itemView视图 [需要自动手动实现]
    open fun onCreateView(itemView: (positon: Int) -> View) {
        var view = context.UI {
            verticalLayout {
                var layoutParams = ViewGroup.LayoutParams(wrapContent, matchParent)
                setLayoutParams(layoutParams)

                //外层容器
                containerView = KRadiusRelativeLayout(context).lparams {
                    width = wrapContent
                    height = wrapContent
                }
                addView(containerView)
                containerView?.apply {
                    relativeLayout {
                        var layoutParams = RelativeLayout.LayoutParams(wrapContent, wrapContent)
                        setLayoutParams(layoutParams)

                        //左边容器
                        leftView = KRadiusRelativeLayout(context)?.apply {
                            id = kpx.id("spinnerPop_leftView")
                        }?.lparams {
                            width = wrapContent
                            height = wrapContent
                            alignParentLeft()
                            bottomOf(kpx.id("spinnerPop_topView"))
                        }
                        addView(leftView)

                        //上方容器
                        topView = KRadiusRelativeLayout(context)?.apply {
                            id = kpx.id("spinnerPop_topView")
                        }?.lparams {
                            width = wrapContent
                            height = wrapContent
                            alignParentTop()
                            leftView?.let {
                                rightOf(it)
                            }
                        }
                        addView(topView)

                        //中间容器，recyclerView容器
                        centerView = KRadiusRelativeLayout(context)
                        addView(centerView)
                        centerView?.apply {
                            id = kpx.id("spinnerPop_centerView")
                            frameLayout {
                                var layoutParams = ViewGroup.LayoutParams(wrapContent, wrapContent)
                                setLayoutParams(layoutParams)
                                recyclerView = recyclerView {
                                    var linearLayoutManager = LinearLayoutManager(context)
                                    linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
                                    layoutManager = linearLayoutManager
                                    adapter = MyAdapter(this@KSpinnerPop, itemView)

                                    setOverScrollMode(OVER_SCROLL_NEVER);//设置滑动到边缘时无效果模式
                                    setVerticalScrollBarEnabled(false);//滚动条隐藏

                                }.lparams {
                                    width = wrapContent
                                    height = wrapContent
                                }
                            }
                        }?.lparams {
                            width = wrapContent
                            height = wrapContent
                            topView?.let {
                                bottomOf(it)
                            }
                            leftView?.let {
                                rightOf(it)
                            }
                            above(kpx.id("spinnerPop_bottomView"))
                        }

                        //下方容器
                        bottomView = KRadiusRelativeLayout(context)?.apply {
                            id = kpx.id("spinnerPop_bottomView")
                        }?.lparams {
                            width = wrapContent
                            height = wrapContent
                            alignParentBottom()
                            leftView?.let {
                                rightOf(it)
                            }
                        }
                        addView(bottomView)

                        //右边容器
                        rightView = KRadiusRelativeLayout(context)?.apply {
                            id = kpx.id("spinnerPop_rightView")
                        }?.lparams {
                            width = wrapContent
                            height = wrapContent
                            centerView?.let {
                                rightOf(it)
                            }
                            topView?.let {
                                bottomOf(it)
                            }
                        }
                        addView(rightView)

                    }
                }
                //fixme 填充popuwindow的底部
                view {
                    onClick {
                        //关闭
                        dismiss()
                    }
                }.lparams {
                    width = matchParent
                    height = matchParent
                }
            }
        }.view
        pop = KPopuWindowUtils.getInstance().showPopuWindow(view, styleAnime)
    }

    var onBindView: ((itemView: View, position: Int) -> Unit)? = null
    //fixme 刷新视图，数据展示+业务逻辑+点击事件 都在这里处理 [需要自动手动实现]
    open fun onBindView(onBindView: (itemView: View, position: Int) -> Unit) {
        this.onBindView = onBindView
    }

    //fixme 关闭
    open fun dismiss() {
        pop?.dismiss()
    }

    //fixme 显示[每次显示的时候，布局都会重新刷新]
    open fun showAsDropDown(view: View?, xoff: Int = 0, yoff: Int = 0) {
        view?.let {
            pop?.showAsDropDown(it, xoff, yoff)//fixme xoff 和 yoff是偏移量。
            //数据刷新
            if (list.size > 1) {
                recyclerView?.adapter?.notifyItemRangeChanged(0, list.size)
            } else {
                recyclerView?.adapter?.notifyItemChanged(0)
            }
        }
    }

    companion object {
        class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {}
        class MyAdapter(var sp: KSpinnerPop, var itemView: (positon: Int) -> View) : RecyclerView.Adapter<MyViewHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
                return MyViewHolder(itemView(viewType))//自定义View每次都是重新实例话出来的
            }

            override fun getItemCount(): Int {
                return sp.list.size
            }

            override fun getItemViewType(position: Int): Int {
                return position
            }

            override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
                sp.onBindView?.let {
                    it(holder.itemView, position)
                }
            }
        }
    }
}