package com.nokia.export.util;

import org.apache.commons.lang.StringUtils;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * 日期工具类
 * Created by yww on 2017/6/14.
 */
public class DateUtil {

    /** 时间格式(yyyy-MM) */
    public final static String DATE_MONTH_PATTERN = "yyyy-MM";
    /** 时间格式(yyyy-MM-dd) */
    public final static String DATE_PATTERN = "yyyy-MM-dd";
    /** 时间格式(yyyy-MM-dd HH:mm) */
    public final static String DATE_M_PATTERN = "yyyy-MM-dd HH:mm";
    /** 时间格式(yyyy) */
    public final static String DATE_YEAR_PATTERN = "yyyy";
    /** 时间格式(yyyy-MM-dd HH:mm:ss) */
    public final static String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    /** 时间格式(yyyyMMddHHmmss) */
    public final static String DATE_TIME_SIMP_PATTERN = "yyyyMMddHHmmss";

    public final static String MONTH_DAY_YEAR_PATTERN = "MM/dd/yyyy";

    public static String format(Date date) {
        return format(date, DATE_PATTERN);
    }

    public static String format(Date date, String pattern) {
        if(date != null){
            SimpleDateFormat df = new SimpleDateFormat(pattern);
            return df.format(date);
        }
        return null;
    }
    public static String formatDateOrTimesTamp(Object date) {
        Date date_ = null;
        if (date instanceof Timestamp){
            Timestamp time = (Timestamp) date;
            date_ = new Date(time.getTime());
        }else {
            date_ = (Date) date;
        }
        return format(date_, DATE_TIME_PATTERN);
    }
    /** 一天24小时8**/

    public final static int  HOURS_OF_DAY = 24;

    /**
     * @param date 某天
     * @return 某一天第一秒的时间
     */
    public static Date dayStart(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        return new Date(c.getTimeInMillis());
    }

    public static String dayStart(String date) {
        return date.substring(0,date.indexOf(" ")) + " 00:00:00";
    }

    public static String dayEnd(String date) {
        return date.substring(0,date.indexOf(" ")) + " 23:59:59";
    }


    /**
     * @param date 某天
     * @return 某一天最后一秒的时间
     */
    public static Date dayEnd(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.set(Calendar.HOUR_OF_DAY, 23);
        c.set(Calendar.MINUTE, 59);
        c.set(Calendar.SECOND, 59);
        return new Date(c.getTimeInMillis());
    }

    /**
     * @param date 某一天
     * @return 某周第一天第一秒的时间(星期一)
     */
    public static Date weekStart(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        if (c.get(Calendar.DAY_OF_WEEK) == 1) {
            c.add(Calendar.DAY_OF_WEEK, -6); // 当为1时表示星期天，获取该周中国的起始时间为星期一
        } else {
            c.set(Calendar.DAY_OF_WEEK, 2); // DAY_OF_WEEK从周日开始，加1变成周一
        }
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        return new Date(c.getTimeInMillis());
    }

    /**
     * @param date 某一天
     * @return 某周最后一天第一秒的时间(星期天)
     */
    public static Date weekEnd(Date date) {
        Date date1= lastDayOfWeek(date);
        date1 = addDay(date1,1);
        return date1;
    }

    /**
     * @param date 某一天
     * @return 某月第一天第一秒的时间
     */
    public static Date monthStart(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.set(Calendar.DAY_OF_MONTH, 1);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        return new Date(c.getTimeInMillis());
    }
    /**
     * @param date 某一天
     * @return 某年最后一秒的时间
     */
    public static Date yearEnd(int date) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, date);
        c.roll(Calendar.DAY_OF_YEAR, -1);
        c.set(Calendar.HOUR_OF_DAY, 23);
        c.set(Calendar.MINUTE, 59);
        c.set(Calendar.SECOND, 59);
        return new Date(c.getTimeInMillis());
    }
    /**
     * @param date 某一天
     * @return 某年第一一秒的时间
     */
    public static Date yearStart(int date) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(Calendar.YEAR, date);
        return new Date(calendar.getTimeInMillis());
    }
    /**
     * @param date 某一天
     * @return 某周第一天(星期天)
     */
    public static Date fristDayOfWeek(Date date){
        Calendar c= Calendar.getInstance();
        c.setTime(date);
        int initDay = c.getFirstDayOfWeek();
        c.set(Calendar.DAY_OF_WEEK,initDay);
        return  c.getTime();
    }

    /**
     * @param date 某一天
     * @return 某周最后一天(星期六)
     */
    public static Date lastDayOfWeek(Date date){
        Calendar now= Calendar.getInstance();
        Calendar c= Calendar.getInstance();
        c.setTime(date);
        c.set(Calendar.DAY_OF_WEEK,7);
        if (now.getTimeInMillis()<=c.getTimeInMillis()){
            return now.getTime();
        }
        return  c.getTime();
    }

    public static Date addDay(Date date, int num) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        int dayOfMonth = c.get(Calendar.DATE);
        c.set(Calendar.DATE, dayOfMonth + num);
        return c.getTime();
    }

    public static Date addHour(Date date, int num) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        int dayOfMonth = c.get(Calendar.HOUR_OF_DAY);

        c.set(Calendar.HOUR_OF_DAY, dayOfMonth + num);
        return c.getTime();
    }

    public static Date addMinute(Date date ,int num) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        int minuteOfHour = c.get(Calendar.MINUTE);
        c.set(Calendar.MINUTE,minuteOfHour+num);
        return c.getTime();
    }

    public static Date addMonth(Date date, int num) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        int dayOfMonth = c.get(Calendar.MONTH);
        c.set(Calendar.MONTH, dayOfMonth + num);
        c.set(Calendar.DAY_OF_MONTH, 1);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        return c.getTime();
    }
    public static Date monthEnd(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        int dayOfMonth = c.get(Calendar.DAY_OF_MONTH);
        c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH));
        c.set(Calendar.HOUR_OF_DAY, 23);
        c.set(Calendar.MINUTE, 59);
        c.set(Calendar.SECOND, 59);
        return c.getTime();
    }
    public static int getHoursOfDay(Date date){
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        int hour = c.get(Calendar.HOUR_OF_DAY);
        return hour;
    }

    public static void main(String[] args) throws ParseException {
        Date date = new Date();
        System.out.println(DateUtil.format(date,DateUtil.MONTH_DAY_YEAR_PATTERN));
    }


    public static long nowMillis() {
        return System.currentTimeMillis();
    }

    public static Date now() {
        return new Date(nowMillis());
    }

    /**
     * 计算两个时间点相差多少天
     * @param startTime
     * @param endTime
     * @return
     */
    public static int getFormatPeriod(Date startTime,Date endTime){
        Instant start_instant = startTime.toInstant();
        Instant end_instant = endTime.toInstant();
        ZoneId zone = ZoneId.systemDefault();
        LocalDate start = LocalDateTime.ofInstant(start_instant,zone).toLocalDate();
        LocalDate end = LocalDateTime.ofInstant(end_instant,zone).toLocalDate();
        return (int) (end.toEpochDay() - start.toEpochDay());
    }

    public static String getAuditDateNow(){
        Date date = new Date();
        SimpleDateFormat dateFm = new SimpleDateFormat("EEEE", Locale.ENGLISH);
        String currSun = dateFm.format(date);
        SimpleDateFormat monthFm = new SimpleDateFormat("MMMM", Locale.ENGLISH);
        String currMon = monthFm.format(date);
        SimpleDateFormat yearFm = new SimpleDateFormat("yyyy");
        String currYear = yearFm.format(date);
        SimpleDateFormat dayFm = new SimpleDateFormat("dd");
        String currDay = dayFm.format(date);
        StringBuilder str = new StringBuilder(currSun).append(",").append(currMon).append(" ").append(currDay).append(",")
                .append(currYear);
        return str.toString();
    }

    public static String getReportSubmissionTime(){
        Date date = new Date();
        SimpleDateFormat monthFm = new SimpleDateFormat("MMM", Locale.ENGLISH);
        String currMon = monthFm.format(date);
        SimpleDateFormat yearFm = new SimpleDateFormat("yy");
        String currYear = yearFm.format(date);
        SimpleDateFormat dayFm = new SimpleDateFormat("dd");
        String currDay = dayFm.format(date);
        StringBuilder str = new StringBuilder(currDay).append("-").append(currMon).append("-").append(currYear);
        return str.toString();
    }

}
