package com.stony.mc;

import java.util.Arrays;

public class GenerateCode {

    public static void main(String[] args) {
        String line = "MACHINE_ID,SERVER_NAME,SERVER_PORT,CONNECTION_COUNT,CONNECTION_ERROR_COUNT,MAX_CONNECTION_COUNT,REQUEST_TOTAL_COUNT,REQUEST_TOTAL_BYTES,RESPONSE_TOTAL_COUNT,RESPONSE_TOTAL_SUCCEED_COUNT,RESPONSE_TOTAL_FAILED_COUNT,RESPONSE_TOTAL_TIME_MS,RESPONSE_TOTAL_BYTES,SUBSCRIBER_COUNT,SUBSCRIBER_MESSAGE_COUNT,CREATE_TIME,UPDATE_TIME";

//        generateFiledMappingCode("info", line);
        printHtml(line);
    }

    public static void printHtml(String selectedFiled) {
        String[] fileds = selectedFiled.split(",");
        for (String filed : fileds){
            String used = filed.trim().toLowerCase();
            System.out.printf("{\n\ttitle: '%s',\n\tkey: '%s'\n},\n", used, used);
        }
    }
    public static void generateFiledMappingCode(String beanName, String selectedFiled){
        String[] fileds = selectedFiled.split(",");
        for (String filed : fileds){
            System.out.printf("%s.%s(rs.getLong(\"%s\"));\n", beanName, setMethodName(filed.trim()), filed);
        }
    }
    public static String setMethodName(String value) {
        return "set" + toCamel(value);
    }
    public static String toCamel(String str) {
        char[] array = str.toLowerCase().toCharArray();
        char[] arr2 = new char[array.length];
        int index = 1;
        arr2[0] = array[0];
        boolean next = false;
        for (int i = 1; i < array.length; i++) {
            char c = array[i];
            if (c == '_') {
                next = true;
            } else {
                if (next) {
                    arr2[index++] = (char) (c - 32);
                } else {
                    arr2[index++] = c;
                }
                next = false;

            }
        }
        if (arr2[0] >= 'a' && arr2[0] <= 'z') {
            arr2[0] -= 32;
        }
        return String.valueOf(Arrays.copyOf(arr2, index));
    }


    public static String toLine(String str) {
        char[] array = str.toCharArray();
        char[] arr2 = new char[array.length * 2];
        int index = 0;
        for (int i = 0; i < array.length; i++) {
            char c = array[i];
            if ('A' <= c && c <= 'Z') {
                array[i] = '_';
                if (index > 0) {
                    arr2[index++] = '_';
                }
                arr2[index++] = c;
            } else {
                arr2[index++] = c;
            }
        }
        return String.valueOf(Arrays.copyOf(arr2, index));
    }
}

