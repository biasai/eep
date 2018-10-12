package cn.android.support.v7.lib.eep.kera.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 正则表达式
 *
 * @author 彭治铭
 */
public class KRegexUtils {
    public static KRegexUtils regex;

    public static KRegexUtils getInstance() {
        if (regex == null) {
            regex = new KRegexUtils();
        }
        return regex;
    }

    private KRegexUtils() {
    }

    /**
     * 判断是否为手机号码
     *
     * @param mobiles
     * @return
     */
    public Boolean isMobileNO(String mobiles) {
//        Pattern p = Pattern.compile("^(13|15|18|16|17)\\d{9}$");
//        Matcher m = p.matcher(mobiles);
//        return m.matches();
        Pattern pattern = Pattern.compile("1[0-9]{10}");
        Matcher matcher = pattern.matcher(mobiles);
        return matcher.matches();
    }

    /**
     * 判断是否为固定电话号码
     *
     * @param phone
     * @return
     */
    public Boolean isPhoneNo(String phone) {
        Pattern p = Pattern.compile("[0]{1}[0-9]{2,3}-[0-9]{7,8}");
        Matcher m = p.matcher(phone);
        return m.matches();
    }

    /**
     * 判断是否为邮箱地址
     *
     * @param email
     * @return
     */
    public Boolean isEmail(String email) {
        Pattern p = Pattern
                .compile("^([a-z0-9A-Z]+[-|_|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$");
        Matcher m = p.matcher(email);
        return m.matches();
    }

    /**
     * 判断是否为合法IP
     *
     * @return the ip
     */
    public Boolean isBoolIp(String ipAddress) {
        if (ipAddress.length() < 7 || ipAddress.length() > 15
                || "".equals(ipAddress)) {
            return false;
        }
        String ip = "([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}";
        Pattern pattern = Pattern.compile(ip);
        Matcher matcher = pattern.matcher(ipAddress);
        return matcher.matches();
    }

    /**
     * 判断是否为身份证号
     *
     * @param idNum
     * @return
     */
    public Boolean isIdCard(String idNum) {
        Pattern p = Pattern
                .compile("(\\d{14}[0-9a-zA-Z])|(\\d{17}[0-9a-zA-Z])");
        Matcher m = p.matcher(idNum);
        return m.matches();
    }

    /**
     * 根据身份证号，获取出生年月日
     *
     * @param idNum
     * @return
     */
    public String getBirth(String idNum) {
        Pattern birthDatePattern = Pattern
                .compile("\\d{6}(\\d{4})(\\d{2})(\\d{2}).*");// 身份证上的前6位以及出生年月日
        // 通过Pattern获得Matcher
        Matcher birthDateMather = birthDatePattern.matcher(idNum);
        // 通过Matcher获得用户的出生年月日
        if (birthDateMather.find()) {
            String year = birthDateMather.group(1);
            String month = birthDateMather.group(2);
            String date = birthDateMather.group(3);
            // 输出用户的出生年月日
            return year + "年" + month + "月" + date + "日";
        }
        return "";
    }

    /**
     * 判断URL是否合理
     *
     * @param url
     * @return
     */
    public Boolean isUrl(String url) {
        String regex = "^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
        Pattern patt = Pattern.compile(regex);
        Matcher matcher = patt.matcher(url);
        boolean isMatch = matcher.matches();
        if (!isMatch) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * 判断字符串是否为指定长度的数字
     *
     * @param str 字符串
     * @param v   长度
     * @return
     */
    public Boolean isNumeric(String str, int v) {
        Pattern pattern = Pattern.compile("[0-9]*");
        if (pattern.matcher(str).matches()) {
            if (str.length() == v) {
                return true;
            }
        }
        return false;
    }

    // 判断是否为数字或（和）字母
    public Boolean isNumLetter(String str) {
        Pattern pattern = Pattern.compile("^[A-Za-z0-9]+");
        Matcher isNum = pattern.matcher(str);
        if (!isNum.matches()) {
            return false;
        }
        return true;
    }
    /**
     * 校验银行卡卡号
     *
     * @param cardId
     * @return
     */
    public  boolean isBankCard(String cardId) {
        char bit = getBankCardCheckCode(cardId
                .substring(0, cardId.length() - 1));
        if (bit == 'N') {
            return false;
        }
        return cardId.charAt(cardId.length() - 1) == bit;
    }

    /**
     * 从不含校验位的银行卡卡号采用 Luhm 校验算法获得校验位
     *
     * @param nonCheckCodeCardId
     * @return
     */
    public  char getBankCardCheckCode(String nonCheckCodeCardId) {
        if (nonCheckCodeCardId == null
                || nonCheckCodeCardId.trim().length() == 0
                || !nonCheckCodeCardId.matches("\\d+")) {
            // 如果传的不是数据返回N
            return 'N';
        }
        char[] chs = nonCheckCodeCardId.trim().toCharArray();
        int luhmSum = 0;
        for (int i = chs.length - 1, j = 0; i >= 0; i--, j++) {
            int k = chs[i] - '0';
            if (j % 2 == 0) {
                k *= 2;
                k = k / 10 + k % 10;
            }
            luhmSum += k;
        }
        return (luhmSum % 10 == 0) ? '0' : (char) ((10 - luhmSum % 10) + '0');
    }

}
