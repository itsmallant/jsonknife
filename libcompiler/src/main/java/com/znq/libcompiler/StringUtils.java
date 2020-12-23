package com.znq.libcompiler;

/**
 * @desc:
 * @author: ningqiang.zhao
 * @time: 2020-12-18 18:10
 **/
public class StringUtils {
    /**
     * 获取字符串s中有几个字符c
     *
     * @param s
     * @param c
     * @return
     */
    static int getCharCount(String s, char c) {
        int len;
        if (s == null || (len = s.length()) == 0) {
            return 0;
        }
        int count = 0;
        char[] chars = s.toCharArray();
        for (int i = 0; i < len; i++) {
            if (chars[i] == c) {
                count++;
            }
        }
        return count;
    }

    /**
     * 获取字符串s中，倒数第number个字符c的index
     *
     * @param s
     * @param number
     * @param c
     * @return
     */
    static int lastNumberIndexOf(String s, int number, char c) {
        int len;
        if (s == null || (len = s.length()) == 0) {
            return 0;
        }
        int index = -1;
        int matchCount = 0;
        char[] chars = s.toCharArray();
        for (int i = len - 1; i >= 0; i--) {
            if (chars[i] == c) {
                matchCount++;
                if (matchCount == number) {
                    index = i;
                }
            }
        }
        return index;
    }
}
