package com.broll.networklib.client;

import java.util.UUID;

public class ClientAuthenticationKey {

    private String secret;

    private ClientAuthenticationKey(String secret){
        this.secret = secret;
    }

    static ClientAuthenticationKey newSecret(){
        return new ClientAuthenticationKey(generateAccountKey());
    }

    static ClientAuthenticationKey fromFileCache(){
        String secret = readFromFileCache();
        if(secret!=null){
            return new ClientAuthenticationKey(secret);
        }
        secret = generateAccountKey();
        writeToFileCache(secret);
        return new ClientAuthenticationKey(secret);
    }

    static void clearFileCache(){

    }

    private static void writeToFileCache(String secret){

    }

    private static String readFromFileCache(){
        return null;
    }


    private static String generateAccountKey(){
        return UUID.randomUUID().toString();
    }

    public String getSecret() {
        return secret;
    }
}
