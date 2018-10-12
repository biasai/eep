package cn.android.support.v7.lib.eep.kera.utils;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import cn.android.support.v7.lib.eep.kera.common.KDate;
import cn.android.support.v7.lib.eep.kera.common.KLunar;

/**
 * 时间
 *
 * @author 彭治铭
 */
public class KTimeUtils {
    private static KTimeUtils time;

    public static KTimeUtils getInstance() {
        if (time == null) {
            time = new KTimeUtils();
        }
        return time;
    }

    /**
     * 获取绝对时间(系统当前时间),
     *
     * @param format 时间格式，如果为空，默认格式为"yyMMddHHmmss" ,h是12小时制，H是24小时制
     * @return
     */
    public String getAbsoluteTime(String format) {
        SimpleDateFormat sdf;
        if (format != null && !format.equals("")) {
            sdf = new SimpleDateFormat(format);
        } else {
            sdf = new SimpleDateFormat("yyMMddHHmmss");
        }

        return sdf.format(new Date());
    }

    /**
     * 获取自定义时间
     * @param oldTime 原有时间字符串
     * @param oldSimple 原有时间格式
     * @param newSimple 新时间格式
     * @return
     */
    public String getAutoTime(String oldTime, String oldSimple, String newSimple) {
        Calendar calendar = getCalendar(oldTime, oldSimple);
        return getFormatTime(calendar, newSimple);
    }

    /**
     * 将指定时间按指定格式输出
     *
     * @param calendar  指定时间
     * @param newFormat 新格式
     * @return
     */
    public String getFormatTime(Calendar calendar, String newFormat) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");

        SimpleDateFormat sdf2;
        if (newFormat != null && !newFormat.equals("")) {
            sdf2 = new SimpleDateFormat(newFormat);
        } else {
            sdf2 = new SimpleDateFormat("yy年MM月dd日   HH:mm:ss");
        }
        return sdf2.format(calendar.getTime());
    }

    /**
     * 获取自定义时间【String转Calendar时间】
     *
     * @param time   字符串时间，如："2018-01-03-23-59-00",必须和参数2的时间格式对于上。
     * @param simple 时间格式。如果为null,默认就是 "yyyy-MM-dd-HH-mm-ss"
     * @return Calendar对象
     */
    public Calendar getCalendar(String time, String simple) {
        try {
            if (time != null && !time.equals("")) {
                String strDate = time;
                SimpleDateFormat sdf = null;
                if (simple == null || simple.trim().equals("")) {
                    sdf = new SimpleDateFormat(
                            "yyyy-MM-dd-HH-mm-ss");
                } else {
                    sdf = new SimpleDateFormat(
                            simple);
                }
                Calendar calendar = new GregorianCalendar();
                Date date = sdf.parse(strDate);
                calendar.setTime(date);
                return calendar;
            }
        } catch (Exception e) {
            // TODO: handle exception
        }
        return Calendar.getInstance();
    }

    //获取当前时间的Calendar对象。
    public Calendar getCurrentCalendar() {
        //System.currentTimeMillis();//返回当前的毫秒时间
        return Calendar.getInstance();//返回当前时间的Calendar对象。
    }

    /**
     * @param year      年
     * @param month     月，从0开始。0等于1月， 11等于12月
     * @param day       日
     * @param hourOfDay 时，24小时制
     * @param minute    分钟
     * @return
     */
    public Calendar getCalendar(int year, int month, int day, int hourOfDay,
                                int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day, hourOfDay, minute);
        return calendar;
    }

    /**
     * Long转Calendar时间
     *
     * @param millis 时间毫秒数
     * @return
     */
    public Calendar getCalendar(long millis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);
        return calendar;
    }

    /**
     * 求两个日期的天数之差（计算结果绝对正确，如果错误。哪一定是你手机本身的时间错误。或者模拟器的时间错误。模拟器的时间一般都是错误的。）
     *
     * @param startDate 开始日期【小】
     * @param endDate   结束日期，一般结束日期要大于开始日期。这样算出的结果为正。【大】
     * @return
     */
    public int getDistance(Calendar startDate, Calendar endDate) {
        long from = 0;
        long to = 0;
        try {
            from = startDate.getTime().getTime();
            to = endDate.getTime().getTime();
        } catch (Exception e) {
            // TODO: handle exception
            System.out.println("日差计算出错");
            e.printStackTrace();
        }
        return (int) ((to - from) / (1000 * 60 * 60 * 24));//结束时间 减去 开始时间
    }

    /**
     * 计算两个日期之间的分钟差。
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return
     */
    public int getDistanceMinutes(Calendar startDate, Calendar endDate) {
        long from = 0;
        long to = 0;
        try {
            from = startDate.getTime().getTime();
            to = endDate.getTime().getTime();
        } catch (Exception e) {
            // TODO: handle exception
            System.out.println("日差计算出错");
            e.printStackTrace();
        }
        return (int) ((to - from) / (1000 * 60));//结束时间 减去 开始时间
    }

    /**
     * 计算两个日期之间的小时差。
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return
     */
    public int getDistanceHours(Calendar startDate, Calendar endDate) {
        long from = 0;
        long to = 0;
        try {
            from = startDate.getTime().getTime();
            to = endDate.getTime().getTime();
        } catch (Exception e) {
            // TODO: handle exception
            System.out.println("日差计算出错");
            e.printStackTrace();
        }
        return (int) ((to - from) / (1000 * 60 * 60));//结束时间 减去 开始时间
    }

    /**
     * 计算两个日期之间的秒差（计算结果绝对正确，如果错误。哪一定是你手机本身的时间错误。或者模拟器的时间错误。模拟器的时间一般都是错误的。）
     *
     * @param startDate 开始日期(小)
     * @param endDate   结束日期(大)
     * @return
     */
    public long getDistanceSeconds(Calendar startDate, Calendar endDate) {
        long from = 0;
        long to = 0;
        try {
            from = startDate.getTime().getTime();
            to = endDate.getTime().getTime();
        } catch (Exception e) {
            // TODO: handle exception
            System.out.println("日差计算出错");
            e.printStackTrace();
        }
        return ((to - from) / (1000));//结束时间 减去 开始时间
    }

    /**
     * 计算两个日期直接的毫秒差（计算结果绝对正确，如果错误。哪一定是你手机本身的时间错误。或者模拟器的时间错误。模拟器的时间一般都是错误的。）
     *
     * @param startDate
     * @param endDate
     * @return
     */
    public long getDistanceLong(Calendar startDate, Calendar endDate) {
        long from = 0;
        long to = 0;
        try {
            from = startDate.getTime().getTime();
            to = endDate.getTime().getTime();
        } catch (Exception e) {
            // TODO: handle exception
            System.out.println("日差计算出错");
            e.printStackTrace();
        }
        return to - from;//结束时间 减去 开始时间
    }

    /**
     * 获取时间--年份
     *
     * @param calendar 如果为空，则已当前时间为标准
     * @return
     */
    public int getYear(Calendar calendar) {
        if (calendar != null) {
            return calendar.get(Calendar.YEAR);// 获取指定calendar对象的年份
        }
        return Calendar.getInstance().get(Calendar.YEAR);// 获取当前年份
    }

    /**
     * 获得这个年的第几天
     *
     * @param calendar 如果为空，则已当前时间为标准
     * @return
     */
    public int getYearDay(Calendar calendar) {
        if (calendar != null) {
            return calendar.get(Calendar.DAY_OF_YEAR);
        }
        return Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
    }

    /**
     * 获取时间--月份
     *
     * @param calendar 如果为空，则已当前时间为标准
     * @return
     */
    public int getMonth(Calendar calendar) {
        if (calendar != null) {
            return calendar.get(Calendar.MONTH) + 1;// 获取指定calendar对象的月份,Calendar月份从0开始。所以加1
        }
        return Calendar.getInstance().get(Calendar.MONTH) + 1;// 获取当前月份,Calendar月份从0开始。所以加1
    }

    /**
     * 获取月份的天数,即该月份共有多少天
     *
     * @param calendar 如果为空，则已当前时间为标准
     * @return
     */
    public int getMonthDay(Calendar calendar) {
        if (calendar != null) {
            calendar.getActualMaximum(Calendar.DAY_OF_MONTH);// 获取指定calendar对象的月份共有多少天。
        }
        return Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH);// 获取当前月份共有多少天。
    }

    /**
     * 获取指定时间的月份共有多少天（含润年非润年）
     *
     * @param date 日期，格式为 "yyyy-MM" ,月份从1开始，按正常运算
     * @return
     */
    public int getMonthDay(String date) {
        int days = 0;
        try {
            String strDate = date;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
            Calendar calendar = new GregorianCalendar();
            Date date1 = sdf.parse(strDate);
            calendar.setTime(date1); // 放入你的日期
            days = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        } catch (Exception e) {
            // TODO: handle exception
        }
        return days;
    }

    /**
     * 获取时间--日期，即几号
     *
     * @param calendar 如果为空，则已当前时间为标准
     * @return
     */
    public int getDay(Calendar calendar) {
        if (calendar != null) {
            return calendar.get(Calendar.DATE);// 获取指定calendar对象的日期，即 几号
        }
        return Calendar.getInstance().get(Calendar.DATE);// 获取当前日期，即 几号
    }

    /**
     * 获取时间--小时
     *
     * @param calendar 如果为null,则以当前时间为标准
     * @param hour     参数传12，返回12制度。 传其他任何数，都返回24制度。
     * @return
     */
    public int getHour(Calendar calendar, int hour) {
        if (calendar != null) {
            if (hour == 12) {
                return calendar.get(Calendar.HOUR);// 12小时制度;
            }
            return calendar.get(Calendar.HOUR_OF_DAY);// 24小时制度
        }
        if (hour == 12) {
            return Calendar.getInstance().get(Calendar.HOUR);// 12小时制度;
        }
        return Calendar.getInstance().get(Calendar.HOUR_OF_DAY);// 24小时制度
    }

    /**
     * 获取时间--分钟
     *
     * @param calendar 如果为null,则已当前的时间为标准
     * @return
     */
    public int getMinute(Calendar calendar) {
        if (calendar != null) {
            return calendar.get(Calendar.MINUTE);// 分;
        }
        return Calendar.getInstance().get(Calendar.MINUTE);// 分;
    }

    /**
     * 获取时间--秒
     *
     * @param calendar 如果为null,则已当前时间为标准
     * @return
     */
    public int getSecond(Calendar calendar) {
        if (calendar != null) {
            calendar.get(Calendar.SECOND);// 秒;
        }
        return Calendar.getInstance().get(Calendar.SECOND);// 秒;
    }

    /**
     * 获取时间--星期
     *
     * @param calendar 如果为null,则已当前时间为标准
     * @return
     */
    public String getWeek(Calendar calendar) {
        String[] week = new String[]{"星期日", "星期一", "星期二", "星期三", "星期四",
                "星期五", "星期六"};
        if (calendar != null) {

            return week[calendar.get(Calendar.DAY_OF_WEEK) - 1];// 国外是从星期天为第一天。中国是星期一为第一天，所以减去1
        }
        return week[Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1];// 国外是从星期天为第一天。中国是星期一为第一天，所以减去1
    }

    /**
     * 获取时间毫秒数。即时间的毫秒总计。
     *
     * @param calendar 如果为null,则已当前时间为标准
     * @return
     */
    public long getTimeMillis(Calendar calendar) {
        if (calendar != null) {
            return calendar.getTimeInMillis();
        }
        return Calendar.getInstance().getTimeInMillis();// 获得当前时间的毫秒表示
    }

    /**
     * 时间加减
     *
     * @param calendar
     * @param year     年份加减 ，正数加，负数减。
     * @param month    月份加减
     * @param day      日期加减
     * @return
     */
    public Calendar subtracDate(Calendar calendar, int year, int month, int day) {
        calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) + year);// 年份加减
        calendar.set(calendar.MONTH, calendar.get(Calendar.MONTH) + month);// 月份加减
        calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH)
                + day);// 日期加减
        return calendar;
    }

    /**
     * 与当前时间比较大小。
     *
     * @param calendar
     * @return true 大于当前时间，false小于当前时间
     */
    public Boolean before(Calendar calendar) {
        return Calendar.getInstance().before(calendar);// 当前时间是否在传入时间之前，true，则大于当前时间，false,小于当前时间。
    }

    /**
     * 获取年龄
     *
     * @param birth 出生日期，Calendar对象
     * @return
     */
    public int getAge(Calendar birth) {
        Calendar cal = Calendar.getInstance();
        // 当前时间
        int yearNow = cal.get(Calendar.YEAR);
        int monthNow = cal.get(Calendar.MONTH) + 1;// 注意此处，如果不加1的话计算结果是错误的
        int dayOfMonthNow = cal.get(Calendar.DAY_OF_MONTH);

        // 出生时间
        int yearBirth = birth.get(Calendar.YEAR);
        int monthBirth = birth.get(Calendar.MONTH) + 1;
        int dayOfMonthBirth = birth.get(Calendar.DAY_OF_MONTH);

        int age = yearNow - yearBirth;

        if (monthNow == monthBirth) {

            if (dayOfMonthNow < dayOfMonthBirth) {
                age--;
            }

        } else if (monthNow < monthBirth) {
            age--;
        }

        return age;
    }

    //星做是按阳历算的。
    public final String[] constellationArray = {"水瓶座", "双鱼座", "白羊座",
            "金牛座", "双子座", "巨蟹座", "狮子座", "处女座", "天秤座", "天蝎座", "射手座", "摩羯座"};

    public final int[] constellationEdgeDay = {20, 19, 21, 21, 21, 22,
            23, 23, 23, 23, 22, 22};

    /**
     * 根据日期Calendar获取星座
     *
     * @param time
     * @return
     */
    public String getConstellation(Calendar time) {
        int month = time.get(Calendar.MONTH);
        int day = time.get(Calendar.DAY_OF_MONTH);
        if (day < constellationEdgeDay[month]) {
            month = month - 1;
        }
        if (month >= 0) {
            return constellationArray[month];
        }
        // default to return 魔羯
        return constellationArray[11];
    }

    /**
     * 根据日期String类型获取星座,时间格式为 yyyy-MM-dd
     *
     * @param time
     * @return
     */
    public String getConstellation(String time) {

        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date date;
        try {
            date = sdf.parse(time);
            c.setTime(date);
            String constellation = getConstellation(c);
            //System.out.println("星座：" + constellation);
            return constellation;
        } catch (Exception e) {
            Log.e("test", "获取星座，日期格式错误:\t" + e.getMessage());
        }
        return null;

    }

    //生肖是按阴历算的。
    public final String[] zodiacArray = {"子鼠", "丑牛", "寅虎", "卯兔", "辰龙", "巳蛇", "午马", "未羊", "申猴", "酉鸡", "戌狗", "亥猪"};


    /**
     * 根据日期Calendar获取生肖
     *
     * @return
     */
    public String get2Zodica(Calendar time) {
        KLunar lunar=new KLunar(time);
        return zodiacArray[lunar.animalsYearPosition()];
    }

    //获取生肖
    public String get2Zodica(KDate kDate) {
        return get2Zodica(kDate.getCalendar());
    }

    /**
     * 根据日期String获取生肖,时间格式为 yyyy-MM-dd
     *
     * @return
     */
    public String getZodica(String time) {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date date;
        try {
            date = sdf.parse(time);
            c.setTime(date);

            String zodica = get2Zodica(c);
            System.out.println("生肖：" + zodica);
            return zodica;
        } catch (Exception e) {
            Log.e("test", "获取十二生肖，日期格式错误:\t" + e.getMessage());
        }
        return null;
    }

    /**
     * 与当前时间作比较，仿QQ聊天记录时间显示。
     *
     * @param startDate 日期。不能大于当前日期。
     * @return
     */
    public String getRelativeCurrentTime(Calendar startDate) {
        try {
            Calendar endDate = Calendar.getInstance();// 当前日期

            int distence = getDistance(startDate, endDate);
            if (distence < 7) {
                if (distence <= 1) {
                    if (distence == 0) {// 显示今天
                        return getFormatTime(startDate, "HH:mm");
                    } else {// 显示昨天
                        return getFormatTime(startDate, "昨天 HH:mm");
                    }
                } else {// 显示星期
                    return getWeek(startDate)
                            + getFormatTime(startDate, " HH:mm");
                }

            } else {// 日差大于七，显示月份日期
                return getFormatTime(startDate, "MM-dd HH:mm");
            }

        } catch (Exception e) {
            Log.e("test", "相对时间出错" + e.getMessage());
        }

        return "";
    }
}
