package com.cx.plugin.dto;

import com.atlassian.bamboo.security.EncryptionException;
import com.atlassian.bamboo.security.EncryptionServiceImpl;

/**
 * Created by galn on 16/02/2017.
 */
public class Encryption {
    public static String decrypt(String password) {
        String encPass;
        try {
            encPass = new EncryptionServiceImpl().decrypt(password);
        } catch (EncryptionException e) {
            encPass = "";
        }

        return encPass;
    }

    public static String encrypt(String password) {
        String encPass;
        try {
            encPass = new EncryptionServiceImpl().encrypt(password);
        } catch (EncryptionException e) {
            encPass = "";
        }
        return encPass;
    }
}
