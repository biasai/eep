package cn.android.support.v7.lib.eep.kera.widget.chart

/**
 * 图表数据
 */
open class KData(var positionX: Float = 0f) {//positionX X轴开始下标，默认就是从0,原点开始计算。一般都是从1开始的。

    var positionStart = 0f//记录开始下标
    var positionEnd = 0f//记录结束下标

    init {
        positionStart = positionX
    }

    private var mapY = mutableMapOf<String, Float>()//X轴数据->Y轴数据
    private var mapY2 = mutableMapOf<Float, Float>()//X轴下标->Y轴数据
    private var mapX = mutableMapOf<String, Float>()//X轴,数据->下标
    private var mapX2 = mutableMapOf<Float, String?>()//X轴,下标->数据
    var unitY: Float = 0F//Y轴的实际单位大小。即每个单位对应的实际数据大小。
    fun unitY(unitY: Float): KData {
        this.unitY = unitY
        return this
    }

    /**
     * x:X轴上的数据,String类型
     * y:Y轴上的数据，Float类型
     */
    open fun put(x: String, y: Float): KData {
        this.mapX.put(x, positionX)
        if (x.contains(defaultValue)) {
            this.mapX2.put(positionX, null)
        } else {
            this.mapX2.put(positionX, x)
        }
        this.mapY.put(x, y)
        this.mapY2.put(positionX, y)
        positionEnd = positionX
        positionX++//fixme X轴上的下标，按数据排列数据自动累加。
        return this
    }

    fun put(x2: ArrayList<String>, y2: FloatArray): KData {
        for (i in 0 until x2.size) {
            var x = x2[i]
            var y = y2[i]
            put(x, y)
        }
        return this
    }

    companion object {
        var defaultValue = "XNULLDATA"
    }

    /**
     * y:Y轴上的数据
     */
    open fun put(y: Float): KData {
        var x = defaultValue + positionX//fixme x轴数据为空,模拟一个数据
        put(x, y)
        return this
    }

    open fun put(y2: FloatArray): KData {
        for (i in 0 until y2.size) {
            var y = y2[i]
            put(y)
        }
        return this
    }

    /**
     * 数据清除。
     */
    open fun clear(): KData {
        mapX.clear()
        mapX2.clear()
        mapY.clear()
        mapY2.clear()
        positionX = 0f
        return this
    }

    /**
     * 根据下标，获取X轴数据
     */
    open fun getX(position: Float): String? {
        return mapX2.get(position)
    }

    /**
     * 根据数据，获取X轴下标
     */
    open fun getX(x: String): Float? {
        return mapX.get(x)
    }

    /**
     * 根据X轴坐标获取Y轴下标
     */
    open fun getY(positionX: Float): Float? {
        var y = mapY2.get(positionX)
        y?.let {
            y = it / unitY//fixme 数据除以单位，获取下标。
        }
        return y
    }

    /**
     * 根据X轴数据，获取Y轴下标
     */
    open fun getY(x: String): Float? {
        var y = mapY.get(x)
        y?.let {
            y = it / unitY
        }
        return y
    }


    /**
     * 回调，获取所有的x轴下标和y轴下标
     */
    open fun getXY(xy: (xPosition: Float, yPosition: Float) -> Unit) {
//        mapX?.forEach { xs, xf ->
//            var yf = mapY.get(xs)
//            yf?.let {
//                xy(xf, yf / unitY)
//            }
//        }
        //forEach在4.3的系统会奔溃，找不到。
        mapX.filter {
            var xs = it.key
            var xf = it.value
            var yf = mapY.get(xs)
            yf?.let {
                xy(xf, yf / unitY)
            }
            true
        }
    }

}