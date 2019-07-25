package com.stupidbird.fixjar.utils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by zhangjianliang on 2018/3/22
 */
public class SafeKeyCreator {

    public static String getSafeKey(String key) {
        if (isEmpty(key)) {
            return "";
        }
        String safeKey = key;
            try {
                MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
                messageDigest.update(key.getBytes("UTF-8"));
                safeKey = CacheUtil.sha256BytesToHex(messageDigest.digest());
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        return safeKey;
    }

    public static boolean isEmpty(CharSequence s) {
        if (s == null) {
            return true;
        } else {
            return s.length() == 0;
        }
    }
}