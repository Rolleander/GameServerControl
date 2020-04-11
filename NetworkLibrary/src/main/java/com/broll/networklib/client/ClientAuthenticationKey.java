package com.broll.networklib.client;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.UUID;


public class ClientAuthenticationKey {

    private String secret;

    private static String FILE = "BRollNetworkUser";

    private ClientAuthenticationKey(String secret) {
        this.secret = secret;
    }

    public static ClientAuthenticationKey newSecret() {
        return new ClientAuthenticationKey(generateAccountKey());
    }

    public static ClientAuthenticationKey fromFileCache() {
        String secret = readFromFileCache();
        if (secret != null) {
            return new ClientAuthenticationKey(secret);
        }
        secret = generateAccountKey();
        writeToFileCache(secret);
        return new ClientAuthenticationKey(secret);
    }

    public static ClientAuthenticationKey custom(String authenticationSecret) {
        return new ClientAuthenticationKey(authenticationSecret);
    }

    static File getFile() throws IOException {
        return File.createTempFile(FILE, ".dat");
    }

    static void clearFileCache() {
        try {
            getFile().delete();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeToFileCache(String secret) {
        try {
            FileUtils.writeStringToFile(getFile(), secret, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String readFromFileCache() {
        try {
            File file = getFile();
            if (file.exists()) {
                return FileUtils.readFileToString(file, "UTF-8");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    private static String generateAccountKey() {
        return UUID.randomUUID().toString();
    }

    public String getSecret() {
        return secret;
    }
}
