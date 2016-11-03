package org.dsa.iot.historian.utils;

import org.dsa.iot.dslink.util.TimeUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Use TimeUtils instead.
 *
 * @author Samuel Grenier
 * @deprecated Use TimeUtils instead.
 */
public class TimeParser {

    public static long parse(String time) {
        return TimeUtils.decode(time);
    }

    public static String parse(long time) {
        return TimeUtils.encode(time,true).toString();
    }

}
