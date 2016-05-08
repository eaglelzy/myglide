package com.lizy.myglide;

import com.lizy.myglide.util.Util;

import org.junit.Test;

import java.security.MessageDigest;

import static org.junit.Assert.*;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
        MessageDigest digest = MessageDigest.getInstance("sha-256");
        digest.update("lizy".getBytes("utf-8"));
        byte[] bytes = digest.digest();
        System.out.println("length=" + bytes.length);
        String result = convertToHexString(bytes);
        System.out.println(result);

    }

    static String convertToHexString(byte data[]) {
        StringBuffer strBuffer = new StringBuffer();
        for (int i = 0; i < data.length; i++) {
            strBuffer.append(Integer.toHexString(0xff & data[i]));
        }
        return strBuffer.toString();
    }
}