package com.stony.mc;

import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;
import java.util.Date;

import static java.time.temporal.ChronoField.*;

/**
 * <p>message-channel
 * <p>com.stony.mc
 *
 * @author stony
 * @version 下午5:32
 * @since 2019/1/23
 */
public class TimeTest {


    public static TemporalAdjuster lastMilliOfDay() {
        return (temporal) -> temporal.with(MILLI_OF_DAY, temporal.range(MILLI_OF_DAY).getMaximum());
    }
    public static TemporalAdjuster firstMilliOfDay() {
        return (temporal) -> temporal.with(MILLI_OF_DAY, temporal.range(MILLI_OF_DAY).getMinimum());
    }

    final ZoneId CHINA = ZoneId.of("Asia/Shanghai");
    public long getTodayMaxMilli() {
        LocalDate date = LocalDate.now();
        LocalTime time = LocalTime.of(23, 59, 59, 0);
        LocalDateTime dateTime = LocalDateTime.of(date, time);
        return dateTime.atZone(CHINA).toInstant().toEpochMilli();
    }
    @Test
    public void test_today_max(){
        System.out.println(getTodayMaxMilli());

        LocalDateTime.now().with(TemporalAdjusters.lastDayOfMonth());

        LocalDateTime t = LocalDateTime.now();
        System.out.println(t.with(lastMilliOfDay()).toEpochSecond(ZoneOffset.of("+8")));
        System.out.println(t.with(lastMilliOfDay()).atZone(CHINA).toInstant().toEpochMilli());

    }

    @Test
    public void test_id_time() throws ParseException {
        LocalDateTime dateTime = LocalDateTime.of(2019, 1, 1, 0, 0 , 0, 0);
        System.out.println(dateTime.toString());
        System.out.println(dateTime.getNano());
        System.out.println(dateTime.getLong(ChronoField.MILLI_OF_SECOND));
        System.out.println(dateTime.get(ChronoField.MILLI_OF_SECOND));
        System.out.println(dateTime.toEpochSecond(ZoneOffset.of("+8")));
        System.out.println(dateTime.toInstant(ZoneOffset.of("+8")).toEpochMilli());

        LocalDate localDate = LocalDate.of(2019, 1, 1);

        System.out.println(localDate);

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String str = "2019-01-01 00:00:00";

        System.out.println(format.parse(str).getTime());
    }

    public static LocalDateTime dateConvertToLocalDateTime(Date date) {
        return date.toInstant().atOffset(ZoneOffset.of("+8")).toLocalDateTime();
    }

    public static Date localDateTimeConvertToDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.toInstant(ZoneOffset.of("+8")));
    }

    public static LocalDateTime getDateTimeOfTimestamp(long timestamp) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
    }
    public static LocalDateTime parseStringToDateTime(String time, String format) {
        return LocalDateTime.parse(time, DateTimeFormatter.ofPattern(format));
    }

    public static boolean isBirthDay(LocalDate birthDate) {
        LocalDate now = LocalDate.now();
        MonthDay birthday = MonthDay.of(birthDate.getMonth(), birthDate.getDayOfMonth());
        return MonthDay.from(now).equals(birthday);
    }
    @Test
    public void test_birth(){
        System.out.println(isBirthDay(LocalDate.of(1980, 1, 15)));
    }
    @Test
    public void test_next(){
        LocalDate now = LocalDate.now();
        System.out.println(now.isLeapYear());
        LocalDate next = now.plus(1, ChronoUnit.WEEKS);
        System.out.println(next);

        int weeks = Period.between(now, next).getDays();

        System.out.println("weeks " + weeks);
    }
    @Test
    public void test_clock(){
        System.out.println(Clock.systemUTC().millis());
        System.out.println(System.currentTimeMillis());
    }
}
