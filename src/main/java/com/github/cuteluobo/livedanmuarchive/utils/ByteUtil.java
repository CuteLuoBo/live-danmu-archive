package com.github.cuteluobo.livedanmuarchive.utils;

/**
 * @author CuteLuoBo
 * @date 2021/12/16 22:35
 */
public class ByteUtil {
    //string转换为byte 参考：https://www.lohoknang.com/2018/11/11/%E6%8A%93%E5%8F%96%E8%99%8E%E7%89%99%E7%9B%B4%E6%92%AD%E5%BC%B9%E5%B9%95%E6%B5%81-wireshark-%E5%89%8D%E7%AB%AF%E6%BA%90%E7%A0%81/#%E8%BF%BD%E8%B8%AA%E5%88%9D%E5%A7%8B%E5%8C%96

    public static byte[] parseHexString(String string) {
        if (string == null || string.length() == 0) {
            return new byte[0];
        }

        //TODO 可能有传入string长度为奇数导致的数组越界问题，考虑解决
        byte[] bytes = new byte[string.length() / 2 + 1];
        //储存的Bytes下标
        int bytesIndex = 0;
        char[] chars = string.toCharArray();
        for (int i = 0; i < chars.length; i += 2) {
            //WARN 使用byte直转可能导致未知错误，待调试
            bytes[bytesIndex] = (Byte.parseByte(String.valueOf(chars[i]) + String.valueOf(chars[i + 1]), 16));
            bytesIndex++;
        }
        return bytes;
    }

}
