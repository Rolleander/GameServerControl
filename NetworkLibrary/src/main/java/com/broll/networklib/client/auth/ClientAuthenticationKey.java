package com.broll.networklib.client.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.UUID;


public class ClientAuthenticationKey {

    private static IFileAccess fileAccess = new TempFileAccess("BRollNetworkUser.dat");
    private String secret;

    public static void setFileAccess(IFileAccess fileAccess) {
        ClientAuthenticationKey.fileAccess = fileAccess;
    }

    private ClientAuthenticationKey(String secret) {
        this.secret = secret;
    }

    public static ClientAuthenticationKey newSecret() {
        return new ClientAuthenticationKey(generateAccountKey());
    }

    public static ClientAuthenticationKey fromFileCache() {
        String secret = null;
        if (fileAccess.exists()) {
            secret = fileAccess.read();
        }
        if (secret != null) {
            return new ClientAuthenticationKey(secret);
        }
        ClientAuthenticationKey auth = new ClientAuthenticationKey(generateAccountKey());
        auth.writeToFileCache();
        return auth;
    }


    public static ClientAuthenticationKey custom(String authenticationSecret) {
        return new ClientAuthenticationKey(authenticationSecret);
    }

    public static void clearFileCache() {
        fileAccess.delete();
    }

    public void writeToFileCache() {
        fileAccess.write(secret);
    }

    private static String generateAccountKey() {
        return UUID.randomUUID().toString();
    }

    public String getSecret() {
        return secret;
    }

}
